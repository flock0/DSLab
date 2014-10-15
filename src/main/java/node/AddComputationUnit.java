package node;

import util.ComputationResult;
import util.ResultStatus;

public class AddComputationUnit implements ComputationUnit {

	@Override
	public ComputationResult compute(String[] request) {
		int number = Integer.parseInt(request[0]) + Integer.parseInt(request[2]);
		return new ComputationResult(ResultStatus.OK, number);
	}

}
