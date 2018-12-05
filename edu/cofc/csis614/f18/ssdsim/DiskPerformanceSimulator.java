package edu.cofc.csis614.f18.ssdsim;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import edu.cofc.csis614.f18.ssdsim.data.SingleTrialResult;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequest;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequestType;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoResponse;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.SsdIoRequest;
import edu.cofc.csis614.f18.ssdsim.machine.system.System;
import edu.cofc.csis614.f18.ssdsim.machine.system.disk.Disk;
import edu.cofc.csis614.f18.ssdsim.machine.system.disk.Ssd;

/**
 * The main class for the simulator.
 * 
 * Responsible for setting up the model system and disk(s) and actually running trials, as well as reporting the results.
 */
public class DiskPerformanceSimulator {
	private static long time;
	
	static System system;
	static Disk diskToTest;
	
	static Queue<IoRequest> requests;

    // static Map<Disk, DiskResults> allResults;
    static List<SingleTrialResult> results;

	public static void main(String[] args) {
		initializeSimulation();
		runSimulation();
		presentResults();
	}
	
	private static void initializeSimulation () {
		// TODO: eventually allow user input for run count, disk types, etc.; for MVP, hard-code
		
		//timer = new Timer();
		time = 0L;

        requests = new LinkedList<IoRequest>();
		populateRequests();
		
		//allResults = new HashMap<Disk, DiskResults>();
		results = new ArrayList<SingleTrialResult>();

		createSystem();
		// FIXME createFiles();
		// FIXME loadFilesToDisk();
		//createFileOperations();
	}
	
	private static void populateRequests() {
        requests.add(new SsdIoRequest(IoRequestType.READ, 2L, 0, time));
	}
	
	private static void createSystem() {
		Disk disk = new Ssd(); // TODO: eventually allow configuring disk in system; for MVP, just use an SSD

		system = new System(disk);
	}
	
	private static void runSimulation() {
//		system.setInitialDiskState();
//		system.enableMemoization();
//		runOneTrial();

		system.setInitialDiskState();
		system.disableMemoization();
		results.add(runOneTrial());
	}

	/**
	 * Simulate a single disk on a single system, with some number of file operations, one time.
	 */
	private static SingleTrialResult runOneTrial() {
		while(isSomeOperationsStillOutstanding()) {
			system.updateTime(time);
			
			while(requests.peek() != null) { // In case multiple requests this time tick, use while
	            if(requests.peek().getTime() == time) {
	                system.handleIoRequest(requests.remove());
	            }
			}
			
			time++;
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

    private static void addToDiskResults(SingleTrialResult latestResult) {
		// FIXME
	}
	
	private static void presentResults() {
	    int numTrials = results.size();
		for(int i = 0; i < numTrials; i++) {
            java.lang.System.out.println("Results for trial " + (i + 1) + ":");
            java.lang.System.out.println(results.get(i).getTimeTakenOverall() + " ns");
            java.lang.System.out.println();
		}

        java.lang.System.out.println("Complete!");
	}
}
