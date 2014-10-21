package node;

import java.io.IOException;
import java.net.Socket;

import channels.Channel;
import channels.ChannelSet;
import channels.ComputationChannel;
import computation.ComputationResult;
import computation.ComputationUnit;
import computation.ComputationUnitFactory;
import computation.NodeRequest;
import computation.ResultStatus;
import util.Config;
import util.NodeLogger;

public class SingleComputationHandler implements Runnable {

	private ComputationChannel channel;
	private Config config;
	private String allowedOperators;
	private ChannelSet openChannels;
	private NodeLogger logger;

	public SingleComputationHandler(Channel channel, Config config) {
		this.channel = new ComputationChannel(channel);
		this.config = config;
		
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
