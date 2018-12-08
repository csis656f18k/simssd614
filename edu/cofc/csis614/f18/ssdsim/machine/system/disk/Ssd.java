package edu.cofc.csis614.f18.ssdsim.machine.system.disk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cofc.csis614.f18.ssdsim.DiskPerformanceSimulator;
import edu.cofc.csis614.f18.ssdsim.Utils;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequest;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoResponse;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.SsdIoRequest;
import edu.cofc.csis614.f18.ssdsim.timer.Timer;

/**
 * A simulation of a solid-state disk.
 */
public class Ssd extends Disk {
	public static final int DEBUG_BLOCKS_PER_SSD = 4;
	public static final int DEBUG_PAGES_PER_BLOCK = 6;
	public static final int DEBUG_PAGE_CAPACITY = 50;

	public static final int DEFAULT_BLOCKS_PER_SSD = 250;
	public static final int DEFAULT_PAGES_PER_BLOCK = 256;
	public static final int DEFAULT_PAGE_CAPACITY = 8000; // bytes

	public static final int DEBUG_READ_LATENCY = 2;
    public static final int DEBUG_WRITE_LATENCY = 20;
    public static final int DEBUG_ERASE_LATENCY = 70;
    public static final int DEBUG_SEEK_LATENCY = 0;

	public static final int DEFAULT_READ_LATENCY = DiskConstants.SSD_TLC.getReadLatency();
    public static final int DEFAULT_WRITE_LATENCY = DiskConstants.SSD_TLC.getWriteLatency();
    public static final int DEFAULT_ERASE_LATENCY = DiskConstants.SSD_TLC.getEraseLatency();
    public static final int DEFAULT_SEEK_LATENCY = DiskConstants.SSD_TLC.getSeekLatency();
	
	private int blocksPerDisk;
	private int blockCapacity;
	private int pagesPerBlock;
	private int pageCapacity;
	
	List<SsdBlock> blocks;
	
	public Ssd(Timer timer) {
		pageCapacity = DiskPerformanceSimulator.DEBUG_MODE ? DEBUG_PAGE_CAPACITY : DEFAULT_PAGE_CAPACITY;
		pagesPerBlock = DiskPerformanceSimulator.DEBUG_MODE ? DEBUG_PAGES_PER_BLOCK : DEFAULT_PAGES_PER_BLOCK;
		blocksPerDisk = DiskPerformanceSimulator.DEBUG_MODE ? DEBUG_BLOCKS_PER_SSD : DEFAULT_BLOCKS_PER_SSD;
		
		blockCapacity = pageCapacity * pagesPerBlock;
		diskCapacity = blockCapacity * blocksPerDisk;
		
		readLatency = DiskPerformanceSimulator.DEBUG_MODE ? DEBUG_READ_LATENCY : DEFAULT_READ_LATENCY;
		writeLatency = DiskPerformanceSimulator.DEBUG_MODE ? DEBUG_WRITE_LATENCY : DEFAULT_WRITE_LATENCY;
		eraseLatency = DiskPerformanceSimulator.DEBUG_MODE ? DEBUG_ERASE_LATENCY : DEFAULT_ERASE_LATENCY;
		seekLatency = DiskPerformanceSimulator.DEBUG_MODE ? DEBUG_SEEK_LATENCY : DEFAULT_SEEK_LATENCY;
		
		this.timer = timer;
		
		blocks = new ArrayList<SsdBlock>();
		for(int i = 0; i < blocksPerDisk; i++) {
			blocks.add(new SsdBlock());
		}
	}

	@Override
	public DiskType getType() {
		return DiskType.SSD;
	}
	
	public int getBlocksPerDisk() {
		return blocksPerDisk;
	}
	
	public int getPagesPerBlock() {
		return pagesPerBlock;
	}
	
	public int getPageCapacity() {
		return pageCapacity;
	}

	/**
	 * This gets run at the start of every time tick.
     * Check to see if anything in progress has finished, and if so, send a response. 
	 * 
	 * Once this stuff is done, the disk can accept new incoming requests for this time tick.
	 */
	@Override
	public void updateTime() {
	    Utils.debugPrint("Requests being processed by disk at time " + timer.getTime() + ": ");
	    for(long time : operationsInProgress.keySet()) {
	        Utils.debugPrint("Scheduled to complete at time " + time + ":");
	        for(IoResponse response : operationsInProgress.get(time)) {
	            Utils.debugPrint("- " + response);
	        }
	    }
	    
		cleanUpOldTasks();
	}

