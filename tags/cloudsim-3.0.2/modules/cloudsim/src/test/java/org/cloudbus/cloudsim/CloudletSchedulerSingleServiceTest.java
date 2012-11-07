/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * @author		Anton Beloglazov
 * @since		CloudSim Toolkit 2.0
 */
public class CloudletSchedulerSingleServiceTest {

	private static final long CLOUDLET_LENGTH = 1000;
	private static final long CLOUDLET_FILE_SIZE = 300;
	private static final long CLOUDLET_OUTPUT_SIZE = 300;

	private static final double MIPS = 1000;
	private static final int PES_NUMBER = 2;

	private CloudletSchedulerDynamicWorkload vmScheduler;

	@Before
	public void setUp() throws Exception {
		vmScheduler = new CloudletSchedulerDynamicWorkload(MIPS, PES_NUMBER);
	}

	@Test
	public void testGetNumberOfPes() {
		assertEquals(PES_NUMBER, vmScheduler.getNumberOfPes());
	}

	@Test
	public void testGetMips() {
		assertEquals(MIPS, vmScheduler.getMips(), 0);
	}

	@Test
	public void testGetUnderAllocatedMips() {
		UtilizationModelStochastic utilizationModel = new UtilizationModelStochastic();
		Cloudlet cloudlet = new Cloudlet(0, CLOUDLET_LENGTH, PES_NUMBER, CLOUDLET_FILE_SIZE, CLOUDLET_OUTPUT_SIZE,
				utilizationModel, utilizationModel, utilizationModel);
		ResCloudlet rcl = new ResCloudlet(cloudlet);

		Map<String, Double> underAllocatedMips = new HashMap<String, Double>();
		assertEquals(underAllocatedMips, vmScheduler.getUnderAllocatedMips());

		underAllocatedMips.put(rcl.getUid(), MIPS / 2);
		vmScheduler.updateUnderAllocatedMipsForCloudlet(rcl, MIPS / 2);
		assertEquals(underAllocatedMips, vmScheduler.getUnderAllocatedMips());

		underAllocatedMips.put(rcl.getUid(), MIPS);
		vmScheduler.updateUnderAllocatedMipsForCloudlet(rcl, MIPS / 2);
		assertEquals(underAllocatedMips, vmScheduler.getUnderAllocatedMips());
	}

	@Test
	public void testGetCurrentRequestedMips() {
		UtilizationModelStochastic utilizationModel = new UtilizationModelStochastic();
		Cloudlet cloudlet = new Cloudlet(0, CLOUDLET_LENGTH, PES_NUMBER, CLOUDLET_FILE_SIZE, CLOUDLET_OUTPUT_SIZE,
				utilizationModel, utilizationModel, utilizationModel);
		cloudlet.setResourceParameter(0, 0, 0);

		List<Double> mipsShare = new ArrayList<Double>();
		mipsShare.add(MIPS);
		mipsShare.add(MIPS);
		vmScheduler.setCurrentMipsShare(mipsShare);

		assertEquals(mipsShare.size(), vmScheduler.getCurrentMipsShare().size(), 0);
		assertEquals(mipsShare.get(0), vmScheduler.getCurrentMipsShare().get(0), 0);
		assertEquals(mipsShare.get(1), vmScheduler.getCurrentMipsShare().get(1), 0);

		double utilization = utilizationModel.getUtilization(0);

		vmScheduler.cloudletSubmit(cloudlet);

		List<Double> requestedMips = new ArrayList<Double>();
		requestedMips.add(MIPS * utilization);
		requestedMips.add(MIPS * utilization);

		assertEquals(requestedMips, vmScheduler.getCurrentRequestedMips());
	}

	@Test
	public void testGetTotalUtilization() {
		UtilizationModelStochastic utilizationModel = new UtilizationModelStochastic();
		Cloudlet cloudlet = new Cloudlet(0, CLOUDLET_LENGTH, PES_NUMBER, CLOUDLET_FILE_SIZE, CLOUDLET_OUTPUT_SIZE,
				utilizationModel, utilizationModel, utilizationModel);
		cloudlet.setResourceParameter(0, 0, 0);

		List<Double> mipsShare = new ArrayList<Double>();
		mipsShare.add(MIPS);
		mipsShare.add(MIPS);
		vmScheduler.setCurrentMipsShare(mipsShare);

		assertEquals(mipsShare.size(), vmScheduler.getCurrentMipsShare().size(), 0);
		assertEquals(mipsShare.get(0), vmScheduler.getCurrentMipsShare().get(0), 0);
		assertEquals(mipsShare.get(1), vmScheduler.getCurrentMipsShare().get(1), 0);

		double utilization = utilizationModel.getUtilization(0);

		vmScheduler.cloudletSubmit(cloudlet, 0);

		assertEquals(utilization, vmScheduler.getTotalUtilizationOfCpu(0), 0);
	}

