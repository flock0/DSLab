package node;

import com.sun.org.apache.xpath.internal.operations.Mult;

public class ComputationUnitFactory {
	
	/**
	 * Creates an appropriate ComputationUnit depending on the request.
	 * @param request An array that contains the first operand, the operator and the second operand in the first three fields
	 * @param allowedOperators A string with the allowed arithmetic operators
	 * @return An appropriate ComputationUnit
	 */
	public static ComputationUnit createUnit(String[] request, String allowedOperators) {
		if(allowedOperators.contains(request[1]))
			return getNewComputationUnit(request[1]);
		else
			return new UnsupportedComputationUnit();
				
	}

	private static ComputationUnit getNewComputationUnit(String string) {
		switch(string) {
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
