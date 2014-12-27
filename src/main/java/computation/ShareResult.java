package computation;

public class ShareResult extends Result {

	private boolean result;
	
	public ShareResult(ResultStatus status, boolean result) {
		super(status);
		this.result = result;
	}

	@Override
	public String toString() {
		if(result)
			return "!ok";
		else
			return "!nok";
	}
}
