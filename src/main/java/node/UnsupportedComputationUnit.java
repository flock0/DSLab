package node;

import util.ComputationResult;
import util.ResultStatus;

public class UnsupportedComputationUnit implements ComputationUnit {

	@Override
	public ComputationResult compute(NodeRequest request) {
		return new ComputationResult(ResultStatus.OperatorNotSupported, 0);
	}

}
