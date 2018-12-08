package edu.cofc.csis614.f18.ssdsim.data;

import java.util.Set;

import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoResponse;

public class SingleTrialResult {
    private Set<IoResponse> rawData;
    
    private boolean finalized;
    
	private long timeTaken;
    private int totalTimeReading;
    private int numReads;
    private int totalTimeWriting;
    private int numWrites;
	
	public SingleTrialResult(Set<IoResponse> rawData) {
	    this.rawData = rawData;

        timeTaken = -1L;
	    numReads = 0;
	    numWrites = 0;
        
        finalized = false;
	}
	
	public void finalize() {
        for(IoResponse response : rawData) {
            timeTaken = (response.getTimeCompleted() > timeTaken) ? response.getTimeCompleted() : timeTaken;
            
            switch(response.getRequest().getType()) {
                case READ:
                    totalTimeReading += (response.getTimeCompleted() - response.getRequest().getStartTime());
                    numReads++;
                    break;
                case WRITE:
                    totalTimeWriting += (response.getTimeCompleted() - response.getRequest().getStartTime());
                    numWrites++;
                    break;
                default:
                    break;
            }
        }
	    
        finalized = true;
	}

    public long getTimeTakenOverall() {
        if(finalized) {
            return timeTaken;
        }
        
        else return -1L;
    }

    public int getReadCount() {
        if(finalized) {
            return numReads;
        }
        
        else return -1;
    }

    public int getWriteCount() {
        if(finalized) {
            return numWrites;
        }
        
        else return -1;
    }

    public int getReadTime() {
        if(finalized) {
            return totalTimeReading / numReads;
        }
        
        else return -1;
    }

    public int getWriteTime() {
        if(finalized) {
            return totalTimeWriting / numWrites;
        }
        
        else return -1;
    }
    
    @Override
    public String toString() {
        return "Time taken for trial: " + timeTaken;
    }
}
