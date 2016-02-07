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
 * A predicate to select events with specific tags.
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 * @see PredicateNotType
 * @see Predicate
 */
public class PredicateType extends Predicate {

	/** Array of tags to verify if the tag of received events correspond to. */
	private final int[] tags;

	/**
	 * Constructor used to select events with the given tag value.
	 * 
	 * @param t1 an event tag value
	 */
	public PredicateType(int t1) {
		tags = new int[] { t1 };
	}

	/**
	 * Constructor used to select events with a tag value equal to any of the specified tags.
	 * 
	 * @param tags the list of tags
	 */
	public PredicateType(int[] tags) {
		this.tags = tags.clone();
	}

	/**
	 * Matches any event that has one of the specified {@link #tags}.
	 * 
	 * @param ev {@inheritDoc}
	 * @return {@inheritDoc}
         * @see #tags
	 */
	@Override
	public boolean match(SimEvent ev) {
		int tag = ev.getTag();
		for (int tag2 : tags) {
			if (tag == tag2) {
				return true;
			}
		}
		return false;
	}

}
