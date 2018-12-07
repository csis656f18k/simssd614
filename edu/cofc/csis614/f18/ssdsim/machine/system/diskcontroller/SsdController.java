package edu.cofc.csis614.f18.ssdsim.machine.system.diskcontroller;

import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequest;
import edu.cofc.csis614.f18.ssdsim.machine.system.disk.Ssd;

public class SsdController extends DiskController {
	public SsdController(Ssd disk) {
		this.disk = disk;
	}

	// Older version of this file had logic for file operations here
	
	public void setInitialDiskState() {
		// TODO - unnecessary if pre-populating disk is not needed
	}
	
	@Override
	public void sendIoRequestToDisk(IoRequest ioRequest) {
		disk.processIoRequest(ioRequest);
	}
    
    public boolean isOperationsInProgress() {
        return disk.hasOperationsInProgress();
    }
}
