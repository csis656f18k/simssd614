package edu.cofc.csis614.f18.ssdsim.machine.system.cache;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
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
	
	// The contents of the actual cache. Sorted in ascending order, i.e. the element that hasn't been used in the longest time is first (index 0)
	CacheItem[] contents;
	private int maxSize;
	private int cacheSpacesUsed;
	
	int readLatency;
	int writeLatency;
    
	// Operations currently being done to the cache itself (e.g. saving a write request), organized by time of completion
    SortedMap<Long, Set<CacheResponse>> operationsInProgress;
    int operationsInProgressCount;
	
	public Cache(System system, Timer timer) {
	    this.timer = timer;
        this.system = system;
	    
        cacheSpacesUsed = 0;
	    maxSize = DiskPerformanceSimulator.DEBUG_MODE ? DEBUG_SIZE : DEFAULT_SIZE;
        contents = new CacheItem[maxSize];
	    
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
            // If this contains a request it just means the request has been written to cache; no action needed here
            // If this contains a completed response, hand it back to the system
            IoResponse response = cr.getResponse();
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

    public void handleReadRequest(IoRequest request) {
        // If cache is empty, hand the request off immediately
        if(cacheSpacesUsed == 0) {
            system.receiveIoRequestContinuingFromCache(request);
            return;
        }

        // Otherwise, there is a cache; loop through it to see if the desired value is present
        // If found, read from cache, and after cache read time, signal that the I/O request is complete
        int requestLatency = 0;
        long completionTime = -1;
        for(int cacheIndex = maxSize - 1; cacheIndex >= maxSize - cacheSpacesUsed; cacheIndex--) {
            requestLatency += readLatency;
            
            if(request.referencesSameMemory(contents[cacheIndex].getRequest())) {
                contents[cacheIndex].setLastReferencedTime(timer.getTime());
                completionTime = timer.getTime() + requestLatency;
                addInProgressCacheOperation(new CacheResponse(null, new IoResponse(request, completionTime), completionTime));
                return;
            }
        }

        // At this point the entire cache has been searched and the value wasn't found; signal that the request needs to be handed off to disk after cache read time
        completionTime = timer.getTime() + requestLatency;
        addInProgressCacheOperation(new CacheResponse(request, null, completionTime));
    }

    public void handleWriteRequest(IoRequest request) {
        // When there is a cache, loop through it to see if the desired value is present
        // If found, the old request is obsolete; replace it with the new one and silently drop the old one
        int requestLatency = 0;
        for(int cacheIndex = maxSize - 1; cacheIndex >= (maxSize - cacheSpacesUsed); cacheIndex--) {
            IoRequest cachedRequest = contents[cacheIndex].getRequest();
            requestLatency += readLatency;
            
            if(request.referencesSameMemory(cachedRequest)) {
                evict(cacheIndex);
                addToCache(cacheIndex, request, requestLatency);
                
                return;
            }
        }

        // If the value isn't found anywhere in the cache, cache the new request
        if(cacheSpacesUsed < maxSize) {
            addToCache(0, request, requestLatency);
            cacheSpacesUsed++;
            return;
        }
        
        // At this point, the value wasn't found in the cache but the cache was full; need to evict least used and hand it off to disk before caching the new request
        IoRequest evictedIoRequest = evict(0);
        system.receiveIoRequestContinuingFromCache(evictedIoRequest);
        addToCache(0, request, requestLatency);
    }
	
	private void addToCache(int index, IoRequest request, int requestLatency) {
	    contents[index] = new CacheItem(request, timer.getTime());
	    Arrays.sort(contents, new CacheComparator());

	    long completionTime = timer.getTime() + requestLatency + writeLatency;
        addInProgressCacheOperation(new CacheResponse(request, null, completionTime));
	}
	
	private void addInProgressCacheOperation(CacheResponse cr) {
	    Set<CacheResponse> existingResponsesForCompletionTime = operationsInProgress.get(cr.getCompletionTime());
        if(existingResponsesForCompletionTime == null) {
            Set<CacheResponse> newSet = new HashSet<CacheResponse>();
            newSet.add(cr);
            operationsInProgress.put(cr.getCompletionTime(), newSet);
        } else {
            existingResponsesForCompletionTime.add(cr);
        }
        operationsInProgressCount++;
	}

    // FUTURE: might want to somehow track evictions later
	private IoRequest evict(int indexToEvict) {
	    IoRequest evictedIoRequest = contents[indexToEvict].getRequest();
	    
	    // Evictee may still be in progress; loop through in-progress ops to check, and if found, drop it
        Set<Long> inProgressKeys = operationsInProgress.keySet();
        outerLoop:
        for(long key : inProgressKeys) {
            Set<CacheResponse> crSet = operationsInProgress.get(key);
            for(CacheResponse cr : crSet) {
                if(evictedIoRequest.referencesSameMemory(cr.getRequest())) {
                    crSet.remove(cr);
                    operationsInProgressCount--;
                    break outerLoop;
                }
            }
        }
	    
	    contents[indexToEvict] = null;
	    
	    return evictedIoRequest;
	}
	
	public boolean isOperationsQueued() {
	    return operationsInProgressCount > 0;
	}
	
	// A null-friendly comparator to use with the cache
	class CacheComparator implements Comparator<CacheItem> {
        @Override
        public int compare(CacheItem o1, CacheItem o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            
            return o1.compareTo(o2);
        }
    }
}
