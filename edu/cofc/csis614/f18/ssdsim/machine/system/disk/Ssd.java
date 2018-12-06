package edu.cofc.csis614.f18.ssdsim.machine.system.disk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	public static final int DEBUG_PAGE_CAPACITY = 50; // arbitrary size units

	public static final int DEFAULT_BLOCKS_PER_SSD = 250;
	public static final int DEFAULT_PAGES_PER_BLOCK = 256;
	public static final int DEFAULT_PAGE_CAPACITY = 8000; // bytes

	public static final int DEBUG_READ_LATENCY = 2;
	public static final int DEBUG_WRITE_LATENCY = 20;

	public static final int DEFAULT_READ_LATENCY = DiskConstants.SSD_TLC.getReadLatency();
	public static final int DEFAULT_WRITE_LATENCY = DiskConstants.SSD_TLC.getWriteLatency();
	
	private int blocksPerDisk;
	private int blockCapacity;
	private int pagesPerBlock;
	private int pageCapacity;
	
	List<SsdBlock> blocks;
	
	public Ssd(Timer timer) {
		pageCapacity = DEBUG_PAGE_CAPACITY;
		pagesPerBlock = DEBUG_PAGES_PER_BLOCK;
		blocksPerDisk = DEBUG_BLOCKS_PER_SSD;
		
		blockCapacity = pageCapacity * pagesPerBlock;
		diskCapacity = blockCapacity * blocksPerDisk;
		
		readLatency = DEBUG_READ_LATENCY;
		writeLatency = DEBUG_WRITE_LATENCY;
		
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
	 * 
	 * Once this stuff is done, the disk can accept new incoming requests for this time tick.
	 */
	@Override
	public void updateTime() {
		cleanUpOldTasks();
		
		if(blocked && unblockTime != timer.getTime()) {
		    return;
		}
		
		doGarbageCollection();
	}

	@Override
	public void cleanUpOldTasks() {
		Set<IoResponse> completedOperations = operationsInProgress.remove(timer.getTime());

		if(completedOperations == null) {
		    return;
		}
		
		operationsInProgressCount -= completedOperations.size();
        for(IoResponse response : completedOperations) {
            system.receiveCompletedIoOperationInfo(response);
        }
	}
	
	// TODO: figure out some way to update the files in the system to know they've been moved to new memory locations
	private void doGarbageCollection() {
		SsdBlock emptyBlock = null;
		for(SsdBlock block : blocks) {
			if(block.readyToBeOverwritten()) {
				emptyBlock = block;
				break;
			}
		}
		if(emptyBlock == null) {
		    return;
		}
		
		blocked = true;
		unblockTime = timer.getTime() + DiskConstants.SSD_TLC.getEraseLatency();
		
		SsdBlock staleBlock = null;
		for(SsdBlock block : blocks) {
			if(!block.containsStalePage()) {
				continue;
			}
			
			staleBlock = block;
			for(int i = 0; i < pagesPerBlock; i++) {
				SsdPage page = staleBlock.pages.get(i);
				if(page.status == SsdPageStatus.IN_USE) {
					emptyBlock.pages.get(i).status = SsdPageStatus.IN_USE;
				} else {
					emptyBlock.pages.get(i).status = SsdPageStatus.AVAILABLE;
				}
				
				page.status = SsdPageStatus.AVAILABLE;
			}
			
			emptyBlock = staleBlock;
		}
	}

	@Override
	public void processIoRequest(IoRequest requestIn) {
		SsdIoRequest request = (SsdIoRequest) requestIn;
		
		int latency;
		switch(request.getType()) {
    		case READ:
    			// No updating needed, reads don't change state
    			// All reads take the same amount of time
    			latency = readLatency;
    			break;
    		case WRITE:
    			// TODO - add logic for if the write needs to wait or copy blocks
    			latency = handleWriteRequest(requestIn);//FIXME
    			break;
    		default:
    			// TODO should never happen, implement exception
    			latency = -111;
    			break;
		}
		
		long completionTime = timer.getTime() + latency; // Don't need to worry about simulating actual I/O; just track how long it takes

		IoResponse response = new IoResponse(request.getId(), request.getType(), completionTime);

		Set<IoResponse> existingResponsesForCompletionTime = operationsInProgress.get(completionTime);
		if(existingResponsesForCompletionTime == null) {
			Set<IoResponse> newSet = new HashSet<IoResponse>();
			newSet.add(response);
			operationsInProgress.put(completionTime, newSet);
		} else {
			existingResponsesForCompletionTime.add(response);
		}
        operationsInProgressCount++;
	}
	
	/*
	 * PSEUDO
	 * 
	 * Preconditions:
	 * IoRequest is a request of type WRITE
	 * IoRequest MAY be just part of a bigger file operation 
	 * 
	 * Behavior:
	 * Set file's existing location(s) to STALE
	 * Write new version of file where there is free space (i.e. change given number of pages from AVAILABLE to IN_USE)
	 * 
	 * Postconditions:
	 * WAIT FOR LATENCY, then return the new location(s) of the file to the controller 
	 * 
	 * TODO: handle case where write is blocked
	 * 
	 * @return the total latency of the write operation
	 */
	private int handleWriteRequest(IoRequest writeRequest) {
		// TODO
		
		return 0;//FIXME
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
		
		public boolean containsStalePage() {
			for(SsdPage page : pages) {
				if(page.status == SsdPageStatus.STALE) {
					return true;
				}
			}
			
			return false;
		}
		
		public boolean readyToBeOverwritten() {
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
	}
	
	/**
	 * Represents one page of memory within one memory block within the SSD.
	 */
	class SsdPage {
		SsdPageStatus status;
		
		SsdPage(SsdPageStatus status) {
			this.status = status;
		}
	}
}
