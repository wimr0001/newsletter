package wr.newsletter;

import java.util.ArrayList;

import wr.freemarker.Formatter;

public class PdfCreator extends App {

	private Formatter formatter;

	public PdfCreator(String envDir) {
		super(envDir, true, true);
		formatter = new Formatter(envDir + PROGRESS_DIR);
	}

	public ProcessResult process(int seq) {
		int n = -1; // the line counter
		if (seq == 0) {
			processResult.setCreatedItems(0);
		}
		try {
			for (ArrayList<String> cols : inputData.getDataLines()) {
				n++;
				if (n < seq) {
					continue;
				}
				if (inputData.isMailAlreadySent(cols)) {
					continue;
				}
				if (!inputData.isDocApplicable(cols)) {
					continue;
				}
				try {
					this.personalizeAttachment(cols);
					cols.set(cols.size() - 2, "Y");
					processResult.increaseCreatedItems();
					processResult.setSequence(n);
				} catch (Exception e) {
					 e.printStackTrace();
					cols.set(cols.size() - 2, "N");
					processResult.addNotification(n, "Fout, waarschijnlijk foutieve variabele");
				} finally {
					processResult.setInputData(inputData);
				}
				break;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			processResult.addNotification(n, "Fout opgetreden bij aanmaak document");
		}
		this.finishProcess(processResult);
		return processResult;
	}

	private void finishProcess(ProcessResult processResult) {
		try {
			super.writeDataFile();
		} catch (Exception e) {
			processResult.addNotification(9999, "Fout opgetreden bij schrijven data file");
		}
	}

	private void personalizeAttachment(ArrayList<String> cols) throws Exception {
		String personalizedContent = Helper.formatContent(formatter, inputData.getHeaders(), cols, ATTACHMENT_FILE);
		String pdfFilename = Helper.formatContent(formatter, inputData.getHeaders(), cols, props.getProperty("pdfname"),
				"pdfname");
		pdfFilename = pdfFilename.replaceAll(" ", "_");
		Helper.html2pdf(personalizedContent, envDir + "pdf/" + pdfFilename);
	}
}
