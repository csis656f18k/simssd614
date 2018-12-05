package edu.cofc.csis614.f18.ssdsim.machine.system.disk;

import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequest;
import edu.cofc.csis614.f18.ssdsim.timer.Timer;

// TODO: implement HDD operations for comparison after SSD done
public class Hdd extends Disk {
	@Override
	public DiskType getType() {
		return DiskType.HDD;
	}

	@Override
	public void updateTime(Timer timer) {
		this.timer = timer;
		// TODO: do anything that happens here at time timeIn
	}

	@Override
	public void processIoRequest(IoRequest request) {
		// TODO Auto-generated method stub
	}

	@Override
	public void cleanUpOldTasks() {
		// TODO Auto-generated method stub
	}
}
