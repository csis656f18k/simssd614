package edu.cofc.csis614.f18.ssdsim.machine.system;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import edu.cofc.csis614.f18.ssdsim.FileOperation;
import edu.cofc.csis614.f18.ssdsim.FileOperationType;
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
	
	DiskController controller;
	Cache cache;

	Disk diskToTest; // FUTURE: support multiple disks
	SortedSet<File> files;

	private Set<IoResponse> responses;

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
	
	public void updateTime(long timeIn) {
		time = timeIn;
		controller.updateTime(time);
		// TODO: do anything that happens here at time timeIn
	}
	
	public void loadFilesToDisk(List<File> files) {
		for(File file : files) {
			FileOperation fileOperation = new FileOperation(FileOperationType.WRITE, file, -1L);
			
			Set<? extends IoRequest> writeRequests = controller.createIoRequestsForFileOperations(fileOperation, -1L);
			
			controller.sendIoRequestsToDisk(writeRequests);
		}
	}

	/**
	 * <p>Will be called by the simulator for each simulated file operation.</p>
	 * 
	 * <p>Tells the disk controller how to make the operation happen.</p>
	 * 
	 * @param fileOperation
	 */
	public void handleFileOperationRequest(FileOperation fileOperation) {
		// TODO: include logic for cache
		// FIXME
	}

	public void receiveCompletedIoOperationInfo(IoResponse response) {
		// FIXME
	}
	
	public boolean isOperationsInProgress() {
		return controller.isOperationsInProgress();
	}
}
