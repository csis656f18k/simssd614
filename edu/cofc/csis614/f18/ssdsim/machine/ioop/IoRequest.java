package edu.cofc.csis614.f18.ssdsim.machine.ioop;

public abstract class IoRequest {
	private IoRequestType type;
	
	private static long maxId;
	private long id;
	
	private long startTime;
	
	static {
		maxId = 0;
	}
	
	public IoRequest(IoRequestType type, long time) {
		maxId++;
		id = maxId;
		
		this.type = type;
		
		startTime = time;
	}

	public long getId() {
		return id;
	}

	public IoRequestType getType() {
		return type;
	}
    
    public long getStartTime() {
        return startTime;
    }

    public abstract boolean referencesSameMemory(IoRequest other);

    public abstract boolean referencesSameMemory(IoResponse other);
}
