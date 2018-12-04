package edu.cofc.csis614.f18.ssdsim.machine.system;

import java.util.ArrayList;
import java.util.List;

/**
 * name is a positive integer representing the filename.
 * 
 * TODO: enforce positive integer in code
 */
public class File implements Comparable<File> {
	private int name; // No need to use actual filenames for simulation purposes
	private List<Long> startAddresses;
	private List<Long> lengths;

	/**
	 * Construct a file saved in one contiguous chunk of memory
	 * 
	 * @param name
	 * @param startAddress
	 * @param length
	 */
	public File(int name, long startAddress, long length) {
		this.name = name;

		startAddresses = new ArrayList<Long>();
		startAddresses.add(startAddress);

		lengths = new ArrayList<Long>();
		lengths.add(length);
	}

	/**
	 * Construct a file that may be fragmented across multiple discontinuous memory areas
	 * 
	 * @param name
	 * @param startAddresses
	 * @param lengths
	 */
	public File(int name, List<Long> startAddresses, List<Long> lengths) {
		this.name = name;

		this.startAddresses = startAddresses;

		this.lengths = lengths;
	}

	public int getName() {
		return name;
	}
	
	public List<Long> getStartAddresses() {
		return startAddresses;
	}
	
	public List<Long> getLengths() {
		return lengths;
	}

	@Override
	public int compareTo(File otherFile) {
		return name - otherFile.getName();
	}
}
