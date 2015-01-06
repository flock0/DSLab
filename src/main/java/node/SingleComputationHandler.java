package node;

import java.io.IOException;

import util.Config;
import util.NodeLogger;
import util.TamperedException;
import channels.Channel;
import channels.ComputationCommunicator;
import computation.CommitRequest;
import computation.ComputationResult;
import computation.ComputationUnit;
import computation.ComputationUnitFactory;
import computation.LogRequest;
import computation.LogResult;
import computation.NodeRequest;
import computation.Request;
import computation.Result;
import computation.ResultStatus;
import computation.ShareRequest;
import computation.ShareResult;
import computation.TamperedResult;

public class SingleComputationHandler implements Runnable {

	private ComputationCommunicator channel;
	private String allowedOperators;
	private Node node;

	public SingleComputationHandler(Channel channel, Config config, Node node) {
		this.channel = new ComputationCommunicator(channel);
		this.node = node;
		allowedOperators = config.getString("node.operators");
	}

	@Override
	public void run() {
		try {
			Request request = channel.getRequest();
			
			Result result;
			if(request == null) { // If the channel is closed, the received requests will be null
				result = new Result(ResultStatus.Error);
			} else if(request instanceof NodeRequest){
				NodeRequest nr = (NodeRequest)request; 				
				ComputationUnit unit = ComputationUnitFactory.createUnit(nr, allowedOperators);
				result = unit.compute(nr);				
				NodeLogger.log(nr, (ComputationResult)result);
			} else if(request instanceof LogRequest){
				result = new LogResult(ResultStatus.OK, node.getLogs());
			} else if(request instanceof ShareRequest){
				ShareRequest sr = (ShareRequest)request;
				result = new ShareResult(ResultStatus.OK, (node.getRmin() <= sr.getResources()));
			} else if(request instanceof CommitRequest){
				CommitRequest cr = (CommitRequest)request;
				handleCommit(cr.getResources());
				result = new Result(ResultStatus.OK);
			}
			else
			{
				result = new Result(ResultStatus.Error); //This should not happen
			}
			channel.sendResult(result);
			
		} catch (TamperedException e) {
			Result res = new TamperedResult(e.getClearText());
			channel.sendResult(res);
			System.out.println(e.getMessage() + ": " + e.getClearText());
		} catch (IOException e) {
			System.out.println("Error on getting request: " + e.getMessage());
		} finally {
			channel.close();
		}
		
	}
	
	private void handleCommit(int resources) {
		if(resources >= 0)
			node.updateResources(resources);
	}
}
