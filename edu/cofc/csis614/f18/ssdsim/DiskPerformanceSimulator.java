package edu.cofc.csis614.f18.ssdsim;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import edu.cofc.csis614.f18.ssdsim.data.SingleTrialResult;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequest;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequestType;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoResponse;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.SsdIoRequest;
import edu.cofc.csis614.f18.ssdsim.machine.system.System;
import edu.cofc.csis614.f18.ssdsim.machine.system.disk.Disk;
import edu.cofc.csis614.f18.ssdsim.machine.system.disk.Ssd;
import edu.cofc.csis614.f18.ssdsim.timer.Timer;

/**
 * The main class for the simulator.
 * 
 * Responsible for setting up the model system and disk(s) and actually running trials, as well as reporting the results.
 */
public class DiskPerformanceSimulator {
    public static final boolean DEBUG_MODE = false;
    
    private static Timer timer;
	
	static System system;
	static Disk diskToTest;
	
	static Queue<IoRequest> requests;

    static List<SingleTrialResult> results;

	public static void main(String[] args) {
	    long startTime = java.lang.System.currentTimeMillis();

        requests = new LinkedList<IoRequest>();
        populateRequests();
	    
        results = new ArrayList<SingleTrialResult>();

        runSimulation();
		presentResults();
		
		long endTime = java.lang.System.currentTimeMillis();
		
        java.lang.System.out.println("Simulation elapsed time: " + ((endTime - startTime) / 1000) + "sec");
	}

    // FUTURE: eventually allow user input for run count, disk types, etc.; for MVP, hard-code
	private static void initializeSimulation () {
		timer = new Timer();

        createSystem();
	}
	
	private static void populateRequests() {
        Random rng = new Random();
        long highestTimeSoFar = 0;

        for(int i = 0; i < 1000; i++) {
            IoRequestType type = generateTypeForRequest(rng);
            long block = generateBlockForRequest(rng);
            int page = generatePageForRequest(rng);
            long time = generateTimeForRequest(rng, highestTimeSoFar);
            highestTimeSoFar = time;
            
            requests.add(new SsdIoRequest(type, block, page, time));
            
            StringBuilder sb = new StringBuilder();
            sb.append("Type: ");
            sb.append(type);
            sb.append("; Block: ");
            sb.append(block);
            sb.append("; Page: ");
            sb.append(page);
            sb.append("; Start Time: ");
            sb.append(time);
            java.lang.System.out.println(sb.toString());
        }
	}
    
    private static IoRequestType generateTypeForRequest(Random rng) {
        return rng.nextBoolean() ? IoRequestType.READ : IoRequestType.WRITE;
    }
	
	private static long generateBlockForRequest(Random rng) {
        int blocksPerDisk = Ssd.DEFAULT_BLOCKS_PER_SSD;
        
        float random = rng.nextFloat(); // 0.0 to 1.0
        
        return (int) (random * (blocksPerDisk - 1));
	}
    
    private static int generatePageForRequest(Random rng) {
        int pagesPerBlock = Ssd.DEFAULT_PAGES_PER_BLOCK;
        
        float random = rng.nextFloat();
        
        return (int) (random * (pagesPerBlock - 1));
    }
    
    private static long generateTimeForRequest(Random rng, long previousOperationTime) {
        int maxLengthBeforeNextOperation = 10000;
        
        float random = rng.nextFloat();
        int timeUntilNextOperation = (int) (random * maxLengthBeforeNextOperation);
        
        return previousOperationTime + timeUntilNextOperation;
    }
	
	private static void createSystem() {
		Disk disk = new Ssd(timer); // FUTURE: eventually allow configuring disk in system; for MVP, just use an SSD

		system = new System(timer, disk);
	}
	
	private static void runSimulation() {
        initializeSimulation();
        system.setInitialDiskState();
        system.disableMemoization();
        results.add(runOneTrial());
        
        initializeSimulation();
        system.setInitialDiskState();
        system.enableMemoization();
        results.add(runOneTrial());
	}

	/**
	 * Simulate a single disk on a single system, with some number of file operations, one time.
	 */
	private static SingleTrialResult runOneTrial() {
		while(isSomeOperationsStillOutstanding()) {
            Utils.debugPrint("");
            Utils.debugPrint("Now starting time tick " + timer.getTime());

            if(timer.getTime() % 250000 == 0) {
                java.lang.System.out.println(timer.getTime() + " / ~6,000,000");
            }
            
			system.updateTime();
			
			// Support multiple requests per time tick
			while(requests.peek() != null && requests.peek().getStartTime() == timer.getTime()) {
                system.handleIoRequest(requests.remove());
            }
			
			timer.stepForward();
		}
		
		Set<IoResponse> rawData = system.getIoResponses();

		return new SingleTrialResult(rawData);
	}

	private static boolean isSomeOperationsStillOutstanding() {
	    if(!requests.isEmpty()) {
	        return true;
	    }
	    
        return system.isOperationsInProgress();
    }
	
	private static void presentResults() {
        Utils.debugPrint("");
        java.lang.System.out.println("----------");
        java.lang.System.out.println("Results:");
        java.lang.System.out.println("");
        java.lang.System.out.println("Trial 1: no memoization");
        java.lang.System.out.println("Trial 2: using memoization");
        java.lang.System.out.println("");
        java.lang.System.out.println("Read latency for cache: " + system.getCacheReadLatency());
        java.lang.System.out.println("Write latency for cache: " + system.getCacheWriteLatency());
        java.lang.System.out.println("Read latency for this disk: " + system.getDiskReadLatency());
        java.lang.System.out.println("Write latency for this disk: " + system.getDiskWriteLatency());
        java.lang.System.out.println("Erase latency for this disk: " + system.getDiskEraseLatency());
        java.lang.System.out.println("Seek latency for this disk: " + system.getDiskSeekLatency());
        java.lang.System.out.println("");
	    int numTrials = results.size();
		for(int i = 0; i < numTrials; i++) {
		    results.get(i).finalize();
            java.lang.System.out.println("Trial " + (i + 1) + ":");
            java.lang.System.out.println("Number of reads recorded: " + results.get(i).getReadCount());
            java.lang.System.out.println("Number of writes recorded: " + results.get(i).getWriteCount());
            java.lang.System.out.println("Total time to complete: " + results.get(i).getTimeTakenOverall() + " ns");
            java.lang.System.out.println("Average time to complete a read request: " + results.get(i).getReadTime() + " ns");
            java.lang.System.out.println("Average time to complete a write request: " + results.get(i).getWriteTime() + " ns");
            java.lang.System.out.println();
		}
	}
}
