package edu.cofc.csis614.f18.ssdsim.machine.system.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.cofc.csis614.f18.ssdsim.DiskPerformanceSimulator;
import edu.cofc.csis614.f18.ssdsim.Utils;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequest;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoResponse;
import edu.cofc.csis614.f18.ssdsim.machine.system.System;
import edu.cofc.csis614.f18.ssdsim.machine.system.disk.DiskConstants;
import edu.cofc.csis614.f18.ssdsim.timer.Timer;

/**
 * A place for the system to store memoized operations. Logically, this would be RAM or some other type of memory that is much faster than disk (either HDD or SSD).
 * 
 * Stores the new values of the most recent n write operations.
 * If and when n is exceeded, the oldest item is evicted.
 * 
 * If a read or write request comes in for a memoized piece of memory, it reads from the cache instead of disk.
 * If a write request comes in for memoized information, the memoized value is overwritten.
 * When a memoized request is used, its time is reset.
 * 
 * When a memoized request is evicted from cache, it is added to the standard IO request queue.
 */
public class Cache {
	public static final int DEBUG_SIZE = 2;
	
	public static final int DEFAULT_SIZE = 500;

    public static final int DEBUG_READ_LATENCY = 1;
    public static final int DEBUG_WRITE_LATENCY = 2;

    public static final int DEFAULT_READ_LATENCY = DiskConstants.RAM.getReadLatency();
    public static final int DEFAULT_WRITE_LATENCY = DiskConstants.RAM.getWriteLatency();
	
	private Timer timer;
    
    System system;
	
	// Sorted in ascending order, i.e. the element that hasn't been used in the longest time is first (index 0)
	List<CacheItem> contents;
	private int maxSize;
	
	int readLatency;
	int writeLatency;
    
    SortedMap<Long, Set<CacheResponse>> operationsInProgress;
    int operationsInProgressCount;
	
	public Cache(System system, Timer timer) {
	    this.timer = timer;
        this.system = system;
	    
	    contents = new ArrayList<CacheItem>();
	    maxSize = DiskPerformanceSimulator.DEBUG_MODE ? DEBUG_SIZE : DEFAULT_SIZE;
	    
	    readLatency = DiskPerformanceSimulator.DEBUG_MODE ? DEBUG_READ_LATENCY : DEFAULT_READ_LATENCY;
	    writeLatency = DiskPerformanceSimulator.DEBUG_MODE ? DEBUG_WRITE_LATENCY : DEFAULT_WRITE_LATENCY;
	    
        operationsInProgress = new TreeMap<Long, Set<CacheResponse>>();
        operationsInProgressCount = 0;
	}

    /**
     * This gets run at the start of every time tick.
     * Check to see if anything in progress has finished, and if so, send a response or forward to disk as appropriate. 
     * 
     * Once this stuff is done, the cache can accept new incoming requests for this time tick.
     */
    public void updateTime() {
        Utils.debugPrint("Requests being processed by cache at time " + timer.getTime() + ": ");
        for(long time : operationsInProgress.keySet()) {
            Utils.debugPrint("Scheduled to complete at time " + time + ":");
            for(CacheResponse response : operationsInProgress.get(time)) {
                Utils.debugPrint("- " + response);
            }
        }
        
        cleanUpOldTasks();
    }
    
    public void cleanUpOldTasks() {
        Set<CacheResponse> completedOperations = operationsInProgress.remove(timer.getTime());

        if(completedOperations == null) {
            return;
        }
        
        operationsInProgressCount -= completedOperations.size();
        for(CacheResponse cr : completedOperations) {
            // if going to disk, send to disk; otherwise, respond to system
            IoRequest request = cr.getRequest();
            IoResponse response = cr.getResponse();
            
            if(request != null) {
              system.receiveIoRequestContinuingFromCache(request);
            }
            if(response != null) {
              system.receiveCompletedIoOperationInfo(response);
            }
        }
    }
	
	public void handleIoRequest(IoRequest request) {
	    switch(request.getType()) {
	        case READ:
                handleReadRequest(request);
                break;
	        case WRITE:
                handleWriteRequest(request);
                break;
            default:
                // FUTURE: this should never happen, implement an exception
                break;
	    }
	}

