import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;


public class MainFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2949687444255909471L;

	public MainFrame() {
		// Menu
		JMenu fileMenu = new JMenu("Bestand");
		JMenuItem exitItem = new JMenuItem("Afsluiten");
		fileMenu.add(exitItem);
		exitItem.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						System.exit(0);
					}
				}
		);
		JMenu raceMenu = new JMenu("Race");
		JMenuItem timeItem = new JMenuItem("Op tijd");
		raceMenu.add(timeItem);
		timeItem.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						RaceLogic rl = new RaceLogic();
						rl.setRaceType(1);
						Thread thread = new Thread(rl);
						thread.start();
					}
				}
		);
		JMenuItem distItem = new JMenuItem("Op afstand");
		raceMenu.add(distItem);
		distItem.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						RaceLogic rl = new RaceLogic();
						rl.setRaceType(2);
						Thread thread = new Thread(rl);
						thread.start();
					}
				}
		);
		JMenuBar bar = new JMenuBar();
		setJMenuBar(bar);
		bar.add(fileMenu);
		bar.add(raceMenu);
		
		drawHiscores();
	}

	private void drawHiscores() {
		String hiscores = "Laatste 5 races:\n";
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection connect = DriverManager
			          .getConnection("jdbc:mysql://localhost/ergoracer?"
			              + "user=root&password=GWDraH7m");
			Statement statement = connect.createStatement();
		    ResultSet resultSet = statement.executeQuery("SELECT totalTime, totalDist, ave500m, datum FROM races WHERE userId = 1 ORDER BY datum DESC LIMIT 5");
		    while (resultSet.next()) {
		    	hiscores += resultSet.getString(4) + ": tijd " + RaceLogic.secsToMinSecString(resultSet.getInt(1)) + "; afstand " +
		    			resultSet.getInt(2) + "m; gem. 500m-tijd " + RaceLogic.secsToMinSecString(resultSet.getInt(3)) + "\n";
		    }
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		JTextArea textArea = new JTextArea(6, 30);
		textArea.setText(hiscores);
		textArea.setEnabled(false);
		textArea.setDisabledTextColor(Color.BLACK);
		add(textArea);
	}
}
