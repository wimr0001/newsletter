package wr.newsletter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * @author wim
 */
public class PdfFrame extends AbstractFrame {
	public static final long serialVersionUID = 1;
	private JLabel messageLabel = new JLabel("");
	private JLabel progressLabel = new JLabel("");
	private int lineCount = 0;
	private JButton executeButton;
	private JButton closeButton;
	private PdfCreator pdfCreator;
	private ActionPerformer actionPerformer;
	private ProcessResult processResult = ProcessResult.getInstance();
	private JPanel pane = new JPanel();
	private InputData inputData;
	private PdfAction action;
	private boolean errorOccurred = false;

	public PdfFrame(String envDir, MainFrame parent) {
		super(envDir, parent);
		this.setTitle("Aanmaken documenten");
		this.setSize(600, 400);
		setLocationRelativeTo(null);
		inputData = new InputData(envDir + PROGRESS_DIR + "/" + DATA_FILE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				parentFrame.pdfFrameFinished(processResult);
				dispose();
			}
		});
	}

	public void process() {
		pane.setLayout(null);
		pane.setSize(this.getWidth(), this.getHeight());
		this.add(pane);
		JLabel lbl1 = new JLabel("Je werkt in directory:");
		lbl1.setLocation(10, 10);
		lbl1.setSize(this.getWidth(), 20);
		pane.add(lbl1);
		JLabel lbl2 = new JLabel(envDir);
		lbl2.setLocation(10, 34);
		lbl2.setSize(this.getWidth(), 20);
		pane.add(lbl2);
		executeButton = new JButton("Aanmaken documenten");
		pane.add(executeButton);
		executeButton.setSize(240, 24);
		executeButton.setLocation(10, 60);
		executeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				executeButton.setEnabled(false);
				executeButton.setText("Bezig....");
				lineCount = inputData.countDocsToCreate();
				action = PdfAction.getInstance();
				action.setSequence(-1);
				action.setPdfFrame(PdfFrame.this);
				pdfCreator = new PdfCreator(envDir);
				action.setPdfCreator(pdfCreator);
				errorOccurred = false;
				processNext(ProcessResult.getInstance(), 0);
			}
		});

		closeButton = new JButton("Afsluiten");
		pane.add(closeButton);
		closeButton.setEnabled(true);
		closeButton.setSize(240, 24);
		closeButton.setLocation(260, 60);
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				MainFrame pFrame = parentFrame;
				if (processResult.getInputData() == null) {
					processResult.setInputData(inputData);
				}
				pFrame.pdfFrameFinished(processResult);
				PdfFrame.this.dispose();
			}
		});

		messageLabel.setSize(this.getWidth(), 20);
		messageLabel.setLocation(10, 90);
		messageLabel.setText("");
		progressLabel.setText("");
		pane.add(messageLabel);
		progressLabel.setSize(this.getWidth(), 20);
		progressLabel.setLocation(10, 120);
		pane.add(progressLabel);
	}

	private void fillProgressLabel(int sequence) {
		if (sequence == 0) {
			progressLabel.setText("Totaal aantal aan te maken documenten: " + lineCount);
		} else {
			progressLabel.setText("Aangemaakt: " + sequence + " van " + lineCount);
		}
		pane.validate();
		pane.repaint();
	}

	private void displayProcessResult(JPanel pane, ProcessResult result, int y) {
		messageLabel.setText("Process is niet correct uitgevoerd");
		DefaultListModel<String> model = new DefaultListModel<String>();

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
		JList<String> errorList = new JList<String>(model);
		pane.add(errorList);
		errorList.setSize(pane.getWidth(), 400);
		errorList.setLocation(10, y);
		errorList.setVisible(true);
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
			return;
		}
		if (errorOccurred) {
			displayProcessResult(pane, processResult, 160);
			progressLabel.setText("");
			return;
		}

		try {
			if (lineCount > processResult.getCreatedItems()) {
				actionPerformer = new ActionPerformer();
				Thread t = new Thread(actionPerformer);
				t.start();
			} else {
				executeButton.setText("Documenten zijn aangemaakt");
				closeButton.setEnabled(true);
			}

		} catch (Exception e) {
			messageLabel.setText("Er zijn fouten opgetreden");
		}
	}
}
