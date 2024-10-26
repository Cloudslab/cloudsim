package TieredConfigurations;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;

import java.util.List;

public class CustomDatacenterBroker extends DatacenterBroker {

    public CustomDatacenterBroker(String name) throws Exception {
        super(name);
    }

    // Method to submit a list of VMs
    public void submitCustomVmList(List<Vm> list) {
        for (Vm vm : list) {
            vm.setUserId(getId());
        }
        this.vmList.addAll(list);  // Add VMs directly to the protected list
    }

    // Method to submit a list of Cloudlets
    public void submitCustomCloudletList(List<Cloudlet> list) {
        for (Cloudlet cloudlet : list) {
            cloudlet.setUserId(getId());
        }
        this.cloudletList.addAll(list);  // Add Cloudlets directly to the protected list
    }

    // Method to bind Cloudlets to VMs
    public void bindCloudletsToVms(List<Cloudlet> cloudletList, List<Vm> vmList) {
        for (int i = 0; i < cloudletList.size(); i++) {
            cloudletList.get(i).setVmId(vmList.get(i % vmList.size()).getId());
        }
    }
}
