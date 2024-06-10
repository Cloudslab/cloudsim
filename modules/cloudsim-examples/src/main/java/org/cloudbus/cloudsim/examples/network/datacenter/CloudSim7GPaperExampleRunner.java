package org.cloudbus.cloudsim.examples.network.datacenter;

import org.cloudbus.cloudsim.Log;

import java.util.HashMap;
import java.util.Map;

public class CloudSim7GPaperExampleRunner {
    private static final String testName = "test";

    // i) $T0$ and $T1$ are co-located, meaning that data is transmitted locally without using the physical network
    private static Map<String, Integer> getConfigI() {
        HashMap<String, Integer> map = new HashMap<>();

        map.put("cla", 0);
        map.put("clb", 0);

        return map;
    }

    // ii) $T0$ and $T1$ are located on the same rack, meaning that data is routed through a ToR switch only
    private static Map<String, Integer> getConfigII() {
        Map<String, Integer> map = new HashMap<>();

        map.put("cla", 0);
        map.put("clb", 1);

        return map;
    }

    // iii) $TO$ and $T1$ are located on different racks, so that data must be transmitted through the aggregate switch as well
    private static Map<String, Integer> getConfigIII() {
        Map<String, Integer> map = new HashMap<>();

        map.put("cla", 0);
        map.put("clb", 2);

        return map;
    }

    public static void main(String[] args) {
        String[] emptyArgs = new String[0];
        Log.setDisabled(true);

        //int minVal = 10;
        //int maxVal = 310;
        //int step = 100;

        int[] vals = {20, 40, 80, 160, 320, 640, 1280, 2560};
        Map<Map<String, Integer>, String> configs = new HashMap<>();
        configs.put(getConfigI(), "-I");
        configs.put(getConfigII(), "-II");
        configs.put(getConfigIII(), "-III");

        TandemAppExample5CloudSim7GPaper.numberOfPeriodicActivations = 50;

        // Runner loop
        for (var entry : configs.entrySet()) {
            // cloudlet->vm map
            TandemAppExample5CloudSim7GPaper.cloudletToHost = entry.getKey();
            TandemAppExample5CloudSim7GPaper.fileInfo = entry.getValue() + "-" + testName;

            for (int val : vals) {
                System.out.println("Running '"+testName+"' example with period "+val+" and config "+entry.getValue());

                TandemAppExample5CloudSim7GPaper.period = val;
                TandemAppExample5CloudSim7GPaper.main(emptyArgs);
            }
        }
    }
}
