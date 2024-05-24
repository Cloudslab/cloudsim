package org.cloudbus.cloudsim.EX.delay;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

import static org.apache.commons.lang3.tuple.ImmutablePair.of;
import static org.cloudbus.cloudsim.Consts.NIX_OS;
import static org.cloudbus.cloudsim.Consts.WINDOWS;

/**
 * 
 * @author nikolay.grozev
 * 
 */
public class ExampleGaussianDelaysPerType {

    private ExampleGaussianDelaysPerType() {
        // Suppress creation.
    }

    /**
     * Sample EC2 delays. Based on
     * "A Performance Study on the VM Startup Time in the Cloud"
     * 
     * by Mao and Humphrey.
     */
    public static Map<Pair<String, String>, Pair<Double, Double>> EC2_BOOT_TIMES = ImmutableMap
            .<Pair<String, String>, Pair<Double, Double>> builder()
            // === Standard On-Demand Instances
            .put(of("m1.small", NIX_OS), of(100.0, 5.0))
            .put(of("m1.medium", NIX_OS), of(100.0, 5.0))
            .put(of("m1.large", NIX_OS), of(100.0, 10.0))
            .put(of("m1.xlarge", NIX_OS), of(100.0, 7.0))

            // === Second Generation Standard On-Demand Instances
            // .put(of("m3.xlarge", NIX_OS), of())
            // .put(of("m3.2xlarge", NIX_OS), of())

            // === Micro On-Demand Instances
            .put(of("t1.micro", NIX_OS), of(55.0, 3.0))

            // === High-Memory On-Demand Instances
            .put(of("m2.xlarge", NIX_OS), of(110.0, 5.0)).put(of("m2.2xlarge", NIX_OS), of(110.0, 5.0))
            .put(of("m2.4xlarge", NIX_OS), of(110.0, 5.0))

            // === High-CPU On-Demand Instances
            .put(of("c1.medium", NIX_OS), of(90.0, 2.0)).put(of("c1.xlarge", NIX_OS), of(90.0, 2.0))

            // === Cluster Compute Instances
            // .put(of("cc2.8xlarge", NIX_OS), of())

            // === High-Memory Cluster On-Demand Instances
            // .put(of("cr1.8xlarge", NIX_OS), of())

            // === Cluster GPU Instances
            // .put(of("cg1.4xlarge", NIX_OS), of())

            // === High-I/O On-Demand Instances
            // .put(of("hi1.4xlarge", NIX_OS), of())

            // === High-Storage On-Demand Instances
            // .put(of("hs1.8xlarge", NIX_OS), of())

            // === Wildcards
            .put(of(null, NIX_OS), of(96.9, 10.0)).put(of(null, WINDOWS), of(810.2, 10.0)).build();

}
