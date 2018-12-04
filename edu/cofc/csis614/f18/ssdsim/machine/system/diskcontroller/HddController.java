package edu.cofc.csis614.f18.ssdsim.machine.system.diskcontroller;

import java.util.Set;

import edu.cofc.csis614.f18.ssdsim.FileOperation;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequest;
import edu.cofc.csis614.f18.ssdsim.machine.system.disk.Disk;

public class HddController extends DiskController {
	public HddController(Disk disk) {
		this.disk = disk;
	}

	@Override
	public Set<IoRequest> createIoRequestsForFileOperations(FileOperation fileOperation, long currentTime) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOperationsInProgress() {
		// TODO Auto-generated method stub
		return false;
	}
}
