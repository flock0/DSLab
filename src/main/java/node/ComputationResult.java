package node;

import util.ResultStatus;

public class ComputationResult {
	
	private ResultStatus status;
	private int number;

	public ComputationResult(ResultStatus status, int number) {
		this.status = status;
		this.number = number;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(status);
		builder.append(" ");
		builder.append(number);
		return builder.toString();
	}

	public static ComputationResult fromString(String in) {
		if(in == null)
			return new ComputationResult(ResultStatus.OperatorNotSupported, 0);
		
		String[] split = in.split("\\s");
		ResultStatus status = ResultStatus.valueOf(split[0]);
		int number = Integer.parseInt(split[1]);
		
		return new ComputationResult(status, number);
	}

	public ResultStatus getStatus() {
		return status;
	}

	public int getNumber() {
		return number;
	}
}
