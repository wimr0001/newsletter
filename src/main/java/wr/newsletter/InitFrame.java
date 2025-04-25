package wr.newsletter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

/**
 * @author wim
 */
public class InitFrame extends AbstractFrame {
	public static final long serialVersionUID = 1;
	private String env;
	private String envDir;
	private int csvIdx = 0;
	private int docIdx = 0;
	private int mailHtmlIdx = 0;
	private int mailTextIdx = 0;
	private int propsIdx = 0;
	private String dataFile;
	private String docFile;
	private String mailHtmlFile;
	private String mailTextFile;
	private String propsFile;
	private JButton executeButton;
	private JLabel messageLabel = new JLabel();

	public InitFrame(String env, MainFrame parentFrame, boolean mailWithAttachment) {
		super(parentFrame, mailWithAttachment);
		this.setTitle("Kopieerscherm");
		this.setSize(600, 600);
		this.env = env;
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				parentFrame.initFrameFinished(processResult);
				dispose();
			}
		});
	}

	public void process() {
		envDir = basePath.getAbsolutePath() + "/" + env + "/";
		File theDir = new File(envDir + "inprogress");
		String[] files = theDir.list();
		int count = 0;
		File data = null;
		for (String file : files) {
			if (file.equals(DATA_FILE)) {
				csvIdx = 9999;
				data = new File(file);
				count++;
			}
			if (file.equals(ATTACHMENT_FILE)) {
				docIdx = 9999;
				count++;
			}
			if (file.equals(MAILBODY_HTML)) {
				mailHtmlIdx = 9999;
				count++;
			}
			if (file.equals(MAILBODY_TEXT)) {
				mailTextIdx = 9999;
				count++;
			}
			if (file.equals(CONFIG_FILE)) {
				propsIdx = 9999;
				count++;
			}
		}
		boolean skipInputData = false;
		if (count == 5) {
			InputData inputData = new InputData(envDir + PROGRESS_DIR + "/" + data);
			if (inputData.isProductionMailSendingInProgress()) {
				skipInputData = true;
			}
		}
		JPanel pane = new JPanel();
		pane.setLayout(null);
		pane.setSize(this.getWidth(), this.getHeight());
		this.add(pane);
		JButton offerButton = new JButton("Ophalen clientgegevens");
		File offer = new File(envDir + "/.offerte");
		if (offer.exists()) {
			offerButton.setSize(240, 30);
			offerButton.setLocation(10, 10);
			pane.add(offerButton);
			offerButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new ClientFrame(env).process();
				}
			});
			skipInputData = true;
		} else {
			JLabel lbl1 = new JLabel("Je werkt in directory:");
			lbl1.setLocation(10, 10);
			lbl1.setSize(this.getWidth(), 20);
			pane.add(lbl1);
			JLabel lbl2 = new JLabel(envDir);
			lbl2.setLocation(10, 34);
			lbl2.setSize(this.getWidth(), 20);
			pane.add(lbl2);
		}
		if (!skipInputData) {
			String[] csvFiles = this.getCsvFiles(envDir);
			String[] csvCbList = new String[csvFiles.length + 1];
			csvCbList[0] = "Kies een input csv file";
			for (int i = 0; i < csvFiles.length; i++) {
				csvCbList[i + 1] = csvFiles[i];
			}
			final JComboBox<String> csvCb = new JComboBox<String>(csvCbList);
			pane.add(csvCb);

			csvCb.setSize(220, 20);
			JLabel lbl3 = new JLabel("Keuze input csv file:");
			pane.add(lbl3);
			lbl3.setSize(220, 16);
			lbl3.setLocation(10, 60);
			csvCb.setLocation(230, 60);
			csvCb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					csvIdx = csvCb.getSelectedIndex();
					if (csvIdx == 0) {
						return;
					}
					dataFile = csvCbList[csvIdx];
				}
			});
		}
		if (mailWithAttachment) {
			String[] docFiles = this.getDocFiles();
			String[] docCbList = new String[docFiles.length + 1];
			docCbList[0] = "Kies een ftlx file als bijlage";
			for (int i = 0; i < docFiles.length; i++) {
				docCbList[i + 1] = docFiles[i];
			}
			final JComboBox<String> docCb = new JComboBox<String>(docCbList);
			pane.add(docCb);
			docCb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					docIdx = docCb.getSelectedIndex();
					if (docIdx == 0) {
						return;
					}
					docFile = docCbList[docIdx];
				}
			});
			docCb.setSize(220, 20);
			JLabel lbl4 = new JLabel("Keuze bijlage:");
			pane.add(lbl4);
			lbl4.setSize(220, 16);
			lbl4.setLocation(10, 90);
			docCb.setLocation(230, 90);
		} else {
			docIdx = 9999;
		}
		if (!offer.exists()) {
			String[] htmlFiles = this.getMailHtmlFiles(); // mailing
			String[] htmlCbList = new String[htmlFiles.length + 1];
			htmlCbList[0] = "Kies een ftlx-template als html-email";
			for (int i = 0; i < htmlFiles.length; i++) {
				htmlCbList[i + 1] = htmlFiles[i];
			}
			final JComboBox<String> htmlCb = new JComboBox<String>(htmlCbList);
			pane.add(htmlCb);

			htmlCb.setSize(220, 20);
			JLabel lbl5 = new JLabel("Keuze html formaat mailing:");
			pane.add(lbl5);
			lbl5.setSize(220, 16);
			lbl5.setLocation(10, 120);
			htmlCb.setLocation(230, 120);

			htmlCb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mailHtmlIdx = htmlCb.getSelectedIndex();
					if (mailHtmlIdx == 0) {
						return;
					}
					mailHtmlFile = htmlCbList[mailHtmlIdx];
				}
			});
			String[] txtFiles = this.getMailTextFiles(); // mailing
			String[] txtCbList = new String[txtFiles.length + 1];
			txtCbList[0] = "Keuze text formaat mailing:";
			for (int i = 0; i < txtFiles.length; i++) {
				txtCbList[i + 1] = txtFiles[i];
			}
			final JComboBox<String> txtCb = new JComboBox<String>(txtCbList);
			pane.add(txtCb);

			txtCb.setSize(220, 20);
			JLabel lbl6 = new JLabel("Keuze tekst formaat mailing:");
			pane.add(lbl6);
			lbl6.setSize(220, 16);
			lbl6.setLocation(10, 150);
			txtCb.setLocation(230, 150);

			txtCb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					mailTextIdx = txtCb.getSelectedIndex();
					if (mailTextIdx == 0) {
						return;
					}
					mailTextFile = txtCbList[mailTextIdx];
				}
			});
		}
		String[] propsFiles = this.getPropsFiles();
		String[] propsCbList = new String[propsFiles.length + 1];
		propsCbList[0] = "Kies een configuratie bestand";
		for (int i = 0; i < propsFiles.length; i++) {
			propsCbList[i + 1] = propsFiles[i];
		}
		final JComboBox<String> propsCb = new JComboBox<String>(propsCbList);
		pane.add(propsCb);

		propsCb.setSize(220, 20);
		JLabel lbl7 = new JLabel("Keuze configuratiebestand:");
		pane.add(lbl7);
		lbl7.setSize(220, 16);
		lbl7.setLocation(10, 180);
		propsCb.setLocation(230, 180);

		propsCb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				propsIdx = propsCb.getSelectedIndex();
				if (propsIdx == 0) {
					return;
				}
				propsFile = propsCbList[propsIdx];
			}
		});
		addExecuteButton(pane, 220);
		addCloseButton(pane, 220);
		messageLabel.setSize(getWidth(), 20);
		messageLabel.setLocation(10, 250);
		pane.add(messageLabel);
	}

	private void addCloseButton(JPanel pane, int y) {
		JButton closeButton = new JButton("Afsluiten");
		pane.add(closeButton);
		closeButton.setSize(180, 24);
		closeButton.setLocation(390, y);
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				parentFrame.initFrameFinished(processResult);
				dispose();
			}
		});
	}

	private void addExecuteButton(JPanel pane, int y) {
		if (executeButton != null) {
			return;
		}
		executeButton = new JButton("Overzetten bestanden");
		pane.add(executeButton);
		executeButton.setSize(200, 24);
		executeButton.setLocation(180, y);
		executeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				messageLabel.setText("");
				// all basefiles selected?
				if (csvIdx > 0 && docIdx > 0 && propsIdx > 0 && mailTextIdx > 0 && mailHtmlIdx > 0) {
					// call StartCampaign
					executeButton.setEnabled(false);
					executeButton.setText("Bezig....");
					try {
						processResult = new StartCampaign(envDir, basePath.toString() + "/", dataFile,
								(csvIdx == 9999) ? false : true, docFile, (docIdx == 9999) ? false : true, mailHtmlFile,
								(mailHtmlIdx == 9999) ? false : true, mailTextFile,
								(mailTextIdx == 9999) ? false : true, propsFile, (propsIdx == 9999) ? false : true,
								mailWithAttachment).process();
						processResult.setActionExecuted(true);
						if (processResult.hasResultErrors()) {
							setProcessResult(processResult, pane);
						} else {
							if (csvIdx == 9999 && docIdx == 9999 && mailHtmlIdx == 9999 && mailTextIdx == 9999
									&& propsIdx == 9999) {
								messageLabel.setText("Er zijn geen bestanden geselecteerd");
							} else {
								messageLabel.setText("Geselecteerde bestanden zijn overgezet");
							}
						}
						executeButton.setEnabled(true);
						executeButton.setText("Overzetten bestanden");
					} catch (Exception e) {
						e.printStackTrace();
						parentFrame.setErrorMessage("Technische fout opgetreden");
					}
				} else {
					messageLabel.setText("Niet alle basisbestanden zijn geselecteerd");
				}
			}
		});
	}

	public void setProcessResult(ProcessResult result, JPanel pane) {
		if (result.hasResultErrors()) {
			messageLabel.setText("Fouten geconstateerd");
			DefaultListModel<String> model = new DefaultListModel<String>();
			JList<String> errorList = new JList<String>(model);
			int j = 0;
			for (Integer n : result.getLineNumbers()) {
				int i = n.intValue() + 1;
				if (i < 9999) {
					model.addElement("Regelnummer: " + i + " " + result.getMessage(j));
				} else {
					model.addElement(result.getMessage(j));
				}
				j++;
			}
			pane.add(errorList);
			errorList.setSize(pane.getWidth(), 200);
			errorList.setLocation(10, 280);
		}
	}

	private String[] getDocFiles() {
		String[] files = new File(envDir).list(new FilenameFilter() {
			public boolean accept(File current, String name) {
				if (new File(current, name).isDirectory()) {
					return false;
				}
				String s = name.substring(0, 1);
				if (s.equals(".")) {
					return false;
				}
				if (name.indexOf(".ftlx") > 0) {
					return true;
				}
				return false;
			}
		});
		return files;
	}

	private String[] getMailHtmlFiles() {
		String[] files = new File(envDir).list(new FilenameFilter() {
			public boolean accept(File current, String name) {
				if (new File(current, name).isDirectory()) {
					return false;
				}
				String s = name.substring(0, 1);
				if (s.equals(".")) {
					return false;
				}
				if (name.indexOf(".ftlx") > 0) {
					return true;
				}
				return false;
			}
		});
		return files;
	}

	private String[] getMailTextFiles() {
		String[] files = new File(envDir).list(new FilenameFilter() {
			public boolean accept(File current, String name) {
				if (new File(current, name).isDirectory()) {
					return false;
				}
				String s = name.substring(0, 1);
				if (s.equals(".")) {
					return false;
				}
				if (name.indexOf(".ftlx") > 0) {
					return true;
				}
				return false;
			}
		});
		return files;
	}

	private String[] getPropsFiles() {
		String[] files = new File(envDir).list(new FilenameFilter() {
			public boolean accept(File current, String name) {
				if (new File(current, name).isDirectory()) {
					return false;
				}
				String s = name.substring(0, 1);
				if (s.equals(".")) {
					return false;
				}
				if (name.indexOf(".props") > 0) {
					return true;
				}
				return false;
			}
		});
		return files;
	}

}
