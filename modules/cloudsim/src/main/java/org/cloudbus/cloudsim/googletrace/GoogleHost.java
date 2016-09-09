package org.cloudbus.cloudsim.googletrace;

import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class GoogleHost extends Host implements Comparable<Host> {

    public GoogleHost(int id, List<? extends Pe> peList, VmScheduler vmScheduler) {
        super(id, new RamProvisionerSimple(Integer.MAX_VALUE),
                new BwProvisionerSimple(Integer.MAX_VALUE), Integer.MAX_VALUE,
                peList, vmScheduler);
    }

    @Override
    public int compareTo(Host other) {
        /*
		 * If this object has bigger amount of available mips it should be
		 * considered before the other one.
		 */
        int result = (-1) * (new Double(getAvailableMips()).compareTo(new Double(other
                .getAvailableMips())));

        if (result == 0)
            return new Integer(getId()).compareTo(new Integer(other.getId()));

        return result;
    }

    @Override
    public int hashCode() {
        return getId();
    }
}
