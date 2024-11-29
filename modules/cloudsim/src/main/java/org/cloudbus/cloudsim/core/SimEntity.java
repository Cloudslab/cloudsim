/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.core;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.NetworkTopology;
import org.cloudbus.cloudsim.core.predicates.Predicate;

import java.util.Iterator;

/**
 * This class represents a simulation entity. An entity handles events and can send events to other
 * entities. When this class is extended, there are a few methods that need to be implemented:
 * <ul>
 * <li> {@link #startEntity()} is invoked by the {@link CloudSim} class when the simulation is
 * started. This method should be responsible for starting the entity up.
 * <li> {@link #processEvent(SimEvent)} is invoked by the {@link CloudSim} class whenever there is
 * an event in the deferred queue, which needs to be processed by the entity.
 * <li> {@link #shutdownEntity()} is invoked by the {@link CloudSim} before the simulation
 * finishes. If you want to save data in log files this is the method in which the corresponding
 * code would be placed.
 * </ul>
 * 
 * @author Marcos Dias de Assuncao
 * @author Remo Andreoli
 * @since CloudSim Toolkit 1.0
 */
public abstract class SimEntity implements Cloneable {
	/** Entity states */
	public enum EntityStatus {
		RUNNABLE,
		WAITING,
		HOLDING,
		FINISHED
	}

	private String name;
	private int id;
	private EntityStatus state;
	protected EventQueue incomingEvents;

	/**
	 * Creates a new entity.
	 * 
	 * @param name the name to be associated with the entity
	 */
	public SimEntity(String name) {
		if (name.contains(" ")) {
			throw new IllegalArgumentException("Entity names can't contain spaces.");
		}
		this.name = name;
		id = -1;
		state = EntityStatus.RUNNABLE;
		incomingEvents = new EventQueue();

		CloudSim.addEntity(this);
	}

	/**
	 * Gets the name of this entity.
	 * 
	 * @return The entity's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the unique id number assigned to this entity.
	 * 
	 * @return The id number
	 */
	public int getId() {
		return id;
	}

	public EventQueue getIncomingEvents() {
		return incomingEvents;
	}

