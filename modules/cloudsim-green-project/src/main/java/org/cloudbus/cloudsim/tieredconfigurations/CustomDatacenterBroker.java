package org.cloudbus.cloudsim.tieredconfigurations;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.GuestEntity;

import java.util.List;

public class CustomDatacenterBroker extends DatacenterBroker {

    public CustomDatacenterBroker(String name) throws Exception {
        super(name);
    }

    // Method to submit a list of VMs
    public void submitCustomVmList(List<Vm> list) {
        // Loop through the VM list and cast each VM to GuestEntity, then add it to vmList
        for (Vm vm : list) {
            vm.setUserId(getId());
            ((List<GuestEntity>) vmList).add(vm);  // Cast each Vm to GuestEntity and add to vmList
        }
    }

    // Method to submit a list of Cloudlets
    public void submitCustomCloudletList(List<Cloudlet> list) {
        for (Cloudlet cloudlet : list) {
            cloudlet.setUserId(getId());
            ((List<Cloudlet>) cloudletList).add(cloudlet);  // Add Cloudlets individually to the cloudletList
        }
    }

    // Method to bind Cloudlets to VMs
    public void bindCloudletsToVms(List<Cloudlet> cloudletList, List<Vm> vmList) {
        for (int i = 0; i < cloudletList.size(); i++) {
            cloudletList.get(i).setVmId(vmList.get(i % vmList.size()).getId());
        }
    }
}
