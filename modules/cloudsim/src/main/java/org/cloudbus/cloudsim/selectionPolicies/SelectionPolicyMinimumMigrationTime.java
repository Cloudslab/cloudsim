/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.selectionPolicies;

import java.util.List;
import java.util.Set;

import org.cloudbus.cloudsim.core.GuestEntity;

/**
 * A VM selection policy that selects for migration the VM with Minimum Migration Time (MMT).
 * 
 * <br/>If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:<br/>
 * 
 * <ul>
 * <li><a href="http://dx.doi.org/10.1002/cpe.1867">Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley &amp; Sons, Ltd, New York, USA, 2012</a>
 * </ul>
 * 
 * @author Anton Beloglazov
 * @author Remo Andreoli
 * @since CloudSim Toolkit 3.0
 */
public class SelectionPolicyMinimumMigrationTime<T extends GuestEntity> implements SelectionPolicy<T> {
	// @TODO: how does it compute the MMT???
	@Override
	public T select(List<T> candidates, Object obj, Set<T> excludedCandidates) {
		if (candidates.isEmpty()) {
			return null;
		}

		T selectedGuest = null;
		double minMetric = Double.MAX_VALUE;

		for (T guest : candidates) {
			if (guest.isInMigration()) {
				continue;
			}
			double metric = guest.getRam();
			if (metric < minMetric) {
				minMetric = metric;
				selectedGuest = guest;
			}
		}
		return selectedGuest;
	}
}
