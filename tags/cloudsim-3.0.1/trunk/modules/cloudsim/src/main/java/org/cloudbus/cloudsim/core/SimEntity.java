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

/**
 * This class represents a simulation entity. An entity handles events and can send events to other
 * entities. When this class is extended, there are a few methods that need to be implemented:
 * <ul>
 * <li> {@link #startEntity()} is invoked by the {@link Simulation} class when the simulation is
 * started. This method should be responsible for starting the entity up.
 * <li> {@link #processEvent(SimEvent)} is invoked by the {@link Simulation} class whenever there is
 * an event in the deferred queue, which needs to be processed by the entity.
 * <li> {@link #shutdownEntity()} is invoked by the {@link Simulation} before the simulation
 * finishes. If you want to save data in log files this is the method in which the corresponding
 * code would be placed.
 * </ul>
 * 
 * @author Marcos Dias de Assuncao
 * @since CloudSim Toolkit 1.0
 */
public abstract class SimEntity implements Cloneable {

	/** The name. */
	private String name;

	/** The id. */
	private int id;

	/** The buffer for selected incoming events. */
	private SimEvent evbuf;

	/** The entity's current state. */
	private int state;

	/**
	 * Creates a new entity.
	 * 
	 * @param name the name to be associated with this entity
	 */
	public SimEntity(String name) {
		if (name.indexOf(" ") != -1) {
			throw new IllegalArgumentException("Entity names can't contain spaces.");
		}
		this.name = name;
		id = -1;
		state = RUNNABLE;
		CloudSim.addEntity(this);
	}

	/**
	 * Get the name of this entity.
	 * 
	 * @return The entity's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the unique id number assigned to this entity.
	 * 
	 * @return The id number
	 */
	public int getId() {
		return id;
	}

	// The schedule functions

	/**
	 * Send an event to another entity by id number, with data. Note that the tag <code>9999</code>
	 * is reserved.
	 * 
	 * @param dest The unique id number of the destination entity
	 * @param delay How long from the current simulation time the event should be sent
	 * @param tag An user-defined number representing the type of event.
	 * @param data The data to be sent with the event.
	 */
	public void schedule(int dest, double delay, int tag, Object data) {
		if (!CloudSim.running()) {
			return;
		}
		CloudSim.send(id, dest, delay, tag, data);
	}

	/**
	 * Send an event to another entity by id number and with <b>no</b> data. Note that the tag
	 * <code>9999</code> is reserved.
	 * 
	 * @param dest The unique id number of the destination entity
	 * @param delay How long from the current simulation time the event should be sent
	 * @param tag An user-defined number representing the type of event.
	 */
	public void schedule(int dest, double delay, int tag) {
		schedule(dest, delay, tag, null);
	}

	/**
	 * Send an event to another entity through a port with a given name, with data. Note that the
	 * tag <code>9999</code> is reserved.
	 * 
	 * @param dest The name of the port to send the event through
	 * @param delay How long from the current simulation time the event should be sent
	 * @param tag An user-defined number representing the type of event.
	 * @param data The data to be sent with the event.
	 */
	public void schedule(String dest, double delay, int tag, Object data) {
		schedule(CloudSim.getEntityId(dest), delay, tag, data);
	}

	/**
	 * Send an event to another entity through a port with a given name, with <b>no</b> data. Note
	 * that the tag <code>9999</code> is reserved.
	 * 
	 * @param dest The name of the port to send the event through
	 * @param delay How long from the current simulation time the event should be sent
	 * @param tag An user-defined number representing the type of event.
	 */
	public void schedule(String dest, double delay, int tag) {
		schedule(dest, delay, tag, null);
	}

	/**
	 * Send an event to another entity by id number, with data. Note that the tag <code>9999</code>
	 * is reserved.
	 * 
	 * @param dest The unique id number of the destination entity
	 * @param tag An user-defined number representing the type of event.
	 * @param data The data to be sent with the event.
	 */
	public void scheduleNow(int dest, int tag, Object data) {
		schedule(dest, 0, tag, data);
	}

	/**
	 * Send an event to another entity by id number and with <b>no</b> data. Note that the tag
	 * <code>9999</code> is reserved.
	 * 
	 * @param dest The unique id number of the destination entity
	 * @param tag An user-defined number representing the type of event.
	 */
	public void scheduleNow(int dest, int tag) {
		schedule(dest, 0, tag, null);
	}

	/**
	 * Send an event to another entity through a port with a given name, with data. Note that the
	 * tag <code>9999</code> is reserved.
	 * 
	 * @param dest The name of the port to send the event through
	 * @param tag An user-defined number representing the type of event.
	 * @param data The data to be sent with the event.
	 */
	public void scheduleNow(String dest, int tag, Object data) {
		schedule(CloudSim.getEntityId(dest), 0, tag, data);
	}

