package edu.cofc.csis614.f18.ssdsim.data;

import java.util.Set;

import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoResponse;

public class SingleTrialResult {
    // private Set<IoResponse> rawData;
	private long timeTaken;
	
	public SingleTrialResult(Set<IoResponse> rawData) {
	    // this.rawData = rawData;
	    
	    // FUTURE processing of stuff like total time taken, time taken by operation type
	    
	    timeTaken = -1L;
	    for(IoResponse response : rawData) {
	        timeTaken = (response.getTimeCompleted() > timeTaken) ? response.getTimeCompleted() : timeTaken;
	    }
	}

    public long getTimeTakenOverall() {
        return timeTaken;
    }
    
    @Override
    public String toString() {
        return "Time taken for trial: " + timeTaken;
    }
}
