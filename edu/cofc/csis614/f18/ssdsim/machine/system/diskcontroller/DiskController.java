package edu.cofc.csis614.f18.ssdsim.machine.system.diskcontroller;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequest;
import edu.cofc.csis614.f18.ssdsim.machine.system.disk.Disk;
import edu.cofc.csis614.f18.ssdsim.timer.Timer;

public abstract class DiskController {
    private Timer timer;
    
	Disk disk;
	
	SortedSet<Long> outstandingIoRequests;
	
	{
		outstandingIoRequests = new TreeSet<Long>();
	}
	
	public void updateTime() {
        disk.updateTime();
        
        // TODO: do anything that happens here at time timeIn
	}
	
	public abstract void setInitialDiskState();

	public abstract void sendIoRequestToDisk(IoRequest ioRequest);

	public void sendIoRequestsToDisk(Set<? extends IoRequest> ioRequests) {
		for(IoRequest ioRequest : ioRequests) {
			disk.processIoRequest(ioRequest);
		}
	}
	
	public abstract boolean isOperationsInProgress();
}
