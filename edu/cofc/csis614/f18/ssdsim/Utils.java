package edu.cofc.csis614.f18.ssdsim;

public class Utils {
    public static void debugPrint(String string) {
        if(DiskPerformanceSimulator.DEBUG_MODE) {
            System.out.println(string);
        }
    }
}
