/**
 * 
 */
package org.cloudbus.cloudsim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import org.junit.Before;
import org.junit.Test;

/**
 * @author abe
 *
 */
public class UtilizationModelStochasticTest {

	private UtilizationModelStochastic utilizationModel;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		utilizationModel = new UtilizationModelStochastic();
	}

	/**
	 * Test method for {@link cloudsim.UtilizationModelStochastic#getUtilization(double)}.
	 */
	@Test
	public void testGetUtilization() {
		double utilization0 = utilizationModel.getUtilization(0);
		double utilization1 = utilizationModel.getUtilization(1);
		assertNotNull(utilization0);
		assertNotNull(utilization1);
		assertNotSame(utilization0, utilization1);
		assertEquals(utilization0, utilizationModel.getUtilization(0), 0);
		assertEquals(utilization1, utilizationModel.getUtilization(1), 0);
	}

}
