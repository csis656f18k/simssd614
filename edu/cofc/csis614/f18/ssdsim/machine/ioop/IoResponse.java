package edu.cofc.csis614.f18.ssdsim.machine.ioop;

public class IoResponse {
	private long requestId;
	private long timeCompleted;
	
	public IoResponse(long requestId, long timeCompleted) {
		this.requestId = requestId;
		this.timeCompleted = timeCompleted;
	}
	
	public long getRequestId() {
		return requestId;
	}
	
	public long getTimeCompleted() {
		return timeCompleted;
	}
}
