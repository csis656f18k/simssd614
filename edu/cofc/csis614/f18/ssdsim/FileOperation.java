package edu.cofc.csis614.f18.ssdsim;

import edu.cofc.csis614.f18.ssdsim.machine.system.File;

/**
 * FUTURE restore file functionality
 * 
 * <p>A single operation on a specified file. Used in constructing test cases for the simulator.</p>
 */
public class FileOperation {
	private static int maxId;
	
	private int id;
	private FileOperationType type;
	private File file;
	private long requestTime;
	
	static {
		maxId = 0;
	}
	
	public FileOperation(FileOperationType fileOperationType, File file, long requestTime) {
		maxId++;
		id = maxId;
		type = fileOperationType;
		this.file = file;
		this.requestTime = requestTime;
	}
	
	public int getId() {
		return id;
	}

	public FileOperationType getType() {
		return type;
	}
	
	public File getFile() {
		return file;
	}
	
	public long getRequestTime() {
		return requestTime;
	}
}
