package edu.cofc.csis614.f18.ssdsim.machine.system;

import java.util.HashSet;
import java.util.Set;

import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequest;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoResponse;
import edu.cofc.csis614.f18.ssdsim.machine.system.disk.Disk;
import edu.cofc.csis614.f18.ssdsim.machine.system.disk.Ssd;
import edu.cofc.csis614.f18.ssdsim.machine.system.diskcontroller.DiskController;
import edu.cofc.csis614.f18.ssdsim.machine.system.diskcontroller.HddController;
import edu.cofc.csis614.f18.ssdsim.machine.system.diskcontroller.SsdController;

/**
 * The computer being simulated.
 * 
 * Responsible for requesting disk operations and reporting results back to the simulator.
 */
public class System {
	private long time;
	
	Disk diskToTest; // FUTURE: support multiple disks
	DiskController controller;

	Cache cache;
	boolean useMemoization;

	private Set<IoResponse> responses;

	public System(Disk diskToTest) {
		cache = new Cache();
		
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
		
		responses = new HashSet<IoResponse>();
	}
	
	public void setInitialDiskState() {
		controller.setInitialDiskState();
	}
	
	public void updateTime(long timeIn) {
		time = timeIn;
		controller.updateTime(time);
		
		// TODO: do anything that happens here at time timeIn
	}
	
	public void enableMemoization() {
		useMemoization = true;
	}
	
	public void disableMemoization() {
		useMemoization = false;
	}
	
	/**
	 * <p>Will be called by the simulator for each simulated operation.</p>
	 * 
	 * <p>Tells the disk controller how to make the operation happen.</p>
	 * 
	 * @param ioRequest
	 */
	public void handleIoRequest(IoRequest ioRequest) {
		if(useMemoization) {
			// TODO: include logic for cache
		}
		
		controller.sendIoRequestToDisk(ioRequest);
	}

	public void receiveCompletedIoOperationInfo(IoResponse response) {
		// A real computer would use the retrieved data here, somehow, but we just need to keep track of stats for simulation purposes
	    
	    responses.add(response);
	}
	
	public boolean isOperationsInProgress() {
		return controller.isOperationsInProgress();
	}
	
	public Set<IoResponse> getIoResponses() {
	    return responses;
	}
}
