package org.cloudbus.cloudsim.EX.billing;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.util.Map;

import static java.math.BigDecimal.valueOf;
import static org.apache.commons.lang3.tuple.ImmutablePair.of;
import static org.cloudbus.cloudsim.Consts.NIX_OS;

/**
 * 
 * @author nikolay.grozev
 * 
 */
public final class ExamplePrices {

    private ExamplePrices() {
        // Suppress creation.
    }

    /** Sample on-demand prices for EC2 in Ireland. */
    public static Map<Pair<String, String>, BigDecimal> EC2_NIX_OS_PRICES_IRELAND = ImmutableMap
            .<Pair<String, String>, BigDecimal> builder()
            // === Standard On-Demand Instances
            .put(of("m1.small", NIX_OS), valueOf(0.065)).put(of("m1.medium", NIX_OS), valueOf(0.130))
            .put(of("m1.large", NIX_OS), valueOf(0.260)).put(of("m1.xlarge", NIX_OS), valueOf(0.520))

            // === Second Generation Standard On-Demand Instances
            .put(of("m3.xlarge", NIX_OS), valueOf(0.550)).put(of("m3.2xlarge", NIX_OS), valueOf(1.100))

            // === Micro On-Demand Instances
            .put(of("t1.micro", NIX_OS), valueOf(0.020))

            // === High-Memory On-Demand Instances
            .put(of("m2.xlarge", NIX_OS), valueOf(0.460)).put(of("m2.2xlarge", NIX_OS), valueOf(0.920))
            .put(of("m2.4xlarge", NIX_OS), valueOf(1.840))

            // === High-CPU On-Demand Instances
            .put(of("c1.medium", NIX_OS), valueOf(0.165)).put(of("c1.xlarge", NIX_OS), valueOf(0.660))

            // === Cluster Compute Instances
            .put(of("cc2.8xlarge", NIX_OS), valueOf(2.700))

            // === High-Memory Cluster On-Demand Instances
            .put(of("cr1.8xlarge", NIX_OS), valueOf(3.750))

            // === Cluster GPU Instances
            .put(of("cg1.4xlarge", NIX_OS), valueOf(2.360))

            // === High-I/O On-Demand Instances
            .put(of("hi1.4xlarge", NIX_OS), valueOf(3.410))

            // === High-Storage On-Demand Instances
            .put(of("hs1.8xlarge", NIX_OS), valueOf(4.900)).build();

    /** Sample on-demand prices for EC2 in Virginia. */
    public static Map<Pair<String, String>, BigDecimal> EC2_NIX_OS_PRICES_VIRGINIA = ImmutableMap
            .<Pair<String, String>, BigDecimal> builder()
            // === Standard On-Demand Instances
            .put(of("m1.small", NIX_OS), valueOf(0.060)).put(of("m1.medium", NIX_OS), valueOf(0.120))
            .put(of("m1.large", NIX_OS), valueOf(0.240)).put(of("m1.xlarge", NIX_OS), valueOf(0.480))

            // === Second Generation Standard On-Demand Instances
            .put(of("m3.xlarge", NIX_OS), valueOf(0.500)).put(of("m3.2xlarge", NIX_OS), valueOf(1.000))

            // === Micro On-Demand Instances
            .put(of("t1.micro", NIX_OS), valueOf(0.020))

            // === High-Memory On-Demand Instances
            .put(of("m2.xlarge", NIX_OS), valueOf(0.410)).put(of("m2.2xlarge", NIX_OS), valueOf(0.820))
            .put(of("m2.4xlarge", NIX_OS), valueOf(1.640))

            // === High-CPU On-Demand Instances
            .put(of("c1.medium", NIX_OS), valueOf(0.145)).put(of("c1.xlarge", NIX_OS), valueOf(0.580))

            // === Cluster Compute Instances
            .put(of("cc2.8xlarge", NIX_OS), valueOf(2.400))

            // === High-Memory Cluster On-Demand Instances
            .put(of("cr1.8xlarge", NIX_OS), valueOf(3.500))

            // === Cluster GPU Instances
            .put(of("cg1.4xlarge", NIX_OS), valueOf(2.100))

