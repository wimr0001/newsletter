package wr.newsletter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;

public class AddData extends App {

	private String addDataFile;
	private boolean mailWithAttachment;
	private boolean overwrite;

	public AddData(String envDir, String addDataFile, boolean mailWithAttachment, boolean overwrite) {
		super(envDir, true, true);
		this.addDataFile = addDataFile;
		this.mailWithAttachment = mailWithAttachment;
		this.overwrite = overwrite;
	}

	public ProcessResult process() {
		try {
			getDataFile();
			BufferedReader reader = new BufferedReader(new FileReader(envDir + addDataFile));
			ArrayList<String> addLines = new ArrayList<String>(100);
			String _line = null;
			while ((_line = reader.readLine()) != null) {
				_line = _line.trim();
				if (!_line.equals("")) {
					addLines.add(_line);
				}
			}
			reader.close();
			ArrayList<String> addHeaders = new ArrayList<String>(Arrays.asList(addLines.get(0).split(";")));
			addLines.remove(0);
			if (addHeaders.size() != super.inputData.getHeaders().size() - 3) {
				processResult.addNotification(9999, "Headers komen niet overeen");
				return processResult;
			}
			// compare content of headers
			int n = 0;
			for (String addHeader : addHeaders) {
				if (!super.inputData.getHeaders().get(n).equals(addHeader)) {
					processResult.addNotification(9999, "Headers komen niet overeen");
					return processResult;
				}
				n++;
			}
			// check number of cols
			ArrayList<String> colsTemp = null;
			ArrayList<ArrayList<String>> cols = new ArrayList<ArrayList<String>>(addLines.size());
			int j = 0;
			for (String addLine : addLines) {
				colsTemp = new ArrayList<String>(Arrays.asList(addLine.split(";")));
				cols.add(colsTemp);
				if (colsTemp.size() != addHeaders.size()) {
					processResult.addNotification(j, "Fout in aantal kolommen");
					return processResult;
				}
				j++;
			}

			checkEmailAddresses(cols, addHeaders);
			if (processResult.hasResultErrors()) {
				return processResult;
			}
			if (!this.overwrite) {
				// copy the new lines to the existing one, if, and only if, there was at least
				// an e-mail sent
				// add the lines
				for (String addLine : addLines) {
					inputData.addLine(addLine);
				}
				try {
					Helper.expandCols(inputData, inputData.getDataLines().size(),
							inputData.getDataLines().size() - addLines.size(), mailWithAttachment);
					processResult.setInputData(inputData);
					super.writeDataFile();
				} catch (Exception e) {
					processResult.addNotification(9999, "Fout opgetreden bij schrijven spreadsheet");
				}
			} else {
				return this.overwrite(addLines);
			}
		} catch (Exception e) {
			processResult.addNotification(9999, "Technische fout opgetreden.");
		}

		return processResult;
	}

	private ProcessResult overwrite(ArrayList<String> addLines) {
		String mail = null;
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>(inputData.getDataLines().size());
		try {
			for (ArrayList<String> cols : inputData.getDataLines()) {
				mail = cols.get(cols.size() - 1);
				if (mail.length() > 5) {
					if (!mail.substring(0, 5).equals("SENT,")) {
						data.add(cols);
						continue;
					}
				}

			}
			inputData.setDataLines(data);
			for (String addLine : addLines) {
				inputData.addLine(addLine);
			}
			try {
				Helper.expandCols(inputData, inputData.getDataLines().size(),
						inputData.getDataLines().size() - addLines.size(), mailWithAttachment);
				processResult.setInputData(inputData);
				super.writeDataFile();
			} catch (Exception e) {
				processResult.addNotification(9999, "Fout opgetreden bij schrijven spreadsheet");
			}

		} catch (Exception e) {
			processResult.addNotification(9999, "Technische fout opgetreden.");
		}
		return processResult;
	}
}