	//FIXME ensure lastUpdatedTime updated correctly
    public void handleReadRequest(IoRequest request) {
        int requestLatency = 0;
        // Loop through the cache to see if the desired value is present
        for(int i = contents.size() - 1; i >= 0; i--) {
            requestLatency += readLatency;
            
            // If found, read from cache, and after the appropriate time interval, signal that the I/O request is complete
            if(request.referencesSameMemory(contents.get(i).getRequest())) {
                addInProgressCacheOperation(timer.getTime() + requestLatency, new CacheResponse(null, new IoResponse(request, timer.getTime() + requestLatency)));
            }
        }

        // Otherwise, value isn't found anywhere in the cache, signal that the request needs to be handed off to disk
        if(requestLatency == 0) {
            // If the cache was empty, no time was spent looking through it; hand off directly
            system.receiveIoRequestContinuingFromCache(request);
        } else {
            // Otherwise wait until the cache reading is complete, then hand off
            addInProgressCacheOperation(timer.getTime() + requestLatency, new CacheResponse(request, null));
        }
    }

    //FIXME confirm removal, not just read, of evicted req; i.e. drop it from operationsInProgress
    public void handleWriteRequest(IoRequest request) {
        int requestLatency = 0;
        
        // Loop through the cache to see if the desired value is present
        int cacheCurrentSize = contents.size();
        for(int i = cacheCurrentSize - 1; i >= 0; i--) {
            requestLatency += readLatency;
            
            IoRequest cachedRequest = contents.get(i).getRequest();
            
            // If found, the old request is obsolete; replace it with the new one and silently drop the old one
            if(request.referencesSameMemory(cachedRequest)) {
                addToCache(i, request);
                //operationsInProgressCount++;
                evict(request);

                // FUTURE: might want to keep tracking evictions later, but current calculations don't use these data so it's a no-op for now
            }
        }

        // If the value isn't found anywhere in the cache, cache the new request
        // If that caused an eviction, signal that it needs to be handed off to disk 
        if(cacheCurrentSize == maxSize) {
            IoRequest requestToEvict = contents.get(0).getRequest();
            
            addToCache(0, request);
            requestLatency += writeLatency;
            
            evict(request);

            //addInProgressCacheOperation(timer.getTime() + requestLatency, new CacheResponse(requestToEvict, null));
        } else {
            addToCache(cacheCurrentSize, request);
        }
    }
	
	private void addToCache(int index, IoRequest request) {
	    contents.add(index, new CacheItem(request, timer.getTime()));
	    addInProgressCacheOperation(timer.getTime() + writeLatency, new CacheResponse(request, null));
	    Collections.sort(contents);
	}
	
	private void addInProgressCacheOperation(long completionTime, CacheResponse cr) {
	    Set<CacheResponse> existingResponsesForCompletionTime = operationsInProgress.get(completionTime);
        if(existingResponsesForCompletionTime == null) {
            Set<CacheResponse> newSet = new HashSet<CacheResponse>();
            newSet.add(cr);
            operationsInProgress.put(completionTime, newSet);
        } else {
            existingResponsesForCompletionTime.add(cr);
        }
        operationsInProgressCount++;
	}
	
	/*
	 * This removes the marker representing the given request from the list of operations in progress
	 * 
	 * Pre-condition: the cache itself has already been updated with the new value; this method is about ONLY operationsInProgress
	 * 
	 * This method will decrement operationsInProgressCount, so the counter must be actively incremented elsewhere to maintain the proper value.
	 * 
	 * @param request an IoRequest referencing the same memory location on disk as the request to evict 
	 */
	private void evict(IoRequest request) {
	    CacheResponse crToEvict = null;
        Set<CacheResponse> containingSet = null;
	    
	    Set<Long> completionTimes = operationsInProgress.keySet();
	    outerLoop:
	    for(long time : completionTimes) {
	        Set<CacheResponse> crs = operationsInProgress.get(time);
	        for(CacheResponse cr : crs) {
	            if(request.referencesSameMemory(cr.getRequest())) {
	                containingSet = crs;
	                crToEvict = cr;
	                break outerLoop;
	            }
	        }
	    }
	    
        // TODO if this block isn't entered it's kind of an error, implement an exception later 
	    if(crToEvict != null) {
	        containingSet.remove(crToEvict);
	        operationsInProgressCount--;
	    }
	}
	
	public boolean isOperationsQueued() {
	    return operationsInProgressCount > 0;
	}
}
