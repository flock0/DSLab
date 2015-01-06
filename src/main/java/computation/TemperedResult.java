package computation;

public class TemperedResult extends Result {

	private String originalTerm;
	
	public TemperedResult(ResultStatus status, String originalTerm) {
		super(status);
		this.originalTerm = originalTerm;
	}
	
	@Override
	public String toString() {
		return "!tempered " + originalTerm;
	}
}
