package computation;

public class TamperedResult extends Result {

	private String originalTerm;
	
	public TamperedResult(String originalTerm) {
		super(ResultStatus.Tampered);
		this.originalTerm = originalTerm;
	}
	@Override
	public String toString() {
		return "!tampered " + originalTerm;
	}

	public static Result fromString(String in) {
		if(in == null || !in.startsWith("!tampered"))
			return new Result(ResultStatus.Error);
		
		return new TamperedResult(in.substring("!tampered".length() + 1));
	}
}