	@Test
	public void testCloudletFinish() {
		UtilizationModelStochastic utilizationModel = new UtilizationModelStochastic();
		Cloudlet cloudlet = new Cloudlet(0, CLOUDLET_LENGTH, PES_NUMBER, CLOUDLET_FILE_SIZE, CLOUDLET_OUTPUT_SIZE,
				utilizationModel, utilizationModel, utilizationModel);
		cloudlet.setResourceParameter(0, 0, 0);

		List<Double> mipsShare = new ArrayList<Double>();
		mipsShare.add(MIPS);
		mipsShare.add(MIPS);
		vmScheduler.setCurrentMipsShare(mipsShare);

		vmScheduler.cloudletSubmit(cloudlet, 0);
		vmScheduler.cloudletFinish(new ResCloudlet(cloudlet));

		assertEquals(Cloudlet.SUCCESS, vmScheduler.getCloudletStatus(0));
		assertTrue(vmScheduler.isFinishedCloudlets());
		assertSame(cloudlet, vmScheduler.getNextFinishedCloudlet());
	}

	@Test
	public void testGetTotalCurrentMips() {
		List<Double> mipsShare = new ArrayList<Double>();
		mipsShare.add(MIPS / 4);
		mipsShare.add(MIPS / 4);
		vmScheduler.setCurrentMipsShare(mipsShare);

		assertEquals(MIPS / 2, vmScheduler.getTotalCurrentMips(), 0);
	}

	@Test
	public void testGetTotalCurrentMipsForCloudlet() {
		UtilizationModelStochastic utilizationModel = new UtilizationModelStochastic();
		Cloudlet cloudlet = new Cloudlet(0, CLOUDLET_LENGTH, PES_NUMBER, CLOUDLET_FILE_SIZE, CLOUDLET_OUTPUT_SIZE,
				utilizationModel, utilizationModel, utilizationModel);
		cloudlet.setResourceParameter(0, 0, 0);
		ResCloudlet rgl = new ResCloudlet(cloudlet);

		List<Double> mipsShare = new ArrayList<Double>();
		mipsShare.add(MIPS / 4);
		mipsShare.add(MIPS / 4);
		mipsShare.add(MIPS / 4);
		mipsShare.add(MIPS / 4);

		assertEquals(MIPS / 4.0 * PES_NUMBER, vmScheduler.getTotalCurrentAvailableMipsForCloudlet(rgl, mipsShare), 0);
	}

	@Test
	public void testGetEstimatedFinishTimeLowUtilization() {
		UtilizationModel utilizationModel = createMock(UtilizationModel.class);
		expect(utilizationModel.getUtilization(0))
		.andReturn(0.11)
		.anyTimes();
		replay(utilizationModel);
		testGetEstimatedFinishTime(utilizationModel);
		verify(utilizationModel);
	}

	@Test
	public void testGetEstimatedFinishTimeHighUtilization() {
		UtilizationModel utilizationModel = createMock(UtilizationModel.class);
		expect(utilizationModel.getUtilization(0))
			.andReturn(0.91)
			.anyTimes();
		replay(utilizationModel);
		testGetEstimatedFinishTime(utilizationModel);
		verify(utilizationModel);
	}

	public void testGetEstimatedFinishTime(UtilizationModel utilizationModel) {
		Cloudlet cloudlet = new Cloudlet(0, CLOUDLET_LENGTH, PES_NUMBER, CLOUDLET_FILE_SIZE, CLOUDLET_OUTPUT_SIZE,
				utilizationModel, utilizationModel, utilizationModel);
		cloudlet.setResourceParameter(0, 0, 0);
		ResCloudlet rgl = new ResCloudlet(cloudlet);

		List<Double> mipsShare = new ArrayList<Double>();
		mipsShare.add(MIPS / 4);
		mipsShare.add(MIPS / 4);
		mipsShare.add(MIPS / 4);
		mipsShare.add(MIPS / 4);

		vmScheduler.setCurrentMipsShare(mipsShare);

		double utilization = utilizationModel.getUtilization(0);
		double totalCurrentMipsForCloudlet = MIPS / 4 * PES_NUMBER;
		double requestedMips = (int) (utilization * PES_NUMBER * MIPS);
		if (requestedMips > totalCurrentMipsForCloudlet) {
			requestedMips = totalCurrentMipsForCloudlet;
		}

		double expectedFinishTime = (double) CLOUDLET_LENGTH * PES_NUMBER / requestedMips;
		double actualFinishTime = vmScheduler.getEstimatedFinishTime(rgl, 0);

		assertEquals(expectedFinishTime, actualFinishTime, 0);
	}

	@Test
	public void testCloudletSubmitLowUtilization() {
		UtilizationModel utilizationModel = createMock(UtilizationModel.class);
		expect(utilizationModel.getUtilization(0))
			.andReturn(0.11)
			.anyTimes();
		replay(utilizationModel);
		testCloudletSubmit(utilizationModel);
		verify(utilizationModel);
	}

	@Test
	public void testCloudletSubmitHighUtilization() {
		UtilizationModel utilizationModel = createMock(UtilizationModel.class);
		expect(utilizationModel.getUtilization(0))
			.andReturn(0.91)
			.anyTimes();
		replay(utilizationModel);
		testCloudletSubmit(utilizationModel);
		verify(utilizationModel);
	}

