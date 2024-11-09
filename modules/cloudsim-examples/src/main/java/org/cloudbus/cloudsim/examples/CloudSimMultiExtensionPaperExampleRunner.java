package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudSimMultiExtensionPaperExampleRunner {

    // i) $T0$ and $T1$ are co-located, meaning that data is transmitted locally without using the physical network
    private static Map<String, Integer> getClConfigV_I() {
        return Map.of("cla", 4,
                      "clb", 4);
    }
    // ii) $T0$ and $T1$ are located on the same rack, meaning that data is routed through a ToR switch only
    private static Map<String, Integer> getClConfigV_II() {
        return Map.of("cla", 4,
                      "clb", 5);
    }
    // iii) $TO$ and $T1$ are located on different racks, so that data must be transmitted through the aggregate switch as well
    private static Map<String, Integer> getClConfigV_III() {
        return Map.of("cla", 4,
                      "clb", 6);
    }

    // As before, but on containers
    private static Map<String, Integer> getClConfigC_I() {
        return Map.of("cla", 8,
                "clb", 8);
    }
    private static Map<String, Integer> getClConfigC_II() {
        return Map.of("cla", 8,
                "clb", 9);
    }
    private static Map<String, Integer> getClConfigC_III() {
        return Map.of("cla", 8,
                "clb", 10);
    }

    public static void main(String[] args) {
        String[] emptyArgs = new String[0];
        Log.setDisabled(true);

        CloudSimMultiExtensionPaperExample.vmVirtOverhead = 5;
        CloudSimMultiExtensionPaperExample.contVirtOverhead = 3;

        // name->(cloudlet->vm) map
        Map<String, Map<String, Integer>> configs = new HashMap<>();
        configs.put("V-I", getClConfigV_I());
        configs.put("V-II", getClConfigV_II());
        configs.put("V-III", getClConfigV_III());
        configs.put("C-I", getClConfigC_I());
        configs.put("C-II", getClConfigC_II());
        configs.put("C-III", getClConfigC_III());
        configs.put("N-I", getClConfigC_I());
        configs.put("N-II", getClConfigC_II());
        configs.put("N-III", getClConfigC_III());

        // name -> payload-size map
        Map<String, Integer> payloads = new HashMap<>();
        payloads.put("small-payload", 1000);
        payloads.put("big-payload", 1000000000);

        // number of activations
        List<Integer> activations = new ArrayList<>();
        activations.add(1);
        activations.add(20);

        // Runner loop
        String voString = (CloudSimMultiExtensionPaperExample.vmVirtOverhead + CloudSimMultiExtensionPaperExample.contVirtOverhead > 0) ? "VO" : "none";
        for (int activation : activations) {
            CloudSimMultiExtensionPaperExample.numberOfPeriodicActivations = activation;
            String actString = (activation > 1) ? "many" : "single";

            for (var config : configs.entrySet()) {
                CloudSimMultiExtensionPaperExample.cloudletToGuest = config.getValue();

                CloudSimMultiExtensionPaperExample.numberOfContainers = (config.getKey().indexOf('V') >= 0) ? 0 : 4;
                CloudSimMultiExtensionPaperExample.numberOfVms = (config.getKey().indexOf('C') >= 0) ? 0 : 4;

                // special nested virtualization scenario
                CloudSimMultiExtensionPaperExample.nestedContainers = config.getKey().indexOf('N') >= 0;

                for (var p : payloads.entrySet()) {
                    System.out.println("Running '" + actString + "' example with payload " + p.getValue() + " and config " + config.getKey());

                    CloudSimMultiExtensionPaperExample.payloadSize = p.getValue();
                    CloudSimMultiExtensionPaperExample.fileInfo = "-" + p.getKey() + "-" + config.getKey() + "-" + actString + "-" + voString;

                    CloudSimMultiExtensionPaperExample.main(emptyArgs);
                    System.out.println();
                }
            }
        }
    }
}
