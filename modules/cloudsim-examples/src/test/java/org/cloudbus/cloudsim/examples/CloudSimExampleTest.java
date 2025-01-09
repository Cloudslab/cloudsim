package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.Cloudlet;
import org.junit.jupiter.api.Test;

import java.util.List;

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
public class CloudSimExampleTest {
    private static final String[] empty = new String[0];

    @Test
    public void runCloudSimExample1() {
        assertDoesNotThrow(() -> CloudSimExample1.main(empty));
        for (Cloudlet cl : CloudSimExample1.broker.getCloudletReceivedList()) {
            assertEquals(Cloudlet.CloudletStatus.SUCCESS, cl.getStatus());
            switch (cl.getCloudletId()) {
                case 0 -> assertEquals(400, cl.getActualCPUTime(), 0);
                default -> fail("Unknown cloudlet id");
            }
        }
    }

    @Test
    public void runCloudSimExample2() {
        assertDoesNotThrow(() -> CloudSimExample2.main(empty));
        for (Cloudlet cl : CloudSimExample2.broker.getCloudletReceivedList()) {
            assertEquals(Cloudlet.CloudletStatus.SUCCESS, cl.getStatus());
            switch (cl.getCloudletId()) {
                case 0, 1 -> assertEquals(1000, cl.getActualCPUTime(), 0);
                default -> fail("Unknown cloudlet id");
            }
        }
    }

    @Test
    public void runCloudSimExample3() {
        assertDoesNotThrow(() -> CloudSimExample3.main(empty));
        for (Cloudlet cl : CloudSimExample3.broker.getCloudletReceivedList()) {
            assertEquals(Cloudlet.CloudletStatus.SUCCESS, cl.getStatus());
            switch (cl.getCloudletId()) {
                case 0 -> assertEquals(160, cl.getActualCPUTime(), 0);
                case 1 -> assertEquals(80, cl.getActualCPUTime(), 0);
                default -> fail("Unknown cloudlet id");
            }
        }
    }

    @Test
    public void runCloudSimExample4() {
        assertDoesNotThrow(() -> CloudSimExample4.main(empty));
        for (Cloudlet cl : CloudSimExample4.broker.getCloudletReceivedList()) {
            assertEquals(Cloudlet.CloudletStatus.SUCCESS, cl.getStatus());
            switch (cl.getCloudletId()) {
                case 0, 1 -> assertEquals(160, cl.getActualCPUTime(), 0);
                default -> fail("Unknown cloudlet id");
            }
        }
    }

    @Test
    public void runCloudSimExample5() {
        assertDoesNotThrow(() -> CloudSimExample5.main(empty));
        for (Cloudlet cl : CloudSimExample5.broker1.getCloudletReceivedList()) {
            assertEquals(Cloudlet.CloudletStatus.SUCCESS, cl.getStatus());
            switch (cl.getCloudletId()) {
                case 0 -> assertEquals(160, cl.getActualCPUTime(), 0);
                default -> fail("Unknown cloudlet id");
            }
        }

        for (Cloudlet cl : CloudSimExample5.broker2.getCloudletReceivedList()) {
            assertEquals(Cloudlet.CloudletStatus.SUCCESS, cl.getStatus());
            switch (cl.getCloudletId()) {
                case 0 -> assertEquals(160, cl.getActualCPUTime(), 0);
                default -> fail("Unknown cloudlet id");
            }
        }
    }

    @Test
    public void runCloudSimExample6() {
        assertDoesNotThrow(() -> CloudSimExample6.main(empty));
        for (Cloudlet cl : CloudSimExample6.broker.getCloudletReceivedList()) {
            assertEquals(Cloudlet.CloudletStatus.SUCCESS, cl.getStatus());
            switch (cl.getCloudletId()) {
                case 4,16,28,5,17,29,6,18,30,7,19,31,8,20,32,10,22,34,9,21,33,11,23,35 -> assertEquals(3, cl.getActualCPUTime(), 0.01);
                case 0,12,24,36,1,13,25,37,2,14,26,38,3,15,27,39 -> assertEquals(4, cl.getActualCPUTime(), 0.01);
                default -> fail("Unknown cloudlet id");
            }
        }
    }

    @Test
    public void runCloudSimExample7() {
        assertDoesNotThrow(() -> CloudSimExample7.main(empty));
        for (Cloudlet cl : CloudSimExample7.broker.getCloudletReceivedList()) {
            assertEquals(Cloudlet.CloudletStatus.SUCCESS, cl.getStatus());
            switch (cl.getCloudletId()) {
                case 0,5,1,6,2,7,4,9,3,8 -> assertEquals(320, cl.getActualCPUTime(), 0.01);
                default -> fail("Unknown cloudlet id");
            }
        }
    }

    @Test
    public void runCloudSimExample8() {
        assertDoesNotThrow(() -> CloudSimExample8.main(empty));
        List<Cloudlet> clList = CloudSimExample8.broker.getCloudletReceivedList();
        clList.addAll(CloudSimExample8.globalBroker.getBroker().getCloudletReceivedList());

        for (Cloudlet cl : clList) {
            assertEquals(Cloudlet.CloudletStatus.SUCCESS, cl.getStatus());
            switch (cl.getCloudletId()) {
                case 0,5,1,6,2,7,4,9,3,8,101,106,103,108,100,105,102,107,104,109 -> assertEquals(320, cl.getActualCPUTime(), 0);
                default -> fail("Unknown cloudlet id");
            }
        }
    }

    @Test
    public void runCloudSimExample9() {
        assertDoesNotThrow(() -> CloudSimExample9.main(empty));
        for (Cloudlet cl : CloudSimExample9.broker.getCloudletReceivedList()) {
            assertEquals(Cloudlet.CloudletStatus.SUCCESS, cl.getStatus());
            switch (cl.getCloudletId()) {
                case 0 -> assertEquals(30, cl.getActualCPUTime(), 0.01);
                case 1 -> assertEquals(210, cl.getActualCPUTime(), 0.01);
                case 2 -> assertEquals(1110, cl.getActualCPUTime(), 0.01);
                case 3 -> assertEquals(10, cl.getActualCPUTime(), 0.01);
                case 4 -> assertEquals(100, cl.getActualCPUTime(), 0.01);
                case 5 -> assertEquals(1000, cl.getActualCPUTime(), 0.01);
                default -> fail("Unknown cloudlet id");
            }
        }
    }
}
