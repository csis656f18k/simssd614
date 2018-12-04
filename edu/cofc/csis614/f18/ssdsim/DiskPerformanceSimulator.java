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
import edu.cofc.csis614.f18.ssdsim.machine.system.File;
import edu.cofc.csis614.f18.ssdsim.machine.system.System;
import edu.cofc.csis614.f18.ssdsim.machine.system.disk.Disk;
import edu.cofc.csis614.f18.ssdsim.machine.system.disk.Ssd;

/**
 * The main class for the simulator.
 * 
 * Responsible for setting up the model system and disk(s) and actually running trials, as well as reporting the results.
 */
public class DiskPerformanceSimulator {
	public static final int DEFAULT_RUNS = 1; // Number of times to run each simulation - for when systems are generated probabilistically
	
	//static Timer timer;
	private static long time;
	
	static System system;
	
	static Set<Disk> disksToTest; // Set up as collection, but for MVP, will use just one disk at a time
	
	static List<File> files;
	
	static Queue<FileOperation> fileOperations;
	
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
		createFiles();
		loadFilesToDisk();
		createFileOperations();
	}
	
	private static void createSystem() {
		Disk disk = new Ssd(); // TODO: eventually allow configuring disk in system; for MVP, just use an SSD

		system = new System(disk);
	}
	
	private static void createFiles() {
		files = new ArrayList<File>();
		
		// FUTURE hard-coded for MVP
		files.add(new File(files.size() + 1, 600L, 12L));
		// TODO: more files
	}
	
	private static void loadFilesToDisk() {
		system.loadFilesToDisk(files);
	}
	
	private static void createFileOperations() {
		fileOperations = new LinkedList<FileOperation>();

		// FUTURE hard-coded for MVP
		fileOperations.add(new FileOperation(FileOperationType.READ, files.get(1), 2L));
		// TODO: more file ops
	}
	
	private static void runSimulation() {
		runOneTrial(); // FIXME save results
		
		// TODO - placeholder for multi-disk setup
		
		/*
		 * 
		 *
		for(Disk disk : disksToTest) {
			DiskResults diskResults = new DiskResults();
			
			system = new System(disk);
			
			for(int run = 0; run < DEFAULT_RUNS; run++) {
				SingleTrialResult result = runOneTrial();
		
				addToDiskResults(result);
			}
			
			allResults.put(disk, diskResults);
		}
		 */
	}
	
	/**
	 * Simulate a single disk on a single system, with some number of file operations, one time.
	 */
	private static SingleTrialResult runOneTrial() {
		/*
		 * Preconditions:
		 * System is fully initialized, including a disk and a disk controller
		 * Files and file operations have been passed to system
		 * 
		 * Behavior:
		 * System parses the received file operations and dispatches them to the disk, in order, via the disk controller
		 * 
		 * Postconditions:
		 * All IO operations performed
		 */

		// NB all the IO requests should be generated here in the simulation class as they are logically part of the simulation, not the system
		// Remember the requests need to have predetermined times
		// This also means all the files need to be generated here too because the IO requests depend on knowing what files are there
		// 
		// TO DO HERE:
		// tell the system to run a single trial, i.e. send all of the IO requests (predetermined content; may change this later) to the disk
		// receive the results of the trial
		// save/send the results of the trial
		
		SingleTrialResult result = new SingleTrialResult();
		
		while(isSomeFileOperationsStillOutstanding()) {
			// TODO: Receive data coming back from earlier ops, and add to result? Or do this via pub-sub?
			system.updateTime(time);
			
			Queue<FileOperation> fileOperationsThisTimeTick = getFileOperationsThisTimeTick();
			
			for(FileOperation fileOperation : fileOperationsThisTimeTick) {
				system.handleFileOperationRequest(fileOperation);
			}
			
			time++;
		}
		
		return result;
	}
	
	private static boolean isSomeFileOperationsStillOutstanding() {
		// If there are operations that aren't even started yet, this is definitely true
		if(!fileOperations.isEmpty()) {
			return true;
		}
		
		// Otherwise, all the operations have been dispatched but maybe not all of them have returned; check on that
		return system.isOperationsInProgress();
	}
	
	private static Queue<FileOperation> getFileOperationsThisTimeTick() {
		Queue<FileOperation> fileOperationsThisTimeTick = new LinkedList<FileOperation>();
		
		while(fileOperations.peek() != null && fileOperations.peek().getRequestTime() == time) {
			fileOperationsThisTimeTick.add(fileOperations.remove());
		}
		
		return fileOperationsThisTimeTick;
	}
	
	private static void addToDiskResults(SingleTrialResult latestResult) {
		// FIXME
	}
	
	private static void presentResults() {
		// FIXME
	}
}
