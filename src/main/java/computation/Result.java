package computation;

public class Result {
	protected ResultStatus status;
	
	public Result(ResultStatus status)
	{
		this.status = status;
	}
	
	public ResultStatus getStatus() {
		return status;
	}
	
}
