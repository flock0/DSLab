package controller;

import computation.ComputationRequestType;

/**
 * Contains a single client command / request
 */
public class ClientRequest {
	private ComputationRequestType type;
	private String username;
	private String password;
	private int buyAmount;
	private int[] operands;
	private char[] operators;
	public ComputationRequestType getType() {
		return type;
	}
	public void setType(ComputationRequestType type) {
		this.type = type;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getBuyAmount() {
		return buyAmount;
	}
	public void setBuyAmount(int buyAmount) {
		this.buyAmount = buyAmount;
	}
	public int[] getOperands() {
		return operands;
	}
	public void setOperands(int[] operands) {
		this.operands = operands;
	}
	public char[] getOperators() {
		return operators;
	}
	public void setOperators(char[] operators) {
		this.operators = operators;
	}
}
