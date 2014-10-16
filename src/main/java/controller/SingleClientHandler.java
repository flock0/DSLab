package controller;

import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import com.sun.jmx.snmp.daemon.CommunicationException;

import node.ComputationChannel;
import node.ComputationResult;
import node.NodeRequest;
import util.Channel;
import util.Config;
import util.FixedParameters;
import util.ResultStatus;
import util.TcpChannel;

public class SingleClientHandler implements Runnable {

	private Config config;
	private ConcurrentHashMap<Character, ConcurrentSkipListSet<Node>> activeNodes;
	private ConcurrentHashMap<String, User> users;
	private ClientChannel channel;
	private User currentUser = null;

	public SingleClientHandler(
			Channel channel,
			ConcurrentHashMap<Character, ConcurrentSkipListSet<Node>> activeNodes,
			ConcurrentHashMap<String, User> users, Config config) {
		this.config = config;
		this.activeNodes = activeNodes;
		this.users = users;
		this.channel = new ClientChannel(channel);
	}

	@Override
	public void run() {
		try {
			while (true) {
				ClientRequest request = channel.getRequest();

				switch (request.getType()) {
				case Login:
					channel.println(handleLogin(request));
					break;
				case Logout:
					channel.println(handleLogout(request));
					break;
				case Credits:
					channel.println(handleCredits(request));
					break;
				case Buy:
					channel.println(handleBuy(request));
					break;
				case List:
					channel.println(handleList(request));
					break;
				case Compute:
					channel.println(handleCompute(request));
					break;
				default:
					break; // Skip invalid requests
				}
			}
		} catch (IOException e) {
			System.out.println("Error on getting request: " + e.getMessage());
		} finally {
			channel.close();
		}

	}

	private String handleLogin(ClientRequest request) {
		if (isLoggedIn())
			return "You are already logged in!";
		if (!credentialsAreValid(request))
			return "Wrong username or password.";

		currentUser = users.get(request.getUsername());
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

	private String handleLogout(ClientRequest request) {
		currentUser = null;
		return "Successfully logged out.";
	}

	private String handleCredits(ClientRequest request) {
		if (!isLoggedIn())
			return "You need to log in first.";
		else
			return String.format("You have %d credits left.",
					currentUser.getCredits());

	}

	private String handleBuy(ClientRequest request) {
		if (!isLoggedIn())
			return "You need to log in first.";
		else {
			currentUser.setCredits(currentUser.getCredits()
					+ request.getBuyAmount());
			return String.format("You now have %d credits.",
					currentUser.getCredits());
		}
	}

	private String handleList(ClientRequest request) {
		if (!isLoggedIn())
			return "You need to log in first.";
		else
			return getAvailableOperators();
	}

	private String getAvailableOperators() {
		StringBuilder builder = new StringBuilder();
		for (Character operator : activeNodes.keySet())
			if (!activeNodes.get(operator).isEmpty())
				builder.append(operator);

		return builder.toString();
	}

	private String handleCompute(ClientRequest request) {
		if (!currentUser.hasEnoughCredits(request))
			return "Not enough credits!";
		if (!canBeComputed(request))
			return "Can't compute that sort of arithmetic expression! (No nodes for all the operators available)";

		int[] operands = request.getOperands();
		char[] operators = request.getOperators();
		final int totalOperatorCount = operators.length;
		final int totalOperandCount = operands.length;
		int firstOperand = operands[0];
		int secondOperand = operands[1];
		int remainingOperationsCount = totalOperatorCount;
		char nextOperator = operators[0];

		while (remainingOperationsCount != 0) {
			NodeRequest computationRequest = new NodeRequest(firstOperand,
													  nextOperator, 
													  secondOperand);

			boolean foundAvailableNode = false;
			Iterator<Node> orderedNodesForNextOperator = activeNodes.get(nextOperator).descendingIterator();
			
			
			while(!foundAvailableNode && orderedNodesForNextOperator.hasNext()) {
				
				Node nextNodeToTry = orderedNodesForNextOperator.next();
				if(nextNodeToTry.isOnline()) {
					ComputationChannel computationChannel = null;
					
					try {
						computationChannel = new ComputationChannel(
											 new TcpChannel(
											 new Socket(nextNodeToTry.getIPAddress(), nextNodeToTry.getTCPPort())));
						computationChannel.requestComputation(computationRequest);
						
						ComputationResult result = computationChannel.getResult();
						
						switch(result.getStatus()) {
						case OK:
							foundAvailableNode = true;
							firstOperand = result.getNumber();
							remainingOperationsCount--;
							nextOperator = operators[totalOperatorCount - remainingOperationsCount]; 
							secondOperand = operands[totalOperandCount - remainingOperationsCount];
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
					} catch (IOException e) {
						
						// Just skip this node for now and try another one
					} finally {
						if(computationChannel != null)
							computationChannel.close();
					}
					
				}
			}
			if(!foundAvailableNode)
				return String.format("Can't compute that sort of arithmetic expression! (All nodes for the '%c' operator suddenly became unavailable)", nextOperator);
			
			deductCredits(totalOperatorCount);
			return String.valueOf(firstOperand);
			
		}
		
		return String.valueOf(firstOperand);
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
		node.setUsage(node.getUsage() + usageCost);
		
		for(ConcurrentSkipListSet<Node> nodeList : activeNodes.values())
			if(nodeList.contains(node)) {
				nodeList.remove(node);
				nodeList.add(node);
			}
		
	}

	private int calculateUsageCost(ComputationResult result) {
		return ((int)(Math.log10(result.getNumber())+1)) * FixedParameters.USAGE_COST_PER_RESULT_DIGIT;
	}

	private void deductCredits(int operatorCount) {
		currentUser.setCredits(currentUser.getCredits() - FixedParameters.CREDIT_COST_PER_OPERATOR * operatorCount);
		
	}

}
