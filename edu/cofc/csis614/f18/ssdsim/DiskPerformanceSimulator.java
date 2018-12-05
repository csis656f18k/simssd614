package edu.cofc.csis614.f18.ssdsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import edu.cofc.csis614.f18.ssdsim.data.DiskResults;
import edu.cofc.csis614.f18.ssdsim.data.SingleTrialResult;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequest;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequestType;
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
	
	static Map<Disk, DiskResults> allResults;

	public static void main(String[] args) {
		initializeSimulation();
		runSimulation();
		presentResults();
	}
	
	private static void initializeSimulation () {
		// TODO: eventually allow user input for run count, disk types, etc.; for MVP, hard-code
		
		//timer = new Timer();
		time = 0L;
		
		allResults = new HashMap<Disk, DiskResults>();

		createSystem();
		// FIXME createFiles();
		// FIXME loadFilesToDisk();
		//createFileOperations();
	}
	
	private static void createSystem() {
		Disk disk = new Ssd(); // TODO: eventually allow configuring disk in system; for MVP, just use an SSD

		system = new System(disk);
	}
	
	private static void runSimulation() {
		system.setInitialDiskState();
		system.enableMemoization();
		runOneTrial();

		system.setInitialDiskState();
		system.disableMemoization();
		runOneTrial();
	}

	/**
	 * Simulate a single disk on a single system, with some number of file operations, one time.
	 */
	private static SingleTrialResult runOneTrial() {
		SingleTrialResult result = new SingleTrialResult();
		
		while(isSomeOperationsStillOutstanding()) {
			// TODO: Receive data coming back from earlier ops, and add to result? Or do this via pub-sub?
			system.updateTime(time);
			
			// TODO: support sending more than one op
			IoRequest request = new SsdIoRequest(IoRequestType.READ, 2L, 0, time);
			system.handleIoRequest(request);
			// TODO finish
			
			time++;
		}
		
		return result;
	}
	
	private static boolean isSomeOperationsStillOutstanding() {
        // TODO handle queue of upcoming IoRequests, once that's re-implemented
	    
        return system.isOperationsInProgress();
    }

    private static void addToDiskResults(SingleTrialResult latestResult) {
		// FIXME
	}
	
	private static void presentResults() {
		// FIXME
	}
}
