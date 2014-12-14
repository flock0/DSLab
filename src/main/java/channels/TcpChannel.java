package channels;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

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
		String message = reader.readLine();
		if(message == null)
			throw new SocketException("socket closed");
		else
			return message;
	}

	@Override
	public void println(String out) {
		writer.println(out);
	}

	@Override
	public void close() {
		try {
			if (socket != null && !socket.isClosed())
				socket.close();
		} catch (IOException e) {
			// Nothing we can do about it
		}
	}

	@Override
	public boolean isClosed() {
		if(socket != null)
			return socket.isClosed();
		else
			return true;
	}

}
