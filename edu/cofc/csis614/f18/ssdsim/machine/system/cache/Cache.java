package edu.cofc.csis614.f18.ssdsim.machine.system.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequest;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequestType;
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
	    maxSize = DEBUG_SIZE;
	    
	    readLatency = DiskConstants.RAM.getReadLatency();
	    writeLatency = DiskConstants.RAM.getWriteLatency();
	    
        operationsInProgress = new TreeMap<Long, Set<CacheResponse>>();
        operationsInProgressCount = 0;
	}

    public void updateTime() {
        // TODO: do anything that happens here at time timeIn, i.e. see if any operations on the cache have finished up and are ready to be sent onwards
        
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
	        case WRITE:
                handleWriteRequest(request);
            default:
                // FUTURE: this should never happen, implement an exception
                break;
	    }
	}

    public void handleReadRequest(IoRequest request) {
        int requestLatency = 0;
        // Loop through the cache to see if the desired value is present
        for(int i = contents.size() - 1; i >= 0; i--) {
            requestLatency += readLatency; //            request.increaseLatency(readLatency);
            
            // If found, read from cache, and after the appropriate time interval, signal that the I/O request is complete
            if(request.referencesSameMemory(contents.get(i).getRequest())) {
                addInProgressCacheOperation(timer.getTime() + requestLatency, new CacheResponse(null, new IoResponse(request.getId(), request.getType(), timer.getTime() + requestLatency)));
            }
        }

        // If the value isn't found anywhere in the cache, signal that the request needs to be handed off to disk
        addInProgressCacheOperation(timer.getTime() + requestLatency, new CacheResponse(request, null));
    }

    public void handleWriteRequest(IoRequest request) {
        int requestLatency = 0;
        // Loop through the cache to see if the desired value is present
        int cacheCurrentSize = contents.size();
        for(int i = cacheCurrentSize - 1; i >= 0; i--) {
            requestLatency += readLatency; //            request.increaseLatency(readLatency);
            
            IoRequest cachedRequest = contents.get(i).getRequest();
            // If found, the old request is obsolete; replace it with the new one and silently drop the old one
            if(request.referencesSameMemory(cachedRequest)) {
                addToCache(i, request); // FIXME: make this write not take effect until after read + write latencies

                // FUTURE: might want to keep tracking evictions later, but current calculations don't use these data so it's a no-op for now
            }
        }

        // If the value isn't found anywhere in the cache, cache the new request
        // If that caused an eviction, signal that it needs to be handed off to disk 
        if(cacheCurrentSize == maxSize) {
            IoRequest requestToEvict = contents.get(0).getRequest();
            
            addToCache(0, request);
            requestLatency += writeLatency;

            addInProgressCacheOperation(timer.getTime() + requestLatency, new CacheResponse(requestToEvict, null));
        } else {
            addToCache(cacheCurrentSize, request);
        }
    }
	
	private void addToCache(int index, IoRequest request) {
	    contents.add(index, new CacheItem(request, timer.getTime()));
        request.increaseLatency(writeLatency);
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
	
	public boolean isOperationsQueued() {
	    return !contents.isEmpty();
	}
}
