package node;

import java.io.IOException;

import channels.Channel;

public class SingleRollbackHandler implements Runnable {

	private Channel channel;
	private CommitHandler commit;
	
	public SingleRollbackHandler(Channel channel, CommitHandler commit) {
		this.channel = channel;
		this.commit = commit;
	}

	@Override
	public void run() {
		channel.println("!rollback");
		try {
			channel.readLine();
			commit.addRollbackAck();
		} catch (IOException e) {
			System.out.println("Error on getting request: " + e.getMessage());
		} finally {
			channel.close();
		}
	}
}