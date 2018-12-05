package edu.cofc.csis614.f18.ssdsim.machine.system;

import java.util.PriorityQueue;

import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequest;

/**
 * A place for the system to store memoized operations. Logically, this would be RAM or some other type of memory that is much faster than disk (either HDD or SSD).
 * 
 * Stores the new values of the most recent n write operations.
 * If and when n is exceeded, the oldest item is evicted.
 * 
 * If a read or write request comes in for a memoized piece of memory, it reads from the cache instead of disk.
 * If a write request comes in for memoized information, the memoized value is overwritten and its time is reset.
 */
public class Cache {
	public static final int DEBUG_SIZE = 2;
	public static final int DEFAULT_SIZE = 500;
	
	PriorityQueue<IoRequest> contents;
	
	public Cache() {
	    contents = new PriorityQueue<IoRequest>();
	}
}
