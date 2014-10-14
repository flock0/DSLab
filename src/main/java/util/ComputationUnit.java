package util;

public interface ComputationUnit {
	/**
	 * Performes an arithmetic operation
	 * @param request An array that contains the first operand, the operator and the second operand in the first three fields
	 * @return The result of the arithmetic operation
	 */
	public ComputationResult compute(String[] request);
}
