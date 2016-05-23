package org.cloudbus.cloudsim.container.containerSelectionPolicies;

import org.cloudbus.cloudsim.container.core.Container;
import org.cloudbus.cloudsim.container.core.PowerContainer;
import org.cloudbus.cloudsim.container.core.PowerContainerHost;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.cloudbus.cloudsim.util.MathUtil;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by sareh on 31/07/15.
 */
public class PowerContainerSelectionPolicyMaximumCorrelation extends PowerContainerSelectionPolicy {


    /**
     * The fallback policy.
     */
    private PowerContainerSelectionPolicy fallbackPolicy;

    /**
     * Instantiates a new power container selection policy maximum correlation.
     *
     * @param fallbackPolicy the fallback policy
     */
    public PowerContainerSelectionPolicyMaximumCorrelation(final PowerContainerSelectionPolicy fallbackPolicy) {
        super();
        setFallbackPolicy(fallbackPolicy);
    }

    /*
     * (non-Javadoc)
     *
     * @see powerContainerSelectionPolicy#getContainerToMigrate()
     */
    @Override
    public Container getContainerToMigrate(final PowerContainerHost host) {
        List<PowerContainer> migratableContainers = getMigratableContainers(host);
        if (migratableContainers.isEmpty()) {
            return null;
        }
        List<Double> metrics = null;
        try {
            metrics = getCorrelationCoefficients(getUtilizationMatrix(migratableContainers));
        } catch (IllegalArgumentException e) { // the degrees of freedom must be greater than zero
            return getFallbackPolicy().getContainerToMigrate(host);
        }
        double maxMetric = Double.MIN_VALUE;
        int maxIndex = 0;
        for (int i = 0; i < metrics.size(); i++) {
            double metric = metrics.get(i);
            if (metric > maxMetric) {
                maxMetric = metric;
                maxIndex = i;
            }
        }
        return migratableContainers.get(maxIndex);
    }

    /**
     * Gets the utilization matrix.
     *
     * @param powerContainers the powerContainers
     * @return the utilization matrix
     */
    protected double[][] getUtilizationMatrix(final List<PowerContainer> powerContainers) {
        int n = powerContainers.size();
        int m = getMinUtilizationHistorySize(powerContainers);
        double[][] utilization = new double[n][m];
        for (int i = 0; i < n; i++) {
            List<Double> vmUtilization = powerContainers.get(i).getUtilizationHistory();
            for (int j = 0; j < vmUtilization.size(); j++) {
                utilization[i][j] = vmUtilization.get(j);
            }
        }
        return utilization;
    }

    /**
     * Gets the min utilization history size.
     *
     * @param containerList the container list
     * @return the min utilization history size
     */
    protected int getMinUtilizationHistorySize(final List<PowerContainer> containerList) {
        int minSize = Integer.MAX_VALUE;
        for (PowerContainer container : containerList) {
            int size = container.getUtilizationHistory().size();
            if (size < minSize) {
                minSize = size;
            }
        }
        return minSize;
    }

    /**
     * Gets the correlation coefficients.
     *
     * @param data the data
     * @return the correlation coefficients
     */
    protected List<Double> getCorrelationCoefficients(final double[][] data) {
        int n = data.length;
        int m = data[0].length;
        List<Double> correlationCoefficients = new LinkedList<Double>();
        for (int i = 0; i < n; i++) {
            double[][] x = new double[n - 1][m];
            int k = 0;
            for (int j = 0; j < n; j++) {
                if (j != i) {
                    x[k++] = data[j];
                }
            }

            // Transpose the matrix so that it fits the linear model
            double[][] xT = new Array2DRowRealMatrix(x).transpose().getData();

            // RSquare is the "coefficient of determination"
            correlationCoefficients.add(MathUtil.createLinearRegression(xT,
                    data[i]).calculateRSquared());
        }
        return correlationCoefficients;
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

}


