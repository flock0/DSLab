package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import util.Channel;
import util.ChannelDecorator;

/**
 * Validates the client requests
 *
 */
public class ClientChannel extends ChannelDecorator {

	public ClientChannel(Channel underlying) {
		super(underlying);
	}
	
	public ClientRequest getRequest() throws IOException {
		String message = underlying.readLine();
		if(message == null) {
			ClientRequest request = new ClientRequest();
			request.setType(RequestType.Invalid);
			return request;
		}
		
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
		default:
			underlying.println("Error: Unknown command");
			ClientRequest request = new ClientRequest();
			request.setType(RequestType.Invalid);
			return request;
		}
	}

	private ClientRequest validateLogin(String[] split) {
		ClientRequest request = new ClientRequest();
		request.setType(RequestType.Invalid);
		
		if(split.length == 3) {
			request.setType(RequestType.Login);
			request.setUsername(split[1]);
			request.setPassword(split[2]);
		} else {
			underlying.println("Usage: !login <username> <password>");
		}
		
		return request;
	}

	private ClientRequest validateLogout(String[] split) {
		ClientRequest request = new ClientRequest();
		request.setType(RequestType.Invalid);
		
		if(split.length == 1)
			request.setType(RequestType.Logout);
		else
			underlying.println("Usage: !logout");
		
		return request;
	}

	private ClientRequest validateCredits(String[] split) {
		ClientRequest request = new ClientRequest();
		request.setType(RequestType.Invalid);
		
		if(split.length == 1)
			request.setType(RequestType.Credits);
		else
			underlying.println("Usage: !credits");
		
		return request;
	}

	private ClientRequest validateBuy(String[] split) {
		ClientRequest request = new ClientRequest();
		request.setType(RequestType.Invalid);
		
		if(split.length == 2 && isInteger(split[1])) {
			request.setType(RequestType.Buy);
			request.setBuyAmount(Integer.parseInt(split[1]));
		} else {
			underlying.println("Usage: !buy <amount>");
		}
		
		return request;
	}

	private ClientRequest validateList(String[] split) {
		ClientRequest request = new ClientRequest();
		request.setType(RequestType.Invalid);
		
		if(split.length == 1)
			request.setType(RequestType.List);
		else
			underlying.println("Usage: !list");
		
		return request;
	}

	private ClientRequest validateCompute(String[] split) {
		ClientRequest request = new ClientRequest();
		request.setType(RequestType.Invalid);
		
		if(split.length % 2 == 0) {
			request.setType(RequestType.Compute);
			extractArithmeticOperation(request, split);
		} else {
			underlying.println("Usage: !compute <operand> <operator> <operand> ...");
		}
		
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
