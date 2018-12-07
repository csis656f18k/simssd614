package edu.cofc.csis614.f18.ssdsim.machine.system.cache;

import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequest;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoResponse;

/**
 * A wrapper object to allow the cache to return both requests it couldn't complete and responses for requests it could complete.
 */
public class CacheResponse {
    private IoRequest ioRequest;
    private IoResponse ioResponse;
    
    long completionTime;
    
    public CacheResponse(IoRequest ioRequest, long completionTime) {
        this.ioRequest = ioRequest;
        this.ioResponse = null;
        this.completionTime = completionTime;
    }
    
    public CacheResponse(IoResponse ioResponse, long completionTime) {
        this.ioRequest = null;
        this.ioResponse = ioResponse;
        this.completionTime = completionTime;
    }
    
    public CacheResponse(IoRequest ioRequest, IoResponse ioResponse, long completionTime) {
        this.ioRequest = ioRequest;
        this.ioResponse = ioResponse;
        this.completionTime = completionTime;
    }
    
    public IoRequest getRequest() {
        return ioRequest;
    }
    
    public IoResponse getResponse() {
        return ioResponse;
    }
    
    public long getCompletionTime() {
        return completionTime;
    }
    
    @Override
    public String toString() {
        return "CACHE: request is [" + ioRequest + "] and response to submit is [" + ioResponse + "]";
    }
}
