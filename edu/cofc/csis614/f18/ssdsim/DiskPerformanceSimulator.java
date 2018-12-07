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
import edu.cofc.csis614.f18.ssdsim.timer.Timer;

/**
 * The main class for the simulator.
 * 
 * Responsible for setting up the model system and disk(s) and actually running trials, as well as reporting the results.
 */
public class DiskPerformanceSimulator {
    public static final boolean DEBUG_MODE = true;
    
    private static Timer timer;
	
	static System system;
	static Disk diskToTest;
	
	static Queue<IoRequest> requests;

    static List<SingleTrialResult> results;

	public static void main(String[] args) {
        results = new ArrayList<SingleTrialResult>();

        runSimulation();
		presentResults();
	}

    // FUTURE: eventually allow user input for run count, disk types, etc.; for MVP, hard-code
	private static void initializeSimulation () {
		timer = new Timer();

        createSystem();

        requests = new LinkedList<IoRequest>();
		populateRequests();
	}
	
	private static void populateRequests() {
        requests.add(new SsdIoRequest(IoRequestType.READ, 2L, 0, 0L));
        requests.add(new SsdIoRequest(IoRequestType.WRITE, 0L, 0, 1L));
        requests.add(new SsdIoRequest(IoRequestType.WRITE, 0L, 4, 2L));
        requests.add(new SsdIoRequest(IoRequestType.WRITE, 2L, 5, 3L));
        requests.add(new SsdIoRequest(IoRequestType.READ, 2L, 5, 4L));
        requests.add(new SsdIoRequest(IoRequestType.WRITE, 0L, 4, 5L));
        requests.add(new SsdIoRequest(IoRequestType.READ, 0L, 4, 6L));
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
        java.lang.System.out.println("");
        java.lang.System.out.println("----------");
        java.lang.System.out.println("Results:");
        java.lang.System.out.println("");
	    int numTrials = results.size();
		for(int i = 0; i < numTrials; i++) {
            java.lang.System.out.println("Trial " + (i + 1) + ":");
            java.lang.System.out.println(results.get(i).getTimeTakenOverall() + " ns");
            java.lang.System.out.println();
		}

        java.lang.System.out.println("Complete!");
	}
}
