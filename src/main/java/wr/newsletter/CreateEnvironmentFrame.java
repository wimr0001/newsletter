package wr.newsletter;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class CreateEnvironmentFrame extends AbstractFrame {
	public static final long serialVersionUID = 1;
	
	private TextInputField tif = new TextInputField(12);
	private boolean actionExecuted = false;
	private boolean withAttachment = true;

	public CreateEnvironmentFrame(MainFrame parentFrame) {
		super(parentFrame);
		this.setTitle("Creer een omgeving");
		this.setSize(400, 400);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				parentFrame.createEnvironmentFrameFinished(processResult);
				dispose();
			}
		});
	}

//	public static void main(String args[]) {
//		new CreateEnvironmentFrame(System.getProperty("user.dir"), null).process();
//	}

	public void process() {
		JPanel pane = new JPanel();
		pane.setLayout(null);
		pane.setSize(this.getWidth(), this.getHeight());
		this.add(pane);
		JLabel lbl1 = new JLabel("Geef naam van omgeving, max. 12 alfabetische tekens");
		lbl1.setLocation(10,20);
		lbl1.setSize(360,16);
		pane.add(lbl1);
		pane.add(tif);
		tif.setLocation(30,45);
		tif.setSize(120,20);
		JCheckBox checkBox1 = new JCheckBox("Email met bijlage?");
		checkBox1.setSize(pane.getWidth(), 20);
		checkBox1.setLocation(10, 80);
		checkBox1.setSelected(true);
		checkBox1.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent changeEvent) {
				if (changeEvent.getStateChange() == ItemEvent.DESELECTED) {
					withAttachment = false;
				} else {
					withAttachment = true;
				}
			}
		});
		pane.add(checkBox1);
		JButton createEnvButton = new JButton("Creër omgeving");
		createEnvButton.setSize(160, 30);
		createEnvButton.setLocation(30, 110);
		pane.add(createEnvButton);
		JLabel msgLbl = new JLabel();
		msgLbl.setSize(pane.getWidth(), 16);
		msgLbl.setLocation(10, 160);
		pane.add(msgLbl);
		createEnvButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String content = tif.getText();
				File theDir = new File(basePath + "/"+content);
				if (theDir.exists()) {
					msgLbl.setText("Omgeving bestaat al");
					msgLbl.setForeground(Color.RED);
					return;
				}
				theDir.mkdir();
				if (!withAttachment) {
					File attachment = new File(theDir+"/.no_attachment");
					try {
						attachment.createNewFile();
					} catch (IOException e1) {
						theDir.delete();
						msgLbl.setText("Omgeving kan niet worden aangemaakt; probleem met aanmaken bestand");
						msgLbl.setForeground(Color.RED);
						return;
					}
				}
				
				File progressDir = new File(theDir+"/"+PROGRESS_DIR);
				progressDir.mkdir();
				File reportDir = new File(theDir+"/"+REPORT_DIR);
				reportDir.mkdir();
				File pdfDir = new File(theDir+"/"+PDF_DIR);
				pdfDir.mkdir();
				File logoDir = new File(theDir+"/"+LOGO_DIR);
				logoDir.mkdir();

				actionExecuted = true;
				msgLbl.setText("Omgeving aangemaakt, vul deze directory met je input");
				msgLbl.setForeground(Color.GREEN);
			}
		});		
		JButton closeButton = new JButton("Afsluiten");
		pane.add(closeButton);
		closeButton.setLocation(210, 110);
		closeButton.setSize(160, 30);
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				ProcessResult result = ProcessResult.getInstance();
				result.setActionExecuted(actionExecuted);
				parentFrame.createEnvironmentFrameFinished(processResult);
				dispose();
			}
		});		
		this.setVisible(true);
	}
}
