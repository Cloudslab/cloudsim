package org.cloudbus.cloudsim.power;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;

/**
 * The Class PowerPe.
 */
public class PowerPe extends Pe {

	/** The power model. */
	private PowerModel powerModel;

	/**
	 * Instantiates a new PowerPe.
	 *
	 * @param id the id
	 * @param peProvisioner the PowerPe provisioner
	 * @param powerModel the power model
	 */
	public PowerPe(int id, PeProvisioner peProvisioner, PowerModel powerModel) {
		super(id, peProvisioner);
		setPowerModel(powerModel);
	}

	/**
	 * Sets the power model.
	 *
	 * @param powerModel the new power model
	 */
	protected void setPowerModel(PowerModel powerModel) {
		this.powerModel = powerModel;
	}

	/**
	 * Gets the power model.
	 *
	 * @return the power model
	 */
	public PowerModel getPowerModel() {
		return powerModel;
	}

}
