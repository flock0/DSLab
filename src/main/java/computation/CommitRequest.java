package computation;

public class CommitRequest implements Request {
	
	private int resources;
	
	public CommitRequest(String resources) {
		this.resources = Integer.valueOf(resources);
	}
	
	public int getResources() {
		return resources;
	}
}
