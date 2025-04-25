package wr.newsletter;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

public abstract class App {
	public static final String CONFIG_FILE = "config.props";
	public static final String DATA_FILE = "data.csv";
	public static final String ATTACHMENT_FILE = "attachment.ftlx";
	public static final String MAILBODY_HTML = "mailbodyhtml.ftlx";
	public static final String MAILBODY_TEXT = "mailbodytxt.ftlx";
	public static final String PROGRESS_DIR = "inprogress";
	public static final String REPORT_DIR = "report";

	public static Properties props = new Properties();
	public String envDir;
	public String progressDir;
	public String reportDir;
	public String docContent;
	public String colNameEmail;
	public ProcessResult processResult = ProcessResult.getInstance();
	protected InputData inputData;

	public App(String envDir, boolean readProps, boolean readCsv) {
		this.envDir = envDir;
		progressDir = envDir + PROGRESS_DIR + "/";
		reportDir = envDir + REPORT_DIR + "/";
		if (readCsv) {
			inputData = new InputData(progressDir + DATA_FILE);
			processResult.setInputData(inputData);
		}
		if (readProps) {
			try {
				InputStream in = new FileInputStream(progressDir + CONFIG_FILE);
				props.load(in);
			} catch (IOException e) {
				e.printStackTrace();
				processResult.addNotification(9999, "Fout bij inlezen parameters");
			}
		}
	}

	protected void getDataFile() throws Exception {
		this.getDataFile(progressDir+DATA_FILE);
	}
	protected void getDataFile(String file) throws Exception {
		inputData = new InputData(file);
		if (inputData.getInputData().size() == 0) {
			throw new Exception("Geen inputdata gevonden");
		}
	}	

	protected void readDocLines(String filename) throws java.io.IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		docContent = this.readLines(reader, true);
	}

	public String readLines(BufferedReader reader, boolean removeLineBreaks) throws java.io.IOException {
		StringBuffer sb = new StringBuffer();
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (removeLineBreaks) {
				line = line.trim();
				if (!line.equals("")) {
					sb.append(line);
				}
			} else {
				sb.append(line);
				sb.append("\r\n");
			}
		}
		reader.close();
		return sb.toString();
	}

//	protected void writeDataFile(ArrayList<String> headers, ArrayList<ArrayList<String>> dataLines) throws Exception {
//		this.inputData.writeDataFile(headers, dataLines);
//	}
	protected void writeDataFile() throws Exception {
		this.inputData.writeDataFile();
	}
	protected void writeDataFile(String file) throws Exception {
		this.inputData.writeDataFile(file);
	}
	public int getColNumber(String parm, ArrayList<String> headers) {
		int i = 0;
		for (String header : headers) {
			if (header.equalsIgnoreCase(parm)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public void checkEmailAddresses(ArrayList<ArrayList<String>> myLines, ArrayList<String> headers) {
		// determine colnumber of emailaddress
		if (props.getProperty("csv.col.emailaddress") == null) {
			processResult.addNotification(9999, "Parameter voor emailadres ontbreekt");
			return;
		} else {
			colNameEmail = props.getProperty("csv.col.emailaddress");
		}
		if (!inputData.checkEmailCol(colNameEmail)) {
			processResult.addNotification(9999, "Naamgeving emailadres in configuratie en header afwijkend");
			return;
		}
		int i = 0;
		int colNumber = 0;
		for (String col : headers) {
			if (col.toLowerCase().equals(this.colNameEmail)) {
				colNumber = i;
			}
			i++;
		}
		String emailaddress = null;
		for (ArrayList<String> cols : myLines) {
			emailaddress = cols.get(colNumber).trim();
			if (!this.isValidEmailAddress(emailaddress)) {
				processResult.addNotification(i, "Foutief emailadres");
			}
		}
	}

	public boolean isValidEmailAddress(String email) {
		String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
		java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
		java.util.regex.Matcher m = p.matcher(email);
		return m.matches();
	}
}