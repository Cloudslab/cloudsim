package org.cloudbus.cloudsim.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkloadFileReaderTest {

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterAll
    public static void tearDownAfterClass() throws Exception {
    }

    @BeforeEach
    public void setUp() throws Exception {
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void read() throws FileNotFoundException {

	WorkloadModel r = new WorkloadFileReader("src"
		+ File.separator
		+ "test"
		+ File.separator
		+ "LCG.swf.gz", 1);
	List<Cloudlet> cloudletlist = r.generateWorkload();
	assertEquals(188041, cloudletlist.size());

	for (Cloudlet cloudlet : cloudletlist) {
	    assertTrue(cloudlet.getCloudletLength() > 0);
	}
    }
}
