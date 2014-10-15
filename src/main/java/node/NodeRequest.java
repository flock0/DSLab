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
	 * @param one The first operand. Must be parsable as an Integer
	 * @param operator
	 * @param two The second operand. Must be parsable as an Integer
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
