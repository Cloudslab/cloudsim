package org.cloudbus.cloudsim;

import java.text.DecimalFormat;//十进制
import java.util.ArrayList;
import java.util.HashMap;//哈希映射 
import java.util.Iterator;//迭代器
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cloudbus.cloudsim.core.CloudSim;//这个类扩展了CloudSimCore使网络模拟在CloudSim中实现
import org.cloudbus.cloudsim.core.CloudSimTags;//含有多种静态命令，当CloudSim实体接收或发送事件时，表明被执行的一种行为
import org.cloudbus.cloudsim.core.SimEntity;//代表一仿真实体，实体操作事件并可以发送事件到其他实体，要扩展这个类需要加入startEntity(),processEvent()and shutdownEntity()
import org.cloudbus.cloudsim.core.SimEvent;//仿真事件   在实体间传递

/**
 * Datacenter class is {a CloudResource whose hostList
 * are virtualized}. It deals with processing of VM queries (i.e., handling
 * of VMs) instead of processing Cloudlet-related queries. So, even though an
 * AllocPolicy will be instantiated (in the init() method of the superclass,
 * it will not be used, as processing of （cloudlets ）are handled by the （CloudletScheduler）
 * and processing of 【VirtualMachines 】are handled by the【 VmAllocationPolicy】.
 *
 * @author        Rodrigo N. Calheiros
 * @author        Anton Beloglazov
 * @since        CloudSim Toolkit 1.0
 */
public class Datacenter extends SimEntity {//扩展SimEntity类

	/** The characteristics. *///数据中心特性
	private DatacenterCharacteristics characteristics;

	/** The regional cis name. *///局部CIS名称
	private String regionalCisName;

	/** The vm provisioner. *///虚拟机供应
	private VmAllocationPolicy vmAllocationPolicy;

	/** The last process time. *///上一次处理时间
	private double lastProcessTime;

	/** The debts. *///费用
	private Map<Integer, Double> debts;

	/** The storage list. *///存储列表
	private List<Storage> storageList;

	/** The vm list. *///虚拟机列表
	private List<? extends Vm> vmList;

	/** The scheduling interval. *///调度间隔
	private double schedulingInterval;

	/**分配一个功率意识的数据中心对象
	 * Allocates a new PowerDatacenter object.
	 *
	 * @param name       the name to be associated with this entity (as
	 * required by Sim_entity class from simjava package)
	 * @param characteristics   an object of DatacenterCharacteristics
	 * @param storageList a LinkedList of storage elements, for data simulation
	 * @param vmAllocationPolicy the vmAllocationPolicy
	 *
	 * @throws Exception This happens when one of the following scenarios occur:
	 * <ul>
	 * <li> creating this entity before initializing CloudSim package
	 * <li> this entity name is <tt>null</tt> or empty
	 * <li> this entity has <tt>zero</tt> number of PEs (Processing
	 * Elements). <br>
	 * No PEs mean the Cloudlets can't be processed.
	 * A CloudResource must contain【 one or more Machines】.
	 * A Machine must contain 【one or more PEs】.
	 * </ul>
	 *
	 * @pre name != null
	 * @pre resource != null
	 * @post $none
	 */
	public Datacenter(String name, DatacenterCharacteristics characteristics, VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList, double schedulingInterval) throws Exception {
		super(name);

		setCharacteristics(characteristics);
		setVmAllocationPolicy(vmAllocationPolicy);
		setLastProcessTime(0.0);
		setDebts(new HashMap<Integer,Double>());
		setStorageList(storageList);
		setVmList(new ArrayList<Vm>());
		setSchedulingInterval(schedulingInterval);

		// If this resource doesn't have any PEs then no useful at all 资源没有PE 则是没有用（不能处理云任务）
//		if (getCharacteristics().getPesNumber() == 0) {
//			throw new Exception(super.getName() + " : Error - this entity has no PEs. Therefore, can't process any Cloudlets.");
//		}

		// stores id of this class类的存储ID
		getCharacteristics().setId(super.getId());
	}

	/**重写此方法 以创建一个新的不同类型的资源
	 * Overrides this method when making a new and different type of resource.
	 * This method is called by {@link #/body()} to register other type to
	 * GIS entity. In doing so, you
	 * need to create a new child class extending from
	 * gridsim.CloudInformationService.
	 * <br>
	 * <b>NOTE:</b> 【You do not need to override {@link #/body()} method, if
	 * you use this method.】
	 *
	 * @pre $none
	 * @post $none
	 */
	protected void registerOtherEntity() {
		// empty. This should be override by a child class 【用一个子类重写】
	}

