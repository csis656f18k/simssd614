package edu.cofc.csis614.f18.ssdsim.machine.system.disk;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequest;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoResponse;
import edu.cofc.csis614.f18.ssdsim.machine.system.System;
import edu.cofc.csis614.f18.ssdsim.timer.Timer;

/**
 * The disk being tested in the simulation.
 * 
 * Responsible for handling incoming I/O requests and returning data about the results, primarily the time taken to perform the operation.
 */
public abstract class Disk {
    Timer timer;
    
    System system;
	
	boolean blocked;
	long unblockTime;
	
	int diskCapacity;
	int readLatency;
	int writeLatency;
	
	SortedMap<Long, Set<IoResponse>> operationsInProgress;
	int operationsInProgressCount;
	
	{
	    blocked = false;
	    operationsInProgress = new TreeMap<Long, Set<IoResponse>>();
		operationsInProgressCount = 0;
	}
	
	public abstract DiskType getType();
    
    public void setSystem(System system) {
        this.system = system;
    }
	
	public abstract void updateTime();
	
	public abstract void cleanUpOldTasks();
	
	public abstract void processIoRequest(IoRequest request);
	
	public boolean hasOperationsInProgress() {
		return operationsInProgressCount > 0;
	}
}
