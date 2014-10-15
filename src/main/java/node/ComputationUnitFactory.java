package node;

import com.sun.org.apache.xpath.internal.operations.Mult;

public class ComputationUnitFactory {
	
	/**
	 * Creates an appropriate ComputationUnit depending on the request.
	 * @param request An array that contains the first operand, the operator and the second operand in the first three fields
	 * @param allowedOperators A string with the allowed arithmetic operators
	 * @return An appropriate ComputationUnit
	 */
	public static ComputationUnit createUnit(NodeRequest request, String allowedOperators) {
		if(allowedOperators.contains(request.getOperator()))
			return getNewComputationUnit(request.getOperator());
		else
			return new UnsupportedComputationUnit();
				
	}

	private static ComputationUnit getNewComputationUnit(String operator) {
		switch(operator) {
		case "+":
			return new AddComputationUnit();
		case "-":
			return new SubtractComputationUnit();
		case "*":
			return new MultiplyComputationUnit();
		case "/":
			return new DivideComputationUnit();
		default:
			return new UnsupportedComputationUnit();
		}
	}
}
