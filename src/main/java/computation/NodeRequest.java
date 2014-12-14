package computation;

/**
 * A request for computation that will be sent to a node
 */
public class NodeRequest {
	private int operand1;
	private int operand2;
	private char operator;
	
	/**
	 * Creates a new NodeRequest
	 * @param split An array containing the request for computation. The first field must include the operand1. The second field the operator and the third field the operand2. Operand1 and 2 must be parsable as an integer.
	 */
	public NodeRequest(String[] split) {
		operand1 = Integer.parseInt(split[0]);
		operand2 = Integer.parseInt(split[2]);
		this.operator = split[1].charAt(0);
	}
	
	public NodeRequest(int operand1, char operator, int operand2) {
		this.operand1 = operand1;
		this.operand2 = operand2;
		this.operator = operator;
	}
	public int getOperand1() {
		return operand1;
	}
	
	public int getOperand2() {
		return operand2;
	}
	
	public char getOperator() {
		return operator;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(operand1);
		builder.append(" ");
		builder.append(operator);
		builder.append(" ");
		builder.append(operand2);
		return builder.toString();
	}
	
	
}
