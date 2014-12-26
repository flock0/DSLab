package controller;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import util.Config;
import util.FixedParameters;
import util.SecureChannelSetup;
import channels.AESChannel;
import channels.Channel;
import channels.ChannelSet;
import channels.ClientCommunicator;
import channels.ComputationCommunicator;
import channels.TcpChannel;
import computation.ComputationResult;
import computation.NodeRequest;

public class SingleClientHandler implements Runnable {

	private Config config;
	private ConcurrentHashMap<Character, ConcurrentSkipListSet<Node>> activeNodes;
	private ConcurrentHashMap<String, User> users;
	private ClientCommunicator communicator;
	private User currentUser = null;
	private ComputationCommunicator currentComputationCommunicator = null;
	private boolean sessionIsBeingTerminated = false;
	private boolean successfullyInitialized = false;
	private ChannelSet openChannels;
	private PrivateKey controllerPrivateKey;

	public SingleClientHandler(
			Channel channel,
			ConcurrentHashMap<Character, ConcurrentSkipListSet<Node>> activeNodes,
			ConcurrentHashMap<String, User> users, PrivateKey controllerPrivateKey, ChannelSet openChannels, Config config) {
		this.config = config;
		this.activeNodes = activeNodes;
		this.users = users;
		this.controllerPrivateKey = controllerPrivateKey;
		this.openChannels = openChannels;
		
		try {
			SecureChannelSetup auth = new SecureChannelSetup(channel, controllerPrivateKey);
			Channel aesChannel = auth.awaitAuthentication();
			this.communicator = new ClientCommunicator(aesChannel);
			currentUser = users.get(auth.getAuthenticatedUser());
			currentUser.increaseOnlineCounter();
			communicator.sendAnswer("Successfully logged in.");
			successfullyInitialized = true;
		} catch(IOException e) {
			channel.close();
		}
	}

	@Override
	public void run() {
		if(successfullyInitialized) {
			try {
				while (true) {
					ClientRequest request = communicator.getRequest();
	
					switch (request.getType()) {
					case Login:
						communicator.sendAnswer(handleLogin(request));
						break;
					case Logout:
						communicator.sendAnswer(handleLogout());
						break;
					case Credits:
						communicator.sendAnswer(handleCredits());
						break;
					case Buy:
						communicator.sendAnswer(handleBuy(request));
						break;
					case List:
						communicator.sendAnswer(handleList());
						break;
					case Compute:
						communicator.sendAnswer(handleCompute(request));
						break;
					default:
						break; // Skip invalid requests
					}
				}
			} catch (SocketException e) {
				sessionIsBeingTerminated = true;
				System.out.println("Socket to client closed: " + e.getMessage());
			} catch (IOException e) {
				System.out.println("Error on getting request: " + e.getMessage());
			} finally {
				logoutAndClose();
			}
		}
	}

	private String handleLogin(ClientRequest request) {
		if (isLoggedIn())
			return "You are already logged in!";
		if (!credentialsAreValid(request))
			return "Wrong username or password.";

		currentUser = users.get(request.getUsername());
		currentUser.increaseOnlineCounter();
		return "Successfully logged in.";
	}

	private boolean isLoggedIn() {
		return currentUser != null;
	}

	private boolean credentialsAreValid(ClientRequest request) {
		return users.containsKey(request.getUsername())
				&& users.get(request.getUsername()).isCorrectPassword(
						request.getPassword());
	}

	private String handleLogout() {
		currentUser.decreaseOnlineCounter();
		currentUser = null;
		return "Successfully logged out.";
	}

	private String handleCredits() {
		if (!isLoggedIn())
			return "You need to log in first.";
		return String.format("You have %d credits left.", currentUser.getCredits());

	}

	private String handleBuy(ClientRequest request) {
		if (!isLoggedIn())
			return "You need to log in first.";
		if(request.getBuyAmount() <= 0)
			return "Error: Amount must be positive.";

		currentUser.setCredits(currentUser.getCredits()
				+ request.getBuyAmount());
		return String.format("You now have %d credits.",
				currentUser.getCredits());
	}

	private String handleList() {
		if (!isLoggedIn())
			return "You need to log in first.";
		return getAvailableOperators();
	}

	private String getAvailableOperators() {
		StringBuilder builder = new StringBuilder();
		// Sync'ed, so no inconsistent list can be returned 
		synchronized(activeNodes) {
			for (Character operator : activeNodes.keySet())
				if (!activeNodes.get(operator).isEmpty())
					builder.append(operator);
		}
		return builder.toString();
	}

