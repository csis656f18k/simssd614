package edu.cofc.csis614.f18.ssdsim.machine.system.cache;

import edu.cofc.csis614.f18.ssdsim.machine.ioop.IoRequest;

public class CacheItem implements Comparable<CacheItem> {
    private IoRequest request;
    private long lastReferencedTime;
    
    public CacheItem(IoRequest request, long currentTime) {
        this.request = request;
        lastReferencedTime = currentTime;
    }
    
    public IoRequest getRequest() {
        return request;
    }
    
    public long getLastReferencedTime() {
        return lastReferencedTime;
    }
    
    public void setLastReferencedTime(long newTime) {
        lastReferencedTime = newTime;
    }
    
    /**
     * <p>From the interface spec:</p>
     * 
     * <blockquote>Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.</blockquote> 
     * 
     * <p>When the cache is full, we want to replace the item that hasn't been referenced in the longest time.<br />
     * This is considered the "smallest" item.<br />
     * In this context, "less than" means the older of the two items, or the one with the earlier reference time.<br />
     * This element is less than the other element if this element has a smaller reference time than the other element.</p>
     */
    @Override
    public int compareTo(CacheItem other) {
        return (int) (lastReferencedTime - other.getLastReferencedTime());
    }
    
    @Override
    public String toString() {
        return "Cache item for request " + request + " - last ref " + lastReferencedTime;
    }
}