	/**处理时间 服务
	 * Processes events or services that are available for this PowerDatacenter.
	 *
	 * @param ev    a Sim_event object
	 *
	 * @pre ev != null
	 * @post $none
	 */
	@Override
	public void processEvent(SimEvent ev) {  //implements org.cloudbus.cloudsim.core.SimEntity.processEvent
		int srcId = -1;
		//Log.printLine(CloudSim.clock()+"[PowerDatacenter]: event received:"+ev.getTag());

		switch (ev.getTag()) {
			// Resource characteristics inquiry 资源特征调查
			case CloudSimTags.RESOURCE_CHARACTERISTICS:
				srcId = ((Integer) ev.getData()).intValue();
				sendNow(srcId, ev.getTag(), getCharacteristics());
				break;

			// Resource dynamic info inquiry 资源动态信息查询
			case CloudSimTags.RESOURCE_DYNAMICS:
				srcId = ((Integer) ev.getData()).intValue();
				sendNow(srcId, ev.getTag(), 0);
				break;

			case CloudSimTags.RESOURCE_NUM_PE:
				srcId = ((Integer) ev.getData()).intValue();
				int numPE = getCharacteristics().getNumberOfPes();
				sendNow(srcId, ev.getTag(), numPE);
				break;

			case CloudSimTags.RESOURCE_NUM_FREE_PE:
				srcId = ((Integer) ev.getData()).intValue();
				int freePesNumber = getCharacteristics().getNumberOfFreePes();
				sendNow(srcId, ev.getTag(), freePesNumber);
				break;

			// New Cloudlet arrives 新的云任务到达
			case CloudSimTags.CLOUDLET_SUBMIT:
				processCloudletSubmit(ev, false);
				break;

			// New Cloudlet arrives, but the sender asks for an ack 确认
			case CloudSimTags.CLOUDLET_SUBMIT_ACK:
				processCloudletSubmit(ev, true);
				break;

			// Cancels a previously submitted Cloudlet取消一个先前提交的云任务
			case CloudSimTags.CLOUDLET_CANCEL:
				processCloudlet(ev, CloudSimTags.CLOUDLET_CANCEL);
				break;

			// Pauses a previously submitted Cloudlet暂停一个先前提交的云任务
			case CloudSimTags.CLOUDLET_PAUSE:
				processCloudlet(ev, CloudSimTags.CLOUDLET_PAUSE);
				break;

			// Pauses a previously submitted Cloudlet, but the sender
			// asks for an acknowledgement取消一个先前提交的云任务 发送则者要求确认
			case CloudSimTags.CLOUDLET_PAUSE_ACK:
				processCloudlet(ev, CloudSimTags.CLOUDLET_PAUSE_ACK);
				break;

			// Resumes a previously submitted Cloudlet重启一个先前提交的云任务
			case CloudSimTags.CLOUDLET_RESUME:
				processCloudlet(ev, CloudSimTags.CLOUDLET_RESUME);
				break;

			// Resumes a previously submitted Cloudlet, but the sender
			// asks for an acknowledgement重启一个先前提交的云任务 发送则者要求确认
			case CloudSimTags.CLOUDLET_RESUME_ACK:
				processCloudlet(ev, CloudSimTags.CLOUDLET_RESUME_ACK);
				break;

			// Moves a previously submitted Cloudlet to a different resource移动一个先前提交的云任务
			case CloudSimTags.CLOUDLET_MOVE:
				processCloudletMove((int[]) ev.getData(), CloudSimTags.CLOUDLET_MOVE);
				break;

			// Moves a previously submitted Cloudlet to a different resource
			case CloudSimTags.CLOUDLET_MOVE_ACK:
				processCloudletMove((int[]) ev.getData(), CloudSimTags.CLOUDLET_MOVE_ACK);
				break;

			// Checks the status of a Cloudlet检查云任务状态
			case CloudSimTags.CLOUDLET_STATUS:
				processCloudletStatus(ev);
				break;

			// Ping packet
			case CloudSimTags.INFOPKT_SUBMIT:
				processPingRequest(ev);
				break;

			case CloudSimTags.VM_CREATE:
				processVmCreate(ev, false);
				break;

			case CloudSimTags.VM_CREATE_ACK:
				processVmCreate(ev, true);
				break;

			case CloudSimTags.VM_DESTROY:
				processVmDestroy(ev, false);
				break;

			case CloudSimTags.VM_DESTROY_ACK:
				processVmDestroy(ev, true);
				break;

			case CloudSimTags.VM_MIGRATE:
				processVmMigrate(ev, false);
				break;

			case CloudSimTags.VM_MIGRATE_ACK:
				processVmMigrate(ev, true);
				break;

			case CloudSimTags.VM_DATA_ADD:
				processDataAdd(ev, false);
				break;

			case CloudSimTags.VM_DATA_ADD_ACK:
				processDataAdd(ev, true);
				break;

			case CloudSimTags.VM_DATA_DEL:
				processDataDelete(ev, false);
				break;

			case CloudSimTags.VM_DATA_DEL_ACK:
				processDataDelete(ev, true);
				break;

			case CloudSimTags.VM_DATACENTER_EVENT:
				updateCloudletProcessing();
				checkCloudletCompletion();
				break;

			// other unknown tags are processed by this method通过这种方法处理其他不知道的标签
			default:
				processOtherEvent(ev);
				break;
		}
	}