	@Override
	public void cleanUpOldTasks() {
		Set<IoResponse> completedOperations = operationsInProgress.remove(timer.getTime());
		
		if(diskIsIdle()) {
		    // FUTURE: restore blocking functionality
	        
	        doGarbageCollection();
		}

		if(completedOperations == null) {
		    return;
		}
		
        for(IoResponse response : completedOperations) {
            system.receiveCompletedIoOperationInfo(response);
            operationsInProgressCount--;
        }
	}
	
	private boolean diskIsIdle() {
	    Set<Long> keys = operationsInProgress.keySet();
	    for(long key : keys) {
	        Set<IoResponse> values = operationsInProgress.get(key);
	        if(values != null && !values.isEmpty()) {
	            return false;
	        }
	    }
	    
	    return true;
	}
	
	private void doGarbageCollection() {
		SsdBlock emptyBlock = findRandomEmptyBlock();
		if(emptyBlock == null) {
		    return;
		}
		
		blocked = true;
		unblockTime = timer.getTime() + eraseLatency;
		
		SsdBlock staleBlock = null;
		for(SsdBlock block : blocks) {
			if(!block.containsStalePage()) {
				continue;
			}
			
			// For all blocks containing any stale pages, copy any useful data to a free block and then wipe the stale block
			staleBlock = block;
			for(int pageIndex = 0; pageIndex < pagesPerBlock; pageIndex++) {
				SsdPage page = staleBlock.pages.get(pageIndex);
				if(page.status == SsdPageStatus.IN_USE) {
					emptyBlock.pages.get(pageIndex).status = SsdPageStatus.IN_USE;
				} else {
					emptyBlock.pages.get(pageIndex).status = SsdPageStatus.AVAILABLE;
				}
				
				page.status = SsdPageStatus.AVAILABLE;
			}
			
			emptyBlock = staleBlock;
		}
	}
	
	private SsdBlock findRandomEmptyBlock() {
        for(SsdBlock block : blocks) {
            if(block.readyToBeWritten()) {
                return block;
            }
        }
        
        return null;
	}

	@Override
	public void processIoRequest(IoRequest requestIn) {
		SsdIoRequest request = (SsdIoRequest) requestIn;
		
		int completionTime;
		switch(request.getType()) {
    		case READ:
    			completionTime = handleReadRequest(request);
    			break;
    		case WRITE:
    			completionTime = handleWriteRequest(requestIn);
    			break;
    		default:
    			// TODO should never happen, implement exception
    			completionTime = -111;
    			break;
		}
		
		// Don't need to worry about simulating actual I/O; just track how long it takes
		addInProgressRequest(completionTime, new IoResponse(request, completionTime));
	}
    
    private void addInProgressRequest(long completionTime, IoResponse ir) {
        Set<IoResponse> existingResponsesForCompletionTime = operationsInProgress.get(completionTime);
        if(existingResponsesForCompletionTime == null) {
            Set<IoResponse> newSet = new HashSet<IoResponse>();
            newSet.add(ir);
            operationsInProgress.put(completionTime, newSet);
        } else {
            existingResponsesForCompletionTime.add(ir);
        }
        operationsInProgressCount++;
    }
    
    private int handleReadRequest(IoRequest readRequest) {
        // No updating needed, reads don't change state
        // All reads take the same amount of time
        
        // If ops in progress are using this mem location, don't start this until they're done (modify latency accordingly)
        Set<IoResponse> allOperationsInProgress = getAllOperationsInProgress();
        long memoryWillBeAvailableTime = timer.getTime();
        for(IoResponse response : allOperationsInProgress) {
            if(readRequest.referencesSameMemory(response)) {
                if(memoryWillBeAvailableTime < response.getTimeCompleted()) {
                    memoryWillBeAvailableTime = response.getTimeCompleted();
                }
            }
        }
        
        return (int) (memoryWillBeAvailableTime + readLatency * getBlocksForRequest(readRequest).size());
    }
	