            // === High-I/O On-Demand Instances
            .put(of("hi1.4xlarge", NIX_OS), valueOf(3.100))

            // === High-Storage On-Demand Instances
            .put(of("hs1.8xlarge", NIX_OS), valueOf(4.600)).build();

    /** Sample on-demand prices for Google in Europe. */
    public static Map<Pair<String, String>, BigDecimal> GOOGLE_NIX_OS_PRICES_EUROPE = ImmutableMap
            .<Pair<String, String>, BigDecimal> builder().put(of("n1-standard-1-d", NIX_OS), valueOf(0.145))
            .put(of("n1-standard-2-d", NIX_OS), valueOf(0.290)).put(of("n1-standard-4-d", NIX_OS), valueOf(0.580))
            .put(of("n1-standard-8-d", NIX_OS), valueOf(1.160)).put(of("n1-standard-1", NIX_OS), valueOf(0.127))
            .put(of("n1-standard-2", NIX_OS), valueOf(0.253)).put(of("n1-standard-4", NIX_OS), valueOf(0.507))
            .put(of("n1-standard-8", NIX_OS), valueOf(1.014)).put(of("n1-highmem-2-d", NIX_OS), valueOf(0.344))
            .put(of("n1-highmem-4-d", NIX_OS), valueOf(0.687)).put(of("n1-highmem-8-d", NIX_OS), valueOf(1.375))
            .put(of("n1-highmem-2", NIX_OS), valueOf(0.275)).put(of("n1-highmem-4", NIX_OS), valueOf(0.549))
            .put(of("n1-highmem-8", NIX_OS), valueOf(1.098)).put(of("n1-highcpu-2-d", NIX_OS), valueOf(0.184))
            .put(of("n1-highcpu-4-d", NIX_OS), valueOf(0.369)).put(of("n1-highcpu-8-d", NIX_OS), valueOf(0.737))
            .put(of("n1-highcpu-2", NIX_OS), valueOf(0.146)).put(of("n1-highcpu-4", NIX_OS), valueOf(0.292))
            .put(of("n1-highcpu-8", NIX_OS), valueOf(0.584)).put(of("f1-micro", NIX_OS), valueOf(0.021))
            .put(of("g1-small", NIX_OS), valueOf(0.059)).build();

    /** Sample on-demand prices for Google in the US. */
    public static Map<Pair<String, String>, BigDecimal> GOOGLE_NIX_OS_PRICES_US = ImmutableMap
            .<Pair<String, String>, BigDecimal> builder().put(of("n1-standard-1-d", NIX_OS), valueOf(0.132))
            .put(of("n1-standard-2-d", NIX_OS), valueOf(0.265)).put(of("n1-standard-4-d", NIX_OS), valueOf(0.530))
            .put(of("n1-standard-8-d", NIX_OS), valueOf(1.060)).put(of("n1-standard-1", NIX_OS), valueOf(0.115))
            .put(of("n1-standard-2", NIX_OS), valueOf(0.230)).put(of("n1-standard-4", NIX_OS), valueOf(0.461))
            .put(of("n1-standard-8", NIX_OS), valueOf(0.922)).put(of("n1-highmem-2-d", NIX_OS), valueOf(0.305))
            .put(of("n1-highmem-4-d", NIX_OS), valueOf(0.611)).put(of("n1-highmem-8-d", NIX_OS), valueOf(1.221))
            .put(of("n1-highmem-2", NIX_OS), valueOf(0.244)).put(of("n1-highmem-4", NIX_OS), valueOf(0.488))
            .put(of("n1-highmem-8", NIX_OS), valueOf(0.975)).put(of("n1-highcpu-2-d", NIX_OS), valueOf(0.163))
            .put(of("n1-highcpu-4-d", NIX_OS), valueOf(0.326)).put(of("n1-highcpu-8-d", NIX_OS), valueOf(0.653))
            .put(of("n1-highcpu-2", NIX_OS), valueOf(0.131)).put(of("n1-highcpu-4", NIX_OS), valueOf(0.261))
            .put(of("n1-highcpu-8", NIX_OS), valueOf(0.522)).put(of("f1-micro", NIX_OS), valueOf(0.019))
            .put(of("g1-small", NIX_OS), valueOf(0.054)).build();

}
