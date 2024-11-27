package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.examples.network.NetworkExample1;
import org.cloudbus.cloudsim.examples.network.NetworkExample2;
import org.cloudbus.cloudsim.examples.network.NetworkExample3;
import org.cloudbus.cloudsim.examples.network.NetworkExample4;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

/**
 * Testsuite that utilizes examples in cloudsim-examples for system / end-to-end (E2E) testing.
 * The expected results for each assertion are derived from CloudSim 6G as of:
 *      tag:  https://github.com/Cloudslab/cloudsim/tree/6.0-pre
 *      hash: 3a64873d8842a0de009931cf026cd7c51295eb5e
 *
 *
 * @TODO: Currently the focus in on cpu time only, but the tests should be extended to other factor too
 *
 * @author Remo Andreoli
 * @since CloudSim Toolkit 7.0
 */
public class NetworkExampleTest {
    private static final String[] empty = new String[0];

    @Test
    public void runNetworkExample1() {
        assertDoesNotThrow(() -> NetworkExample1.main(empty));
        for (Cloudlet cl : NetworkExample1.broker.getCloudletReceivedList()) {
            assertEquals(Cloudlet.CloudletStatus.SUCCESS, cl.getStatus());
            switch (cl.getCloudletId()) {
                case 0 -> assertEquals(160, cl.getActualCPUTime(), 0);
                default -> fail("Unknown cloudlet id");
            }
        }
    }

    @Test
    public void runNetworkExample2() {
        assertDoesNotThrow(() -> NetworkExample2.main(empty));
        for (Cloudlet cl : NetworkExample2.broker.getCloudletReceivedList()) {
            assertEquals(Cloudlet.CloudletStatus.SUCCESS, cl.getStatus());
            switch (cl.getCloudletId()) {
                case 0,1 -> assertEquals(160, cl.getActualCPUTime(), 0);
                default -> fail("Unknown cloudlet id");
            }
        }
    }

    @Test
    public void runNetworkExample3() {
        assertDoesNotThrow(() -> NetworkExample3.main(empty));
        for (Cloudlet cl : NetworkExample3.broker1.getCloudletReceivedList()) {
            assertEquals(Cloudlet.CloudletStatus.SUCCESS, cl.getStatus());
            switch (cl.getCloudletId()) {
                case 0 -> assertEquals(160, cl.getActualCPUTime(), 0);
                default -> fail("Unknown cloudlet id");
            }
        }

        for (Cloudlet cl : NetworkExample3.broker2.getCloudletReceivedList()) {
            assertEquals(Cloudlet.CloudletStatus.SUCCESS, cl.getStatus());
            switch (cl.getCloudletId()) {
                case 0 -> assertEquals(160, cl.getActualCPUTime(), 0);
                default -> fail("Unknown cloudlet id");
            }
        }
    }

    @Test
    public void runNetworkExample4() {
        assertDoesNotThrow(() -> NetworkExample4.main(empty));
        for (Cloudlet cl : NetworkExample4.broker.getCloudletReceivedList()) {
            assertEquals(Cloudlet.CloudletStatus.SUCCESS, cl.getStatus());
            switch (cl.getCloudletId()) {
                case 0 -> assertEquals(160, cl.getActualCPUTime(), 0);
                default -> fail("Unknown cloudlet id");
            }
        }
    }
}
