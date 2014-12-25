package channels;

import java.io.IOException;

import computation.ComputationResult;
import computation.NodeRequest;

/**
 * Handles communication between the controller and the node
 */
public class ComputationCommunicator{

	private Channel underlying;
	
	public ComputationCommunicator(Channel underlying) {
		this.underlying = underlying;
	}
	
	/**
	 * Send a computation request to the node 
	 */
	public void requestComputation(NodeRequest request) {
		underlying.println(String.format("!compute %s %s %s", 
				request.getOperand1(), request.getOperator(), request.getOperand2()));
	}
	

	public void sendResult(ComputationResult result) {
		underlying.println(result.toString());
	}
	
	/**
	 * Gets the next computation request
	 */
	public NodeRequest getRequest() throws IOException {
		String message = underlying.readStringLine();
		if(message != null && message.startsWith("!compute "))
			return new NodeRequest(message.substring(9).split("\\s"));
		return null;
	}
	
	public ComputationResult getResult() throws IOException {
		return ComputationResult.fromString(underlying.readStringLine());
	}

	public void close() {
		underlying.close();
	}
}
