package node;

import java.io.IOException;

import util.Config;
import util.NodeLogger;
import channels.Channel;
import channels.ComputationCommunicator;
import computation.ComputationResult;
import computation.ComputationUnit;
import computation.ComputationUnitFactory;
import computation.LogRequest;
import computation.LogResult;
import computation.NodeRequest;
import computation.Request;
import computation.Result;
import computation.ResultStatus;

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
			}
			else
			{
				result = new Result(ResultStatus.Error); //This should not happen
			}
			channel.sendResult(result);
			
			
		} catch (IOException e) {
			System.out.println("Error on getting request: " + e.getMessage());
		} finally {
			channel.close();
		}
		
	}
}
