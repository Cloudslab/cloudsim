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
 * A predicate which will match any event on the deferred event queue. 
 * See the publicly accessible instance of this predicate in
 * {@link org.cloudbus.cloudsim.core.CloudSim#SIM_ANY}, so no new instances needs to be created. <br/>
 * The idea of simulation predicates was copied from SimJava 2.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 * @see Predicate
 * @see Simulation
 * @todo If there is only one instance of this class, it should be defined as a Singleton.
 * The same may apply for the other predicates.
 */
public class PredicateAny extends Predicate {

	/**
	 * Considers there is no criteria to match an event,
         * so any event received by the predicate will match.
	 * 
	 * @param ev {@inheritDoc}
	 * @return always true to indicate that any received event is accepted
	 */
	@Override
	public boolean match(SimEvent ev) {
		return true;
	}

}
