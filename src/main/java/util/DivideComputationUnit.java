package util;

public class DivideComputationUnit implements ComputationUnit {

	@Override
	public ComputationResult compute(String[] request) {
		int dividend = Integer.parseInt(request[0]);
		int divisor = Integer.parseInt(request[2]);
		
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
