package wr.newsletter;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;

public class MainFrame extends AbstractFrame {

	public static final long serialVersionUID = 1;

	private String progressDir;

	private String env = "";
	private JPanel mainPanel;
	private InitFrame initFrame;
	private JButton initButton;
	private PdfFrame pdfFrame;
	private MailFrame mailFrame;
	private AddFrame addFrame;
	private JLabel messageLabel = new JLabel("");
	private JButton pdfButton;
	private JButton mailButton;
	private JButton addButton;
	private JButton cleanUpButton;
	private JButton testButton;
	private JComboBox<String> cb = new JComboBox<String>();
	private String[] cblist;
	private JLabel envLabel = new JLabel();
	private JButton createEnvButton;
	private JPanel actionPanel;
	private boolean dataInProgress;
	private File dataFile;

	public static void main(String[] args) {
		new MainFrame().setup();
	}

	private void setup() {
		super.setParent(this);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setSize(500, 480);
		this.setTitle("Mailingcampagne, kies omgeving");
		mainPanel = new JPanel();
		mainPanel.setLayout(null);
		mainPanel.setSize(this.getWidth(), this.getHeight());
		mainPanel.setLocation(0, 0);
		setLocationRelativeTo(null);
		messageLabel.setSize(mainPanel.getWidth(), 20);
		messageLabel.setLocation(10, 280);
		mainPanel.add(messageLabel);
		add(mainPanel);

		envLabel.setSize(200, 16);
		envLabel.setLocation(10, 40);
		mainPanel.add(envLabel);
		this.buildEnvComboBox();

		cb.setSize(250, 20);
		JLabel lbl1 = new JLabel("Keuze omgeving:");

		lbl1.setSize(150, 16);
		lbl1.setLocation(10, 10);

		mainPanel.add(lbl1);
		envLabel.setLocation(10, 32);
		envLabel.setSize(getWidth(), 20);
		mainPanel.add(envLabel);
		setHtmlTestButton(mainPanel);
		createEnvButton = new JButton("Creer een nieuwe omgeving");
		createEnvButton.setSize(220, 30);
		createEnvButton.setLocation(20, 80);
		mainPanel.add(createEnvButton);
		createEnvButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String dir = basePath.getAbsolutePath() + "/";
				CreateEnvironmentFrame cef = new CreateEnvironmentFrame(MainFrame.this);
				cef.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				cef.setLocation(420, 0);
				cef.setVisible(true);
				cef.process();
			}
		});
		//
		actionPanel = new JPanel();
		mainPanel.add(actionPanel);
		actionPanel.setLocation(0, 60);
		actionPanel.setSize(mainPanel.getWidth(), 260);
		actionPanel.setVisible(false);
		actionPanel.setLayout(null);
		setActionButtons();
		mainPanel.setBackground(Color.LIGHT_GRAY);
		actionPanel.setBackground(Color.LIGHT_GRAY);
		this.setVisible(true);
	}

	private void buildEnvComboBox() {

		mainPanel.add(cb);
		cb.setLocation(160, 10);

		cb.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				messageLabel.setText("");
				int idx = cb.getSelectedIndex();
				envLabel.setText("");
				if (idx > 0) {
					env = cblist[idx];
					envLabel.setText("Geselecteerde omgeving: " + env);
					envDir = basePath.getAbsolutePath() + "/" + env + "/";
					progressDir = envDir + "inprogress/";

					if (!checkBaseFiles()) {
						messageLabel.setText("Er ontbreekt een basisbesteand, .csv, .ftlx, .props bestand");
						return;
					}
					dataInProgress = false;
					dataFile = new File(envDir + PROGRESS_DIR + "/" + DATA_FILE);
					if (dataFile.exists()) {
						dataInProgress = true;
						inputData = new InputData(dataFile);
					}

					File theDir = new File(envDir + "/inprogress");
					if (!theDir.exists()) {
						theDir.mkdir();
					}
					File attach = new File(envDir + "/.no_attachment");
					if (attach.exists()) {
						mailWithAttachment = false;
					} else {
						mailWithAttachment = true;
					}
					actionPanel.setVisible(true);
					initButton.setEnabled(true);
					checkInitButton();
					checkAddButton();
					checkDocButton();
					checkMailButton();
					checkCleanUpButton();
					testButton.setVisible(false);
					createEnvButton.setVisible(false);
				} else {
					testButton.setVisible(true);
					createEnvButton.setVisible(true);
					actionPanel.setVisible(false);
				}
			}
		});
		String[] dirs = basePath.list(new FilenameFilter() {
			public boolean accept(File current, String name) {
				if (!new File(current, name).isDirectory()) {
					return false;
				}
				String s = name.substring(0, 1);
				if (s.equals(".")) {
					return false;
				}
				if (name.equals("src") || name.equals("target") || name.equals("lib")) {
					return false;
				}
				return true;
			}
		});

		cblist = new String[dirs.length + 1];
		cblist[0] = "Kies een omgeving....";
		for (int i = 0; i < dirs.length; i++) {
			cblist[i + 1] = dirs[i];
		}
		ComboBoxModel<String> model = new DefaultComboBoxModel<String>(cblist);
		cb.setModel(model);
	}

	private void setHtmlTestButton(JPanel pane) {
		testButton = new JButton("Test omzetten html naar pdf");
		pane.add(testButton);
		testButton.setEnabled(true);
		testButton.setSize(240, 30);
		testButton.setLocation(pane.getWidth() / 2, 80);
		testButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Html2PdfTest().process();
			}
		});
	}

	private boolean setActionButtons() {
		JLabel actionLabel = new JLabel("Mogelijke acties");
		actionPanel.add(actionLabel);
		actionLabel.setSize(mainPanel.getWidth(), 20);
		actionLabel.setLocation(10, 10);
		initButton = new JButton("Start campagne");
		initButton.setAlignmentY(Component.TOP_ALIGNMENT);
		actionPanel.add(initButton);
		initButton.setEnabled(false);
		initButton.setSize(200, 30);
		initButton.setLocation(10, 30);
		initButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (initFrame == null || !initFrame.isVisible()) {
					initFrame = new InitFrame(env, MainFrame.this, mailWithAttachment);
					initFrame.process();
					initFrame.setLocation(420, 0);
					initFrame.setVisible(true);
					initFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					messageLabel.setText("Start campagne");
					initButton.setEnabled(false);
					pdfButton.setEnabled(false);
					mailButton.setEnabled(false);
					addButton.setEnabled(false);
					cleanUpButton.setEnabled(false);
				}
				if (pdfFrame != null) {
					pdfFrame.dispose();
					pdfFrame = null;
				}
				if (mailFrame != null) {
					mailFrame.dispose();
					mailFrame = null;
				}
				if (addFrame != null) {
					addFrame.dispose();
					addFrame = null;
				}
			}
		});
		pdfButton = new JButton("Aanmaken documenten");
		actionPanel.add(pdfButton);
		pdfButton.setEnabled(false);
		pdfButton.setSize(200, 30);
		pdfButton.setLocation(10, 70);
		mailButton = new JButton("Versturen e-mails");
		actionPanel.add(mailButton);
		mailButton.setEnabled(false);
		mailButton.setSize(200, 30);
		mailButton.setLocation(10, 110);
		addButton = new JButton("Data regels toevoegen");
		actionPanel.add(addButton);
		addButton.setEnabled(false);
		addButton.setSize(200, 30);
		addButton.setLocation(10, 150);
		cleanUpButton = new JButton("Opruimen	");
		cleanUpButton.setEnabled(false);
		cleanUpButton.setSize(200, 30);
		cleanUpButton.setLocation(10, 190);
		actionPanel.add(cleanUpButton);
		pdfButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (pdfFrame == null || !pdfFrame.isVisible()) {
					pdfFrame = new PdfFrame(envDir, MainFrame.this);
					pdfFrame.process();
					pdfFrame.setLocation(420, 0);
					pdfFrame.setVisible(true);
					pdfFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					messageLabel.setText("Documenten worden aangemaakt");
					initButton.setEnabled(false);
					pdfButton.setEnabled(false);
					mailButton.setEnabled(false);
					addButton.setEnabled(false);
					cleanUpButton.setEnabled(false);
				}
				if (initFrame != null) {
					initFrame.dispose();
					initFrame = null;
				}
				if (mailFrame != null) {
					mailFrame.dispose();
					mailFrame = null;
				}
			}
		});
		mailButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (mailFrame == null || !mailFrame.isVisible()) {
					mailFrame = new MailFrame(envDir, MainFrame.this, mailWithAttachment);
					mailFrame.process();
					mailFrame.setLocation(420, 0);
					mailFrame.setVisible(true);
					mailFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					messageLabel.setText("Start versturen e-mails");
					initButton.setEnabled(false);
					pdfButton.setEnabled(false);
					mailButton.setEnabled(false);
					addButton.setEnabled(false);
					cleanUpButton.setEnabled(false);
				}
				if (initFrame != null) {
					initFrame.dispose();
					initFrame = null;
				}
				if (pdfFrame != null) {
					pdfFrame.dispose();
					pdfFrame = null;
				}
				if (addFrame != null) {
					addFrame.dispose();
					addFrame = null;
				}
			}
		});
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (addFrame == null || !addFrame.isVisible()) {
					addFrame = new AddFrame(envDir, MainFrame.this, mailWithAttachment);
					addWindowListener(addFrame);
					addFrame.process();
					addFrame.setLocation(420, 0);
					addFrame.setVisible(true);
					addFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					messageLabel.setText("Toevoegen data regels");
					initButton.setEnabled(false);
					pdfButton.setEnabled(false);
					mailButton.setEnabled(false);
					addButton.setEnabled(false);
					cleanUpButton.setEnabled(false);
				}
				if (initFrame != null) {
					initFrame.dispose();
					initFrame = null;
				}
				if (pdfFrame != null) {
					pdfFrame.dispose();
					pdfFrame = null;
				}
				if (mailFrame != null) {
					mailFrame.dispose();
					mailFrame = null;
				}
			}
		});
		cleanUpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File dir = new File(envDir + PROGRESS_DIR);
				File[] files = dir.listFiles();
				for (File file : files) {
					file.delete();
				}
				dir = new File(envDir + PDF_DIR);
				files = dir.listFiles();
				for (File file : files) {
					file.delete();
				}
				dir = new File(envDir + REPORT_DIR);
				files = dir.listFiles();
				for (File file : files) {
					file.delete();
				}
				messageLabel.setText("Actie uitgevoerd");
				checkInitButton();
				checkDocButton();
				checkMailButton();
				checkAddButton();
				checkCleanUpButton();
			}
		});
		return true;
	}

	private void checkInitButton() {
		initButton.setEnabled(true);
		if (checkProgressFiles()) {
			initButton.setText("Wijzig campagne");
		}
	}

	private void checkDocButton() {
		File theDir = new File(envDir + "inprogress");
		String[] files = theDir.list();
		int nFiles = (mailWithAttachment) ? 5 : 4;
		if (!mailWithAttachment) {
			pdfButton.setVisible(false);
		} else {
			pdfButton.setVisible(true);
		}
		if (files.length < nFiles) {
			pdfButton.setEnabled(false);
			return;
		}
		if (inputData.areAllMailsSent()) {
			pdfButton.setEnabled(false);
			return;
		}
		pdfButton.setEnabled(true);
	}

	private void checkMailButton() {
		File theDir = new File(envDir + "inprogress");
		String[] files = theDir.list();
		int nFiles = (mailWithAttachment) ? 5 : 4;
		if (files.length < nFiles) {
			mailButton.setEnabled(false);
			return;
		}
		// and ask if readyForMailing
		if (dataInProgress) {
			if (inputData.areAllMailsSent()) {
				mailButton.setEnabled(false);
			} else if (inputData.isReadyForMailing(mailWithAttachment)) {
				mailButton.setEnabled(true);
			}
		} else {
			mailButton.setEnabled(false);
		}
	}

	private void checkAddButton() {
		File theDir = new File(envDir + "inprogress");
		String[] files = theDir.list();
		int nFiles = (mailWithAttachment) ? 5 : 4;
		if (files.length < nFiles) {
			addButton.setEnabled(false);
			return;
		}
		if (dataInProgress) {
			if (inputData.areAllMailsSent()) {
				addButton.setEnabled(true);
			} else {
				addButton.setEnabled(false);
			}
		} else {
			cleanUpButton.setEnabled(false);
		}
	}

	private void checkCleanUpButton() {
		File theDir = new File(envDir + "inprogress");
		String[] files = theDir.list();
		int nFiles = (mailWithAttachment) ? 5 : 4;
		if (files.length < nFiles) {
			cleanUpButton.setEnabled(false);
			return;
		}
		if (dataInProgress) {
			if (inputData.areAllMailsSent()) {
				cleanUpButton.setEnabled(true);
			} else {
				cleanUpButton.setEnabled(false);
			}
		} else {
			cleanUpButton.setEnabled(false);
		}
	}

	private boolean checkBaseFiles() {
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
				if (name.indexOf(".ftlx") > 0) {
					return true;
				}
				if (name.indexOf(".props") > 0) {
					return true;
				}
				return false;
			}
		});
		int nFiles = (mailWithAttachment) ? 5 : 4;
		if (files.length >= nFiles) {
			return true;
		}
		return false;
	}

	private boolean checkProgressFiles() {
		String[] files = new File(progressDir).list(new FilenameFilter() {
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
				if (name.indexOf(".ftlx") > 0) {
					return true;
				}
				if (name.indexOf(".props") > 0) {
					return true;
				}
				return false;
			}
		});
		int nFiles = (mailWithAttachment) ? 5 : 4;
		if (files.length >= 5) {
			return true;
		}
		return false;
	}

	public void setErrorMessage(String msg) {
		messageLabel.setText(msg);
	}

	public void createEnvironmentFrameFinished(ProcessResult processResult) {
		if (processResult.isActionExecuted()) {
			buildEnvComboBox();
		}
	}

	public void initFrameFinished(ProcessResult processResult) {
		if (!processResult.hasResultErrors() && processResult.isActionExecuted()) {
			messageLabel.setText("Bestanden zijn gecontroleerd en overgezet");
			dataInProgress = true;
		} else if (processResult.hasResultErrors() && processResult.isActionExecuted()) {
			messageLabel.setText("Er zijn fouten geconstateerd bij overzetten bestanden");
		} else {
			messageLabel.setText("Je hebt de verwerking afgebroken!");
		}
		if (!processResult.hasResultErrors()) {
			inputData = new InputData(envDir + PROGRESS_DIR + "/" + DATA_FILE);
			this.checkDocButton();
			this.checkMailButton();
			this.checkAddButton();
			this.checkCleanUpButton();
		}
		processResult.cleanup();
		initButton.setEnabled(true);
		
	}

	public void pdfFrameFinished(ProcessResult processResult) {
		inputData = processResult.getInputData();
		if (!processResult.hasResultErrors() && processResult.isActionExecuted()) {
			messageLabel.setText(processResult.getCreatedItems() + " document(en) aangemaakt");
			dataInProgress = true;
		} else if (processResult.hasResultErrors() && processResult.isActionExecuted()) {
			messageLabel.setText("Er zijn fouten opgetreden bij aanmaken documenten");
		} else {
			messageLabel.setText("Je hebt de verwerking afgebroken!");
		}
		processResult.cleanup();
		pdfButton.setEnabled(true);
		pdfButton.setText("Aanmaken documenten");
		this.checkInitButton();
		this.checkDocButton();
		this.checkMailButton();
		this.checkAddButton();
		this.checkCleanUpButton();
	}

	public void mailFrameFinished(ProcessResult processResult) {
		if (!processResult.hasResultErrors() && processResult.isActionExecuted()) {
			messageLabel.setText(processResult.getCreatedItems() + " e-mails verstuurd");
			inputData = processResult.getInputData();
			dataInProgress = true;
		} else if (processResult.hasResultErrors() && processResult.isActionExecuted()) {
			messageLabel.setText("Er zijn fouten opgetreden bij versturen e-mails");
		} else {
			messageLabel.setText("Je hebt de verwerking afgebroken!");
		}
		processResult.cleanup();
		this.checkInitButton();
		this.checkDocButton();
		this.checkMailButton();
		this.checkAddButton();
		this.checkCleanUpButton();
	}

	public void addFrameFinished(ProcessResult processResult) {
		if (!processResult.hasResultErrors() && processResult.isActionExecuted()) {
			messageLabel.setText("Data regels zijn toegevoegd.");
			inputData = processResult.getInputData();
			dataInProgress = true;
		} else if (processResult.hasResultErrors() && processResult.isActionExecuted()) {
			messageLabel.setText("Er zijn fouten geconstateerd");
			addButton.setEnabled(true);
		} else {
			messageLabel.setText("Je hebt de verwerking afgebroken!");
		}
		this.checkInitButton();
		this.checkDocButton();
		this.checkMailButton();
		this.checkAddButton();
		this.checkCleanUpButton();
		processResult.cleanup();
	}

	private void addWindowListener(JFrame aFrame) {
		aFrame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				if (aFrame instanceof AddFrame) {
					messageLabel.setText("Einde toevoegen data regels");
				}
			}
		});
	}
}
