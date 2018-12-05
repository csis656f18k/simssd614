package edu.cofc.csis614.f18.ssdsim.machine.system.diskcontroller;

import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequest;
import edu.cofc.csis614.f18.ssdsim.machine.system.disk.Disk;

public class HddController extends DiskController {
	public HddController(Disk disk) {
		this.disk = disk;
	}

	@Override
	public void setInitialDiskState() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void sendIoRequestToDisk(IoRequest ioRequest) {
		disk.processIoRequest(ioRequest);
	}

	@Override
	public boolean isOperationsInProgress() {
		// TODO Auto-generated method stub
		return false;
	}
}
