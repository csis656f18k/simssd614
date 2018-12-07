package edu.cofc.csis614.f18.ssdsim.timer;

public class Timer {
	private long currentTime;
	
	{
	    currentTime = 0L;
	}
	
	public void stepForward() {
		currentTime++;
	}
	
	public long getTime() {
	    return currentTime;
	}
    
    @Override
    public String toString() {
        return "Current time: " + currentTime;
    }
}
