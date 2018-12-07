package edu.cofc.csis614.f18.ssdsim.machine.system.cache;

import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequest;
import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoResponse;

/**
 * A wrapper object to allow the cache to return both requests it couldn't complete and responses for requests it could complete.
 */
public class CacheResponse {
    private IoRequest ioRequest;
    private IoResponse ioResponse;
    
    public CacheResponse(IoRequest ioRequest) {
        this.ioRequest = ioRequest;
        this.ioResponse = null;
    }
    
    public CacheResponse(IoResponse ioResponse) {
        this.ioRequest = null;
        this.ioResponse = ioResponse;
    }
    
    public CacheResponse(IoRequest ioRequest, IoResponse ioResponse) {
        this.ioRequest = ioRequest;
        this.ioResponse = ioResponse;
    }
    
    public IoRequest getRequest() {
        return ioRequest;
    }
    
    public IoResponse getResponse() {
        return ioResponse;
    }
    
    @Override
    public String toString() {
        return "CACHE: request is [" + ioRequest + "] and response to submit is [" + ioResponse + "]";
    }
}
