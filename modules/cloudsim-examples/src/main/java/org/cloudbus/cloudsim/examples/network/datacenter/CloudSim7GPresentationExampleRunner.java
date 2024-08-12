package org.cloudbus.cloudsim.examples.network.datacenter;

import org.cloudbus.cloudsim.Log;

import java.util.HashMap;
import java.util.Map;

public class CloudSim7GPresentationExampleRunner {
    private static String testName = "";

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

        TandemAppExample5CloudSim7GPresentation.numberOfPeriodicActivations = 1;

        // name->(cloudlet->vm) map
        Map<String, Map<String, Integer>> configs = new HashMap<>();
        configs.put("I", getConfigI());
        configs.put("II", getConfigII());
        configs.put("III", getConfigIII());

        // name -> payload-size map
        Map<String, Integer> vals = new HashMap<>();
        vals.put("small-payload", 1000);
        vals.put("big-payload", 1000000000);

        if (TandemAppExample5CloudSim7GPresentation.numberOfPeriodicActivations > 1) {
            testName = "many";
        } else {
            testName = "single";
        }

        // Runner loop
        for (var config : configs.entrySet()) {
            TandemAppExample5CloudSim7GPresentation.cloudletToHost = config.getValue();

            for (var val : vals.entrySet()) {
                System.out.println("Running '"+testName+"' example with payload "+val.getValue()+" and config "+config.getKey());

                TandemAppExample5CloudSim7GPresentation.payloadSize = val.getValue();
                TandemAppExample5CloudSim7GPresentation.fileInfo = "-"+ val.getKey() +"-"+ config.getKey() +"-"+ testName;
                TandemAppExample5CloudSim7GPresentation.main(emptyArgs);
            }
        }
    }
}
