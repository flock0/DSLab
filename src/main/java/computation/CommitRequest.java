package computation;

public class CommitRequest implements Request {
	
	private int resources;
	
	public CommitRequest(String message) {
		// if resources >= 0: !commit
		// else !rollback
		if(message.startsWith("!commit "))
			this.resources = Integer.valueOf(message.substring(8).trim());
		else
			this.resources = -1;
	}
	
	public int getResources() {
		return resources;
	}
}
