package node;

import java.io.IOException;

import channels.Channel;

public class SingleCommitHandler implements Runnable {

	private Channel channel;
	private CommitHandler commit;
	private int res;
	
	public SingleCommitHandler(Channel channel, CommitHandler commit, int res) {
		this.channel = channel;
		this.commit = commit;
		this.res = res;
	}

	@Override
	public void run() {
		channel.println(String.format("!commit %d", res));
		try {
			channel.readStringLine();
			commit.addCommitAck();
		} catch (IOException e) {
			System.out.println("Error on getting request: " + e.getMessage());
		} finally {
			channel.close();
		}
	}
}
