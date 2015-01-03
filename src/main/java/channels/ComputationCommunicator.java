package channels;

import java.io.IOException;

import computation.CommitRequest;
import computation.ComputationResult;
import computation.LogRequest;
import computation.LogResult;
import computation.NodeRequest;
import computation.Request;
import computation.Result;
import computation.ShareRequest;

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
	
	public void sendRequest(Request reqeust)
	{
		underlying.println(reqeust.toString());
	}
	

	public void sendResult(Result result) {
		underlying.println(result.toString());
	}
	
	/**
	 * Gets the next computation request
	 */
	public Request getRequest() throws IOException {
		String message = underlying.readLine();
		if(message != null && message.startsWith("!compute "))		
			return new NodeRequest(message.substring(9).split("\\s"));		
		if(message != null && message.equals("!getLogs"))		
			return new LogRequest();
		if(message != null && message.startsWith("!share "))		
			return new ShareRequest(message.substring(7).trim());
		if(message != null && (message.startsWith("!commit ") || message.equals("!rollback")))		
			return new CommitRequest(message);
		return null;
	}
	
	public Result getResult() throws IOException {
		String response = underlying.readLine();
		if(response.startsWith("!logs "))
			return LogResult.fromString(response);
		else
			return ComputationResult.fromString(response);
	}

	public void close() {
		underlying.close();
	}	
}
