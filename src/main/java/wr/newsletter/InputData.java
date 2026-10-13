package wr.newsletter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class InputData {
	private File inputData;
	private ArrayList<ArrayList<String>> dataLines = new ArrayList<ArrayList<String>>(10);
	private ArrayList<String> headers = new ArrayList<String>(10);

	public InputData(String input) {
		this.inputData = new File(input);
		this.getInputData();
	}

	public InputData(File input) {
		this.inputData = input;
		this.getInputData();
	}

	public ArrayList<ArrayList<String>> getInputData() {
		if (this.inputData == null) {
			return new ArrayList<ArrayList<String>>(0);
		}
		dataLines = new ArrayList<ArrayList<String>>(100);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputData));

			String line = null;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (!line.equals("") && (line.split(";")).length > 1) {
					this.addLine(line.trim());
				}
			}
			reader.close();
			this.headers = dataLines.get(0);
			dataLines.remove(0);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dataLines;
	}

	public ArrayList<String> getHeaders() {
			return this.headers;
	}

	public ArrayList<ArrayList<String>> getDataLines() {
		return this.dataLines;
	}
	public void setDataLines(ArrayList<ArrayList<String>> data) {
		this.dataLines = data;
	}

	public boolean isDocApplicable(ArrayList<String> cols) {
		String pdf = cols.get(cols.size() - 2);
		if (pdf.equals("Y") || pdf.equals("N")) {
			return true;
		}
		return false;
	}

	public boolean mailHasAttachment(ArrayList<String> cols) {
		String pdf = cols.get(cols.size() - 2);
		if (pdf.equals("NA")) {
			return false;
		}
		return false;
	}

	public int countDocsToCreate() {
		if (this.dataLines == null || this.dataLines.size() == 0) {
			return 0;
		}
		int count = 0;
		for (ArrayList<String> cols : dataLines) {
			if (isMailAlreadySent(cols)) {
				continue;
			}
			if (!isDocApplicable(cols)) {
				continue;
			}
			count++;
		}
		return count;
	}

	public boolean hasMailAttachment(ArrayList<String> cols) {
		if (cols.get(cols.size() - 2).equals("NA")) {
			return false;
		}
		return true;
	}

	public int countMailsToSend() {
		if (this.dataLines == null || this.dataLines.size() == 0) {
			return 0;
		}
		int count = 0;
		for (ArrayList<String> cols : dataLines) {
			if (isMailAlreadySent(cols)) {
				continue;
			}
			count++;
		}
		return count;
	}

	public boolean isMailAlreadySent(ArrayList<String> cols) {
		String mail = cols.get(cols.size() - 1);
		if (mail.length() > 5) {
			if (mail.substring(0, 5).equals("SENT,")) {
				return true;
			}
		}
		return false;
	}

	public int isProductionMailSendingInProgress() { // 0 = no, 1 = partly, 2 = full
		if (this.dataLines == null || this.dataLines.size() == 0) {
			return 0;
		}
		int count = 0;
		String mail = null;
		for (ArrayList<String> cols : dataLines) {
			mail = cols.get(cols.size() - 1);
			if (mail.length() > 5) {
				if (mail.substring(0, 5).equals("SENT,")) {
					count++;
				}
			}
		}
		return (count == dataLines.size()) ? 2 : 1;
	}

	public boolean isReadyForMailing(boolean mailWithAttachment) {
		String mail = null;
		int countDocs = 0;
		String doc = null;
		int countSentMails = 0;
		for (ArrayList<String> cols : dataLines) {
			mail = cols.get(cols.size() - 1);
			doc = cols.get(cols.size() - 2);
			if (mail.length() > 5) {
				if (mail.substring(0, 5).equals("SENT,")) {
					countSentMails++;
				}
			}
			if (doc.equals("NA")) {
				countDocs++;
			} else if (doc.equals("Y")) {
				countDocs++;
			}
		}
		if (!mailWithAttachment) {
			if (countSentMails < dataLines.size()) {
				return true;
			}
		}
		if (countDocs == dataLines.size() && countSentMails < dataLines.size()) {
			return true;
		}
		return false;
	}

	public boolean areAllMailsSent() {
		String mail = null;
		for (ArrayList<String> cols : dataLines) {
			mail = cols.get(cols.size() - 1);
			if (mail.length() > 5) {
				if (!mail.substring(0, 5).equals("SENT,")) {
					return false;
				}
			}
			if (mail.length() == 1) {
				return false;
			}
		}
		return true;
	}

	public boolean checkEmailCol(String colName) {
		for (String name : headers) {
			if (name.equals(colName)) {
				return true;
			}
		}
		return false;
	}

	public void addLine(String line) {
		ArrayList<String> cols = this.lineToCols(line);
		dataLines.add(cols);
	}

	public void writeDataFile() throws IOException {
		this.writeDataFile(inputData, headers, dataLines);
	}

	public void writeDataFile(String filename) throws IOException {
		this.writeDataFile(new File(filename), headers, dataLines);
	}

	public void writeDataFile(File filename, ArrayList<String> headers, ArrayList<ArrayList<String>> dataLines)
			throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		String line = colsToLine(headers);
		writer.write(line + "\r\n");
		Iterator<ArrayList<String>> itercols = dataLines.iterator();
		ArrayList<String> cols = null;
		while (itercols.hasNext()) {
			cols = itercols.next();
			line = colsToLine(cols);
			writer.write(line + "\r\n");
		}
		writer.close();
	}

	private String colsToLine(ArrayList<String> cols) {
		Iterator<String> iter = cols.iterator();
		StringBuffer sb = new StringBuffer();
		while (iter.hasNext()) {
			sb.append(iter.next());
			if (iter.hasNext()) {
				sb.append(";");
			}
		}
		return sb.toString();
	}

	protected ArrayList<String> lineToCols(String line) {
		return new ArrayList<String>(Arrays.asList(line.split(";")));
	}
}
