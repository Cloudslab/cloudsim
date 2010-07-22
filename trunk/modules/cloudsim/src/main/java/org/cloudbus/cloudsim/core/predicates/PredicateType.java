package org.cloudbus.cloudsim.core.predicates;

import org.cloudbus.cloudsim.core.SimEvent;

/**
 * A predicate to select events with specific tags.
 *
 * @see PredicateNotType
 * @see Predicate
 * @author Marcos Dias de Assuncao
 */

public class PredicateType extends Predicate {

  /** The tags. */
  private final int[] tags;

	/**
	 * Constructor used to select events with the tag value <code>t1</code>.
	 *
	 * @param t1 an event tag value
	 */
	public PredicateType(int t1) {
		tags = new int[] { t1 };
	}

	/**
	 * Constructor used to select events with a tag value equal to any of the
	 * specified tags.
	 *
	 * @param tags the list of tags
	 */
	public PredicateType(int[] tags) {
		this.tags = tags.clone();
	}

	/**
	 * The match function called by <code>Sim_system</code>, not used directly
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
				return true;
			}
		}
		return false;
	}

}