	/**处理数据删除文件
	 * Process data del.
	 *
	 * @param ev the ev
	 * @param ack the ack
	 */
	protected void processDataDelete(SimEvent ev, boolean ack) {
		if (ev == null) {
			return;
		}

		Object[] data = (Object[]) ev.getData();
		if (data == null) {
			return;
		}

		String filename = (String) data[0];
		int req_source = ((Integer) data[1]).intValue();
		int tag = -1;

		// check if this file can be deleted (do not delete is right now)检查文件是否可以删除
		int msg = deleteFileFromStorage(filename);
		if (msg == DataCloudTags.FILE_DELETE_SUCCESSFUL) {
			tag = DataCloudTags.CTLG_DELETE_MASTER;
		} else { // if an error occured, notify user
			tag = DataCloudTags.FILE_DELETE_MASTER_RESULT;
		}

		if (ack){
			// send back to sender
			Object pack[] = new Object[2];
			pack[0] = filename;
			pack[1] = Integer.valueOf(msg);

			sendNow(req_source, tag, pack);
		}
	}

	/**数据增加
	 * Process data add.
	 *
	 * @param ev the ev
	 * @param ack the ack
	 */
	protected void processDataAdd(SimEvent ev, boolean ack) {
		if (ev == null) {
			return;
		}

		Object[] pack = (Object[]) ev.getData();
		if (pack == null) {
			return;
		}

		File file = (File) pack[0]; // get the file
		file.setMasterCopy(true); // set the file into a master copy
		int sentFrom = ((Integer) pack[1]).intValue(); // get sender ID

		/******     // DEBUG
		 Log.printLine(super.get_name() + ".addMasterFile(): " +
		 file.getName() + " from " + CloudSim.getEntityName(sentFrom));
		 *******/

		Object[] data = new Object[3];
		data[0] = file.getName();

		int msg = addFile(file); // add the file

		double debit;
		if (getDebts().containsKey(sentFrom)) {
			debit = getDebts().get(sentFrom);
		} else {
			debit = 0.0;
		}

		debit += getCharacteristics().getCostPerBw() * file.getSize();

		getDebts().put(sentFrom, debit);

		if (ack) {
			data[1] = Integer.valueOf(-1); // no sender id
			data[2] = Integer.valueOf(msg); // the result of adding a master file
			sendNow(sentFrom, DataCloudTags.FILE_ADD_MASTER_RESULT, data);
		}
	}

	/**
	 * Processes a ping request.
	 *
	 * @param ev  a Sim_event object
	 *
	 * @pre ev != null
	 * @post $none
	 */
	protected void processPingRequest(SimEvent ev) {
		InfoPacket pkt = (InfoPacket) ev.getData();
		pkt.setTag(CloudSimTags.INFOPKT_RETURN);
		pkt.setDestId(pkt.getSrcId());

		// sends back to the sender
		sendNow(pkt.getSrcId(), CloudSimTags.INFOPKT_RETURN, pkt);
	}

	/**处理事件：用户/代理想知道云任务状态
	 * Process the event for an User/Broker who wants to know the status of a Cloudlet.
	 * This PowerDatacenter will then send the status back to the User/Broker.
	 *
	 * @param ev   a Sim_event object
	 *
	 * @pre ev != null
	 * @post $none
	 */
	protected void processCloudletStatus(SimEvent ev) {
		int cloudletId = 0;
		int userId = 0;
		int vmId = 0;
		int status = -1;

		try{
			// if a sender using 【cloudletXXX() methods】
			int data[] = (int[]) ev.getData();
			cloudletId = data[0];
			userId = data[1];
			vmId = data[2];

			status = getVmAllocationPolicy().getHost(vmId, userId).getVm(userId, vmId).getCloudletScheduler().getCloudletStatus(cloudletId);
		}

		// if a sender using normal 【send() methods】
		catch (ClassCastException c) {
			try {
				Cloudlet cl = (Cloudlet) ev.getData();
				cloudletId = cl.getCloudletId();
				userId = cl.getUserId();

				status = getVmAllocationPolicy().getHost(vmId, userId).getVm(userId, vmId).getCloudletScheduler().getCloudletStatus(cloudletId);
			}
			catch (Exception e) {
				Log.printLine(getName() +
						": Error in processing CloudSimTags.CLOUDLET_STATUS");
				Log.printLine( e.getMessage() );
				return;
			}
		}
		catch (Exception e) {
			Log.printLine(getName() +
					": Error in processing CloudSimTags.CLOUDLET_STATUS");
			Log.printLine( e.getMessage() );
			return;
		}

		int[] array = new int[3];
		array[0] = getId();
		array[1] = cloudletId;
		array[2] = status;

		int tag = CloudSimTags.CLOUDLET_STATUS;
		sendNow(userId, tag, array);
	}

