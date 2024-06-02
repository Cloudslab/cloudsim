package org.cloudbus.cloudsim.container.utils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.opencsv.CSVReader;
import lombok.Getter;
import lombok.Setter;

/**
 * Run.
 *

 */
public class CustomCSVReader {
	@Getter @Setter
	private static List<String[]> fileData;

	public CustomCSVReader(File inputFile) {
		// TODO Auto-generated method stub
		CSVReader reader = null;
		try {
//			Log.printLine(inputFile);
			//Get the CSVReader instance with specifying the delimiter to be used
			reader = new CSVReader(new FileReader(inputFile));
			fileData = reader.readAll();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}

