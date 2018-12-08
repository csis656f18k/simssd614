package edu.cofc.csis614.f18.ssdsim.machine.system.diskcontroller;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequest;
import edu.cofc.csis614.f18.ssdsim.machine.system.disk.Disk;
import edu.cofc.csis614.f18.ssdsim.machine.system.disk.Ssd;

public abstract class DiskController {
	Disk disk;
	
	SortedSet<Long> outstandingIoRequests;
	
	{
		outstandingIoRequests = new TreeSet<Long>();
	}
	
	public void updateTime() {
        disk.updateTime();
	}
	
	public abstract void setInitialDiskState();

	public abstract void sendIoRequestToDisk(IoRequest ioRequest);

	public void sendIoRequestsToDisk(Set<? extends IoRequest> ioRequests) {
		for(IoRequest ioRequest : ioRequests) {
			disk.processIoRequest(ioRequest);
		}
	}
	
	public abstract boolean isOperationsInProgress();
    
    public int getReadLatency() {
        if(disk instanceof Ssd) { // TODO: this is a kluge for last-minute feature addition, fix it
            return ((Ssd) disk).getReadLatency();
        }
        
        return -1;
    }
    
    public int getWriteLatency() {
        if(disk instanceof Ssd) { // TODO: this is a kluge for last-minute feature addition, fix it
            return ((Ssd) disk).getWriteLatency();
        }
        
        return -1;
    }
    
    public int getEraseLatency() {
        if(disk instanceof Ssd) { // TODO: this is a kluge for last-minute feature addition, fix it
            return ((Ssd) disk).getEraseLatency();
        }
        
        return -1;
    }
    
    public int getSeekLatency() {
        if(disk instanceof Ssd) { // TODO: this is a kluge for last-minute feature addition, fix it
            return ((Ssd) disk).getSeekLatency();
        }
        
        return -1;
    }
}
