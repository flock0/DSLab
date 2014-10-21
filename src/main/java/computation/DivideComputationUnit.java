package computation;


public class DivideComputationUnit implements ComputationUnit {

	@Override
	public ComputationResult compute(NodeRequest request) {
		int dividend = request.getOperand1();
		int divisor = request.getOperand2();
		
		if(divisor == 0)
			return new ComputationResult(ResultStatus.DivisionByZero, 0);
		
		int quotient = divideAndRound(dividend, divisor);
		
		return new ComputationResult(ResultStatus.OK, quotient);
	}

	private int divideAndRound(double dividend, double divisor) {
		return roundHalfAwayFromZero(dividend / divisor);
	}

	private int roundHalfAwayFromZero(double i) {
		if(i > 0)
			return (int) (i + 0.5d);
		else
			return (int) (i - 0.5d);
	}

}