	/** Handle incoming event functions */
	/**
	 * Checks if events for a specific entity are present in the deferred event queue.
	 *
	 * @param p the p
	 * @return the int
	 */
	public int waiting(Predicate p) {
		int count = 0;
		for (SimEvent event : incomingEvents) {
			if ((event.getDestinationId() == id) && (p.match(event))) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Selects an event matching a predicate.
	 *
	 * @param p the p
	 * @return the sim event
	 */
	public SimEvent selectEvent(Predicate p) {
		if (!CloudSim.running()) {
			return null;
		}

		SimEvent ev;
		Iterator<SimEvent> iterator = incomingEvents.iterator();
		while (iterator.hasNext()) {
			ev = iterator.next();
			if (ev.getDestinationId() == id && p.match(ev)) {
				iterator.remove();
				return ev;
			}
		}
		return null;
	}

	/**
	 * Find first deferred event matching a predicate.
	 *
	 * @param p the p
	 * @return the sim event
	 */
	public SimEvent findFirstDeferred(Predicate p) {
		SimEvent ev;
		for (SimEvent simEvent : incomingEvents) {
			ev = simEvent;
			if (ev.getDestinationId() == id && p.match(ev)) {
				return ev;
			}
		}
		return null;
	}

	/** Schedule event functions */
	/**
	 * Sends an event to another entity by id number, with data.
	 * 
	 * @param dstId The unique id number of the destination entity
	 * @param delay How long from the current simulation time the event should be sent
	 * @param tag event type.
	 * @param data The data to be sent with the event.
	 */
	public void schedule(int dstId, double delay, CloudSimTags tag, Object data) {
		if (!CloudSim.running()) {
			return;
		}
		CloudSim.send(id, dstId, delay, tag, data);
	}

	/**
	 * Sends an event to another entity by id number and with <b>no</b> data.
	 * 
	 * @param dstId The unique id number of the destination entity
	 * @param delay How long from the current simulation time the event should be sent
	 * @param tag event type.
	 */
	public void schedule(int dstId, double delay, CloudSimTags tag) {
		schedule(dstId, delay, tag, null);
	}

	/**
	 * Sends an event to another entity through a port with a given name, with data.
	 * 
	 * @param dstName The name of the port to send the event through
	 * @param delay How long from the current simulation time the event should be sent
	 * @param tag event type.
	 * @param data The data to be sent with the event.
	 */
	public void schedule(String dstName, double delay, CloudSimTags tag, Object data) {
		schedule(CloudSim.getEntityId(dstName), delay, tag, data);
	}

	/**
	 * Sends an event to another entity through a port with a given name, with <b>no</b> data.
	 * 
	 * @param dstName The name of the port to send the event through
	 * @param delay How long from the current simulation time the event should be sent
	 * @param tag event type.
	 */
	public void schedule(String dstName, double delay, CloudSimTags tag) {
		schedule(dstName, delay, tag, null);
	}

	/**
	 * Sends an event to another entity by id number, with data.
	 * 
	 * @param dstId The unique id number of the destination entity
	 * @param tag event type.
	 * @param data The data to be sent with the event.
	 */
	public void scheduleNow(int dstId, CloudSimTags tag, Object data) {
		schedule(dstId, 0, tag, data);
	}

	/**
	 * Sends an event to another entity by id number and with <b>no</b> data
         * and no delay.
	 * 
	 * @param dstId The unique id number of the destination entity
	 * @param tag event type.
	 */
	public void scheduleNow(int dstId, CloudSimTags tag) {
		schedule(dstId, 0, tag, null);
	}

	/**
	 * Sends an event to another entity through a port with a given name, with data
         * but no delay.
	 * 
	 * @param dstName The name of the port to send the event through
	 * @param tag event type.
	 * @param data The data to be sent with the event.
	 */
	public void scheduleNow(String dstName, CloudSimTags tag, Object data) {
		schedule(CloudSim.getEntityId(dstName), 0, tag, data);
	}

	/**
	 * Send an event to another entity through a port with a given name, with <b>no</b> data
         * and no delay.
	 * 
	 * @param dstName The name of the port to send the event through
	 * @param tag event type.
	 */
	public void scheduleNow(String dstName, CloudSimTags tag) {
		schedule(dstName, 0, tag, null);
	}

	/**
	 * Sends a high priority event to another entity by id number, with data.
	 * 
	 * @param dstId The unique id number of the destination entity
	 * @param delay How long from the current simulation time the event should be sent
	 * @param tag event type.
	 * @param data The data to be sent with the event.
	 */
	public void scheduleFirst(int dstId, double delay, CloudSimTags tag, Object data) {
		if (!CloudSim.running()) {
			return;
		}
		CloudSim.sendFirst(id, dstId, delay, tag, data);
	}

	/**
	 * Sends a high priority event to another entity by id number and with <b>no</b> data.
	 * 
	 * @param dstId The unique id number of the destination entity
	 * @param delay How long from the current simulation time the event should be sent
	 * @param tag event type.
	 */
	public void scheduleFirst(int dstId, double delay, CloudSimTags tag) {
		scheduleFirst(dstId, delay, tag, null);
	}

	/**
	 * Sends a high priority event to another entity through a port with a given name, with data.
	 * 
	 * @param dstName The name of the port to send the event through
	 * @param delay How long from the current simulation time the event should be sent
	 * @param tag event type.
	 * @param data The data to be sent with the event.
	 */
	public void scheduleFirst(String dstName, double delay, CloudSimTags tag, Object data) {
		scheduleFirst(CloudSim.getEntityId(dstName), delay, tag, data);
	}

	/**
	 * Sends a high priority event to another entity through a port with a given name, with <b>no</b>
	 * data.
	 * 
	 * @param dstName The name of the port to send the event through
	 * @param delay How long from the current simulation time the event should be sent
	 * @param tag event type.
	 */
	public void scheduleFirst(String dstName, double delay, CloudSimTags tag) {
		scheduleFirst(dstName, delay, tag, null);
	}

	/**
	 * Sends a high priority event to another entity by id number, with data
         * and no delay.
	 * 
	 * @param dstId The unique id number of the destination entity
	 * @param tag event type.
	 * @param data The data to be sent with the event.
	 */
	public void scheduleFirstNow(int dstId, CloudSimTags tag, Object data) {
		scheduleFirst(dstId, 0, tag, data);
	}

	/**
	 * Sends a high priority event to another entity by id number and with <b>no</b> data
         * and no delay.
	 * 
	 * @param dstId The unique id number of the destination entity
	 * @param tag event type.
	 */
	public void scheduleFirstNow(int dstId, CloudSimTags tag) {
		scheduleFirst(dstId, 0, tag, null);
	}

	/**
	 * Sends a high priority event to another entity through a port with a given name, with data
         * and no delay.
	 * 
	 * @param dstName The name of the port to send the event through
	 * @param tag event type.
	 * @param data The data to be sent with the event.
	 */
	public void scheduleFirstNow(String dstName, CloudSimTags tag, Object data) {
		scheduleFirst(CloudSim.getEntityId(dstName), 0, tag, data);
	}

	/**
	 * Sends a high priority event to another entity through a port with a given name, with <b>no</b>
	 * data and no delay.
	 * 
	 * @param dstName The name of the port to send the event through
	 * @param tag event type.
	 */
	public void scheduleFirstNow(String dstName, CloudSimTags tag) {
		scheduleFirst(dstName, 0, tag, null);
	}

	/**
	 * Sets the entity to be inactive for a time period.
	 * 
	 * @param delay the time period for which the entity will be inactive
	 */
	public void pause(double delay) {
		if (delay < 0) {
			throw new IllegalArgumentException("Negative delay supplied.");
		}
		if (!CloudSim.running()) {
			return;
		}
		CloudSim.pause(id, delay);
	}

	/**
	 * Counts how many events matching a predicate are waiting in the entity's deferred queue.
	 *
	 * @param p The event selection predicate
	 * @return The count of matching events
	 */
	@Deprecated
	public int numEventsWaiting(Predicate p) {
		return waiting(p);
	}

	/**
	 * Counts how many events are waiting in the entity's deferred queue.
	 *
	 * @return The count of events
	 */
	@Deprecated
	public int numEventsWaiting() {
		return waiting(CloudSim.SIM_ANY);
	}

	/**
	 * Cancels the first event matching a predicate waiting in the entity's future queue.
	 * 
	 * @param p The event selection predicate
	 * @return The number of events cancelled (0 or 1)
	 */
	public SimEvent cancelEvent(Predicate p) {
		if (!CloudSim.running()) {
			return null;
		}

		return CloudSim.cancel(id, p);
	}

	/**
	 * Gets the first event matching a predicate from the deferred queue, or if none match, wait for
	 * a matching event to arrive.
	 * 
	 * @param p The predicate to match
	 * @return the simulation event
	 */
	public SimEvent getNextEvent(Predicate p) {
		if (!CloudSim.running()) {
			return null;
		}
		return selectEvent(p);
	}

	/**
	 * Waits for an event matching a specific predicate. This method does not check the entity's
	 * deferred queue.
	 * 
	 * @param p The predicate to match
	 */
	public void waitForEvent(Predicate p) {
		if (!CloudSim.running()) {
			return;
		}

		CloudSim.wait(id, p);
		state = EntityStatus.WAITING;
	}

	/**
	 * This method is invoked by the {@link CloudSim} class when the simulation is started. 
	 * It should be responsible for starting the entity up.
	 */
	public void startEntity() {
		Log.printlnConcat(CloudSim.clock(), ": ", getName(), " is starting...");
	}

	/**
	 * Processes events or services that are available for the entity.
	 * This method is invoked by the {@link CloudSim} class whenever there is an event in the
	 * deferred queue, which needs to be processed by the entity.
	 * 
	 * @param ev information about the event just happened
	 */
	public abstract void processEvent(SimEvent ev);

	/**
         * Shuts down the entity.
	 * This method is invoked by the {@link CloudSim} before the simulation finishes. If you want
	 * to save data in log files this is the method in which the corresponding code would be placed.
	 */
	public void shutdownEntity() {
		Log.printlnConcat(CloudSim.clock(), ": ", getName(), " is shutting down...");
		incomingEvents = null;
		state = EntityStatus.FINISHED;
	}

	/**
	 * The run loop to process events fired during the simulation.
	 * The events that will be processed are defined
	 * in the {@link #processEvent(org.cloudbus.cloudsim.core.SimEvent)} method.
	 *
	 * @see #processEvent(org.cloudbus.cloudsim.core.SimEvent)
	 */
	public void run() {
		SimEvent ev =  incomingEvents.poll();

		while (ev != null) {
			processEvent(ev);
			if (state != EntityStatus.RUNNABLE) {
				break;
			}
			ev = incomingEvents.poll();
		}
	}

	/**
	 * Gets a clone of the entity. This is used when independent replications have been specified as
	 * an output analysis method. Clones or backups of the entities are made in the beginning of the
	 * simulation in order to reset the entities for each subsequent replication. This method should
	 * not be called by the user.
	 * 
	 * @return A clone of the entity
	 * @throws CloneNotSupportedException when the entity doesn't support cloning
	 */
	@Override
	protected final Object clone() throws CloneNotSupportedException {
		SimEntity copy = (SimEntity) super.clone();
		copy.setName(name);
		return copy;
	}

	// Used to set a cloned entity's name
	/**
	 * Sets the name.
	 * 
	 * @param new_name the new name
	 */
	private void setName(String new_name) {
		name = new_name;
	}

	// --------------- PACKAGE LEVEL METHODS ------------------

	/**
	 * Gets the entity state.
	 * 
	 * @return the state
	 */
	protected EntityStatus getState() {
		return state;
	}

	/**
	 * Sets the entity state.
	 * 
	 * @param state the new state
	 */
	protected void setState(EntityStatus state) {
		this.state = state;
	}

	/**
	 * Sets the entity id.
	 * 
	 * @param id the new id
	 */
	protected void setId(int id) {
		this.id = id;
	}

	// --------------- EVENT / MESSAGE SEND WITH NETWORK DELAY METHODS ------------------

	/**
	 * Sends an event/message to another entity by <tt>delaying</tt> the simulation time from the
	 * current time, with a tag representing the event type.
	 * 
	 * @param dstId the id number of the destination entity
	 * @param delay how long from the current simulation time the event should be sent. If delay is
	 *            a negative number, then it will be changed to 0
	 * @param cloudSimTag an user-defined number representing the type of an event/message
	 * @param data A reference to data to be sent with the event
	 * @pre dstId > 0
	 * @pre delay >= 0.0
	 * @pre data != null
	 * @post $none
	 */
	protected void send(int dstId, double delay, CloudSimTags cloudSimTag, Object data) {
		if (dstId < 0) {
			return;
		}

		// if delay is -ve, then it doesn't make sense. So resets to 0.0
		if (delay < 0) {
			delay = 0;
		}

		if (Double.isInfinite(delay)) {
			throw new IllegalArgumentException("The specified delay is infinite value");
		}

		if (dstId < 0) {
			Log.printlnConcat(getName(), ".send(): Error - invalid entity id ", dstId);
			return;
		}

		int srcId = getId();
		if (dstId != srcId) {// only delay messages between different entities
			delay += getNetworkDelay(srcId, dstId);
		}

		schedule(dstId, delay, cloudSimTag, data);
	}

	/**
	 * Sends an event/message to another entity by <tt>delaying</tt> the simulation time from the
	 * current time, with a tag representing the event type.
	 *
	 * @param dstId the id number of the destination entity
	 * @param delay    how long from the current simulation time the event should be sent. If delay is
	 *                 a negative number, then it will be changed to 0
	 * @param tag      an user-defined number representing the type of an event/message
	 * @pre dstId > 0
	 * @pre delay >= 0.0
	 * @post $none
	 */
	protected void send(int dstId, double delay, CloudSimTags tag) {
		send(dstId, delay, tag, null);
	}

	/**
	 * Sends an event/message to another entity by <tt>delaying</tt> the simulation time from the
	 * current time, with a tag representing the event type.
	 *
	 * @param dstName the name of the destination entity
	 * @param delay      how long from the current simulation time the event should be sent. If delay is
	 *                   a negative number, then it will be changed to 0
	 * @param tag        an user-defined number representing the type of an event/message
	 * @param data       A reference to data to be sent with the event
	 * @pre entityName != null
	 * @pre delay >= 0.0
	 * @pre data != null
	 * @post $none
	 */
	protected void send(String dstName, double delay, CloudSimTags tag, Object data) {
		send(CloudSim.getEntityId(dstName), delay, tag, data);
	}

	/**
	 * Sends an event/message to another entity by <tt>delaying</tt> the simulation time from the
	 * current time, with a tag representing the event type.
	 *
	 * @param dstName the name of the destination entity
	 * @param delay      how long from the current simulation time the event should be sent. If delay is
	 *                   a negative number, then it will be changed to 0
	 * @param tag        an user-defined number representing the type of an event/message
	 * @pre entityName != null
	 * @pre delay >= 0.0
	 * @post $none
	 */
	protected void send(String dstName, double delay, CloudSimTags tag) {
		send(dstName, delay, tag, null);
	}

	/**
	 * Sends an event/message to another entity, with a tag representing the event type.
	 *
	 * @param dstId the id number of the destination entity
	 * @param tag      an user-defined number representing the type of an event/message
	 * @param data     A reference to data to be sent with the event
	 * @pre dstId > 0
	 * @pre delay >= 0.0
	 * @pre data != null
	 * @post $none
	 */
	protected void sendNow(int dstId, CloudSimTags tag, Object data) {
		send(dstId, 0, tag, data);
	}

	/**
	 * Sends an event/message to another entity, with a tag representing the event type.
	 *
	 * @param dstId the id number of the destination entity
	 * @param tag      an user-defined number representing the type of an event/message
	 * @pre dstId > 0
	 * @pre delay >= 0.0
	 * @post $none
	 */
	protected void sendNow(int dstId, CloudSimTags tag) {
		send(dstId, 0, tag, null);
	}

	/**
	 * Sends an event/message to another entity, with a tag representing the event type.
	 *
	 * @param dstId the name of the destination entity
	 * @param tag        an user-defined number representing the type of an event/message
	 * @param data       A reference to data to be sent with the event
	 * @pre entityName != null
	 * @pre delay >= 0.0
	 * @pre data != null
	 * @post $none
	 */
	protected void sendNow(String dstId, CloudSimTags tag, Object data) {
		send(CloudSim.getEntityId(dstId), 0, tag, data);
	}

	/**
	 * Sends an event/message to another entity, with a tag representing the event type.
	 *
	 * @param dstName the name of the destination entity
	 * @param tag        an user-defined number representing the type of an event/message
	 * @pre entityName != null
	 * @pre delay >= 0.0
	 * @post $none
	 */
	protected void sendNow(String dstName, CloudSimTags tag) {
		send(dstName, 0, tag, null);
	}

	/**
	 * Gets the network delay associated to the sent of a message from a given source to a given
	 * destination.
	 * 
	 * @param src source of the message
	 * @param dst destination of the message
	 * @return delay to send a message from src to dst
	 * @pre src >= 0
	 * @pre dst >= 0
	 */
	private double getNetworkDelay(int src, int dst) {
		if (NetworkTopology.isNetworkEnabled()) {
			return NetworkTopology.getDelay(src, dst);
		}
		return 0.0;
	}

}
