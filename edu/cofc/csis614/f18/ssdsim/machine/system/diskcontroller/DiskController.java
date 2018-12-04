package edu.cofc.csis614.f18.ssdsim.machine.system.diskcontroller;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.cofc.csis614.f18.ssdsim.FileOperation;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequest;
import edu.cofc.csis614.f18.ssdsim.machine.system.disk.Disk;

public abstract class DiskController {
	long time;
	
	Disk disk;
	
	SortedSet<Long> outstandingIoRequests;
	
	{
		outstandingIoRequests = new TreeSet<Long>();
	}
	
	public void updateTime(long timeIn) {
		time = timeIn;
	}
	
	public abstract Set<? extends IoRequest> createIoRequestsForFileOperations(FileOperation fileOperation, long currentTime);

	public void sendIoRequestsToDisk(Set<? extends IoRequest> ioRequests) {
		for(IoRequest ioRequest : ioRequests) {
			disk.processIoRequest(ioRequest);
		}
	}
	
	public abstract boolean isOperationsInProgress();
}
