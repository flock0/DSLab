package util;

public class SubtractComputationUnit implements ComputationUnit {

	@Override
	public ComputationResult compute(String[] request) {
		int number = Integer.parseInt(request[0]) - Integer.parseInt(request[2]);
		return new ComputationResult(ResultStatus.OK, number);
	}

}