	private String handleCompute(ClientRequest request) throws IOException {
		
		//// Prerequisites ////
		if (!isLoggedIn())
			return "You need to log in first.";
		if (!currentUser.hasEnoughCredits(request))
			return "Not enough credits!";
		if (!canBeComputed(request))
			return "Error: Operators unsupported!";

		//// Declarations ////
		int[] operands = request.getOperands();
		char[] operators = request.getOperators();
		final int totalOperatorCount = operators.length;
		final int totalOperandCount = operands.length;
		int remainingOperationsCount = totalOperatorCount;
		int firstOperand = operands[0];
		int secondOperand;
		char nextOperator;


		try {
			while (remainingOperationsCount != 0) {

				//// Get Next Request ////
				nextOperator = operators[totalOperatorCount - remainingOperationsCount]; 
				secondOperand = operands[totalOperandCount - remainingOperationsCount];
				NodeRequest computationRequest = new NodeRequest(firstOperand,
						nextOperator, 
						secondOperand);

				boolean foundAvailableNode = false;
				Iterator<Node> orderedNodesForNextOperator = activeNodes.get(nextOperator).iterator();

				while(!foundAvailableNode && orderedNodesForNextOperator.hasNext()) {

					//// Find Node for next request ////
					Node nextNodeToTry = orderedNodesForNextOperator.next();
					if(nextNodeToTry.isOnline()) {
						currentComputationCommunicator = null;
						try {
							Channel channelForCommunicator = new TcpChannel(
									new Socket(nextNodeToTry.getIPAddress(), nextNodeToTry.getTCPPort())); 
							openChannels.add(channelForCommunicator);
							currentComputationCommunicator = new ComputationCommunicator(channelForCommunicator);
							
							currentComputationCommunicator.requestComputation(computationRequest);

							ComputationResult result = currentComputationCommunicator.getResult();

							//// Check Result ////
							switch(result.getStatus()) {
							case OK:
								foundAvailableNode = true;
								firstOperand = result.getNumber();
								remainingOperationsCount--;

								updateUsageStatistics(nextNodeToTry, result);
								break;
							case DivisionByZero:
								deductCredits(totalOperatorCount - remainingOperationsCount + 1);
								return "Error: division by 0";
							case OperatorNotSupported:
								break; // Just skip this node for now and try another one
							default:
								break; // Just skip this node for now and try another one
							}
						} catch (SocketException e) {
							// Just skip this node for now and try another one
						} finally {
							if(currentComputationCommunicator != null)
								currentComputationCommunicator.close();
						}

					}
				}
				if(!foundAvailableNode)
					return "Error: Nodes crashed!";
			}

			deductCredits(totalOperatorCount);
			return String.valueOf(firstOperand);

		} catch (IOException e) {
			System.out.println("Error on getting result: " + e.getMessage());
			if(!sessionIsBeingTerminated)
				return "Error: An internal error occured";
			throw e;
		} finally {
			if(currentComputationCommunicator != null)
				currentComputationCommunicator.close();
		}
	}


	private boolean canBeComputed(ClientRequest request) {
		String availableOperators = getAvailableOperators();
		char[] requestOperators = request.getOperators();

		for (int i = 0; i < requestOperators.length; i++)
			if (availableOperators.indexOf(requestOperators[i]) == -1)
				return false;

		return true;
	}

	private void updateUsageStatistics(Node node, ComputationResult result) {
		int usageCost = calculateUsageCost(result);
		synchronized(node) {
			synchronized(activeNodes) {
				// Remove Node from all Operator Sets, change the Usage value and readd.
				List<ConcurrentSkipListSet<Node>> setsWithNodes = new ArrayList<>();
				for(ConcurrentSkipListSet<Node> operatorSet : activeNodes.values()) {
					if(operatorSet.contains(node))
						setsWithNodes.add(operatorSet);
					
					operatorSet.remove(node);
				}
				node.setUsage(node.getUsage() + usageCost);
				for(ConcurrentSkipListSet<Node> operatorSet : setsWithNodes)
					operatorSet.add(node);
			}
		}

	}

	private int calculateUsageCost(ComputationResult result) {
		int abs = Math.abs(result.getNumber());
		if(abs == 0)
			abs++;

		int digits = (int)(Math.log10(abs)+1);
		return digits * FixedParameters.USAGE_COST_PER_RESULT_DIGIT;
	}

	private void deductCredits(int operatorCount) {
		currentUser.setCredits(currentUser.getCredits() - FixedParameters.CREDIT_COST_PER_OPERATOR * operatorCount);

	}

	private void logoutAndClose() {
		if(currentUser != null) {
			currentUser.decreaseOnlineCounter();
			currentUser = null;
		}
		communicator.close();
	}

}
