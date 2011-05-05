package org.cloudbus.cloudsim.power.models;

/**
 * The power model of an IBM server x3550 (2 x [Xeon X5670 2933 MHz, 6 cores], 12GB).
 * http://www.spec.org/power_ssj2008/results/res2010q2/power_ssj2008-20100315-00239.html
 */
public class PowerModelSpecPowerIbmX3550XeonX5670 extends PowerModelSpecPower {

	/** The power. */
	private final double[] power = { 66, 107, 120, 131, 143, 156, 173, 191, 211, 229, 247 };

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.power.models.PowerModelSpecPower#getPowerData(int)
	 */
	@Override
	protected double getPowerData(int index) {
		return power[index];
	}

}
