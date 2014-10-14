package util;

public class ComputationResult {
	private ComputationStatus status;
	private int number;

	public ComputationStatus getStatus() {
		return status;
	}

	public void setStatus(ComputationStatus status) {
		this.status = status;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
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
		ComputationResult result = new ComputationResult();
		
		String[] split = in.split(" ");
		result.status = ComputationStatus.valueOf(split[0]);
		result.number = Integer.parseInt(split[1]);
		
		return result;
	}
}
