/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.core.predicates;

import org.cloudbus.cloudsim.core.SimEvent;

/**
 * A predicate which will match any event on the deferred event queue. There is a publicly
 * accessible instance of this predicate in <code>Simulation</code>, called
 * <code>Simulation.SIM_ANY</code>, so no new instances need to be created. <br>
 * The idea of simulation predicates was copied from SimJava 2.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 * @see Predicate
 * @see Simulation
 */
public class PredicateAny extends Predicate {

	/**
	 * The match function called by <code>Simulation</code>, not used directly by the user.
	 * 
	 * @param ev the ev
	 * @return true, if match
	 */
	@Override
	public boolean match(SimEvent ev) {
		return true;
	}

}
