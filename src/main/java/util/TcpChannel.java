package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * A two-way communication channel based on a TCP connection
 *
 */
public class TcpChannel implements Channel {

	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;

	public TcpChannel(Socket socket) {
		this.socket = socket;
		initializeIOStreams();
	}

	private void initializeIOStreams() {
		try {
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			System.out.println("Error on creating stream IO: " + e.getMessage());
			close();
		}
	}

	@Override
	public String readLine() throws IOException {
		return reader.readLine();
		
	}

	@Override
	public void println(String out) {
		writer.println(out);
	}

	@Override
	public void close() {
		try {
			if (reader != null)
				reader.close();
			if (writer != null)
				writer.close();
			if (socket != null && !socket.isClosed())
				socket.close();
		} catch (IOException e) {
			// Nothing we can do about it
		}
	}

}
