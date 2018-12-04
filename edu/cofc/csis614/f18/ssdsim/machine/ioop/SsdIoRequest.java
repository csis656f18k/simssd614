package edu.cofc.csis614.f18.ssdsim.machine.ioop;

public class SsdIoRequest extends IoRequest {
	// TODO: determine whether static maxId in parent is shared across all child classes
	
	private long targetBlock;
	private int targetPage;
	private long timeReceived;
	
	public SsdIoRequest(IoRequestType type, long targetBlock, int targetPage, long currentTime) {
		super(type);
		
		this.targetBlock = targetBlock;
		this.targetPage = targetPage;
		timeReceived = currentTime;
	}
	
	public long getTargetBlock() {
		return targetBlock;
	}
	
	public long getTargetPage() {
		return targetPage;
	}
	
	public long getTimeReceived() {
		return timeReceived;
	}
}