	/**
	 * Send an event to another entity through a port with a given name, with <b>no</b> data. Note
	 * that the tag <code>9999</code> is reserved.
	 * 
	 * @param dest The name of the port to send the event through
	 * @param tag An user-defined number representing the type of event.
	 */
	public void scheduleNow(String dest, int tag) {
		schedule(dest, 0, tag, null);
	}

	/**
	 * Send a high priority event to another entity by id number, with data. Note that the tag
	 * <code>9999</code> is reserved.
	 * 
	 * @param dest The unique id number of the destination entity
	 * @param delay How long from the current simulation time the event should be sent
	 * @param tag An user-defined number representing the type of event.
	 * @param data The data to be sent with the event.
	 */
	public void scheduleFirst(int dest, double delay, int tag, Object data) {
		if (!CloudSim.running()) {
			return;
		}
		CloudSim.sendFirst(id, dest, delay, tag, data);
	}

	/**
	 * Send a high priority event to another entity by id number and with <b>no</b> data. Note that
	 * the tag <code>9999</code> is reserved.
	 * 
	 * @param dest The unique id number of the destination entity
	 * @param delay How long from the current simulation time the event should be sent
	 * @param tag An user-defined number representing the type of event.
	 */
	public void scheduleFirst(int dest, double delay, int tag) {
		scheduleFirst(dest, delay, tag, null);
	}

	/**
	 * Send a high priority event to another entity through a port with a given name, with data.
	 * Note that the tag <code>9999</code> is reserved.
	 * 
	 * @param dest The name of the port to send the event through
	 * @param delay How long from the current simulation time the event should be sent
	 * @param tag An user-defined number representing the type of event.
	 * @param data The data to be sent with the event.
	 */
	public void scheduleFirst(String dest, double delay, int tag, Object data) {
		scheduleFirst(CloudSim.getEntityId(dest), delay, tag, data);
	}

	/**
	 * Send a high priority event to another entity through a port with a given name, with <b>no</b>
	 * data. Note that the tag <code>9999</code> is reserved.
	 * 
	 * @param dest The name of the port to send the event through
	 * @param delay How long from the current simulation time the event should be sent
	 * @param tag An user-defined number representing the type of event.
	 */
	public void scheduleFirst(String dest, double delay, int tag) {
		scheduleFirst(dest, delay, tag, null);
	}

	/**
	 * Send a high priority event to another entity by id number, with data. Note that the tag
	 * <code>9999</code> is reserved.
	 * 
	 * @param dest The unique id number of the destination entity
	 * @param tag An user-defined number representing the type of event.
	 * @param data The data to be sent with the event.
	 */
	public void scheduleFirstNow(int dest, int tag, Object data) {
		scheduleFirst(dest, 0, tag, data);
	}

	/**
	 * Send a high priority event to another entity by id number and with <b>no</b> data. Note that
	 * the tag <code>9999</code> is reserved.
	 * 
	 * @param dest The unique id number of the destination entity
	 * @param tag An user-defined number representing the type of event.
	 */
	public void scheduleFirstNow(int dest, int tag) {
		scheduleFirst(dest, 0, tag, null);
	}

	/**
	 * Send a high priority event to another entity through a port with a given name, with data.
	 * Note that the tag <code>9999</code> is reserved.
	 * 
	 * @param dest The name of the port to send the event through
	 * @param tag An user-defined number representing the type of event.
	 * @param data The data to be sent with the event.
	 */
	public void scheduleFirstNow(String dest, int tag, Object data) {
		scheduleFirst(CloudSim.getEntityId(dest), 0, tag, data);
	}

	/**
	 * Send a high priority event to another entity through a port with a given name, with <b>no</b>
	 * data. Note that the tag <code>9999</code> is reserved.
	 * 
	 * @param dest The name of the port to send the event through
	 * @param tag An user-defined number representing the type of event.
	 */
	public void scheduleFirstNow(String dest, int tag) {
		scheduleFirst(dest, 0, tag, null);
	}

	/**
	 * Set the entity to be inactive for a time period.
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
	 * Count how many events matching a predicate are waiting in the entity's deferred queue.
	 * 
	 * @param p The event selection predicate
	 * @return The count of matching events
	 */
	public int numEventsWaiting(Predicate p) {
		return CloudSim.waiting(id, p);
	}

	/**
	 * Count how many events are waiting in the entity's deferred queue.
	 * 
	 * @return The count of events
	 */
	public int numEventsWaiting() {
		return CloudSim.waiting(id, CloudSim.SIM_ANY);
	}

	/**
	 * Extract the first event matching a predicate waiting in the entity's deferred queue.
	 * 
	 * @param p The event selection predicate
	 * @return the simulation event
	 */
	public SimEvent selectEvent(Predicate p) {
		if (!CloudSim.running()) {
			return null;
		}

		return CloudSim.select(id, p);
	}

