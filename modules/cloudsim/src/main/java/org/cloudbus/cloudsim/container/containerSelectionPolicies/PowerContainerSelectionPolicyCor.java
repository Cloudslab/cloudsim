package org.cloudbus.cloudsim.container.containerSelectionPolicies;

import org.cloudbus.cloudsim.container.containerSelectionPolicies.PowerContainerSelectionPolicy;
import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.core.PowerContainer;
import org.cloudbus.cloudsim.container.core.PowerContainerHost;
import org.cloudbus.cloudsim.container.core.PowerContainerHostUtilizationHistory;
import org.cloudbus.cloudsim.container.utils.Correlation;
import org.cloudbus.cloudsim.Log;
import java.util.List;

/**
 * Created by sareh on 7/08/15.
 */
public class PowerContainerSelectionPolicyCor extends PowerContainerSelectionPolicy {
    /**
     * The fallback policy.
     */
    private PowerContainerSelectionPolicy fallbackPolicy;

    /**
     * Instantiates a new power container selection policy maximum correlation.
     *
     * @param fallbackPolicy the fallback policy
     */
    public PowerContainerSelectionPolicyCor(final PowerContainerSelectionPolicy fallbackPolicy) {
        super();
        setFallbackPolicy(fallbackPolicy);
    }

    /*
    * (non-Javadoc)
    *
    * @see PowerContainerSelectionPolicy#getContainerToMigrate
    */
    @Override
    public Container getContainerToMigrate(final PowerContainerHost host) {
        List<PowerContainer> migratableContainers = getMigratableContainers(host);
        if (migratableContainers.isEmpty()) {
            return null;
        }
        Container container= getContainer(migratableContainers, host);
        migratableContainers.clear();
        if (container!= null) {
//            Log.printConcatLine("We have to migrate the container with ID", container.getId());
            return container;
        } else {
            return getFallbackPolicy().getContainerToMigrate(host);
        }
    }

    /**
     * Gets the fallback policy.
     *
     * @return the fallback policy
     */
    public PowerContainerSelectionPolicy getFallbackPolicy() {
        return fallbackPolicy;
    }


    /**
     * Sets the fallback policy.
     *
     * @param fallbackPolicy the new fallback policy
     */
    public void setFallbackPolicy(final PowerContainerSelectionPolicy fallbackPolicy) {
        this.fallbackPolicy = fallbackPolicy;
    }

    public Container getContainer(List<PowerContainer> migratableContainers, PowerContainerHost host) {

        double[] corResult = new double[migratableContainers.size()];
        Correlation correlation = new Correlation();
        int i = 0;
        double maxValue = -2.0;
        int id = -1;
        if (host instanceof PowerContainerHostUtilizationHistory) {

            double[] hostUtilization = ((PowerContainerHostUtilizationHistory) host).getUtilizationHistory();
            for (Container container : migratableContainers) {
                double[] containerUtilization = ((PowerContainer) container).getUtilizationHistoryList();

                double cor = correlation.getCor(hostUtilization, containerUtilization);
                if (Double.isNaN(cor)) {
                    cor = -3;
                }
                corResult[i] = cor;
                
                if(corResult[i] > maxValue){
                	maxValue = corResult[i];
                	id = i;
                }
                
                i++;
            }

        }

        if (id == -1) {
            Log.printConcatLine("Problem with correlation list.");
        }

        return migratableContainers.get(id);

    }


}
