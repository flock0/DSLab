package node;

/**
 * A request for computation that will be sent to a node
 */
public class NodeRequest {
	private int operand1;
	private int operand2;
	private String operator;
	
	/**
	 * Creates a new NodeRequest
	 * @param split An array containing the request for computation. The first field must include the operand1. The second field the operator and the third field the operand2. Operand1 and 2 must be parsable as an integer.
	 */
	public NodeRequest(String[] split) {
		operand1 = Integer.parseInt(split[0]);
		operand2 = Integer.parseInt(split[2]);
		this.operator = split[1];
	}
	
	public int getOperand1() {
		return operand1;
	}
	
	public int getOperand2() {
		return operand2;
	}
	
	public String getOperator() {
		return operator;
	}
	
	
}
