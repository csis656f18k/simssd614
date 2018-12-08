package edu.cofc.csis614.f18.ssdsim.machine.system;

import java.util.HashSet;
import java.util.Set;

import edu.cofc.csis614.f18.ssdsim.Utils;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequest;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoResponse;
import edu.cofc.csis614.f18.ssdsim.machine.system.cache.Cache;
import edu.cofc.csis614.f18.ssdsim.machine.system.disk.Disk;
import edu.cofc.csis614.f18.ssdsim.machine.system.disk.Ssd;
import edu.cofc.csis614.f18.ssdsim.machine.system.diskcontroller.DiskController;
import edu.cofc.csis614.f18.ssdsim.machine.system.diskcontroller.HddController;
import edu.cofc.csis614.f18.ssdsim.machine.system.diskcontroller.SsdController;
import edu.cofc.csis614.f18.ssdsim.timer.Timer;

/**
 * The computer being simulated.
 * 
 * Responsible for requesting disk operations and reporting results back to the simulator.
 */
public class System {
	Disk diskToTest; // FUTURE: support multiple disks
	DiskController controller;

	Cache cache;
	boolean useMemoization;

	private Set<IoResponse> responses;

	public System(Timer timer, Disk diskToTest) {
		this.diskToTest = diskToTest;
		diskToTest.setSystem(this);
		switch (diskToTest.getType()) {
		case SSD:
			controller = new SsdController((Ssd) diskToTest);
			break;
		case HDD:
			controller = new HddController(diskToTest);
			break;
		default:
			// TODO: implement exception, since this should never happen
			break;
		}
		
        cache = new Cache(this, timer);
		
		responses = new HashSet<IoResponse>();
	}
	
	public void setInitialDiskState() {
		controller.setInitialDiskState();
	}
	
	public void updateTime() {
        cache.updateTime();
        controller.updateTime();
	}
	
	public void enableMemoization() {
		useMemoization = true;
	}
	
	public void disableMemoization() {
		useMemoization = false;
	}

	public void handleIoRequest(IoRequest ioRequest) {
		if(useMemoization) {
			cache.handleIoRequest(ioRequest);
		} else {
			controller.sendIoRequestToDisk(ioRequest);
		}
	}
	
	public void receiveIoRequestContinuingFromCache(IoRequest requestToSendToDisk) {
	    Utils.debugPrint("Now going from cache to disk: " + requestToSendToDisk);
	    
        controller.sendIoRequestToDisk(requestToSendToDisk);
	}

	public void receiveCompletedIoOperationInfo(IoResponse response) {
		// A real computer would use the retrieved data here, somehow, but we just need to keep track of stats for simulation purposes
	    responses.add(response);
	}
	
	public boolean isOperationsInProgress() {
		return controller.isOperationsInProgress() || cache.isOperationsQueued();
	}
	
	public Set<IoResponse> getIoResponses() {
	    return responses;
	}
    
    public int getCacheReadLatency() {
        return cache.getReadLatency();
    }
    
    public int getCacheWriteLatency() {
        return cache.getWriteLatency();
    }
    
    public int getDiskReadLatency() {
        return controller.getReadLatency();
    }
    
    public int getDiskWriteLatency() {
        return controller.getWriteLatency();
    }
    
    public int getDiskEraseLatency() {
        return controller.getEraseLatency();
    }
    
    public int getDiskSeekLatency() {
        return controller.getSeekLatency();
    }
}
