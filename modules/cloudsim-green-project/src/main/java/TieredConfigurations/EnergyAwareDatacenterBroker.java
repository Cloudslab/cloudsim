package TieredConfigurations;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudActionTags;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.VmList;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class EnergyAwareDatacenterBroker extends DatacenterBroker {

    private Map<Integer, Double> datacenterEnergyConsumption;

    public EnergyAwareDatacenterBroker(String name) throws Exception {
        super(name);
        datacenterEnergyConsumption = new HashMap<>();
    }

    // Override the default processEvent to include energy-awareness
    @Override
    protected void processVmCreateAck(SimEvent ev) {
        super.processVmCreateAck(ev);

        // For each Datacenter, fetch the energy consumption value and store it
        int[] data = (int[]) ev.getData();
        int datacenterId = data[0];
        Datacenter datacenter = (Datacenter) CloudSim.getEntity(datacenterId);

        if (datacenter instanceof EnergyAwareDatacenter) {
            double energyConsumption = ((EnergyAwareDatacenter) datacenter).getEstimatedEnergyConsumption();
            datacenterEnergyConsumption.put(datacenterId, energyConsumption);
            Log.printLine(CloudSim.clock() + ": Datacenter " + datacenter.getName() +
                    " has energy consumption: " + energyConsumption);
        }
    }

    // Override submitCloudlets to make energy-aware cloudlet submission
    @Override
    protected void submitCloudlets() {
        List<Cloudlet> successfullySubmitted = new ArrayList<>();

        // Get the list of available datacenters sorted by energy consumption
        List<Integer> sortedDatacenters = getSortedDatacentersByEnergyConsumption();

        for (Cloudlet cloudlet : getCloudletList()) {
            GuestEntity vm;
            int datacenterId;

            // Submit cloudlets to the Datacenter with the lowest energy consumption
            for (int dcId : sortedDatacenters) {
                datacenterId = dcId;

                // Find a VM in the datacenter to submit the cloudlet
                vm = VmList.getById(getGuestsCreatedList(), cloudlet.getGuestId());

                if (vm == null) {
                    continue;
                }

                if (!Log.isDisabled()) {
                    Log.printlnConcat(CloudSim.clock(), ": ", getName(), ": Sending cloudlet ", cloudlet.getCloudletId(),
                            " to " + vm.getClassName() + " #", vm.getId());
                }

                cloudlet.setGuestId(vm.getId());
                sendNow(datacenterId, CloudActionTags.CLOUDLET_SUBMIT, cloudlet);
                cloudletsSubmitted++;
                getCloudletSubmittedList().add(cloudlet);
                successfullySubmitted.add(cloudlet);
                break;
            }
        }

        // Remove submitted cloudlets from waiting list
        getCloudletList().removeAll(successfullySubmitted);
    }

    // Method to sort datacenters by energy consumption (lowest to highest)
    private List<Integer> getSortedDatacentersByEnergyConsumption() {
        List<Map.Entry<Integer, Double>> list = new ArrayList<>(datacenterEnergyConsumption.entrySet());
        list.sort(Map.Entry.comparingByValue());

        List<Integer> sortedDatacenters = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : list) {
            sortedDatacenters.add(entry.getKey());
        }
        return sortedDatacenters;
    }

    @Override
    protected void processOtherEvent(SimEvent ev) {
        Log.printLine(getName() + ".processOtherEvent(): Error - event unknown by this DatacenterBroker.");
    }
}

