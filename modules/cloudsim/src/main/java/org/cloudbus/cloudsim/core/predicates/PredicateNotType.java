package org.cloudbus.cloudsim.core.predicates;

import org.cloudbus.cloudsim.core.SimEvent;

/**
 * A predicate to select events that don't match specific tags.
 *
 * @see PredicateType
 * @see Predicate
 * @author Marcos Dias de Assuncao
 */

public class PredicateNotType extends Predicate {

  /** The tags. */
  private final int[] tags;

	/**
	 * Constructor used to select events whose tags do not match a given tag.
	 *
	 * @param tag An event tag value
	 */
	public PredicateNotType(int tag) {
		tags = new int[] { tag };
	}

	/**
	 * Constructor used to select events whose tag values do not match any of the
	 * given tags.
	 *
	 * @param tags the list of tags
	 */
	public PredicateNotType(int[] tags) {
		this.tags = tags.clone();
	}

	/**
	 * The match function called by {@link Simulation}, not used directly
	 * by the user.
	 *
	 * @param ev the ev
	 *
	 * @return true, if match
	 */
	@Override
	public boolean match(SimEvent ev) {
		int tag = ev.getTag();
		for (int i = 0; i < tags.length; i++) {
			if (tag == tags[i]) {
				return false;
			}
		}
		return true;
	}

}