	public void testCloudletSubmit(UtilizationModel utilizationModel) {
		Cloudlet cloudlet = new Cloudlet(0, CLOUDLET_LENGTH, PES_NUMBER, CLOUDLET_FILE_SIZE, CLOUDLET_OUTPUT_SIZE,
				utilizationModel, utilizationModel, utilizationModel);
		cloudlet.setResourceParameter(0, 0, 0);

		List<Double> mipsShare = new ArrayList<Double>();
		mipsShare.add(MIPS / 4);
		mipsShare.add(MIPS / 4);
		mipsShare.add(MIPS / 4);
		mipsShare.add(MIPS / 4);

		vmScheduler.setCurrentMipsShare(mipsShare);

		double utilization = utilizationModel.getUtilization(0);
		double totalCurrentMipsForCloudlet = MIPS / 4 * PES_NUMBER;
		double requestedMips = (int) (utilization * PES_NUMBER * MIPS);
		if (requestedMips > totalCurrentMipsForCloudlet) {
			requestedMips = totalCurrentMipsForCloudlet;
		}

		double expectedFinishTime = (double) CLOUDLET_LENGTH * PES_NUMBER / requestedMips;
		double actualFinishTime = vmScheduler.cloudletSubmit(cloudlet);

		assertEquals(expectedFinishTime, actualFinishTime, 0);
	}

	@Test
	public void testUpdateVmProcessingLowUtilization() {
		UtilizationModel utilizationModel = createMock(UtilizationModel.class);

		expect(utilizationModel.getUtilization(0))
			.andReturn(0.11)
			.anyTimes();

		expect(utilizationModel.getUtilization(1.0))
			.andReturn(0.11)
			.anyTimes();

		replay(utilizationModel);

		testUpdateVmProcessing(utilizationModel);

		verify(utilizationModel);
	}

	@Test
	public void testUpdateVmProcessingHighUtilization() {
		UtilizationModel utilizationModel = createMock(UtilizationModel.class);

		expect(utilizationModel.getUtilization(0))
			.andReturn(0.91)
			.anyTimes();

		expect(utilizationModel.getUtilization(1.0))
			.andReturn(0.91)
			.anyTimes();

		replay(utilizationModel);

		testUpdateVmProcessing(utilizationModel);

		verify(utilizationModel);
	}


	@Test
	public void testUpdateVmProcessingLowAndHighUtilization() {
		UtilizationModel utilizationModel = createMock(UtilizationModel.class);

		expect(utilizationModel.getUtilization(0))
		.andReturn(0.11)
		.anyTimes();

		expect(utilizationModel.getUtilization(1.0))
		.andReturn(0.91)
		.anyTimes();

		replay(utilizationModel);

		testUpdateVmProcessing(utilizationModel);

		verify(utilizationModel);
	}

	public void testUpdateVmProcessing(UtilizationModel utilizationModel) {
		Cloudlet cloudlet = new Cloudlet(0, CLOUDLET_LENGTH, PES_NUMBER, CLOUDLET_FILE_SIZE, CLOUDLET_OUTPUT_SIZE,
				utilizationModel, utilizationModel, utilizationModel);
		cloudlet.setResourceParameter(0, 0, 0);

		List<Double> mipsShare = new ArrayList<Double>();
		mipsShare.add(MIPS / 4);
		mipsShare.add(MIPS / 4);
		mipsShare.add(MIPS / 4);
		mipsShare.add(MIPS / 4);

		vmScheduler.setCurrentMipsShare(mipsShare);

		vmScheduler.cloudletSubmit(cloudlet);

		double totalCurrentMipsForCloudlet = MIPS / 4 * PES_NUMBER;

		double utilization1 = utilizationModel.getUtilization(0);
		double requestedMips1 = (int) (utilization1 * PES_NUMBER * MIPS);
		if (requestedMips1 > totalCurrentMipsForCloudlet) {
			requestedMips1 = totalCurrentMipsForCloudlet;
		}

		double expectedCompletiontime1 = ((double) CLOUDLET_LENGTH * PES_NUMBER) / requestedMips1;
		double actualCompletionTime1 = vmScheduler.updateVmProcessing(0, mipsShare);
		assertEquals(expectedCompletiontime1, actualCompletionTime1, 0);

		double utilization2 = utilizationModel.getUtilization(1);
		double requestedMips2 = (int) (utilization2 * PES_NUMBER * MIPS);
		if (requestedMips2 > totalCurrentMipsForCloudlet) {
			requestedMips2 = totalCurrentMipsForCloudlet;
		}

		double expectedCompletiontime2 = 1 + ((CLOUDLET_LENGTH * PES_NUMBER - requestedMips1 * 1)) / requestedMips2;
		double actualCompletionTime2 = vmScheduler.updateVmProcessing(1, mipsShare);
		assertEquals(expectedCompletiontime2, actualCompletionTime2, 0);

		assertFalse(vmScheduler.isFinishedCloudlets());

		assertEquals(0, vmScheduler.updateVmProcessing(CLOUDLET_LENGTH, mipsShare), 0);

		assertTrue(vmScheduler.isFinishedCloudlets());
	}

}