	/**
	 * Cancel the first event matching a predicate waiting in the entity's future queue.
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
	 * Get the first event matching a predicate from the deferred queue, or if none match, wait for
	 * a matching event to arrive.
	 * 
	 * @param p The predicate to match
	 * @return the simulation event
	 */
	public SimEvent getNextEvent(Predicate p) {
		if (!CloudSim.running()) {
			return null;
		}
		if (numEventsWaiting(p) > 0) {
			return selectEvent(p);
		}
		return null;
	}

	/**
	 * Wait for an event matching a specific predicate. This method does not check the entity's
	 * deferred queue.
	 * 
	 * @param p The predicate to match
	 */
	public void waitForEvent(Predicate p) {
		if (!CloudSim.running()) {
			return;
		}

		CloudSim.wait(id, p);
		state = WAITING;
	}

	/**
	 * Get the first event waiting in the entity's deferred queue, or if there are none, wait for an
	 * event to arrive.
	 * 
	 * @return the simulation event
	 */
	public SimEvent getNextEvent() {
		return getNextEvent(CloudSim.SIM_ANY);
	}

	/**
	 * This method is invoked by the {@link Simulation} class when the simulation is started. This
	 * method should be responsible for starting the entity up.
	 */
	public abstract void startEntity();

	/**
	 * This method is invoked by the {@link Simulation} class whenever there is an event in the
	 * deferred queue, which needs to be processed by the entity.
	 * 
	 * @param ev the event to be processed by the entity
	 */
	public abstract void processEvent(SimEvent ev);

	/**
	 * This method is invoked by the {@link Simulation} before the simulation finishes. If you want
	 * to save data in log files this is the method in which the corresponding code would be placed.
	 */
	public abstract void shutdownEntity();

	public void run() {
		SimEvent ev = evbuf != null ? evbuf : getNextEvent();

		while (ev != null) {
			processEvent(ev);
			if (state != RUNNABLE) {
				break;
			}

			ev = getNextEvent();
		}

		evbuf = null;
	}

