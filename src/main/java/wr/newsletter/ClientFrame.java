package wr.newsletter;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.json.JSONArray;
import org.json.JSONObject;

import wr.http.connection.HttpConnector;

public class ClientFrame extends AbstractFrame {
	public static final long serialVersionUID = 1;
	private String dataFile;
	private InputData inputData;
	private int y = 10;
	private String searchContent = "";
	private JLabel msgLabel;
	private JPanel pane;
	private JTable table;
	private int lastShown;
	private JSONObject[] clients;
	private JButton dataButton;
	private String offertenr = "9999-99";

	public static void main(String[] args) {
		try {
			new ClientFrame("/home/wim/eclipse-workspace/newsletter/pedicure").process();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ClientFrame(String env) {
		this.envDir = env;
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setSize(600, 580);
		this.setTitle("Ophalen clientgegevens");
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
	}

	public void process() {
		pane = new JPanel();
		this.add(pane);
		pane.setSize(this.getWidth(), this.getHeight());
		pane.setLayout(null);
		JLabel lbl1 = new JLabel(
				"Selecteer de spreadsheet. Het deel na de underscore wordt gebruikt als offertenummmer");
		lbl1.setSize(pane.getWidth(), 20);
		lbl1.setLocation(10, y);
		pane.add(lbl1);
		y += 30;
		String[] csvFiles = this.getCsvFiles(envDir);
		String[] csvCbList = new String[csvFiles.length + 1];
		csvCbList[0] = "Kies een input csv file";
		for (int i = 0; i < csvFiles.length; i++) {
			csvCbList[i + 1] = csvFiles[i];
		}
		final JComboBox<String> csvCb = new JComboBox<String>(csvCbList);
		pane.add(csvCb);
		csvCb.setSize(220, 20);
		csvCb.setLocation(20, y);
		y += 40;
		msgLabel = new JLabel();
		msgLabel.setSize(pane.getWidth(), 20);
		msgLabel.setLocation(10, y);
		pane.add(msgLabel);
		csvCb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final int csvIdx = csvCb.getSelectedIndex();
				if (csvIdx == 0) {
					return;
				}
				dataFile = csvCbList[csvIdx];
				int idx = dataFile.indexOf("_");
				int idx2 = dataFile.indexOf(".csv");
				if (idx < 1) {
					msgLabel.setForeground(Color.red);
					msgLabel.setText("De naam van de geselecteerde spreadsheet heeft geen offertenummer");
					return;
				}
				try {
					inputData = new InputData(envDir + "/" + dataFile);
					if (inputData.getDataLines().size() > 1) {
						msgLabel.setForeground(Color.red);
						msgLabel.setText("Een offerte mag maar 1 rij met klantgegevens bevatten");
						return;
					}
				} catch (Exception e1) {
					e1.printStackTrace();
					msgLabel.setText("Inlezen van de spreadsheet is mislukt");
					return;
				}
				offertenr = dataFile.substring(idx + 1, idx2);
				msgLabel.setText("In behandeling is offerte met nummer: " + offertenr);
				msgLabel.setForeground(Color.BLUE);
				doClientSearch(pane, y, msgLabel);
			}
		});
		this.setVisible(true);
	}

	private void doClientSearch(JPanel pane, int y, JLabel msgLabel) {
		JLabel c1Label = new JLabel("Zoek client door opgave van deel van de naam, minimaal 3 posities invoeren");
		c1Label.setSize(pane.getWidth(), 20);
		c1Label.setLocation(10, y);
		pane.add(c1Label);
		y += 30;
		TextInputField tif = new TextInputField(12);
		tif.setSize(120, 24);
		tif.setLocation(10, y);
		y += 30;
		pane.add(tif);
		msgLabel.setVisible(false);
		final JButton searchButton = new JButton("Zoek client(en)");
		pane.add(searchButton);
		searchButton.setSize(220, 30);
		searchButton.setLocation(10, y);
		y += 40;
		tif.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {
				searchContent = tif.getText();
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		final int vertical = y;
		searchButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				msgLabel.setText("");
				if (searchContent.length() < 3) {
					return;
				}
				String url = "http://localhost/codeigniter/api/customer/search";
				HashMap<String, String> map = new HashMap<String, String>(3);
				map.put("username", "wim");
				map.put("query", searchContent);
				map.put("token", "ddddddddddddd");
				try {
					String response = new HttpConnector().doPostRequest(url, map);
					processJsonResponse(response, vertical);
				} catch (Exception e1) {
					e1.printStackTrace();
					msgLabel.setText("Zoeken naar een client is foutief verlopen");
					msgLabel.setForeground(Color.RED);
				}
			}
		});
	}

	private void processJsonResponse(String response, int vertical) {
		if (table != null) {
			pane.remove(table);
		}
		y = vertical;
		JSONObject resp = new JSONObject(response);
		JSONArray arr = resp.getJSONArray("customers");
		msgLabel.setLocation(10, y);
		String[] columnNames = { "Naam", "Email" };
		if (arr.length() > 15) {
			msgLabel.setText("Te veel zoekresultaten: " + arr.length() + ". Verfijn je zoekgegeven");
			msgLabel.setForeground(Color.CYAN);
			msgLabel.setVisible(true);
			return;
		}
		if (arr.length() == 0) {
			msgLabel.setText("Geen client gevonden");
			msgLabel.setForeground(Color.CYAN);
			msgLabel.setVisible(true);
			return;
		}
		Object[][] data = new Object[arr.length()][];
		clients = new JSONObject[arr.length()];
		String[] dataContent;
		for (int i = 0; i < arr.length(); i++) {
			JSONObject obj = arr.getJSONObject(i);
			String name = (String) obj.get("fullname");
			String email = (String) obj.get("email");
			clients[i] = obj;
			dataContent = new String[2];
			dataContent[0] = name;
			dataContent[1] = email;
			data[i] = dataContent;
		}
		table = new JTable(data, columnNames);
		// JScrollPane scrollPane = new JScrollPane();
		// scrollPane.add(table);

		// pane.add(scrollPane);
		pane.add(table);
//		scrollPane.setSize(pane.getWidth(), 200);
//		scrollPane.setLocation(0, y);
		table.setSize(pane.getWidth() - 40, 160);
		table.setLocation(10, y);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//		TableColumn column = null;
//		for (int i = 0; i < 2; i++) {
//			column = table.getColumnModel().getColumn(i);
//			column.setPreferredWidth(50);
//
//		}
		y += 180;

		table.setRowSelectionAllowed(true);
		ListSelectionModel listModel = new DefaultListSelectionModel();
		table.setSelectionModel(listModel);
		listModel.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		listModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int lastSelected = lastShown;
				int selectedIndex = 0;
				if (!e.getValueIsAdjusting()) {
					return;
				}
				if (e.getLastIndex() == lastShown) {
					selectedIndex = e.getFirstIndex();
					lastSelected = selectedIndex;
				} else {
					selectedIndex = e.getLastIndex();
					lastSelected = selectedIndex;
				}
				lastShown = lastSelected;
				askConfirmation(data[selectedIndex], clients[selectedIndex]);
			}
		});
	}

	private void askConfirmation(Object[] data, JSONObject client) {
		if (dataButton == null) {
			msgLabel.setLocation(10, y);
			msgLabel.setForeground(Color.BLUE);
			msgLabel.setVisible(true);
			dataButton = new JButton("Akkoord?, Dan aanvullen spreadsheet");
			y += 30;
			dataButton.setSize(320, 30);
			dataButton.setLocation(10, y);
			pane.add(dataButton);
			dataButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ArrayList<ArrayList<String>> lines = inputData.getDataLines();
					ArrayList<String> line = lines.get(0);
					JSONArray arr = client.names();
					Map<String, Object> map = client.toMap();
					for (Map.Entry<String, Object> entry : map.entrySet()) {
						if (!entry.getKey().equals("onWaitingList")) {
							line.add(0, "" + entry.getValue());
						}
					}
					line.add(0, offertenr);
					line.add("N");
					ArrayList<String> headers = inputData.getHeaders();
					for (Map.Entry<String, Object> entry : map.entrySet()) {
						if (!entry.getKey().equals("onWaitingList")) {
							headers.add(0, entry.getKey());
						}
					}
					headers.add(0, "offertenr");
					headers.add("pdf");
					try {
						inputData.writeDataFile(envDir + "/" + PROGRESS_DIR + "/" + DATA_FILE);
						msgLabel.setText("Operatie is geslaagd");
						msgLabel.setForeground(Color.GREEN);
					} catch (IOException e1) {
						e1.printStackTrace();
						msgLabel.setText("Aanmaak spreadsheet met clientgegevens is mislukt");
						msgLabel.setForeground(Color.RED);
					}
				}
			});
		}
		msgLabel.setText("Client " + data[0] + ", met emailadres " + data[1] + " is geselecteerd.");
	}
}
