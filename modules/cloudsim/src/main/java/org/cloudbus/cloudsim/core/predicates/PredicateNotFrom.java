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
 * A predicate which selects events that have not been sent by specific entities.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 * @see PredicateFrom
 * @see Predicate
 */
public class PredicateNotFrom extends Predicate {

	/** The IDs of source entities to check if events were not sent from. */
	private final int[] ids;

	/**
	 * Constructor used to select events that were not sent by a specific entity.
	 * 
	 * @param sourceId the id number of the source entity
	 */
	public PredicateNotFrom(int sourceId) {
		ids = new int[] { sourceId };
	}

	/**
	 * Constructor used to select events that were not sent by any entity from a given set.
	 * 
	 * @param sourceIds the set of id numbers of the source entities
	 */
	public PredicateNotFrom(int[] sourceIds) {
		ids = sourceIds.clone();
	}

	/**
	 * Matches any event <b>not</b> received from the registered sources.
	 * 
	 * @param ev {@inheritDoc}
	 * @return {@inheritDoc}
         * @see #ids
	 */
	@Override
	public boolean match(SimEvent ev) {
		int src = ev.getSource();
		for (int id : ids) {
			if (src == id) {
				return false;
			}
		}
		return true;
	}

}
