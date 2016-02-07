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
 * A predicate which selects events coming from specific entities.<br/>
 * The idea of simulation predicates was copied from SimJava 2.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 * @see PredicateNotFrom
 * @see Predicate
 */
public class PredicateFrom extends Predicate {

	/** The IDs of source entities to check the reception of events from. */
	private final int[] ids;

	/**
	 * Constructor used to select events that were sent by a specific entity.
	 * 
	 * @param sourceId the id number of the source entity
	 */
	public PredicateFrom(int sourceId) {
		ids = new int[] { sourceId };
	}

	/**
	 * Constructor used to select events that were sent by any entity from a given set.
	 * 
	 * @param sourceIds the set of id numbers of the source entities
	 */
	public PredicateFrom(int[] sourceIds) {
		ids = sourceIds.clone();
	}

	/**
	 * Matches any event received from the registered sources.
	 * 
	 * @param ev {@inheritDoc}
	 * @return {@inheritDoc}
         * @see #ids
	 */
	@Override
	public boolean match(SimEvent ev) {
		int src = ev.getSource();
                /*
                @todo Instead of using an array where each position stores
                the id of an entity (that requires a loop over the array, it would be 
                used a HashSet (Set interface) to reduce the time
                to match the event. 
                This should be applied to the other implementations
                of the super class.
                */
		for (int id : ids) {
			if (src == id) {
				return true;
			}
		}
		return false;
	}

}
