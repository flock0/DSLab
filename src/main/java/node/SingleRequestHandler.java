package node;

import java.io.IOException;
import java.net.Socket;

import util.Channel;
import util.ComputationChannel;
import util.ComputationResult;
import util.Config;

public class SingleRequestHandler implements Runnable {

	private ComputationChannel channel;
	private Config config;
	private String allowedOperators;

	public SingleRequestHandler(Channel channel, Config config) {
		this.channel = new ComputationChannel(channel);
		this.config = config;

		allowedOperators = config.getString("node.operators");
	}

	@Override
	public void run() {
		try {
			NodeRequest request = channel.getRequest();
			ComputationUnit unit = ComputationUnitFactory.createUnit(request, allowedOperators);
			
			ComputationResult result = unit.compute(request);
			channel.sendResult(result);
			
		} catch (IOException e) {
			System.out.println("Error on getting request: " + e.getMessage());
		} finally {
			channel.close();
		}
		
	}
}
