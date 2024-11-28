/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.core;

import org.cloudbus.cloudsim.Log;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * This class implements the future event queue used by {@link CloudSim}. 
 * The event queue uses a {@link TreeSet} in order to store the events.
 * 
 * @author Marcos Dias de Assuncao
 * @author Remo Andreoli
 * @since CloudSim Toolkit 1.0
 * @see java.util.TreeSet
 * 
 * //TODO It would be used a common interface for queues
 * such as this one and {@link DeferredQueue}
 */
public class FutureQueue extends TreeSet<SimEvent> {
	/** A incremental number used for event attribute */
	private long serial = 0;

	/**
	 * Adds a new event to the queue. Adding a new event to the queue preserves the temporal order of
	 * the events in the queue.
	 * 
	 * @param newEvent The event to be put in the queue.
	 */
	public void addEvent(SimEvent newEvent) {
		newEvent.setSerial(serial++);
		this.add(newEvent);
	}

	/**
	 * Adds a new event to the head of the queue.
	 * 
	 * @param newEvent The event to be put in the queue.
	 */
	public void addEventFirst(SimEvent newEvent) {
		newEvent.setSerial(0);
		this.add(newEvent);
	}

	public void print() {
		Iterator<SimEvent> iter = iterator();
		int i = 0;
		while(iter.hasNext()) {
			Log.printlnConcat("[", i, "] -> ", iter.next().toString());
			i++;
		}
	}
}