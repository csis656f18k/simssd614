package edu.cofc.csis614.f18.ssdsim.machine.ioop;

public class IoResponse {
	private long requestId;
    private IoRequestType type;
	private long timeCompleted;
	private IoRequest request;
    
    public IoResponse(IoRequest request, long timeCompleted) {
        requestId = request.getId();
        type = request.getType();
        this.timeCompleted = timeCompleted;
        this.request = request;
    }
    
    public long getRequestId() {
        return requestId;
    }
    
    public IoRequestType getRequestType() {
        return type;
    }
    
    public IoRequest getRequest() {
        return request;
    }
	
	public long getTimeCompleted() {
		return timeCompleted;
	}
    
    @Override
    public String toString() {
        return "IO response for request #" + requestId + " (" + request + ") - complete at " + timeCompleted;
    }
}
