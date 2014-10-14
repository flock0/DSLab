package util;

import java.io.IOException;

public class ComputationChannel extends ChannelDecorator {

	public ComputationChannel(Channel underlying) {
		super(underlying);
	}
	
	/**
	 * Send a computation request to the node
	 * @param request An array that contains the first operand, the operator and the second operand in the first three fields
	 * @return 
	 */
	public void requestComputation(String[] request) {
		println(String.format("!compute %s %s %s", request[0], request[1], request[2]));
	}
	

	public void sendResult(ComputationResult result) {
		println(result.toString());
	}
	
	/**
	 * Gets the next computation request
	 * @return An array that contains the first operand, the operator and the second operand in the first three fields
	 */
	public String[] getRequest() throws IOException {
		String request = readLine();
		if(request.startsWith("!compute "))
			return request.substring(9).split(" ");
		return null;
	}
	
	public ComputationResult getResult() throws IOException {
		return ComputationResult.fromString(readLine());
	}
}
