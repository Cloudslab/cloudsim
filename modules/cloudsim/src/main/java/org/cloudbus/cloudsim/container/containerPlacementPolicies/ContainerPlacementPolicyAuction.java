package org.cloudbus.cloudsim.container.containerPlacementPolicies;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.auction.bidder.BidderContainerVM;
import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.core.ContainerVm;
import org.cloudbus.cloudsim.container.utils.RandomGen;

import java.util.List;
import java.util.Set;

/**
 * @author henri.vandenbulk
 * For container placement based on performing an action.
 */
public class ContainerPlacementPolicyAuction extends ContainerPlacementPolicy {
    @Override
    public ContainerVm getContainerVm(List<ContainerVm> vmList, Object obj, Set<? extends ContainerVm> excludedVmList) {
        ContainerVm containerVm = null;

        ContainerVm selectedVm = null;
        Container container = (Container)obj;
        double lowestBid = Double.MAX_VALUE;
        double bid = 0;

        // Look at what's requested
        //double totalMips = containerVm1.getMips();
        double totalMips = container.getCurrentRequestedTotalMips();
        double totalMemory = container.getCurrentRequestedRam();

        for (ContainerVm containerVm1 : vmList) {
            if (excludedVmList.contains(containerVm1)) {
                continue;
            }

            // Ask the VM to bid
            if (containerVm1 instanceof BidderContainerVM) {
                bid = ((BidderContainerVM) containerVm1).bidOnAuction(totalMips, totalMemory);
            }


            Log.formatLine(container.getId() + " -> " + containerVm1.getId() + " :" +
                            "Auction, bid %.2f, total request %.2f millicores %.2f memory bytes",
                    bid, totalMips, totalMemory);

            // Select the VM with the max score
            if (Double.compare(lowestBid, bid) > 0) {
                lowestBid = bid;
                selectedVm = containerVm1;
            }
        }
        return selectedVm;

    }
}
