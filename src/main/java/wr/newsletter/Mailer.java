package wr.newsletter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.mail.AuthenticationFailedException;
import javax.mail.Session;

import wr.freemarker.Formatter;

public class Mailer extends App {

	private MailSender service;
	private Session session;
	private FileWriter logWriter;
	private Formatter formatter;

	public Mailer(String envDir) {
		super(envDir, true, true);
		formatter = new Formatter(envDir + PROGRESS_DIR);
		processInit();
	}

//    public static void main(String[] args) {
//        Mailer mailer = new Mailer("/media/wim/DATA2/projects/java/eclipse/newsletter/sportraad");
//        ProcessResult result = mailer.processInit();
//        System.out.println(result.hasResultErrors());
//    }
	public void processInit() {
		final String username = props.getProperty("mail.smtp.user");
		final String password = props.getProperty("mail.smtp.password");
		final String host = props.getProperty("mail.smtp.host");
		final int port = Integer.parseInt(props.getProperty("mail.smtp.port"));
		final int ssl = Integer.parseInt(props.getProperty("mail.smtp.ssl.enable"));
		final int startTls = Integer.parseInt(props.getProperty("mail.smtp.starttls.enable"));
		String timeString = props.getProperty("mail.smtp.timeout");
		int timeout = 2000;
		if (timeString != null) {
			timeout = Integer.parseInt(timeString);
		}
		if (timeout < 1000) {
			timeout = 2000;
		}

		boolean sslEnable = true;
		if (ssl == 0) {
			sslEnable = false;
		}
		boolean startTlsEnable = true;
		if (startTls == 0) {
			startTlsEnable = false;
		}
		try {
			if (!inTestMode()) {
				logWriter = new FileWriter(reportDir + "mail.log");
			}
		} catch (IOException e) {
			//
		}
		service = new MailSender(username, password, host, port, timeout, sslEnable, startTlsEnable);
		session = service.getSession();
	}

