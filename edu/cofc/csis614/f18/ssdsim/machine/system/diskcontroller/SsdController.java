package edu.cofc.csis614.f18.ssdsim.machine.system.diskcontroller;

import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequest;
import edu.cofc.csis614.f18.ssdsim.machine.system.disk.Ssd;

public class SsdController extends DiskController {
	// private final int blocksPerDisk;
	private final int pagesPerBlock;
	private final int pageCapacity;

	public SsdController(Ssd disk) {
		this.disk = disk;

		// blocksPerDisk = disk.getBlocksPerDisk();
		pagesPerBlock = disk.getPagesPerBlock();
		pageCapacity = disk.getPageCapacity();
	}

	/*
	@Override
	public Set<? extends IoRequest> createIoRequestsForFileOperations(FileOperation fileOperation, long currentTime) {
		Set<SsdIoRequest> requestsForFile = new HashSet<SsdIoRequest>();
		
		SsdIoRequest currentRequest;
		
		IoRequestType ioRequestType = convertFileOperationTypeToIoRequestType(fileOperation.getType());

		File file = fileOperation.getFile();
		List<Long> startAddresses = file.getStartAddresses();
		List<Long> lengths = file.getLengths();

		int numFragments = startAddresses.size();
		for (int i = 0; i < numFragments; i++) {
			long startAddress = startAddresses.get(i);
			long length = lengths.get(i);

			long overallPageNumber = startAddress / pageCapacity;
			long targetBlock = overallPageNumber / pagesPerBlock;
			int targetPage = (int) (overallPageNumber % pagesPerBlock);

			currentRequest = new SsdIoRequest(ioRequestType, targetBlock, targetPage, currentTime);
			requestsForFile.add(currentRequest);

			int additionalPageCount = (int) ((length - 1) / pageCapacity);
			long currentBlock = targetBlock;
			int currentPage = targetPage;
			for (int j = 0; j < additionalPageCount; j++) {
				if (currentPage == pagesPerBlock - 1) {
					currentBlock++;
					currentPage = 0;
				} else {
					currentPage++;
				}

				currentRequest = new SsdIoRequest(ioRequestType, currentBlock, currentPage, currentTime);
				requestsForFile.add(currentRequest);
			}
		}
		
		return requestsForFile;
	}
	*/
	
	public void setInitialDiskState() {
		// TODO
	}
	
	@Override
	public void sendIoRequestToDisk(IoRequest ioRequest) {
		disk.processIoRequest(ioRequest);
	}
    
    public boolean isOperationsInProgress() {
        return disk.hasOperationsInProgress();
    }
}
