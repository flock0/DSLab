package computation;

public class TamperedResult extends Result {

	private String originalTerm;
	
	public TamperedResult(String originalTerm) {
		super(ResultStatus.Tampered);
		this.originalTerm = originalTerm;
	}
	
	@Override
	public String toString() {
		return "!tempered " + originalTerm;
	}
}