	/**
	 * Get a clone of the entity. This is used when independent replications have been specified as
	 * an output analysis method. Clones or backups of the entities are made in the beginning of the
	 * simulation in order to reset the entities for each subsequent replication. This method should
	 * not be called by the user.
	 * 
	 * @return A clone of the entity
	 * @throws CloneNotSupportedException the clone not supported exception
	 */
	@Override
	protected final Object clone() throws CloneNotSupportedException {
		SimEntity copy = (SimEntity) super.clone();
		copy.setName(name);
		copy.setEventBuffer(null);
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
	 * Gets the state.
	 * 
	 * @return the state
	 */
	protected int getState() {
		return state;
	}

	/**
	 * Gets the event buffer.
	 * 
	 * @return the event buffer
	 */
	protected SimEvent getEventBuffer() {
		return evbuf;
	}

	// The entity states
	/** The Constant RUNNABLE. */
	public static final int RUNNABLE = 0;

	/** The Constant WAITING. */
	public static final int WAITING = 1;

	/** The Constant HOLDING. */
	public static final int HOLDING = 2;

	/** The Constant FINISHED. */
	public static final int FINISHED = 3;

	/**
	 * Sets the state.
	 * 
	 * @param state the new state
	 */
	protected void setState(int state) {
		this.state = state;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id the new id
	 */
	protected void setId(int id) {
		this.id = id;
	}

	/**
	 * Sets the event buffer.
	 * 
	 * @param e the new event buffer
	 */
	protected void setEventBuffer(SimEvent e) {
		evbuf = e;
	}

	// --------------- EVENT / MESSAGE SEND WITH NETWORK DELAY METHODS ------------------

	/**
	 * Sends an event/message to another entity by <tt>delaying</tt> the simulation time from the
	 * current time, with a tag representing the event type.
	 * 
	 * @param entityId the id number of the destination entity
	 * @param delay how long from the current simulation time the event should be sent. If delay is
	 *            a negative number, then it will be changed to 0
	 * @param cloudSimTag an user-defined number representing the type of an event/message
	 * @param data A reference to data to be sent with the event
	 * @pre entityID > 0
	 * @pre delay >= 0.0
	 * @pre data != null
	 * @post $none
	 */
	protected void send(int entityId, double delay, int cloudSimTag, Object data) {
		if (entityId < 0) {
			return;
		}

		// if delay is -ve, then it doesn't make sense. So resets to 0.0
		if (delay < 0) {
			delay = 0;
		}

		if (Double.isInfinite(delay)) {
			throw new IllegalArgumentException("The specified delay is infinite value");
		}

		if (entityId < 0) {
			Log.printLine(getName() + ".send(): Error - " + "invalid entity id " + entityId);
			return;
		}

		int srcId = getId();
		if (entityId != srcId) {// does not delay self messages
			delay += getNetworkDelay(srcId, entityId);
		}

		schedule(entityId, delay, cloudSimTag, data);
	}

	/**
	 * Sends an event/message to another entity by <tt>delaying</tt> the simulation time from the
	 * current time, with a tag representing the event type.
	 * 
	 * @param entityId the id number of the destination entity
	 * @param delay how long from the current simulation time the event should be sent. If delay is
	 *            a negative number, then it will be changed to 0
	 * @param cloudSimTag an user-defined number representing the type of an event/message
	 * @pre entityID > 0
	 * @pre delay >= 0.0
	 * @post $none
	 */
	protected void send(int entityId, double delay, int cloudSimTag) {
		send(entityId, delay, cloudSimTag, null);
	}

	/**
	 * Sends an event/message to another entity by <tt>delaying</tt> the simulation time from the
	 * current time, with a tag representing the event type.
	 * 
	 * @param entityName the name of the destination entity
	 * @param delay how long from the current simulation time the event should be sent. If delay is
	 *            a negative number, then it will be changed to 0
	 * @param cloudSimTag an user-defined number representing the type of an event/message
	 * @param data A reference to data to be sent with the event
	 * @pre entityName != null
	 * @pre delay >= 0.0
	 * @pre data != null
	 * @post $none
	 */
	protected void send(String entityName, double delay, int cloudSimTag, Object data) {
		send(CloudSim.getEntityId(entityName), delay, cloudSimTag, data);
	}

	/**
	 * Sends an event/message to another entity by <tt>delaying</tt> the simulation time from the
	 * current time, with a tag representing the event type.
	 * 
	 * @param entityName the name of the destination entity
	 * @param delay how long from the current simulation time the event should be sent. If delay is
	 *            a negative number, then it will be changed to 0
	 * @param cloudSimTag an user-defined number representing the type of an event/message
	 * @pre entityName != null
	 * @pre delay >= 0.0
	 * @post $none
	 */
	protected void send(String entityName, double delay, int cloudSimTag) {
		send(entityName, delay, cloudSimTag, null);
	}

	/**
	 * Sends an event/message to another entity by <tt>delaying</tt> the simulation time from the
	 * current time, with a tag representing the event type.
	 * 
	 * @param entityId the id number of the destination entity
	 * @param delay how long from the current simulation time the event should be sent. If delay is
	 *            a negative number, then it will be changed to 0
	 * @param cloudSimTag an user-defined number representing the type of an event/message
	 * @param data A reference to data to be sent with the event
	 * @pre entityID > 0
	 * @pre delay >= 0.0
	 * @pre data != null
	 * @post $none
	 */
	protected void sendNow(int entityId, int cloudSimTag, Object data) {
		send(entityId, 0, cloudSimTag, data);
	}

	/**
	 * Sends an event/message to another entity by <tt>delaying</tt> the simulation time from the
	 * current time, with a tag representing the event type.
	 * 
	 * @param entityId the id number of the destination entity
	 * @param delay how long from the current simulation time the event should be sent. If delay is
	 *            a negative number, then it will be changed to 0
	 * @param cloudSimTag an user-defined number representing the type of an event/message
	 * @pre entityID > 0
	 * @pre delay >= 0.0
	 * @post $none
	 */
	protected void sendNow(int entityId, int cloudSimTag) {
		send(entityId, 0, cloudSimTag, null);
	}

	/**
	 * Sends an event/message to another entity by <tt>delaying</tt> the simulation time from the
	 * current time, with a tag representing the event type.
	 * 
	 * @param entityName the name of the destination entity
	 * @param delay how long from the current simulation time the event should be sent. If delay is
	 *            a negative number, then it will be changed to 0
	 * @param cloudSimTag an user-defined number representing the type of an event/message
	 * @param data A reference to data to be sent with the event
	 * @pre entityName != null
	 * @pre delay >= 0.0
	 * @pre data != null
	 * @post $none
	 */
	protected void sendNow(String entityName, int cloudSimTag, Object data) {
		send(CloudSim.getEntityId(entityName), 0, cloudSimTag, data);
	}

	/**
	 * Sends an event/message to another entity by <tt>delaying</tt> the simulation time from the
	 * current time, with a tag representing the event type.
	 * 
	 * @param entityName the name of the destination entity
	 * @param delay how long from the current simulation time the event should be sent. If delay is
	 *            a negative number, then it will be changed to 0
	 * @param cloudSimTag an user-defined number representing the type of an event/message
	 * @pre entityName != null
	 * @pre delay >= 0.0
	 * @post $none
	 */
	protected void sendNow(String entityName, int cloudSimTag) {
		send(entityName, 0, cloudSimTag, null);
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
