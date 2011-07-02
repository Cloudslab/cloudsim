package org.cloudbus.cloudsim.power.models;

/**
 * The power model of an HP ProLiant ML110 G3 (1 x [Pentium D930 3000 MHz, 2 cores], 4GB).
 * http://www.spec.org/power_ssj2008/results/res2011q1/power_ssj2008-20110127-00342.html
 */
public class PowerModelSpecPowerHpProLiantMl110G3PentiumD930 extends PowerModelSpecPower {

	/** The power. */
	private final double[] power = { 105, 112, 118, 125, 131, 137, 147, 153, 157, 164, 169 };

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.power.models.PowerModelSpecPower#getPowerData(int)
	 */
	@Override
	protected double getPowerData(int index) {
		return power[index];
	}

}
