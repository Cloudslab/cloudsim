package org.cloudbus.cloudsim.power.models;

/**
 * The power model of an IBM server x3250 (1 x [Xeon X3470 2933 MHz, 4 cores], 8GB).
 * http://www.spec.org/power_ssj2008/results/res2009q4/power_ssj2008-20091104-00213.html
 */
public class PowerModelSpecPowerIbmX3250XeonX3470 extends PowerModelSpecPower {

	/** The power. */
	private final double[] power = { 41.6, 46.7, 52.3, 57.9, 65.4, 73, 80.7, 89.5, 99.6, 105, 113 };

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.power.models.PowerModelSpecPower#getPowerData(int)
	 */
	@Override
	protected double getPowerData(int index) {
		return power[index];
	}

}
