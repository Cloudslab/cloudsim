package org.cloudbus.cloudsim.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class implements the future event queue used by {@link Simulation}.
 * The event queue uses a {@link TreeSet} in order to store the events.
 *
 * @see Simulation
 * @see java.util.TreeSet
 * @author Marcos Dias de Assuncao
 */
public class FutureQueue {

	/** The sorted set. */
	private final SortedSet<SimEvent> sortedSet = new TreeSet<SimEvent>();

	/** The serial. */
	private long serial = 0;

	/**
	 * Add a new event to the queue. Adding a new event to the queue
	 * preserves the temporal order of the events in the queue.
	 *
	 * @param newEvent The event to be put in the queue.
	 */
	public void addEvent(SimEvent newEvent) {
		newEvent.setSerial(serial++);
		sortedSet.add(newEvent);
	}

	/**
	 * Returns an iterator to the queue.
	 *
	 * @return the iterator
	 */
	public Iterator<SimEvent> iterator() {
		return sortedSet.iterator();
	}

	/**
	 * Returns the size of this event queue.
	 *
	 * @return the size
	 */
	public int size() {
		return sortedSet.size();
	}

	/**
	 * Removes the event from the queue.
	 *
	 * @param event the event
	 *
	 * @return true, if successful
	 */
	public boolean remove(SimEvent event) {
		return sortedSet.remove(event);
	}

	/**
	 * Removes all the events from the queue.
	 *
	 * @param events the events
	 *
	 * @return true, if successful
	 */
	public boolean removeAll(Collection<SimEvent> events) {
		return sortedSet.removeAll(events);
	}

	/**
	 * Clears the queue.
	 */
	public void clear(){
		sortedSet.clear();
	}

}