	public ProcessResult process(int seq) {
		if (seq == 0) {
			processResult.setCreatedItems(0);
		}
		if (!inTestMode() && this.logWriter == null) {
			processResult.addNotification(9999, "Technisch probleem, log kan niet worden geschreven");
			return processResult;
		}
		int n = -1; // the line counter
		try {
			for (ArrayList<String> cols : inputData.getDataLines()) {
				n++;
				if (n < seq) {
					continue;
				}
				if (inputData.isMailAlreadySent(cols)) {
					continue;
				}
				if (inputData.mailHasAttachment(cols)) {
					if (!inputData.isDocApplicable(cols)) {
						continue;
					}
				}
				try {
					processResult = this.sendEmail(cols, n);
					processResult.increaseCreatedItems();
					processResult.setSequence(n);
				} catch (Exception e) {
					e.printStackTrace();
					processResult.addNotification(n, "Fout opgetreden bij versturen e-mail");
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
		return processResult;
	}

	public ProcessResult sendEmail(ArrayList<String> cols, int count) {
		Exception exception = null;
		StringBuffer sbSent = new StringBuffer();
		try {
			String htmlBody = Helper.formatContent(formatter, inputData.getHeaders(), cols, MAILBODY_HTML);
			String txtBody = Helper.formatContent(formatter, inputData.getHeaders(), cols, MAILBODY_TEXT);
			String mailSubject = props.getProperty("mail.subject");
			String subject = Helper.formatContent(formatter, inputData.getHeaders(), cols, mailSubject, "mailsubject");
			String personalName = getColumnContent(props.getProperty("csv.col.personalName"), cols);
			SendToAddress sendTo = null;
			String sendCC = null;

			if (inTestMode()) {
				sendTo = new SendToAddress(props.getProperty("mail.test.sendTo"), personalName);
				subject += " TEST";
			} else {
				sendTo = new SendToAddress(this.getColumnContent(props.getProperty("csv.col.emailaddress"), cols),
						personalName);
			}
			if (sendTo.getMailAddress() == null) {
				exception = new Exception("Column " + props.getProperty("csv.col.emailaddress") + " not found");
				processResult.addNotification(count, "Geen emailadress gevonden");
			}
			if (inTestMode()) {
				sendCC = props.getProperty("mail.smtp.cc");
			} else {
				sendCC = this.getColumnContent(props.getProperty("mail.column.cc"), cols);
			}
			String attachment = null;
			if (inputData.hasMailAttachment(cols)) {
				String pdfFilename = determinePdfFilename(cols);
				StringBuffer fna = new StringBuffer();
				fna.append(envDir);
				fna.append("pdf");
				fna.append(FileSystems.getDefault().getSeparator());
				fna.append(pdfFilename);
				attachment = fna.toString();
			}

			service.sendMail(session, sendTo, sendCC, subject, htmlBody, txtBody, attachment,
					props.getProperty("mail.name.sendFrom"));

			if (inTestMode()) {
				sbSent.append("SENTTEST,");
			} else {
				sbSent.append("SENT,");
			}
			String ts = new Timestamp(System.currentTimeMillis()).toString();
			sbSent.append(ts);
		} catch (AuthenticationFailedException afe) {
			afe.printStackTrace();
			exception = new Exception("authorization failed");
		} catch (com.sun.mail.util.MailConnectException mce) {
			mce.printStackTrace();
			exception = new Exception("timeout");
//				} catch (javax.mail.SendFailedException sfe) {
//					throw new Exception("Send failed");
		} catch (Exception e) {
			e.printStackTrace();
			exception = e;
		} finally {
			if (exception != null) {
				processResult.addNotification(9999, "Exceptie opgetreden bij versturen e-mails");
				if (inTestMode()) {
					sbSent.append("FAILEDTEST,");
				} else {
					sbSent.append("FAILED,");
				}
			}
			cols.set(cols.size() - 1, sbSent.toString());
			try {
				super.writeDataFile();
			} catch (Exception e) {
				processResult.addNotification(9999, "Exceptie opgetreden bij schrijven datafile");
			}
			this.writeLog(cols, sbSent.toString(), (exception == null) ? true : false, count);
			this.finishProcess(processResult);
		}
		return processResult;
	}

	private String determinePdfFilename(ArrayList<String> cols) throws Exception {
		String pdfFilename = Helper.formatContent(formatter, inputData.getHeaders(), cols, props.getProperty("pdfname"),
				"pdfname");
		pdfFilename = pdfFilename.replaceAll(" ", "_");
		return pdfFilename;
	}

	private void writeLog(ArrayList<String> cols, String msg, boolean correctSent, int count) {
		if (inTestMode()) {
			return;
		}
		try {
			String fn = (correctSent) ? reportDir + cols.get(cols.size() - 3) + "_correct.txt"
					: reportDir + cols.get(cols.size() - 3) + "_failed.txt";
			BufferedWriter writer = new BufferedWriter(new FileWriter(fn));
			writer.write(msg + "\n");
			writer.close();
			StringBuffer sb = new StringBuffer();
			sb.append(cols.get(cols.size() - 3));
			sb.append(" ");
			sb.append(msg);
			sb.append("\r\n");
			logWriter.write(sb.toString());
			if (count == inputData.getDataLines().size() - 1) {
				logWriter.close();
			} else {
				logWriter.flush();
			}
		} catch (Exception e) {
			processResult.addNotification(9999, "Fout bij schrijven log voor volgnummer: " + cols.get(cols.size() - 3));
		}
	}

	private void finishProcess(ProcessResult processResult) {
		try {
			super.writeDataFile();
		} catch (Exception e) {
			processResult.addNotification(9999, "Fout opgetreden bij schrijven data file");
		}
	}

	private String getColumnContent(String colName, List<String> cols) throws Exception {
		for (int i = 0; i < inputData.getHeaders().size(); i++) {
			if (inputData.getHeaders().get(i).equalsIgnoreCase(colName)) {
				if (cols.size() > i) {
					return cols.get(i);
				}
			}
		}
		return null;
	}

	private boolean inTestMode() {
		String s = props.getProperty("testMode");
		if (s == null || s.equals("0")) {
			return false;
		}
		return true;
	}
}
