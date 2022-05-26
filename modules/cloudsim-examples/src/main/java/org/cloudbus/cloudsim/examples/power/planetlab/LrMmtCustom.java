package org.cloudbus.cloudsim.examples.power.planetlab;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumns;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleInfo;


/**
 * A simulation of a heterogeneous power aware data center that applies the Local Regression (LR) VM
 * allocation policy and Minimum Migration Time (MMT) VM selection policy.
 * 
 * This example uses a real PlanetLab workload: 20110303.
 * 
 * The remaining configuration parameters are in the Constants and PlanetLabConstants classes.
 * 
 * If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 * @since Jan 5, 2012
 */
public class LrMmtCustom {

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		boolean enableOutput = true;
		//boolean outputToFile = false;
		boolean outputToFile = true;
		String inputFolder = LrMmt.class.getClassLoader().getResource("workload/planetlab").getPath();
		String outputFolder = "output";
		String workload = "20110306"; // PlanetLab workload
		String vmAllocationPolicy = "lr"; // Local Regression (LR) VM allocation policy
		String vmSelectionPolicy = "mmt"; // Minimum Migration Time (MMT) VM selection policy
		String parameter = "1.2"; // the safety parameter of the LR policy

		// try {
		// 	walkDirTree(inputFolder);
		// } catch (Exception e) {
		// 	// TODO Auto-generated catch block
		// 	e.printStackTrace();
		// }


		//Workbook workbook = new XSSFWorkbook();
		Workbook workbook = setupWorksheet();
		attachDataToWorksheet(workbook);
		writeToWorkbook(workbook , "/workspace/cloudsim-latest/output/log/graphs.xlsx");
		
		// File file2 = new File("/workspace/cloudsim-latest/modules/cloudsim-examples/target/classes/workload/planetlab");
		// String[] directories = file2.list(new FilenameFilter() {
		// @Override
		// public boolean accept(File current, String name) {
		// 	return new File(current, name).isDirectory();
		// }
		// });

		//System.out.println(Arrays.toString(directories));

		// Loop thorugh all workloads
		// for (String d: directories) {           
			
		// 	workload = d;
			
		// 	new PlanetLabRunner(
		// 		enableOutput,
		// 		outputToFile,
		// 		inputFolder,
		// 		outputFolder,
		// 		workload,
		// 		vmAllocationPolicy,
		// 		vmSelectionPolicy,
		// 		parameter);


		// 	System.out.println(d); 
		// }
		



