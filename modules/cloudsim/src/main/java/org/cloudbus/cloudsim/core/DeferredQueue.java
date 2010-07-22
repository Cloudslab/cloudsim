package org.cloudbus.cloudsim.core;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * This class implements the deferred event queue used by {@link Simulation}.
 * The event queue uses a linked list to store the events.
 *
 * @see Simulation
 * @see SimEvent
 * @author Marcos Dias de Assuncao
 */
public class DeferredQueue {

	/** The list. */
	private final List<SimEvent> list = new LinkedList<SimEvent>();

	/** The max time. */
	private double maxTime = -1;

	/**
	 * Adds a new event to the queue. Adding a new event to the queue
	 * preserves the temporal order of the events.
	 *
	 * @param newEvent The event to be added to the queue.
	 */
	public void addEvent(SimEvent newEvent) {
		// The event has to be inserted as the last of all events
		// with the same event_time(). Yes, this matters.
		double eventTime = newEvent.eventTime();
		if (eventTime >= maxTime) {
			list.add(newEvent);
			maxTime = eventTime;
			return;
		}

		ListIterator<SimEvent> iterator = list.listIterator();
		SimEvent event;
		while (iterator.hasNext()) {
			event = iterator.next();
			if (event.eventTime() > eventTime) {
				iterator.previous();
				iterator.add(newEvent);
				return;
			}
		}

	    list.add(newEvent);
	}

	/**
	 * Returns an iterator to the events in the queue.
	 *
	 * @return the iterator
	 */
	public Iterator<SimEvent> iterator() {
		return list.iterator();
	}

	/**
	 * Returns the size of this event queue.
	 *
	 * @return the number of events in the queue.
	 */
	public int size() {
		return list.size();
	}

	/**
	 * Clears the queue.
	 */
	public void clear(){
		list.clear();
	}

}