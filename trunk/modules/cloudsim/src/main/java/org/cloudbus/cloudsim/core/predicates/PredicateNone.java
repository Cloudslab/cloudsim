package org.cloudbus.cloudsim.core.predicates;

import org.cloudbus.cloudsim.core.SimEvent;

/**
 * A predicate which will <b>not</b> match any event on the deferred event queue.
 * There is a publicly accessible instance of this predicate in the
 * {@link Simulation} class, called {@link Simulation#SIM_NONE},
 * so the user does not need to create any new instances.
 * The idea of simulation predicates was copied from SimJava 2.
 *
 * @see Predicate
 * @see Simulation
 * @author Marcos Dias de Assuncao
 */

public class PredicateNone extends Predicate {

	/**
	 * The match function called by {@link Simulation}, not used directly by the
	 * user.
	 *
	 * @param ev the event to check
	 *
	 * @return <code>true</code> if the event matches the predicate,
	 * <code>false</code> otherwise
	 */
	@Override
	public boolean match(SimEvent ev) {
		return false;
	}

}