	/*
	 * PSEUDO
	 * 
	 * Preconditions:
	 * IoRequest is a request of type WRITE
	 * 
	 * Behavior:
	 * Set file's existing location(s) to STALE
	 * Write new version of file where there is free space (i.e. change given number of pages from AVAILABLE to IN_USE)
	 * 
	 * Postconditions:
	 * WAIT FOR LATENCY, then return the new location(s) of the file to the controller 
	 * 
	 * @return the total latency of the write operation
	 */
	private int handleWriteRequest(IoRequest writeRequest) {
		// FUTURE - get more sophisticated with tracking status of individual pages on disk
	    int latency = 0;
	    
	    Set<SsdBlock> blocksToWrite = getBlocksForRequest(writeRequest);

        // If ops in progress are using this mem location, don't start this until they're done (modify latency accordingly)
	    Set<IoResponse> allOperationsInProgress = getAllOperationsInProgress();
	    long memoryWillBeAvailableTime = timer.getTime();
        for(IoResponse response : allOperationsInProgress) {
            if(writeRequest.referencesSameMemory(response)) {
                if(memoryWillBeAvailableTime < response.getTimeCompleted()) {
                    memoryWillBeAvailableTime = response.getTimeCompleted();
                }
            }
        }
	    latency = (int) memoryWillBeAvailableTime;
	    
	    for(SsdBlock block : blocksToWrite) {
	        int blockLatency = writeToBlock(block);
	        
	        latency += blockLatency;
	    }
		
		return latency;
	}
	
    // FUTURE - If there's time, restore the more sophisticated support for variable length and multiple fragments
	private Set<SsdBlock> getBlocksForRequest(IoRequest request) {
        SsdIoRequest writeRequest = (SsdIoRequest) request;
        long blockAddress = writeRequest.getTargetBlock();
        SsdBlock target = blocks.get((int) blockAddress);
        Set<SsdBlock> retval = new HashSet<SsdBlock>();
        retval.add(target);
        
	    return retval;
	}
	
	private int writeToBlock(SsdBlock targetBlock) {
	    int totalLatency = 0;
	    // If the block is in use, it needs to be copied to an empty block
	    if(!targetBlock.readyToBeWritten()) {
	        // First, find an empty block
	        SsdBlock availableBlock = targetBlock;
	        for(SsdBlock block : blocks) {
	            if(block.readyToBeWritten()) {
	                availableBlock = block;
	                totalLatency += seekLatency;
	                break;
	            }
	        }
	        
	        // Then, copy the useful content to the empty block
	        for(int i = 0; i < pagesPerBlock; i++) { // SsdPage page : targetBlock.getPages()
	            SsdPage pageInTarget = targetBlock.getPages().get(i);
	            if(pageInTarget.getStatus() == SsdPageStatus.IN_USE) {
	                availableBlock.writePage(i, SsdPageStatus.IN_USE);
	                targetBlock.writePage(i, SsdPageStatus.STALE);
	            }
	        }
	        totalLatency += eraseLatency;
            totalLatency += writeLatency;
	    }
	    
	    // At this point the target block can be written
	    totalLatency += writeLatency;
	    
	    return totalLatency;
	}
    
    public int getReadLatency() {
        return readLatency;
    }
    
    public int getWriteLatency() {
        return writeLatency;
    }
    
    public int getEraseLatency() {
        return eraseLatency;
    }
    
    public int getSeekLatency() {
        return seekLatency;
    }
	
	//////////
	
	/**
	 * Represents one block of memory within the SSD.
	 */
	class SsdBlock {
		List<SsdPage> pages;
		
		SsdBlock() {
			pages = new ArrayList<SsdPage>();
			for(int i = 0; i < pagesPerBlock; i++) {
				pages.add(new SsdPage(SsdPageStatus.AVAILABLE));
			}
		}
		
		public List<SsdPage> getPages() {
		    return pages;
		}
		
		public boolean containsStalePage() {
			for(SsdPage page : pages) {
				if(page.status == SsdPageStatus.STALE) {
					return true;
				}
			}
			
			return false;
		}
		
		public boolean readyToBeWritten() {
			for(SsdPage page : pages) {
				if(page.status == SsdPageStatus.IN_USE) {
					return false;
				}
			}
			
			return true;
		}
		
		public int pagesAvailable() {
			int count = 0;
			
			for(SsdPage page : pages) {
				if(page.status == SsdPageStatus.AVAILABLE) {
					count++;
				}
			}
			
			return count;
		}
		
		public void writePage(int index, SsdPageStatus status) {
		    SsdPage pageAtIndex = pages.get(index);
		    pageAtIndex.setStatus(status);
		}
	}
	
	/**
	 * Represents one page of memory within one memory block within the SSD.
	 */
	class SsdPage {
		SsdPageStatus status;
		
		SsdPage(SsdPageStatus status) {
			this.status = status;
		}
		
		public SsdPageStatus getStatus() {
		    return status;
		}
		
		public void setStatus(SsdPageStatus status) {
		    this.status = status;
		}
	}
}
