package org.cloudbus.cloudsim.tieredconfigurations;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.visualdata.DatacenterSelectionData;
import org.cloudbus.cloudsim.visualdata.CloudletExecutionData;

import org.cloudbus.cloudsim.workload.CloudletDataParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Queue;
import org.cloudbus.cloudsim.workload.BorgMapper;
import org.cloudbus.cloudsim.workload.BorgMapper.CloudLet; 

public class BorgPowerMain {
    private static List<Cloudlet> cloudletList;
    private static List<Vm> vmList;
    private static Map<Integer, Vm> vmMap;
    private static Datacenter highResDatacenter;
    private static Datacenter mediumResDatacenter;
    private static Datacenter lowResDatacenter;

    public static void main(String[] args) {
        try {
            // Step 1: Initialize CloudSim
            int numUsers = 1;
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false;
            CloudSim.init(numUsers, calendar, traceFlag, 0.720);

            // Step 2: Load fossil-free percentages
            Queue<Double> fossilFreePercentages = loadFossilFreePercentages("/powerBreakdownData.csv");
            System.out.println(fossilFreePercentages);
            PowerData initialPowerData = new PowerData(fossilFreePercentages.poll(), 0);

            // Step 3: Create fixed datacenters
            highResDatacenter = DatacenterFactory.createHighResourceDatacenter("High_Resource_Datacenter");
            mediumResDatacenter = DatacenterFactory.createMediumResourceDatacenter("Medium_Resource_Datacenter");
            lowResDatacenter = DatacenterFactory.createLowResourceDatacenter("Low_Resource_Datacenter");

            // Step 4: Create broker
            DatacenterBroker broker = createBroker();
            int brokerId = broker.getId();

            // Step 5: Initialize VM and Cloudlet lists
            vmList = new ArrayList<>();
            cloudletList = new ArrayList<>();
            vmMap = new HashMap<>();

            // Step 6: Load Borg dataset
            String borgFilePath = "/borg_traces_data.csv";
            int batchSize = 1000;

            try (InputStream inputStream = BorgMapper.class.getResourceAsStream(borgFilePath);
                 BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                List<BorgMapper.BorgDatasetRow> batch = new ArrayList<>();
                while ((line = br.readLine()) != null) {
                    // Parse the line into a BorgDatasetRow object
                    BorgMapper.BorgDatasetRow row = BorgMapper.parseLineToRow(line);

                    if (row != null) {
                        batch.add(row);
                    }

                    if (batch.size() == batchSize) {
                        processBatch(batch, broker, brokerId);
                        batch.clear();
                    }
                }
                // Process any remaining rows
                if (!batch.isEmpty()) {
                    processBatch(batch, broker, brokerId);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            vmList.addAll(vmMap.values());
            
            broker.submitGuestList(vmList);
            broker.submitCloudletList(cloudletList);
            System.out.println("Submitted " + cloudletList.size() + " cloudlets to broker, " + vmList.size() + " VMs to broker");


            // Step 7: Simulate for each hour
            //monitors and updates the datacenter selection based on the fossil-free energy %
            
            Runnable monitor = () -> {
                //track simulation time
                double temp = 0;
                //track the hours elapsed
                double hour = 0;
                String tempName = "";

                List<String[]> datacenterLogs = new ArrayList<>();
                
                while (CloudSim.running()) {
                    if (CloudSim.clock() <= temp) {
//                        System.out.println("Clock: " + CloudSim.clock());
                        if(!fossilFreePercentages.isEmpty() && temp/3600 > hour) {
                            initialPowerData.setFossilFreePercentage(fossilFreePercentages.poll());
                            hour += 1;
                        }
                        initialPowerData.setFossilFreePercentage(initialPowerData.getFossilFreePercentage());
                        Datacenter selectedDatacenter = selectDatacenterBasedOnPowerData(initialPowerData);
                        if (!selectedDatacenter.getName().equals(tempName)) {
                            System.out.println(" ------- Hour: " + temp/3600 + " Update Datacenter: ("
                                    + selectedDatacenter.getName() + ") Simulating based on fossil-free percentage: "
                                    + initialPowerData.getFossilFreePercentage());
                            tempName = selectedDatacenter.getName();
                        }
                        else {
                            System.out.println(" ------- Hour: " + temp/3600 + " No Datacenter change based on " +
                                    "fossil-free percentage: " + initialPowerData.getFossilFreePercentage());
                        }
                        datacenterLogs.add(new String[]{
                                String.valueOf(temp / 3600),
                                selectedDatacenter.getName(),
                                String.valueOf(initialPowerData.getFossilFreePercentage())
                        });
                        temp += 1800;
//                        if(temp > CloudSim.clock()) {temp = CloudSim.clock();}
                    }
                }
                
                // Time Series Plot for Datacenter Selection Over Time
                System.out.println("Current working directory: " + System.getProperty("user.dir"));
                DatacenterSelectionData.saveDatacenterSelectionCSV("./datacenterSelection.csv", datacenterLogs);
            };

            new Thread(monitor).start();
                      
            // Step 7: Start Simulation
            CloudSim.startSimulation();

            CloudSim.terminateSimulation(System.currentTimeMillis() + 120000);

            CloudSim.stopSimulation();

            // Step 8: Print Results
            printCloudletResults(broker.getCloudletReceivedList());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processBatch(List<BorgMapper.BorgDatasetRow> batch, DatacenterBroker broker, int brokerId) {
        for (BorgMapper.BorgDatasetRow row : batch) {
            // Create VM and Cloudlet from the row
                    // Create CloudLet instance
            Vm vm = null; 
            if (vmMap.get(row.instanceIndex) == null) { 
                vm = new Vm(row.instanceIndex, brokerId, row.mips, row.pes, row.memory, 1000, 10000, "Xen", new CloudletSchedulerTimeShared());
                System.out.println("Created VM: " + vm.getId());
                vmMap.put(row.instanceIndex, vm);
            } else {
                vm = vmMap.get(row.instanceIndex);
            }
            Cloudlet cloudlet = new Cloudlet(row.cloudletId, row.cloudletLength, row.pes, row.cloudletFileSize, row.cloudletOutputSize,
                    new UtilizationModelStochastic(), new UtilizationModelStochastic(), new UtilizationModelStochastic());
            cloudlet.setUserId(brokerId);
            cloudlet.setGuestId(vm.getId());
            cloudlet.setSubmissionTime(System.currentTimeMillis()+720);
            System.out.println("Created cloudlet " + cloudlet.getCloudletId() + " for VM " + vm.getId());
            // Cloudlet to the lists
            cloudletList.add(cloudlet);
        }

        // Submit VMs and Cloudlets to the broker
        //broker.submitGuestList(vmList);
        //broker.submitCloudletList(cloudletList);
    }


    private static Datacenter selectDatacenterBasedOnPowerData(PowerData powerData) {
        double fossilFreePercentage = powerData.getFossilFreePercentage();
        if (fossilFreePercentage > 70) {
            return highResDatacenter;
        } else if (fossilFreePercentage > 35) {
            return mediumResDatacenter;
        } else {
            return lowResDatacenter;
        }
    }


    private static void printCloudletResults(List<Cloudlet> cloudlets) {
        System.out.println("========== OUTPUT ==========");
        System.out.println("Cloudlet ID    STATUS    Datacenter ID    VM ID    Time    Start Time    Finish Time");
        for (Cloudlet cloudlet : cloudlets) {
            if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
                System.out.printf("%10d    SUCCESS    %12d    %5d    %4.2f    %9.2f    %10.2f%n",
                        cloudlet.getCloudletId(), cloudlet.getResourceId(), cloudlet.getVmId(),
                        cloudlet.getActualCPUTime(), cloudlet.getExecStartTime(), cloudlet.getFinishTime());
            }
        }

        // Bar/Scatter Plot for Cloudlet Execution Data over Time
        String filePath = "./cloudletExecutionTime.csv";
        CloudletExecutionData.exportCloudletExecutionTime(cloudlets, filePath);
    }

    private static DatacenterBroker createBroker() throws Exception {
        return new DatacenterBroker("Broker");
    }

    public static Queue<Double> loadFossilFreePercentages(String filePath) {
        Queue<Double> percentages = new LinkedList<>();
        try (InputStream inputStream = PowerMain.class.getResourceAsStream(filePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                String[] columns = line.split(",");
                double fossilFreePercentage = Double.parseDouble(columns[28].trim());
                percentages.add(fossilFreePercentage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return percentages;
    }
}
