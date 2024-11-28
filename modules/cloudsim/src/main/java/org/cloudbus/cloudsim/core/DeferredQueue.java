/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.core;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * This class implements the deferred event queue used by {@link CloudSim}. 
 * The event queue uses a linked list to store the events.
 * 
 * @author Marcos Dias de Assuncao
 * @author Remo Andreoli
 * @since CloudSim Toolkit 1.0
 * @see CloudSim
 * @see SimEvent
 *
 * @TODO: Why is this a linked-list that tries to preserve temporal order?
 *  this should probably be replaced with a priority list, or we should simply remove the temporal preservation logic,
 *  given that FutureQueue guarantees that events are added in here in temporal order.
 */
public class DeferredQueue extends LinkedList<SimEvent> {
	/** The max time that an added event is scheduled. */
	private double maxTime = -1;

	/**
	 * Adds a new event to the queue. Adding a new event to the queue preserves the temporal order
	 * of the events.
	 * 
	 * @param newEvent The event to be added to the queue.
	 */
	public void addEvent(SimEvent newEvent) {
		// The event has to be inserted as the last of all events
		// with the same event_time(). Yes, this matters.
		double eventTime = newEvent.eventTime();
		if (eventTime >= maxTime) {
			this.add(newEvent);
			maxTime = eventTime;
			return;
		}

		ListIterator<SimEvent> iterator = this.listIterator();
		SimEvent event;
		while (iterator.hasNext()) {
			event = iterator.next();
			if (event.eventTime() > eventTime) {
				iterator.previous();
				iterator.add(newEvent);
				return;
			}
		}

		this.add(newEvent);
	}
}