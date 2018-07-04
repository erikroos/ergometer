import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class RaceLogic implements Runnable {
	
	private int raceType = 1;
	
	public void setRaceType(int raceType) {
		this.raceType = raceType;
	}
	
	public void run() {
		int distanceToRace = 0;
		int timeToRace = 0;
		JOptionPane.showMessageDialog(null, "Zet de ergometer aan of reset hem (knop ON/OFF)", "ErgoRacer", JOptionPane.PLAIN_MESSAGE);
		if (raceType == 1) {
			String timeString = JOptionPane.showInputDialog("Hoelang wil je roeien (min:sec)?", "20:00");
			String[] timeParts = timeString.split(":");
			timeToRace = (Integer.parseInt(timeParts[0]) * 60) + Integer.parseInt(timeParts[1]);
		} else if (raceType == 2) {
			String distString = JOptionPane.showInputDialog("Hoeveel meter wil je roeien?", "2000");
			distanceToRace = Integer.parseInt(distString);
		}
		String advSpeedString = JOptionPane.showInputDialog("Wat is je streef-500m-tijd (min:sec)?", "2:00");
		String[] advSpeedParts = advSpeedString.split(":");
		int advSpeed = (Integer.parseInt(advSpeedParts[0]) * 60) + Integer.parseInt(advSpeedParts[1]);
		
		ErgoCommunicator ergoCom = new ErgoCommunicator();
		boolean connectionSetUp = false;
		while (!connectionSetUp) {
			connectionSetUp = ergoCom.setUpConnection();
		}
		
		Thread thread = new Thread(ergoCom);
		thread.start();
		
		JFrame app = new JFrame("ErgoRacer");
		app.setFocusable(true);
		app.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // geef controle terug aan MainFrame
		RacePanel racePanel = new RacePanel();
		racePanel.setSize(800, 400);
		if (raceType == 1) {
			racePanel.setTimeToRace(timeToRace);
		} else if (raceType == 2) {
			racePanel.setDistanceToRace(distanceToRace);
		}
		racePanel.setAdvSpeedString(advSpeedString);
		app.add(racePanel);
		app.setSize(800, 400);
		app.setAutoRequestFocus(true);
		app.setVisible(true);
		
		// De game-loop:
		int distanceRaced = 0;
		long startTime = 0;
		while (true) {
			if (startTime == 0) {
				if (ergoCom.getElapsedTime() == 0) {
					continue;
				} else {
					startTime = System.currentTimeMillis();
					racePanel.setStarted();
				}
			}
			
			long timeElapsed = Math.round((System.currentTimeMillis() - startTime) / 1000.0);
			
			// Klaar?
			if ((distanceToRace > 0 && distanceRaced >= distanceToRace) ||
				(timeToRace > 0 && timeElapsed >= timeToRace)) {
				break;
			}
			
			racePanel.setElapsedTime(timeElapsed);
			racePanel.setTempo(ergoCom.getStrokeRate());
			racePanel.setAvgSpeedString(secsToMinSecString(ergoCom.getCurrent500mTime()));
			distanceRaced = ergoCom.getDistance();
			racePanel.setDistanceRaced(distanceRaced);
			int distanceRacedByAdv = (int) (timeElapsed / (double)advSpeed * 500);
			racePanel.setDistanceRacedByAdv(distanceRacedByAdv);
			
			racePanel.repaint();
		}
		
		long timeElapsed = Math.round((System.currentTimeMillis() - startTime) / 1000.0);
		long ave500mTime = Math.round(timeElapsed * 500 / (double)distanceRaced);
		JOptionPane.showMessageDialog(null, "Klaar! Je hebt " + distanceRaced + "m geroeid in " + secsToMinSecString(timeElapsed) + "; daarmee was je overall 500m-tijd " + secsToMinSecString(ave500mTime), "ErgoRacer", JOptionPane.PLAIN_MESSAGE);
	
		// Opslaan in DB
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection connect = DriverManager
			          .getConnection("jdbc:mysql://localhost/ergoracer?"
			              + "user=root&password=GWDraH7m");
			PreparedStatement preparedStatement = connect.prepareStatement("INSERT INTO races (totalTime, totalDist, ave500m, datum)" +
					"VALUES (" + timeElapsed + "," + distanceRaced + "," + ave500mTime + ",NOW())");
			preparedStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		ergoCom.shutDownConnection();
	}
	
	public static String secsToMinSecString(long secs) {
		long mins = secs / 60;
		secs = secs % 60;
		return String.format("%d", mins) + ":" + String.format("%02d", secs);
	}
}
