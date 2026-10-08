package wr.newsletter;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import wr.freemarker.Formatter;

public class Html2PdfTest extends JFrame implements ActionListener {
	public static final long serialVersionUID = 1;
	private static JLabel fileLabel;
	private static String inputFilename;
	private JLabel msgLabel;
	private JButton button2;
	private Formatter formatter;

	public Html2PdfTest() {
		this.setTitle("Test conversie template naar pdf");
		this.setSize(600, 400);
		this.setLocation(600, 0);
	}

	public void process() {
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JLabel introLabel = new JLabel("Selecteer inputbestand; output: output.pdf in directory van deze app.");
		// button to open open dialog
		JButton button1 = new JButton("Selecteer templatefile");

		// make an object of the class filechooser

		// add action listener to the button to capture user
		// response on buttons
		button1.addActionListener(this);
		button1.setActionCommand("select");

		// make a panel to add the buttons and labels
		JPanel p = new JPanel();
		p.add(introLabel);

		// add buttons to the frame
		p.add(button1);

		// set the label to its initial value
		fileLabel = new JLabel("geen bestand geselecteerd");

		// add panel to the frame
		p.add(fileLabel);
		add(p);
		button2 = new JButton("Converteer naar PDF");
		button2.setEnabled(false);
		p.add(button2);
		button2.setActionCommand("convert");
		button2.addActionListener(this);
		msgLabel = new JLabel();
		msgLabel.setForeground(Color.RED);
		p.add(msgLabel);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent evt) {
		msgLabel.setText("");
		if (evt.getActionCommand().equals("convert")) {
			if (inputFilename != null) {
				try {
					this.htmlfile2pdf(inputFilename, System.getProperty("user.dir") + "/output.pdf");
					msgLabel.setText("Uitgevoerd. Bekijk resultaat in output.pdf in actuele directory");
					msgLabel.setForeground(Color.BLACK);
				} catch (Exception e) {
					e.printStackTrace();
					msgLabel.setText("Conversie naar pdf is mislukt");
					msgLabel.setForeground(Color.RED);
				}
				return;
			}
		}
		JFileChooser j = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Template", "ftlx");
		j.setFileFilter(filter);

		// invoke the showsOpenDialog function to show the save dialog
		int r = j.showOpenDialog(null);

		// if the user selects a file
		if (r == JFileChooser.APPROVE_OPTION) {
			// set the label to the path of the selected file
			fileLabel.setText(j.getSelectedFile().getAbsolutePath());
			inputFilename = j.getSelectedFile().getAbsolutePath();
			button2.setEnabled(true);
		}
		// if the user cancelled the operation
		else {
			fileLabel.setText("geen selectie uitgevoerd");
			button2.setEnabled(false);
		}
	}

	private void htmlfile2pdf(String inputFilename, String output) throws Exception {
		File f = new File(inputFilename);
		String path = f.getParent();
		String name = f.getName();
		formatter = new Formatter(path);

		BufferedReader reader = new BufferedReader(new FileReader(inputFilename));
		StringBuffer sb = new StringBuffer();
		String line = null;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (!line.equals("")) {
				sb.append(line);
			}
		}
		reader.close();
		ArrayList<String> headers = new ArrayList<String>();
		ArrayList<String> cols = new ArrayList<String>();
		this.fillParameters(sb.toString(), headers, cols);
		String content = Helper.formatContent(formatter, headers, cols, name);
		Helper.html2pdf(content, output);
	}

	private void fillParameters(String contentLine, ArrayList<String> headers, ArrayList<String> cols) {
		String toReplace = null;
		int idx = contentLine.indexOf("${");
		int idxEnd = 0;
		while (idx >= 0) {
			idxEnd = contentLine.indexOf("}", idx);
			toReplace = contentLine.substring(idx + 2, idxEnd);
			if (!inHeaders(headers, toReplace)) {
				headers.add(toReplace);
				cols.add(toReplace);
			}
			idx = contentLine.indexOf("${", idx + 1);
		}
	}

	private boolean inHeaders(ArrayList<String> headers, String parm) {
		if (parm.equals("userdir")) {
			return true;
		}
		if (parm.equals("actual_date")) {
			return true;
		}
		if (parm.equals("actual_year")) {
			return true;
		}
		if (parm.equals("next_year")) {
			return true;
		}
		if (parm.equals("previous_year")) {
			return true;
		}		
	
		for (String header : headers) {
			if (header.equals(parm)) {
				return true;
			}
		}
		return false;
	}
}
