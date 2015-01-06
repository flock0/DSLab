package channels;

import java.io.IOException;

import util.TamperedException;
import computation.CommitRequest;
import computation.ComputationResult;
import computation.LogRequest;
import computation.LogResult;
import computation.NodeRequest;
import computation.Request;
import computation.Result;
import computation.ShareRequest;
import computation.TamperedResult;

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
		String message = underlying.readStringLine();
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
	
	public Result getResult() throws TamperedException, IOException {
		String response = underlying.readStringLine();
		if(response.startsWith("!logs "))
			return LogResult.fromString(response);
		else if(response.startsWith("!tampered"))
			return TamperedResult.fromString(response);
		else
			return ComputationResult.fromString(response);
	}

	public void close() {
		underlying.close();
	}	
}
