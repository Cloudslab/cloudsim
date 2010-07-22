package org.cloudbus.cloudsim.core.predicates;

import org.cloudbus.cloudsim.core.SimEvent;

/**
 * Predicates are used to select events from the deferred queue.
 * This class is abstract and must be extended when writing a new
 * predicate. Some standard predicates are provided.<br>
 * The idea of simulation predicates was copied from SimJava 2.
 *
 * @see	PredicateType
 * @see PredicateFrom
 * @see PredicateAny
 * @see PredicateNone
 * @see Simulation
 * @author Marcos Dias de Assuncao
 */
public abstract class Predicate {

	/**
	 * The match function which must be overridden when writing a new predicate.
	 * The function is called with each event in the deferred queue as its
	 * parameter when a <code>Simulation.select()</code> call is made by the
	 * user.
	 *
	 * @param event The event to test for a match.
	 *
	 * @return The function should return <code>true</code> if the event matches
	 * and should be selected, or <code>false</code> if it does not
	 * match the predicate.
	 */
	public abstract boolean match(SimEvent event);

}
