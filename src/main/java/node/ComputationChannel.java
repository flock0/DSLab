package node;

import java.io.IOException;

import util.Channel;
import util.ChannelDecorator;

public class ComputationChannel extends ChannelDecorator {

	public ComputationChannel(Channel underlying) {
		super(underlying);
	}
	
	/**
	 * Send a computation request to the node 
	 */
	public void requestComputation(NodeRequest request) {
		println(String.format("!compute %s %s %s", 
				request.getOperand1(), request.getOperator(), request.getOperand2()));
	}
	

	public void sendResult(ComputationResult result) {
		println(result.toString());
	}
	
	/**
	 * Gets the next computation request
	 */
	public NodeRequest getRequest() throws IOException {
		String message = readLine();
		if(message.startsWith("!compute "))
			return new NodeRequest(message.substring(9).split("\\s"));
		return null;
	}
	
	public ComputationResult getResult() throws IOException {
		return ComputationResult.fromString(readLine());
	}
}
