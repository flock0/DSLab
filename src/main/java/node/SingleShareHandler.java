package node;

import java.io.IOException;

import channels.Channel;

public class SingleShareHandler implements Runnable {

	private Channel channel;
	private CommitHandler commit;
	private int res;
	
	public SingleShareHandler(Channel channel, CommitHandler commit, int res) {
		this.channel = channel;
		this.commit = commit;
		this.res = res;
	}

	@Override
	public void run() {
		channel.println(String.format("!share %d", res));
		try {
			String nodeReply = channel.readLine();
			if (nodeReply != null) {
				if (nodeReply.equals("!ok")) {
					commit.addNodeReply(true);
				} else if (nodeReply.equals("!nok")) {
					commit.addNodeReply(false);
				}
			}
		} catch (IOException e) {
			System.out.println("Error on getting request: " + e.getMessage());
		} finally {
			channel.close();
		}
	}
}
