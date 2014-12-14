package node;

import java.io.IOException;

import util.Config;
import util.NodeLogger;
import channels.Channel;
import channels.ComputationCommunicator;

import computation.ComputationResult;
import computation.ComputationUnit;
import computation.ComputationUnitFactory;
import computation.NodeRequest;
import computation.ResultStatus;

public class SingleComputationHandler implements Runnable {

	private ComputationCommunicator channel;
	private String allowedOperators;

	public SingleComputationHandler(Channel channel, Config config) {
		this.channel = new ComputationCommunicator(channel);
		allowedOperators = config.getString("node.operators");
	}

	@Override
	public void run() {
		try {
			NodeRequest request = channel.getRequest();
			ComputationResult result;
			if(request == null) { // If the channel is closed, the received requests will be null
				result = new ComputationResult(ResultStatus.Error, 0);
			} else {
				ComputationUnit unit = ComputationUnitFactory.createUnit(request, allowedOperators);
				result = unit.compute(request);
			}
			
			channel.sendResult(result);
			NodeLogger.log(request, result);
			
		} catch (IOException e) {
			System.out.println("Error on getting request: " + e.getMessage());
		} finally {
			channel.close();
		}
		
	}
}
