package computation;

public class ShareRequest implements Request {
	
	private int resources;
	
	public ShareRequest(String resources) {
		this.resources = Integer.valueOf(resources);
	}
	
	public int getResources() {
		return resources;
	}
}
