package model;

import java.io.Serializable;

/**
 * Please note that this class is not needed for Lab 1, but will later be
 * used in Lab 2. Hence, you do not have to implement it for the first
 * submission.
 */
public class ComputationRequestInfo implements Comparable<ComputationRequestInfo>, Serializable {	
	private static final long serialVersionUID = 430836973450693772L;
	private String nodeName;
	private String term;
	private String result;
	private String timeStamp;
	public String getNodeName() {
		return nodeName;
	}
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	public String getTerm() {
		return term;
	}
	public void setTerm(String term) {
		this.term = term;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	@Override
	public int compareTo(ComputationRequestInfo o) {
		if(timeStamp == null)
			throw new IllegalStateException("The timestamp must not be null.");
		if(o == null)
			throw new IllegalArgumentException("The parameter o must not be null.");
		return timeStamp.compareTo(o.getTimeStamp());
	}
	@Override
	public String toString() {
		return timeStamp + " [" + nodeName + "]: " + term + " = " + result;
	}		
}
