package edu.cofc.csis614.f18.ssdsim.machine.ioop;

public class IoResponse {
	private long requestId;
    private IoRequestType type;
	private long timeCompleted;
	
	public IoResponse(long requestId, IoRequestType type, long timeCompleted) {
		this.requestId = requestId;
		this.type = type;
		this.timeCompleted = timeCompleted;
	}
    
    public long getRequestId() {
        return requestId;
    }
    
    public IoRequestType getRequestType() {
        return type;
    }
	
	public long getTimeCompleted() {
		return timeCompleted;
	}
}
