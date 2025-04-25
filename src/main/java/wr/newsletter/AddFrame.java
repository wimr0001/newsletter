package wr.newsletter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AddFrame extends AbstractFrame {
	public static final long serialVersionUID = 1;
	private String dataFile;
	private JButton executeButton;
	private JLabel messageLabel = new JLabel();

	public AddFrame(String envDir, MainFrame parent, boolean mailWithAttachment) {
		super(envDir, parent, mailWithAttachment);
		this.setTitle("Spreadsheet regels toevoegen");
		this.setSize(600, 500);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				parentFrame.addFrameFinished(processResult);
				dispose();
			}
		});
	}

	public void process() {
		JPanel pane = new JPanel();
		pane.setLayout(null);
		pane.setSize(this.getWidth(), this.getHeight());
		this.add(pane);
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
		lbl3.setLocation(10, 10);
		csvCb.setLocation(230, 10);
		csvCb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int csvIdx = csvCb.getSelectedIndex();
				if (csvIdx > 0) {
					dataFile = csvCbList[csvIdx];
					addExecuteButton(pane, 50);
				}
			}
		});
		addCloseButton(pane, 50);
		messageLabel.setSize(getWidth(), 20);
		messageLabel.setLocation(10, 90);
		pane.add(messageLabel);
	}

	private void addExecuteButton(JPanel pane, int y) {
		if (executeButton != null) {
			return;
		}
		executeButton = new JButton("Voeg samen");
		pane.add(executeButton);
		executeButton.setSize(220, 24);
		executeButton.setLocation(10, y);
		executeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				// call add data
				executeButton.setEnabled(false);
				executeButton.setText("Bezig....");
				processResult = new AddData(envDir, dataFile, mailWithAttachment).process();
				processResult.setActionExecuted(true);
				if (processResult.hasResultErrors()) {
					setProcessResult(processResult, pane);
				} else {
					messageLabel.setText("Regels zijn toegevoegd");
					executeButton.setText("Gereed");
				}
			}
		});
	}
	private void addCloseButton(JPanel pane, int y) {
		JButton closeButton = new JButton("Afsluiten");
		pane.add(closeButton);
		closeButton.setSize(180, 24);
		closeButton.setLocation(250, y);
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				parentFrame.addFrameFinished(processResult);
				dispose();
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
			errorList.setLocation(10, 120);
		}
	}
}
