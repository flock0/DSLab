package node;

import util.ComputationResult;

public interface ComputationUnit {
	/**
	 * Performes an arithmetic operation
	 */
	public ComputationResult compute(NodeRequest request);
}
