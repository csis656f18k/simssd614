package edu.cofc.csis614.f18.ssdsim.machine.ioop;

public class IoRequest {
	private IoRequestType type;
	
	private static long maxId;
	private long id;
	
	private long time;
	
	static {
		maxId = 0;
	}
	
	public IoRequest(IoRequestType type) {
		maxId++;
		id = maxId;
		
		this.type = type;
	}

	public long getId() {
		return id;
	}

	public IoRequestType getType() {
		return type;
	}
	
	public long getTime() {
		return time;
	}
}
