package edu.cofc.csis614.f18.ssdsim.machine.system;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

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
	
	Disk diskToTest;
	DiskController controller;

	Cache cache;
	boolean useMemoization;

	//private Set<IoResponse> responses;

	public System(Disk diskToTest) {
		cache = new Cache();
		
		this.diskToTest = diskToTest;
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
		// FIXME - TOP PRIORITY handle this (probably just pass it back to the simulator for processing)
	}
	
	public boolean isOperationsInProgress() {
		return controller.isOperationsInProgress();
	}
}
