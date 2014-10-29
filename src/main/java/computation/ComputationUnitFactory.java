package computation;

public class ComputationUnitFactory {
	
	/**
	 * Creates an appropriate ComputationUnit depending on the request.
	 * @param request The request that should be computed
	 * @param allowedOperators A string with the allowed arithmetic operators
	 * @return An appropriate ComputationUnit
	 */
	public static ComputationUnit createUnit(NodeRequest request, String allowedOperators) {
		if(allowedOperators.indexOf(request.getOperator()) != -1)
			return getNewComputationUnit(request.getOperator());
		else
			return new UnsupportedComputationUnit();
				
	}

	private static ComputationUnit getNewComputationUnit(char operator) {
		switch(operator) {
		case '+':
			return new AddComputationUnit();
		case '-':
			return new SubtractComputationUnit();
		case '*':
			return new MultiplyComputationUnit();
		case '/':
			return new DivideComputationUnit();
		default:
			return new UnsupportedComputationUnit();
		}
	}
}
