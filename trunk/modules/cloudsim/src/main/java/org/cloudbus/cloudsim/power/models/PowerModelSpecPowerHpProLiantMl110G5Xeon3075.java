package org.cloudbus.cloudsim.power.models;

/**
 * The power model of an HP ProLiant ML110 G5 (1 x [Xeon 3075 2660 MHz, 2 cores], 4GB).
 * http://www.spec.org/power_ssj2008/results/res2011q1/power_ssj2008-20110124-00339.html
 */
public class PowerModelSpecPowerHpProLiantMl110G5Xeon3075 extends PowerModelSpecPower {

	/** The power. */
	private final double[] power = { 93.7, 97, 101, 105, 110, 116, 121, 125, 129, 133, 135 };

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.power.models.PowerModelSpecPower#getPowerData(int)
	 */
	@Override
	protected double getPowerData(int index) {
		return power[index];
	}

}
