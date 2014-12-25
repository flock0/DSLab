package channels;

import java.io.IOException;

import computation.ComputationRequestType;
import controller.ClientRequest;

/**
 * Validates the client requests
 *
 */
public class ClientCommunicator {

	private Channel underlying;
	
	public ClientCommunicator(Channel underlying) {
		this.underlying = underlying;
	}
	
	public ClientRequest getRequest() throws IOException {
		String message = underlying.readStringLine();
		
		String[] split = message.split("\\s");
		
		switch(split[0]) {
		case "!login":
			return validateLogin(split);
		case "!logout":
			return validateLogout(split);
		case "!credits":
			return validateCredits(split);
		case "!buy":
			return validateBuy(split);
		case "!list":
			return validateList(split);
		case "!compute":
			return validateCompute(split);
		case "!authenticate":
			return validateAuthenticate(split);
		default:
			underlying.println("Error: Unknown command");
			ClientRequest request = new ClientRequest();
			request.setType(ComputationRequestType.Invalid);
			return request;
		}
	}

	public void sendAnswer(String answer) {
		underlying.println(answer);
	}

	public void close() {
		underlying.close();
	}

	private ClientRequest validateLogin(String[] split) {
		ClientRequest request = new ClientRequest();
		request.setType(ComputationRequestType.Invalid);
		
		if(split.length == 3) {
			request.setType(ComputationRequestType.Login);
			request.setUsername(split[1]);
			request.setPassword(split[2]);
		} else {
			underlying.println("Usage: !login <username> <password>");
		}
		
		return request;
	}

	private ClientRequest validateLogout(String[] split) {
		ClientRequest request = new ClientRequest();
		request.setType(ComputationRequestType.Invalid);
		
		if(split.length == 1)
			request.setType(ComputationRequestType.Logout);
		else
			underlying.println("Usage: !logout");
		
		return request;
	}

	private ClientRequest validateCredits(String[] split) {
		ClientRequest request = new ClientRequest();
		request.setType(ComputationRequestType.Invalid);
		
		if(split.length == 1)
			request.setType(ComputationRequestType.Credits);
		else
			underlying.println("Usage: !credits");
		
		return request;
	}

	private ClientRequest validateBuy(String[] split) {
		ClientRequest request = new ClientRequest();
		request.setType(ComputationRequestType.Invalid);
		
		if(split.length == 2 && isInteger(split[1])) {
			request.setType(ComputationRequestType.Buy);
			request.setBuyAmount(Integer.parseInt(split[1]));
		} else {
			underlying.println("Usage: !buy <amount>");
		}
		
		return request;
	}

	private ClientRequest validateList(String[] split) {
		ClientRequest request = new ClientRequest();
		request.setType(ComputationRequestType.Invalid);
		
		if(split.length == 1)
			request.setType(ComputationRequestType.List);
		else
			underlying.println("Usage: !list");
		
		return request;
	}

	private ClientRequest validateCompute(String[] split) {
		ClientRequest request = new ClientRequest();
		request.setType(ComputationRequestType.Invalid);
		
		if(split.length % 2 == 0) {
			request.setType(ComputationRequestType.Compute);
			extractArithmeticOperation(request, split);
		} else {
			underlying.println("Usage: !compute <operand> <operator> <operand> ...");
		}
		
		return request;
	}
	
	private ClientRequest validateAuthenticate(String[] split) {
		//An dieser Stelle sind wir auf jeden Fall schon authentifiziert. Der Befehl ist also unnötig
		ClientRequest request = new ClientRequest();
		request.setType(ComputationRequestType.Invalid);
		underlying.println("Already authenticated");
		return request;
	}

	private boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	private void extractArithmeticOperation(ClientRequest request,
			String[] split) {
		int[] operands = new int[split.length / 2];
		char[] operators = new char[split.length / 2 - 1];
		
		for(int i = 1; i < split.length; i++)
			if(i % 2 == 1)
				operands[i / 2] = Integer.parseInt(split[i]);
			else
				operators[i / 2 - 1] = split[i].charAt(0);
		
		request.setOperands(operands);
		request.setOperators(operators);
		
	}
}
