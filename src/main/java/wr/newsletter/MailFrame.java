package wr.newsletter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

/**
 * @author wim
 */
public class MailFrame extends AbstractFrame {
	public static final long serialVersionUID = 1;
	private ProcessResult processResult = ProcessResult.getInstance();
	private JButton executeButton;
	private JButton closeButton;
	private JLabel messageLabel = new JLabel("");
	private JLabel progressLabel = new JLabel("");
	private int toSendCount = 0;
	private JList<String> propsList;
	private Mailer mailer;
	private MailActionPerformer actionPerformer;
	private JPanel mainPane = new JPanel();
	private Properties map;
	private InputData inputData;
	private MailAction action;
	private boolean errorOccurred = false;
	private boolean stopProcess = false;
	private JButton stopButton = new JButton("Stop versturen e-mails");

	public MailFrame(String envDir, MainFrame parent, boolean attachment) {
		super(envDir, parent);
		this.mailWithAttachment = attachment;
		this.setTitle("Versturen e-mails");
		this.setSize(600, 800);
		inputData = new InputData(envDir + PROGRESS_DIR + "/" + DATA_FILE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				parentFrame.mailFrameFinished(processResult);
				dispose();
			}
		});
	}

//    public static void main(String[] args) {
//        new MailFrame("/media/wim/DATA2/projects/java/eclipse/newsletter/sportraad", null).process();
//    }

	public void process() {
		mainPane.setLayout(null);
		mainPane.setSize(this.getWidth(), this.getHeight());
		JLabel lbl1 = new JLabel("Voor je verder gaat: Bekijk de instellingen en wijzig eventueel de testmode:");
		mainPane.add(lbl1);
		lbl1.setSize(this.getWidth(), 16);
		lbl1.setLocation(10, 10);
		map = this.buildPropsList(mainPane);

		JCheckBox checkBox1 = new JCheckBox("Wijzigen testmode?");
		checkBox1.setSize(mainPane.getWidth(), 20);
		checkBox1.setLocation(10, 380);
		checkBox1.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent changeEvent) {
				String test = (String) map.get("testMode");
				if (test.equals("0")) {
					map.replace("testMode", "1");
					stopButton.setVisible(true);
				} else {
					map.replace("testMode", "0");
					stopButton.setVisible(false);
					executeButton.setText("Start mailing");
					executeButton.setEnabled(true);
				}
				messageLabel.setText("");
				progressLabel.setText("");
				if (processResult != null) {
					processResult.setCreatedItems(0);
				}
				writeConfigProps((String) map.get("testMode"));
				mainPane.remove(propsList);
				map = buildPropsList(mainPane);
			}
		});
		mainPane.add(checkBox1);
		this.addExecuteButton(mainPane, 420);
		this.addCloseButton(mainPane, 420);
		int y = 450;
		if (map.get("testMode").equals("1")) {
			this.addStopButton(mainPane, y);
			y += 40;
		}
		messageLabel.setSize(this.getWidth(), 20);
		progressLabel.setSize(this.getWidth(), 20);
		messageLabel.setLocation(10, y);
		progressLabel.setLocation(10, y + 40);
		mainPane.add(messageLabel);
		mainPane.add(progressLabel);
		this.add(mainPane);
		this.setVisible(true);
	}

	private void writeConfigProps(String testMode) {
		// first read the file
		ArrayList<String> lines = new ArrayList<String>(20);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(envDir + PROGRESS_DIR + "/" + CONFIG_FILE));
			StringBuffer sb = new StringBuffer();
			String line = null;
			while ((line = reader.readLine()) != null) {
				lines.add(line.trim());
			}
			reader.close();
			BufferedWriter writer = new BufferedWriter(new FileWriter(envDir + PROGRESS_DIR + "/" + CONFIG_FILE));
			String l = null;
			for (String _line : lines) {
				l = _line.trim();
				if (l.length() > 0) {
					if (l.length() > 9) {
						if (_line.substring(0, 9).equals("testMode=")) {
							_line = "testMode=" + testMode;
						}
					}
					_line += "\r\n";
					writer.write(_line);
				}
			}
			writer.close();
		} catch (IOException e) {
			messageLabel.setText("Fout bij schrijven van de parameters");
		}
	}

	private Properties getConfig() {
		Properties props = new Properties();
		// HashMap<String, String> relevantProps = new HashMap<String, String>(10);
		try {
			InputStream in = Files.newInputStream(Paths.get(envDir + PROGRESS_DIR + "/" + CONFIG_FILE));
			props.load(in);
//			for (Map.Entry<Object, Object> e : props.entrySet()) {
//				if (((String) e.getKey()).indexOf("mail.smtp") >= 0) {
//					relevantProps.put((String) e.getKey(), (String) e.getValue());
//				}
//				if (((String) e.getKey()).equalsIgnoreCase("mail.subject")) {
//					relevantProps.put((String) e.getKey(), (String) e.getValue());
//				}
//				if (((String) e.getKey()).equalsIgnoreCase("mail.name")) {
//					relevantProps.put((String) e.getKey(), (String) e.getValue());
//				}
//				if (((String) e.getKey()).indexOf("mail.test") >= 0) {
//					relevantProps.put((String) e.getKey(), (String) e.getValue());
//				}
//				if (((String) e.getKey()).equalsIgnoreCase("testMode")) {
//					relevantProps.put((String) e.getKey(), (String) e.getValue());
//				}
//			}
		} catch (IOException e) {
			processResult.addNotification(9999, "Fout bij inlezen parameters");
			this.dispose();
			return props;
		}
		return props;
	}

	private Properties buildPropsList(JPanel pane) {
		Properties map = getConfig();
		DefaultListModel<String> model = new DefaultListModel<String>();
		propsList = new JList<String>(model);

		Enumeration<Object> keys = map.keys();
		String key = null;
		while (keys.hasMoreElements()) {
			key = (String) keys.nextElement();
			if (!key.substring(0, 4).equals("csv.")) {
				model.addElement(key + " = " + map.get(key));
			}
		}
		pane.add(propsList);
		propsList.setLocation(10, 60);
		propsList.setSize(this.getWidth(), 300);
		return map;
	}

	private void addExecuteButton(JPanel pane, int y) {
		if (executeButton != null) {
			return;
		}
		executeButton = new JButton("Start mailing");
		pane.add(executeButton);
		executeButton.setSize(220, 24);
		executeButton.setLocation(20, y);
		executeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				executeButton.setEnabled(false);
				executeButton.setText("Bezig....");
				toSendCount = inputData.countMailsToSend();
				action = MailAction.getInstance();
				action.setSequence(-1);
				action.setMailFrame(MailFrame.this);
				mailer = new Mailer(envDir);
				action.setMailer(mailer);
				errorOccurred = false;
				closeButton.setEnabled(false);
				processNext(ProcessResult.getInstance(), 0);
			}
		});
	}

	private void fillProgressLabel(int sequence) {
		if (sequence == 0) {
			progressLabel.setText("Totaal aantal te verzenden e-mails: " + toSendCount);
		} else {
			progressLabel.setText("Verzonden " + sequence + " van " + toSendCount);
		}
	}

	private void addCloseButton(JPanel pane, int y) {
		if (closeButton != null) {
			return;
		}
		closeButton = new JButton("Afsluiten");
		pane.add(closeButton);
		closeButton.setSize(220, 24);
		closeButton.setLocation(280, y);
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				parentFrame.mailFrameFinished(processResult);
				MailFrame.this.dispose();
			}
		});
	}

	private void addStopButton(JPanel pane, int y) {
		pane.add(stopButton);
		stopButton.setSize(220, 24);
		stopButton.setLocation(140, y);
		stopButton.setEnabled(false);
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				stopProcess = true;
			}
		});
	}

	private void displayProcessResult(JPanel pane, ProcessResult result, int y) {
		messageLabel.setText("Process is niet correct uitgevoerd");
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
		errorList.setLocation(10, y);
		errorList.setVisible(true);
		closeButton.setEnabled(true);
	}

	public void processNext(ProcessResult processResult, int sequence) {
		if (processResult != null) {
			inputData = processResult.getInputData();
			if (processResult.hasResultErrors()) {
				errorOccurred = true;
			} else {
				fillProgressLabel(processResult.getCreatedItems());
			}
		} else {
			messageLabel.setText("Er zijn fouten opgetreden");
			closeButton.setEnabled(true);
			return;
		}
		if (errorOccurred) {
			displayProcessResult(mainPane, processResult, 160);
			progressLabel.setText("");
			return;
		}

		try {
			if (this.stopProcess) {
				stopProcess = false;
				messageLabel.setText("Verzending e-mails gestopt");
				closeButton.setEnabled(true);
				// executeButton.setEnabled(true);
				// executeButton.setText("Start mailing (opnieuw)");
				// processResult.setCreatedItems(0);
				return;
			}
			if (toSendCount > processResult.getCreatedItems()) {
				if (processResult.getCreatedItems() > 0) {
					stopButton.setEnabled(true);
				}
				actionPerformer = new MailActionPerformer();
				Thread t = new Thread(actionPerformer);
				if (sequence > 0) {
					Thread.sleep(1000);
				}
				t.start();
			} else {
				stopButton.setVisible(false);
				stopButton.setEnabled(false);
				executeButton.setText("Documenten zijn aangemaakt");
				closeButton.setEnabled(true);
			}

		} catch (Exception e) {
			messageLabel.setText("Er zijn fouten opgetreden");
		}
	}
}
