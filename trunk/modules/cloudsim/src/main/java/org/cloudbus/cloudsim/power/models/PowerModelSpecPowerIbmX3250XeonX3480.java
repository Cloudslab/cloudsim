package org.cloudbus.cloudsim.power.models;

/**
 * The power model of an IBM server x3250 (1 x [Xeon X3480 3067 MHz, 4 cores], 8GB).
 * http://www.spec.org/power_ssj2008/results/res2010q4/power_ssj2008-20101001-00297.html
 */
public class PowerModelSpecPowerIbmX3250XeonX3480 extends PowerModelSpecPower {

	/** The power. */
	private final double[] power = { 42.3, 46.7, 49.7, 55.4, 61.8, 69.3, 76.1, 87, 96.1, 106, 113 };

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.power.models.PowerModelSpecPower#getPowerData(int)
	 */
	@Override
	protected double getPowerData(int index) {
		return power[index];
	}

}