	/**
	 * Here all the method related to 【VM requests 】will be received and forwarded to the related method.
	 *
	 * @param ev the received event
	 *
	 * @pre $none
	 * @post $none
	 */
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null){
			Log.printLine(getName() + ".processOtherEvent(): Error - an event is null.");
		}
	}

	/**处理事件：用户/代理创建一个虚拟机
	 * Process the event for an User/Broker who wants to create a VM
	 * in this PowerDatacenter. This PowerDatacenter will then send the status back to
	 * the User/Broker.
	 *
	 * @param ev   a Sim_event object
	 * @param ack the ack
	 *
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmCreate(SimEvent ev, boolean ack) {
		Vm vm = (Vm) ev.getData();

		boolean result = getVmAllocationPolicy().allocateHostForVm(vm);

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();

			if (result) {
				data[2] = CloudSimTags.TRUE;
			} else {
				data[2] = CloudSimTags.FALSE;
			}
			sendNow(vm.getUserId(), CloudSimTags.VM_CREATE_ACK, data);
		}

		if (result) {
			double amount = 0.0;
			if (getDebts().containsKey(vm.getUserId())) {
				amount = getDebts().get(vm.getUserId());
			}
			amount += getCharacteristics().getCostPerMem() * vm.getRam();
			amount += getCharacteristics().getCostPerStorage() * vm.getSize();

			getDebts().put(vm.getUserId(), amount);

			getVmList().add(vm);

			vm.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(vm).getVmScheduler().getAllocatedMipsForVm(vm));
		}

	}

	/**处理事件：用户/代理销毁虚拟机
	 * Process the event for an User/Broker who wants to destroy a VM
	 * previously created in this PowerDatacenter. This PowerDatacenter may send,
	 * upon request, the status back to the User/Broker.
	 *
	 * @param ev   a Sim_event object
	 * @param ack the ack
	 *
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmDestroy(SimEvent ev, boolean ack) {
		Vm vm = (Vm) ev.getData();
		getVmAllocationPolicy().deallocateHostForVm(vm);

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();
			data[2] = CloudSimTags.TRUE;

			sendNow(vm.getUserId(),    CloudSimTags.VM_DESTROY_ACK, data);
		}

		getVmList().remove(vm);
	}

	/**处理事件：用户/代理迁移虚拟机
	 * Process the event for an User/Broker who wants to migrate a VM.
	 * This PowerDatacenter will then send the status back to the User/Broker.
	 * @param ev   a Sim_event object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmMigrate(SimEvent ev, boolean ack) {
		Object tmp = ev.getData();
		if (!(tmp instanceof Map<?, ?>)) {
			throw new ClassCastException("The data object must be Map<String, Object>");
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> migrate = (HashMap<String, Object>) tmp;

		Vm vm = (Vm) migrate.get("vm");
		Host host = (Host) migrate.get("host");

		getVmAllocationPolicy().deallocateHostForVm(vm);
		host.removeMigratingInVm(vm);
		boolean result = getVmAllocationPolicy().allocateHostForVm(vm, host);
		if (!result) {
			Log.printLine("Allocation failed");
		}

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();

			if (result) {
				data[2] = CloudSimTags.TRUE;
			} else {
				data[2] = CloudSimTags.FALSE;
			}
			sendNow(ev.getSource(), CloudSimTags.VM_CREATE_ACK, data);
		}

		double amount=0.0;
		if (debts.containsKey(vm.getUserId())) {
			amount = debts.get(vm.getUserId());
		}

		amount += getCharacteristics().getCostPerMem() * vm.getRam();
		amount += getCharacteristics().getCostPerStorage() * vm.getSize();

		debts.put(vm.getUserId(), amount);

		Log.formatLine("%.2f: Migration of VM #%d to Host #%d is completed", CloudSim.clock(), vm.getId(), host.getId());
		vm.setInMigration(false);
	}

	/**基于事件类别处理云任务
	 * Processes a Cloudlet based on the event type.
	 *
	 * @param ev   a Sim_event object
	 * @param type event type
	 *
	 * @pre ev != null
	 * @pre type > 0
	 * @post $none
	 */
	protected void processCloudlet(SimEvent ev, int type) {
		int cloudletId = 0;
		int userId = 0;
		int vmId = 0;

		try { // if the sender using 【cloudletXXX() methods】
			int data[] = (int[]) ev.getData();
			cloudletId = data[0];
			userId = data[1];
			vmId = data[2];
		}

		// if the sender using 【normal send() methods】
		catch (ClassCastException c) {
			try {
				Cloudlet cl = (Cloudlet) ev.getData();
				cloudletId = cl.getCloudletId();
				userId = cl.getUserId();
				vmId = cl.getVmId();
			} catch (Exception e) {
				Log.printLine(super.getName() + ": Error in processing Cloudlet");
				Log.printLine(e.getMessage());
				return;
			}
		} catch (Exception e) {
			Log.printLine(super.getName() + ": Error in processing a Cloudlet.");
			Log.printLine( e.getMessage() );
			return;
		}

		// begins executing ....
		switch (type) {
			case CloudSimTags.CLOUDLET_CANCEL:
				processCloudletCancel(cloudletId, userId, vmId);
				break;

			case CloudSimTags.CLOUDLET_PAUSE:
				processCloudletPause(cloudletId, userId, vmId, false);
				break;

			case CloudSimTags.CLOUDLET_PAUSE_ACK:
				processCloudletPause(cloudletId, userId, vmId, true);
				break;

			case CloudSimTags.CLOUDLET_RESUME:
				processCloudletResume(cloudletId, userId, vmId, false);
				break;

			case CloudSimTags.CLOUDLET_RESUME_ACK:
				processCloudletResume(cloudletId, userId, vmId, true);
				break;
			default:
				break;
		}

	}

	/**处理事件：用户/代理移动云任务
	 * Process the event for an User/Broker who wants to move a Cloudlet.
	 *
	 * @param receivedData   information about the migration
	 * @param type  event tag
	 *
	 * @pre receivedData != null
	 * @pre type > 0
	 * @post $none
	 */
	protected void processCloudletMove(int[] receivedData, int type) {
		updateCloudletProcessing();

		int[] array = receivedData;
		int cloudletId = array[0];
		int userId = array[1];
		int vmId = array[2];
		int vmDestId = array[3];
		int destId = array[4];

		//get the cloudlet
		Cloudlet cl = getVmAllocationPolicy().getHost(vmId, userId).getVm(userId, vmId).getCloudletScheduler().cloudletCancel(cloudletId);

		boolean failed=false;
		if (cl == null) {// cloudlet doesn't exist
			failed = true;
		} else {
			// has the cloudlet already finished?
			if (cl.getCloudletStatus() == Cloudlet.SUCCESS) {// if yes, send it back to user
				int[] data = new int[3];
				data[0] = this.getId();
				data[1] = cloudletId;
				data[2] = 0;
				sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_SUBMIT_ACK, data);
				sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
			}

			// prepare cloudlet for migration
			cl.setVmId(vmDestId);

			// the cloudlet will migrate from one vm to another does the destination VM exist?
			if (destId == this.getId()) {
				Vm vm = getVmAllocationPolicy().getHost(vmDestId, userId).getVm(userId, vmDestId);
				if (vm == null) {
					failed = true;
				} else {
					double fileTransferTime = predictFileTransferTime(cl.getRequiredFiles()); // time to transfer the files
					vm.getCloudletScheduler().cloudletSubmit(cl, fileTransferTime);
				}
			} else {// the cloudlet will migrate from one resource to another
				int tag = ((type == CloudSimTags.CLOUDLET_MOVE_ACK) ? CloudSimTags.CLOUDLET_SUBMIT_ACK : CloudSimTags.CLOUDLET_SUBMIT);
				sendNow(destId, tag, cl);
			}
		}

		if (type == CloudSimTags.CLOUDLET_MOVE_ACK) {// send ACK if requested
			int[] data = new int[3];
			data[0] = this.getId();
			data[1] = cloudletId;
			if (failed) {
				data[2] = 0;
			} else {
				data[2] = 1;
			}
			sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_SUBMIT_ACK, data);
		}
	}

	/**处理事件：云任务提交
	 * Processes a Cloudlet submission.
	 *
	 * @param ev  a SimEvent object
	 * @param ack  an acknowledgement
	 *
	 * @pre ev != null
	 * @post $none
	 */
	protected void processCloudletSubmit(SimEvent ev, boolean ack) {
		updateCloudletProcessing();

		try {
			// gets the Cloudlet object
			Cloudlet cl = (Cloudlet) ev.getData();

			// checks whether this Cloudlet has finished or not
			if (cl.isFinished()){
				String name = CloudSim.getEntityName(cl.getUserId());
				Log.printLine(getName()+": Warning - Cloudlet #"+cl.getCloudletId()+" owned by "+name+" is already completed/finished.");
				Log.printLine("Therefore, it is not being executed again");
				Log.printLine();

				// NOTE: If a Cloudlet has finished, then it won't be processed.
				// So, if ack is required, this method sends back a result.
				// If ack is not required, this method don't send back a result.
				// Hence, this might cause CloudSim to be hanged since waiting
				// for this Cloudlet back.
				if (ack) {
					int[] data = new int[3];
					data[0] = getId();
					data[1] = cl.getCloudletId();
					data[2] = CloudSimTags.FALSE;

					// unique tag = operation tag
					int tag = CloudSimTags.CLOUDLET_SUBMIT_ACK;
					sendNow(cl.getUserId(), tag, data);
				}

				sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);

				return;
			}

			// process this Cloudlet to this CloudResource
			cl.setResourceParameter(getId(), getCharacteristics().getCostPerSecond(), getCharacteristics().getCostPerBw());

			int userId = cl.getUserId();
			int vmId = cl.getVmId();

			double fileTransferTime = predictFileTransferTime(cl.getRequiredFiles()); //time to transfer the files

			Host host = getVmAllocationPolicy().getHost(vmId, userId);
			Vm vm = host.getVm(vmId, userId);
			CloudletScheduler scheduler = vm.getCloudletScheduler();
			double estimatedFinishTime = scheduler.cloudletSubmit(cl,fileTransferTime);

			//if (estimatedFinishTime > 0.0 && estimatedFinishTime < getSchedulingInterval()) { //if this cloudlet is in the exec queue
			if (estimatedFinishTime > 0.0) { //if this cloudlet is in the exec queue
				//double estimatedFinishTime = (cl.getCloudletTotalLength()/(capacity*cl.getPesNumber())); //time to process the cloudlet
				//Log.printLine(estimatedFinishTime+"="+gl.getCloudletLength()+"/("+capacity+"*"+gl.getNumPE()+")");
				estimatedFinishTime += fileTransferTime;
				//estimatedFinishTime += CloudSim.clock();
				//Log.printLine(CloudSim.clock()+": Next event scheduled to +"+estimatedFinishTime);
				send(getId(), estimatedFinishTime, CloudSimTags.VM_DATACENTER_EVENT);
			}

		}
		catch (ClassCastException c) {
			Log.printLine(getName() + ".processCloudletSubmit(): " + "ClassCastException error.");
			c.printStackTrace();
		}
		catch (Exception e) {
			Log.printLine(getName() + ".processCloudletSubmit(): " + "Exception error.");
			e.printStackTrace();
		}

		checkCloudletCompletion();
	}

	/**预测文件传输时间
	 * Predict file transfer time.
	 *
	 * @param requiredFiles the required files
	 *
	 * @return the double
	 */
	protected double predictFileTransferTime(List<String> requiredFiles) {
		double time = 0.0;

		Iterator<String> iter = requiredFiles.iterator();//【迭代器的使用】
		while (iter.hasNext()) {
			String fileName = iter.next();
			for (int i = 0; i < getStorageList().size(); i++) {
				Storage tempStorage = getStorageList().get(i);
				File tempFile = tempStorage.getFile(fileName);
				if (tempFile != null) {
					time += tempFile.getSize() / tempStorage.getMaxTransferRate();
					break;
				}
			}
		}
		return time;
	}

	/**处理云任务重启请求
	 * Processes a Cloudlet resume request.
	 *
	 * @param cloudletId  resuming cloudlet ID
	 * @param userId  ID of the cloudlet's owner
	 * @param ack $true if an ack is requested after operation
	 * @param vmId the vm id
	 *
	 * @pre $none
	 * @post $none
	 */
	protected void processCloudletResume(int cloudletId, int userId, int vmId, boolean ack) {
		double eventTime = getVmAllocationPolicy().getHost(vmId,userId).getVm(userId, vmId).getCloudletScheduler().cloudletResume(cloudletId);

		boolean status = false;
		if (eventTime > 0.0) { //if this cloudlet is in the exec queue
			status = true;
			if (eventTime > CloudSim.clock()) {
				schedule(getId(), eventTime, CloudSimTags.VM_DATACENTER_EVENT);
			}
		}

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = cloudletId;
			if (status) {
				data[2] = CloudSimTags.TRUE;
			} else {
				data[2] = CloudSimTags.FALSE;
			}
			sendNow(userId, CloudSimTags.CLOUDLET_RESUME_ACK, data);
		}
	}

	/**处理云任务暂停请求
	 * Processes a Cloudlet pause request.
	 *
	 * @param cloudletId  resuming cloudlet ID
	 * @param userId  ID of the cloudlet's owner
	 * @param ack $true if an ack is requested after operation
	 * @param vmId the vm id
	 *
	 * @pre $none
	 * @post $none
	 */
	protected void processCloudletPause(int cloudletId, int userId, int vmId, boolean ack) {
		boolean status = getVmAllocationPolicy().getHost(vmId,userId).getVm(userId, vmId).getCloudletScheduler().cloudletPause(cloudletId);

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = cloudletId;
			if (status) {
				data[2] = CloudSimTags.TRUE;
			} else {
				data[2] = CloudSimTags.FALSE;
			}
			sendNow(userId, CloudSimTags.CLOUDLET_PAUSE_ACK, data);
		}
	}

	/**处理云任务取消请求
	 * Processes a Cloudlet cancel request.
	 *
	 * @param cloudletId  resuming cloudlet ID
	 * @param userId  ID of the cloudlet's owner
	 * @param vmId the vm id
	 *
	 * @pre $none
	 * @post $none
	 */
	protected void processCloudletCancel(int cloudletId, int userId, int vmId) {
		Cloudlet cl = getVmAllocationPolicy().getHost(vmId,userId).getVm(userId, vmId).getCloudletScheduler().cloudletCancel(cloudletId);

		sendNow(userId, CloudSimTags.CLOUDLET_CANCEL, cl);
	}

	/**更新处理云任务
	 * Updates processing of each cloudlet running in this PowerDatacenter. It is necessary because
	 * Hosts and VirtualMachines are simple objects, not entities. So, they don't receive events
	 * and updating cloudlets inside them must be called from the outside.
	 *
	 * @pre $none
	 * @post $none
	 */
	protected void updateCloudletProcessing() {
		//Log.printLine(CloudSim.clock()+": PowerDatacenter #"+this.get_id()+": updating cloudlet processing.......................................");
		//if some time passed since last processing
		if (CloudSim.clock() > this.getLastProcessTime()) {
			List<? extends Host> list = getVmAllocationPolicy().getHostList();
			double smallerTime = Double.MAX_VALUE;
			//for each host...
			for (int i = 0; i < list.size(); i++) {
				Host host = list.get(i);
				double time = host.updateVmsProcessing(CloudSim.clock());//inform VMs to update processing
				//what time do we expect that the next cloudlet will finish?
				if (time < smallerTime) {
					smallerTime = time;
				}
			}
			//schedules an event to the next time, if valid
			//if (smallerTime > CloudSim.clock() + 0.01 && smallerTime != Double.MAX_VALUE && smallerTime < getSchedulingInterval()) {
			if (smallerTime > CloudSim.clock() + 0.01 && smallerTime != Double.MAX_VALUE) {
				schedule(getId(), (smallerTime - CloudSim.clock()), CloudSimTags.VM_DATACENTER_EVENT);
			}
			setLastProcessTime(CloudSim.clock());
		}
	}

	/**证实 如果一些任务已经完成
	 * Verifies if some cloudlet inside this PowerDatacenter already finished.
	 * If yes, send it to the User/Broker
	 *
	 * @pre $none
	 * @post $none
	 */
	protected void checkCloudletCompletion() {
		List<? extends Host> list = getVmAllocationPolicy().getHostList();
		for (int i = 0; i < list.size(); i++) {
			Host host = list.get(i);
			for (Vm vm : host.getVmList()) {
				while (vm.getCloudletScheduler().isFinishedCloudlets()){
					Cloudlet cl = vm.getCloudletScheduler().getNextFinishedCloudlet();
					if (cl != null) {
						sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
					}
				}
			}
		}
	}

	/**实验开始前增加一个文件到资源存储    file  ？？？
	 * Adds a file into the resource's storage before the experiment starts.
	 * If the file is a master file, then it will be registered to the RC when
	 * the experiment begins.
	 *
	 * @param file  a DataCloud file
	 *
	 * @return a tag number denoting whether this operation is a success or not
	 */
	public int addFile(File file) {
		if (file == null) {
			return DataCloudTags.FILE_ADD_ERROR_EMPTY;
		}

		if (contains(file.getName())) {
			return DataCloudTags.FILE_ADD_ERROR_EXIST_READ_ONLY;
		}

		// check storage space first
		if (getStorageList().size() <= 0) {
			return DataCloudTags.FILE_ADD_ERROR_STORAGE_FULL;
		}

		Storage tempStorage = null;
		int msg = DataCloudTags.FILE_ADD_ERROR_STORAGE_FULL;

		for (int i = 0; i < getStorageList().size(); i++) {
			tempStorage = getStorageList().get(i);
			if (tempStorage.getAvailableSpace() >= file.getSize()) {
				tempStorage.addFile(file);
				msg = DataCloudTags.FILE_ADD_SUCCESSFUL;
				break;
			}
		}

		return msg;
	}

	/**核实是否资源拥有给定的文件
	 * Checks whether the resource has the given file.
	 *
	 * @param file  a file to be searched
	 *
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	protected boolean contains(File file) {
		if (file == null) {
			return false;
		}
		return contains( file.getName() );
	}

	/**
	 * Checks whether the resource has the given file.
	 *
	 * @param fileName  a file name to be searched
	 *
	 * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
	 */
	protected boolean contains(String fileName) {
		if (fileName == null || fileName.length() == 0) {
			return false;
		}

		Iterator<Storage> it = getStorageList().iterator();
		Storage storage = null;
		boolean result = false;

		while (it.hasNext()) {
			storage = it.next();
			if (storage.contains(fileName)) {
				result = true;
				break;
			}
		}

		return result;
	}

	/**删除文件
	 * Deletes the file from the storage. Also, check whether it is
	 * possible to delete the file from the storage.
	 *
	 * @param fileName      the name of the file to be deleted
	 *
	 * @return the error message as defined in
	 *
	 */
	private int deleteFileFromStorage(String fileName) {
		Storage tempStorage = null;
		File tempFile = null;
		int msg = DataCloudTags.FILE_DELETE_ERROR;

		for (int i = 0; i < getStorageList().size(); i++) {
			tempStorage = getStorageList().get(i);
			tempFile = tempStorage.getFile(fileName);
			tempStorage.deleteFile(fileName, tempFile);
			msg = DataCloudTags.FILE_DELETE_SUCCESSFUL;
		} // end for

		return msg;
	}

	/**打印费用
	 * Prints the debts.
	 */
	public void printDebts() {
		Log.printLine("*****PowerDatacenter: "+this.getName()+"*****");
		Log.printLine("User id\t\tDebt");

		Set<Integer> keys = getDebts().keySet();
		Iterator<Integer> iter = keys.iterator();
		DecimalFormat df = new DecimalFormat("#.##");
		while (iter.hasNext()) {
			int key = iter.next();
			double value = getDebts().get(key);
			Log.printLine(key+"\t\t"+df.format(value));
		}
		Log.printLine("**********************************");
	}

	/* (non-Javadoc)
	 * @see cloudsim.core.SimEntity#shutdownEntity()
	 */
	@Override
	public void shutdownEntity() {
		Log.printLine(getName() + " is shutting down...");
	}

	/* (non-Javadoc)
	 * @see cloudsim.core.SimEntity#startEntity()
	 */
	@Override
	public void startEntity() {
		Log.printLine(getName() + " is starting...");
		// this resource should register to regional GIS.
		// However, if not specified, then register to system GIS (the
		// default CloudInformationService) entity.
		int gisID = CloudSim.getEntityId(regionalCisName);
		if (gisID == -1) {
			gisID = CloudSim.getCloudInfoServiceEntityId();
		}

		// send the registration to GIS
		sendNow(gisID, CloudSimTags.REGISTER_RESOURCE, getId());
		// Below method is for a child class to override
		registerOtherEntity();
	}

	/**获取主机列表
	 * Gets the host list.
	 *
	 * @return the host list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Host> List<T> getHostList() {
		return (List<T>) getCharacteristics().getHostList();
	}

	/**获取数据中心特征
	 * Gets the characteristics.
	 *
	 * @return the characteristics
	 */
	protected DatacenterCharacteristics getCharacteristics() {
		return characteristics;
	}

	/**设置数据中心特征
	 * Sets the characteristics.
	 *
	 * @param characteristics the new characteristics
	 */
	protected void setCharacteristics(DatacenterCharacteristics characteristics) {
		this.characteristics = characteristics;
	}

	/**获取局部CIS名
	 * Gets the regional cis name.
	 *
	 * @return the regional cis name
	 */
	protected String getRegionalCisName() {
		return regionalCisName;
	}

	/**
	 * Sets the regional cis name.
	 *
	 * @param regionalCisName the new regional cis name
	 */
	protected void setRegionalCisName(String regionalCisName) {
		this.regionalCisName = regionalCisName;
	}

	/**虚拟机分配协议
	 * Gets the vm allocation policy.
	 *
	 * @return the vm allocation policy
	 */
	public VmAllocationPolicy getVmAllocationPolicy() {
		return vmAllocationPolicy;
	}

	/**
	 * Sets the vm allocation policy.
	 *
	 * @param vmAllocationPolicy the new vm allocation policy
	 */
	protected void setVmAllocationPolicy(VmAllocationPolicy vmAllocationPolicy) {
		this.vmAllocationPolicy = vmAllocationPolicy;
	}

	/**
	 * Gets the last process time.
	 *
	 * @return the last process time
	 */
	protected double getLastProcessTime() {
		return lastProcessTime;
	}

	/**
	 * Sets the last process time.
	 *
	 * @param lastProcessTime the new last process time
	 */
	protected void setLastProcessTime(double lastProcessTime) {
		this.lastProcessTime = lastProcessTime;
	}

	/**费用
	 * Gets the debts.
	 *
	 * @return the debts
	 */
	protected Map<Integer, Double> getDebts() {
		return debts;
	}

	/**
	 * Sets the debts.
	 *
	 * @param debts the debts
	 */
	protected void setDebts(Map<Integer, Double> debts) {
		this.debts = debts;
	}

	/**存储列表
	 * Gets the storage list.
	 *
	 * @return the storage list
	 */
	protected List<Storage> getStorageList() {
		return storageList;
	}

	/**
	 * Sets the storage list.
	 *
	 * @param storageList the new storage list
	 */
	protected void setStorageList(List<Storage> storageList) {
		this.storageList = storageList;
	}

	/**虚拟机列表
	 * Gets the vm list.
	 *
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmList() {
		return (List<T>) vmList;
	}

	/**
	 * Sets the vm list.
	 *
	 * @param vmList the new vm list
	 */
	protected <T extends Vm> void setVmList(List<T> vmList) {
		this.vmList = vmList;
	}

	/**调度间隔
	 * Gets the scheduling interval.
	 *
	 * @return the scheduling interval
	 */
	protected double getSchedulingInterval() {
		return schedulingInterval;
	}

	/**
	 * Sets the scheduling interval.
	 *
	 * @param schedulingInterval the new scheduling interval
	 */
	protected void setSchedulingInterval(double schedulingInterval) {
		this.schedulingInterval = schedulingInterval;
	}

}