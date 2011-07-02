package org.cloudbus.cloudsim.power.models;

/**
 * The power model of an HP ProLiant ML110 G4 (1 x [Xeon 3040 1860 MHz, 2 cores], 4GB).
 * http://www.spec.org/power_ssj2008/results/res2011q1/power_ssj2008-20110127-00342.html
 */
public class PowerModelSpecPowerHpProLiantMl110G4Xeon3040 extends PowerModelSpecPower {

	/** The power. */
	private final double[] power = { 86, 89.4, 92.6, 96, 99.5, 102, 106, 108, 112, 114, 117 };

	/* (non-Javadoc)
	 * @see org.cloudbus.cloudsim.power.models.PowerModelSpecPower#getPowerData(int)
	 */
	@Override
	protected double getPowerData(int index) {
		return power[index];
	}

}
