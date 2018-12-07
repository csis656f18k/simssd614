package edu.cofc.csis614.f18.ssdsim.machine.system.disk;

/**
 * Constants related to disks.
 * 
 * All times are in nanoseconds.
 * 
 * Sources:
 * https://www.extremetech.com/extreme/210492-extremetech-explains-how-do-ssds-work
 * http://codecapsule.com/2014/02/12/coding-for-ssds-part-2-architecture-of-an-ssd-and-benchmarking/
 */
public enum DiskConstants {
	CACHE_L1(1, 1, 0, 0),
	CACHE_L2(4, 4, 0, 0),
	RAM(25, 25, 0, 0),
	SSD_SLC(25000, 250000, 1500000, 0),
	SSD_MLC(50000, 900000, 3000000, 0),
	SSD_TLC(100000, 1500000, 5000000, 0),
	HDD_10000(4500000, 4500000, -1, 12000000),
	HDD_7200(4500000, 4500000, -1, 16666667);

	private final int readLatency;
	private final int writeLatency;
	private final int eraseLatency;
	private final int seekLatency;
	
    DiskConstants(int readLatency, int writeLatency, int eraseLatency, int seekLatency) {
        this.readLatency = readLatency;
        this.writeLatency = writeLatency;
        this.eraseLatency = eraseLatency;
        this.seekLatency = seekLatency;
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
}
