package wr.newsletter;

import javax.swing.*;
import java.io.File;
import java.io.FilenameFilter;

public class AbstractFrame extends JFrame {
	public static final long serialVersionUID = 1;

	public static final String PROGRESS_DIR = "inprogress";
	public static final String REPORT_DIR = "report";
	public static final String PDF_DIR = "pdf";
	public static final String LOGO_DIR = "LOGO";
	public static final String NO_ATTACHMENT_FILE = ".no_attachment";
	public static final String DATA_FILE = "data.csv";
	public static final String CONFIG_FILE = "config.props";
	public static final String ATTACHMENT_FILE = "attachment.ftlx";
	public static final String MAILBODY_HTML = "mailbodyhtml.ftlx";
	public static final String MAILBODY_TEXT = "mailbodytxt.ftlx";
	protected MainFrame parentFrame;
	public File basePath = new File(System.getProperty("user.dir"));
	public boolean mailWithAttachment = true;
	public ProcessResult processResult = ProcessResult.getInstance();
	public String envDir;
	public InputData inputData;

	public AbstractFrame() {
	}

	public AbstractFrame(String envDir, MainFrame frame) {
		this.envDir = envDir;
		parentFrame = frame;
	}
	public AbstractFrame(String envDir, MainFrame frame, boolean mailWithAttachment) {
		this.envDir = envDir;
		parentFrame = frame;
		this.mailWithAttachment = mailWithAttachment;
	}	
	public AbstractFrame(MainFrame frame, boolean mailWithAttachment) {
		parentFrame = frame;
		this.mailWithAttachment = mailWithAttachment;
	}
	public AbstractFrame(MainFrame frame) {
		parentFrame = frame;
	}	
	protected void setParent(MainFrame frame) {
		parentFrame = frame;
	}

	protected String[] getCsvFiles(String envDir) {
		String[] files = new File(envDir).list(new FilenameFilter() {
			public boolean accept(File current, String name) {
				if (new File(current, name).isDirectory()) {
					return false;
				}
				String s = name.substring(0, 1);
				if (s.equals(".")) {
					return false;
				}
				if (name.indexOf(".csv") > 0) {
					return true;
				}
				return false;
			}
		});
		return files;
	}
}
