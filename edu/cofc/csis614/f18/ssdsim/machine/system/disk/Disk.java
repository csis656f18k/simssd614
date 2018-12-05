package edu.cofc.csis614.f18.ssdsim.machine.system.disk;

import java.util.Set;
import java.util.SortedMap;

import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequest;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoResponse;

/**
 * The disk being tested in the simulation.
 * 
 * Responsible for handling incoming I/O requests and returning data about the results, primarily the time taken to perform the operation.
 */
public abstract class Disk {
	long time;
	
	boolean blocked;
	long unblockTime;
	
	int diskCapacity;
	int readLatency;
	int writeLatency;
	
	SortedMap<Long, Set<IoResponse>> operationsInProgress;
	int operationsInProgressCount;
	
	{
	    blocked = false;
		operationsInProgressCount = 0;
	}
	
	public abstract DiskType getType();
	
	public abstract void updateTime(long timeIn);
	
	public abstract void cleanUpOldTasks();
	
	public abstract void processIoRequest(IoRequest request);
	
	public boolean hasOperationsInProgress() {
		return operationsInProgressCount > 0;
	}
}
