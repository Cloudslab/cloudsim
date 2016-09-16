package org.cloudbus.cloudsim.googletrace;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.junit.Assert;
import org.junit.Test;

public class PriorityHostSkinTest {

	@Test
	public void testEquals() {
		List<Pe> peList1 = new ArrayList<Pe>();
		peList1.add(new Pe(0, new PeProvisionerSimple(100)));
		GoogleHost host1 = new GoogleHost(1, peList1, new VmSchedulerMipsBased(
				peList1), 1);

		PriorityHostSkin skin1 = new PriorityHostSkin(host1, 0);

		PriorityHostSkin skin2 = new PriorityHostSkin(host1, 0);

		Assert.assertEquals(skin1, skin2);

	}

}
