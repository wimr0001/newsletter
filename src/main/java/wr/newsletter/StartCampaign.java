package wr.newsletter;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class StartCampaign extends App {

	private String dataFile;
	private File mailHtmlFile;
	private File mailTxtFile;
	private File propsFile;
	private String docFile;
	private boolean mustCopyCsv;
	private boolean mustCopyDoc;
	private boolean mustCopyMailHtml;
	private boolean mustCopyMailTxt;
	private boolean mustCopyProps;
	private boolean mailWithAttachment;

	public StartCampaign(

			String envDir, String basePath, String csv, boolean mustCopyCsv, String doc, boolean mustCopyDoc,
			String mailHtml, boolean mustCopyMailHtml, String mailTxt, boolean mustCopyMailText, String props,
			boolean mustCopyProps, boolean attachment) {
		super(envDir, false, false);
		this.mustCopyCsv = mustCopyCsv;
		if (this.mustCopyCsv) {
			this.dataFile = envDir + csv;
		}
		this.mustCopyDoc = mustCopyDoc;
		if (this.mustCopyDoc) {
			this.docFile = envDir + doc;
		}
		this.mustCopyMailHtml = mustCopyMailHtml;
		if (this.mustCopyMailHtml) {
			this.mailHtmlFile = new File(envDir + mailHtml);
		}
		this.mustCopyMailTxt = mustCopyMailText;
		if (mustCopyMailText) {
			this.mailTxtFile = new File(envDir + mailTxt);
		}
		this.mustCopyProps = mustCopyProps;
		if (this.mustCopyProps) {
			this.propsFile = new File(envDir + props);
		} else {
			this.propsFile = new File(progressDir + CONFIG_FILE);
		}
		this.mailWithAttachment = attachment;
	}

//    public static void main(String[] args) {
//        StartCampaign obj = new StartCampaign("/media/wim/DATA2/projects/java/eclipse/newsletter/sportraad", "data.csv", "attachment.html.txt",
//                "mailbody.html.txt", "mailbody.txt", "config_mailtrap.props");
//        try {
//            obj.process();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
	public ProcessResult process() throws Exception {
		// read datafile and check on number of columns
		// if correct, expand with system columns and write under new directory
		// first line is the header
		getDataFile((mustCopyCsv) ? this.dataFile : progressDir + DATA_FILE);
		int colCount = inputData.getHeaders().size();
		handleProps();
		if (!mustCopyCsv) {
			colCount -= 3; // the added header colums must not calculated
		} else {
			this.checkDataLinesOnColumns(colCount);
			this.checkEmailAddresses(inputData.getDataLines(), inputData.getHeaders());
		}

		if (mailWithAttachment) {
			readDocLines((mustCopyDoc) ? this.docFile : progressDir + ATTACHMENT_FILE);
			this.checkDocVariables(docContent);
		}
//		if (mustCopyDoc) {
//			this.adjustImages();
//		}
		String mailHtml = readMailHtmlLines(
				(mustCopyMailHtml) ? this.mailHtmlFile.getAbsolutePath() : progressDir + MAILBODY_HTML);
		String mailTxt = readMailTextLines(
				(mustCopyMailTxt) ? this.mailTxtFile.getAbsolutePath() : progressDir + MAILBODY_TEXT);
		this.checkDocVariables(mailTxt);
		this.checkDocVariables(mailHtml);

		if (!processResult.hasResultErrors()) {
			copyFiles();
		}
		return processResult;
	}

	private void copyFiles() throws Exception {
		if (mustCopyCsv) {
			Helper.expandCols(inputData, inputData.getDataLines().size(), 0, mailWithAttachment);
			super.writeDataFile(progressDir + "/" + DATA_FILE);
		}
		String line = null;
		BufferedReader reader = null;
		BufferedWriter writer = null;
		String docFilename;
		if (mustCopyDoc) {

			// second the document as attachment
			docFilename = progressDir + ATTACHMENT_FILE;
			writer = new BufferedWriter(new FileWriter(docFilename));
			writer.write(docContent);
			writer.close();
		}
		if (mustCopyMailHtml) {
			// third the mailbody in html
			reader = new BufferedReader(new FileReader(this.mailHtmlFile));
			docFilename = progressDir + MAILBODY_HTML;
			writer = new BufferedWriter(new FileWriter(docFilename));
			while ((line = reader.readLine()) != null) {
				writer.write(line + "\r\n");
			}
			reader.close();
			writer.close();
		}
		if (mustCopyMailTxt) {
			// Fourth the mailbody in text format
			reader = new BufferedReader(new FileReader(this.mailTxtFile));
			docFilename = progressDir + MAILBODY_TEXT;
			writer = new BufferedWriter(new FileWriter(docFilename));
			while ((line = reader.readLine()) != null) {
				writer.write(line + "\r\n");
			}
			reader.close();
			writer.close();
		}
		if (mustCopyProps) {
			// and last the props
			reader = new BufferedReader(new FileReader(this.propsFile));
			docFilename = progressDir + "config.props";
			writer = new BufferedWriter(new FileWriter(docFilename));
			while ((line = reader.readLine()) != null) {
				writer.write(line + "\r\n");
			}
			reader.close();
			writer.close();
		}
	}

	private void checkDataLinesOnColumns(int colCount) {
		if (!mustCopyCsv) {
			return;
		}
		int i = 0;
		try {
			for (ArrayList<String> cols : inputData.getDataLines()) {
				if (cols.size() != colCount) {
					processResult.addNotification(i, "Fout in aantal kolommen");
				}
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			processResult.addNotification(i, "Geen inputdata");
		}
	}

	private String readMailHtmlLines(String fn) throws java.io.IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fn));
		return this.readLines(reader, true);
	}

	private String readMailTextLines(String fn) throws java.io.IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fn));
		return this.readLines(reader, false);
	}

	private void handleProps() {
		props = new Properties();
		try {
			InputStream in = new FileInputStream(propsFile);
			props.load(in);
			// check for required props
			if (props.getProperty("csv.col.emailaddress") == null) {
				processResult.addNotification(9999, "Parameter voor emailadres ontbreekt");
			} else {
				colNameEmail = props.getProperty("csv.col.emailaddress");
			}
			if (props.getProperty("mail.smtp.user") == null) {
				processResult.addNotification(9999, "Parameter mailuser ontbreekt");
			}
			if (props.getProperty("mail.smtp.password") == null) {
				processResult.addNotification(9999, "Parameter mail password ontbreekt");
			}
			if (props.getProperty("mail.name.sendFrom") == null) {
				processResult.addNotification(9999, "Parameter naam mail verzender ontbreekt");
			}
			if (props.getProperty("mail.smtp.host") == null) {
				processResult.addNotification(9999, "Parameter hostnaam mailserver ontbreekt");
			}
			if (props.getProperty("mail.smtp.port") == null) {
				processResult.addNotification(9999, "Parameter poortnummer mailserver ontbreekt");
			}
			if (props.getProperty("mail.smtp.ssl.enable") == null) {
				processResult.addNotification(9999, "Parameter ssl enabled ontbreekt");
			}
			if (props.getProperty("mail.smtp.starttls.enable") == null) {
				processResult.addNotification(9999, "Parameter starttls enable ontbreekt");
			}
			if (props.getProperty("mail.subject") == null) {
				processResult.addNotification(9999, "Parameter onderwerp e-mail ontbreekt");
			}
			if (props.getProperty("mail.test.sendTo") == null) {
				processResult.addNotification(9999, "Parameter mail.test.sendTO ontbreekt");
			}
			if (props.getProperty("testMode") == null) {
				processResult.addNotification(9999, "Parameter testindicatie ontbreekt");
			}
			if (mailWithAttachment) {
				if (props.getProperty("pdfname") == null) {
					processResult.addNotification(9999, "Parameter naam document ontbreekt");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			processResult.addNotification(9999, "Foutieve inhoud configuratie bestand");
		}
	}

	private void checkDocVariables(String content) {
		int idx = content.indexOf("${");
		int idx2 = 0;
		String var = null;
		while (idx >= 0) {
			idx2 = content.indexOf("}", idx);
			if (idx2 <= 0) {
				processResult.addNotification(9999, "Een parameter naam in het document wordt niet afgesloten");
				idx2 = idx + 1;
			} else {
				var = content.substring(idx + 2, idx2).trim().toLowerCase();
				boolean internalVar1 = false;
				if (var.length() > 8) {
					internalVar1 = var.substring(0, 8).equals("assigned");
				}
				boolean internalVar2 = false;
				if (var.length() > 7) {
					internalVar2 = var.substring(0, 7).equals("myutils");
				}				
				if (!var.equals("userdir") && !var.equals("actual_date") 
						&& !var.equals("actual_year")
						&& !var.equals("next_year")
						&& !var.equals("previous_year")
						&& !var.equals("volgnr") 
						&& !internalVar1 &&!internalVar2) {
					if (!parmInHeader(var)) {
						processResult.addNotification(9999,
								"Parameter " + var + " komt niet voor in de header van de datafile");
					}
				}
			}
			idx = content.indexOf("${", idx + 2);
		}
	}

	private boolean parmInHeader(String parm) {
		try {
			for (String col : inputData.getHeaders()) {
				if (col.toLowerCase().equals(parm)) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

}