		// new PlanetLabRunner(
		// 		enableOutput,
		// 		outputToFile,
		// 		inputFolder,
		// 		outputFolder,
		// 		workload,
		// 		vmAllocationPolicy,
		// 		vmSelectionPolicy,
		// 		parameter);
	}

	// Prints all file/directory names in the entire directory tree!
	private static void walkDirTree(String rootFolder) throws Exception {
		Files.walk(Paths.get(rootFolder)).forEach(path -> {
			System.out.println(path);
		});
	}

	//	@SuppressWarnings("deprecation")
	public static Workbook setupWorksheet() {
		// Link to existing workbook or create new one if not found
		//Workbook workbook = linkOrCreateWorkbook(pathToWorkbook);
		Workbook workbook = new XSSFWorkbook();
		// Create Workbook sheet
		XSSFSheet tableSheet = (XSSFSheet) workbook.createSheet("Table"); // Table_DATA_WORKSHEET
						
	    // Create a table on the Table Sheet
		createWorkbookTable(workbook, tableSheet);
		
		// Populate Table Sheet with pre-run data
		excelPopulaterString(tableSheet, 0, 0, "Workload No.");
		excelPopulaterString(tableSheet, 0, 1, "Workload");
		excelPopulaterString(tableSheet, 0, 2, "Dvsf");
		excelPopulaterString(tableSheet, 0, 3, "IqrMc");
		excelPopulaterString(tableSheet, 0, 4, "IqrMmt");
		excelPopulaterString(tableSheet, 0, 5, "IqrMu");
		excelPopulaterString(tableSheet, 0, 6, "IqrRs");
		excelPopulaterString(tableSheet, 0, 7, "LrMc");
		excelPopulaterString(tableSheet, 0, 8, "LrMmt");
		
		return workbook;
	}
	
	public static void attachDataToWorksheet(Workbook workbook) {
		XSSFSheet tableSheet = (XSSFSheet) workbook.getSheet("Table");

		excelPopulaterString(tableSheet, 1, 1, "20110303");

	}

    private static Workbook linkOrCreateWorkbook(String filePath) {
    	File file = new File(filePath);
    	FileInputStream	inputStream = null;
    	Workbook workbook = null;
    	
    	if (file.exists()){
	    	try {
	    		try {
	    			inputStream = new FileInputStream(file);
	    		} catch (FileNotFoundException e) {
					System.out.println("Please close Data Output.xlsx and try again");
	    		}
	    		// If the file is found, connect to it
				workbook = new XSSFWorkbook(inputStream);
				
				// delete all sheets in workbook
				int numOfSheets = workbook.getNumberOfSheets();
				for (int i = 0; i < numOfSheets; i++) {
					workbook.removeSheetAt(0);
				}	
	    	} catch (IOException e) {
					e.printStackTrace();
			}	
		} else {
			workbook = new XSSFWorkbook();
		}	
    	return workbook;		
    }
    
	public static XSSFTable createWorkbookTable(Workbook workbook, XSSFSheet tableSheet) {

		// Create an table
		XSSFTable table = tableSheet.createTable(null); // Create Table for Table Sheet
		table.setName("Test");
		table.setDisplayName("Test_Table");

		/* get CTTable object */
		CTTable cttable = table.getCTTable();

		/* Let us define the required Style for the table */
		CTTableStyleInfo table_style = cttable.addNewTableStyleInfo();
		table_style.setName("TableStyleMedium9");

		/* Set Table Style Options */
		table_style.setShowColumnStripes(false); // showColumnStripes=0
		table_style.setShowRowStripes(true); // showRowStripes=1

		/* Set Range to the Table */
		// cttable.setRef(my_data_range.formatAsString());
		cttable.setDisplayName("MYTABLE"); /* this is the display name of the table */
		cttable.setName("Test"); /* This maps to "displayName" attribute in <table>, OOXML */
		cttable.setId(1L); // id attribute against table as long value

		CTTableColumns columns = cttable.addNewTableColumns();
		columns.setCount(3L); // define number of columns

		/* Define Header Information for the Table */
		for (int i = 0; i < 7; i++) {
			CTTableColumn column = columns.addNewTableColumn();
			column.setName("Column" + i);
			column.setId(i + 1);
		}
		
		return table;
	}
	
	// public static void sendEpisodeDataToWorksheet(Workbook workbook, Gridworld grid, Agent agent, int stepsPerEpisode, State agentInitState) {
	// 	XSSFSheet episidesSheet = (XSSFSheet) workbook.getSheet("Episodes"); // EPISODES_DATA_WORKSHEET
	// 	XSSFSheet runSheet = (XSSFSheet) workbook.getSheet("Run"); // METADATA_WORKSHEET
	// 	XSSFSheet tableSheet = (XSSFSheet) workbook.getSheet("Table"); // Table_DATA_WORKSHEET
		
	// 	excelPopulaterInt(episidesSheet, episodeCounter, 0, episodeCounter);
	// 	excelPopulaterBoolean(episidesSheet, episodeCounter, 1, agentInGoalState(grid, agent));
	// 	excelPopulaterInt(episidesSheet, episodeCounter, 2, grid.getConvergedStatesCounter());
	// 	excelPopulaterFloat(episidesSheet, episodeCounter, 3, grid.probOfReachingGoalFromGivenStateinMinStepsWithNoLearning(grid, agentInitState));
	// 	excelPopulaterInt(episidesSheet, episodeCounter, 4, agentInitState.getOptimalPathLength());
	// 	excelPopulaterInt(episidesSheet, episodeCounter, 5, stepsPerEpisode);
	// 	excelPopulaterInt(episidesSheet, episodeCounter, 6, stepsPerEpisode - agentInitState.getOptimalPathLength());
	// 	excelPopulaterFloat(episidesSheet, episodeCounter, 7, agent.getRewardCounter());
		
	// 	excelPopulaterInt(runSheet, 4, 1, episodeCounter);
	// 	excelPopulaterInt(runSheet, 5, 1, stepCounter);
	// 	excelPopulaterFloat(runSheet, 6, 1, stepCounter / (float) episodeCounter);
	// 	excelPopulaterBoolean(runSheet, 7, 1, grid.checkForTotalConvergence());
		
	// 	tableSheet.createRow(episodeCounter);
	// 	excelPopulaterInt(tableSheet, episodeCounter, 0, episodeCounter);
	// 	excelPopulaterBoolean(tableSheet, episodeCounter, 1, agentInGoalState(grid, agent));
	// 	excelPopulaterInt(tableSheet, episodeCounter, 2, grid.getConvergedStatesCounter());
	// 	excelPopulaterFloat(tableSheet, episodeCounter, 3, grid.probOfReachingGoalFromGivenStateinMinStepsWithNoLearning(grid, agentInitState));
	// 	excelPopulaterInt(tableSheet, episodeCounter, 4, agentInitState.getOptimalPathLength());
	// 	excelPopulaterInt(tableSheet, episodeCounter, 5, stepsPerEpisode);
	// 	excelPopulaterInt(tableSheet, episodeCounter, 6, stepsPerEpisode - agentInitState.getOptimalPathLength());
	// }
	
	public static void writeToWorkbook(Workbook workbook, String pathToWorkbook) {
	
		XSSFSheet tableSheet = (XSSFSheet) workbook.getSheet("Table");
		List<XSSFTable> tableList = tableSheet.getTables(); // Table_DATA_WORKSHEET
		XSSFTable table = tableList.get(0);
		
		/* Define the data range including headers */
		//AreaReference my_data_range = null;
		CellReference cellTopLeft = null;
		CellReference cellBottomRight = null;
		cellTopLeft = new CellReference(0, 0);
		cellBottomRight = new CellReference(10, 8);
		AreaReference reference = workbook.getCreationHelper().createAreaReference(cellTopLeft, cellBottomRight);
        table.setCellReferences(reference);

        try {
//			if (inputStream != null) {
//				inputStream.close();
//			}
			//FileInputStream	inputStream = new FileInputStream(pathToWorkbook);
        	FileOutputStream outputStream = new FileOutputStream(pathToWorkbook);
			
			// delete all sheets in workbook
//			int numOfSheets = workbook.getNumberOfSheets();
//			for (int i = 0; i < numOfSheets; i++) {
//				workbook.removeSheetAt(0);
//			}
			workbook.write(outputStream);
	        workbook.close();
	        outputStream.close();
		} catch (Exception ex) {
            ex.printStackTrace();
       }
	}
	
	public static void excelPopulaterString(Sheet sheetNum, int rowNum, int columnNum, String data) {
		Row row = sheetNum.getRow(rowNum);
		if (row == null) {
			row = sheetNum.createRow(rowNum);
		}
		
		Cell cell = row.getCell(columnNum);
		if (cell == null) {
			cell = row.createCell(columnNum);
		}
		cell.setCellValue(data);
	}
	
	public static void excelPopulaterInt(Sheet sheetNum, int rowNum, int columnNum, int data) {
		Row row = sheetNum.getRow(rowNum);
		if (row == null) {
			row = sheetNum.createRow(rowNum);
		}
		
		Cell cell = row.getCell(columnNum);
		if (cell == null) {
			cell = row.createCell(columnNum);
		}
		cell.setCellValue((Integer) data);
	}
	
	public static void excelPopulaterFloat(Sheet sheetNum, int rowNum, int columnNum, float data) {
		Row row = sheetNum.getRow(rowNum);
		if (row == null) {
			row = sheetNum.createRow(rowNum);
		}
		
		Cell cell = row.getCell(columnNum);
		if (cell == null) {
			cell = row.createCell(columnNum);
		}
		cell.setCellValue((Float) data);
	}
	
	public static void excelPopulaterBoolean(Sheet sheetNum, int rowNum, int columnNum, Boolean data) {
		Row row = sheetNum.getRow(rowNum);
		if (row == null) {
			row = sheetNum.createRow(rowNum);
		}
		
		Cell cell = row.getCell(columnNum);
		if (cell == null) {
			cell = row.createCell(columnNum);
		}
		cell.setCellValue((Boolean) data);
	}
	
	public static void excelPopulaterStringInt(Sheet sheetNum, int rowNum, int columnNum, String dataName, int dataValue) {
		Row row = sheetNum.getRow(rowNum);
		if (row == null) {
			row = sheetNum.createRow(rowNum);
		}

		// Choose First column
		Cell cell = row.getCell(columnNum);
		if (cell == null) {
			cell = row.createCell(columnNum);
		}
		cell.setCellValue(dataName);

		// Increment Column
		columnNum++;
		cell = row.getCell(columnNum);
		if (cell == null) {
			cell = row.createCell(columnNum);
		}
		cell.setCellValue((Integer) dataValue);
	}
	
	public static void clearSheet(Sheet sheet) {
		// Clear Sheet Data
	    for (int i = sheet.getLastRowNum(); i >= sheet.getFirstRowNum(); i--) {
	        sheet.removeRow(sheet.getRow(i));
	    }
	}

}
