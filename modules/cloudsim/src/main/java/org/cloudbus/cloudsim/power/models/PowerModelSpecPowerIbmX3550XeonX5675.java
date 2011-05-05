package org.cloudbus.cloudsim.power.models;

/**
 * The power model of an IBM server x3550 (2 x [Xeon X5675 3067 MHz, 6 cores], 16GB).
 * http://www.spec.org/power_ssj2008/results/res2011q2/power_ssj2008-20110406-00368.html
 */
public class PowerModelSpecPowerIbmX3550XeonX5675 extends PowerModelSpecPower {

	/** The power. */
	private final double[] power = { 58.4, 98, 109, 118, 128, 140, 153, 170, 189, 205, 222 };

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.power.models.PowerModelSpecPower#getPowerData(int)
	 */
	@Override
	protected double getPowerData(int index) {
		return power[index];
	}

}
