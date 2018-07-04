import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;

import javax.swing.JPanel;


public class RacePanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -349572931818619784L;
	private int distanceToRace = 0;
	private int distanceRaced = 0;
	private int distanceRacedByAdv = 0;
	private int timeToRace = 0;
	private boolean started = false;
	private long elapsedTime = 0;
	private int tempo = 0;
	private int advTempo = 22;
	private String avgSpeedString = "0:00";
	private String advSpeedString = "0:00";
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// Posities op de baan
		int x = 0;
		int xAdv = 0;
		if (timeToRace > 0) {
			// Bij tijdrace laten we 2K zien
			x = (int) ((distanceRaced % 2000) / 2000.0 * (this.getWidth() - 40));
			// "Tegenstander" verschuift weer naar de 0 als hijzelf over de 2K gaat
			xAdv = (int) ((distanceRacedByAdv % 2000) / 2000.0 * (this.getWidth() - 40));
		} else if (distanceToRace > 0) {
			// Bij afstandsrace is de baan zo lang als de afstand
			x = (int) (distanceRaced / (double)distanceToRace * (this.getWidth() - 40));
			xAdv = (int) (distanceRacedByAdv / (double)distanceToRace * (this.getWidth() - 40));
		}
		
		// De baan
		g.setColor(Color.GREEN);
		g.fillRect(0, 0, 800, 50);
		g.fillRect(0, 150, 800, 50);
		g.setColor(Color.BLUE);
		g.fillRect(0, 50, 800, 100);
		g.setColor(Color.BLACK);
		g.fillRect(0, 200, 800, 200);
		
		// Eigen bootje
		g.setColor(Color.WHITE);
		g.fillRect(x + 16, 65, 8, 20);
		Polygon polygon = new Polygon();
		polygon.addPoint(x, 75);
		polygon.addPoint(x + 16, 65);
		polygon.addPoint(x + 16, 85);
		g.fillPolygon(polygon);
		polygon = new Polygon();
		polygon.addPoint(x + 24, 65);
		polygon.addPoint(x + 40, 75);
		polygon.addPoint(x + 24, 85);
		g.fillPolygon(polygon);
		// Riemen
		double strokeTime = this.elapsedTime * this.tempo / 60.0;
		int strokePhase = (int) Math.floor((strokeTime - Math.floor(strokeTime)) * 100);
		if (strokePhase < 25) {
			// intik
			g.drawLine(x + 14, 55, x + 20, 65);
			g.drawLine(x + 14, 95, x + 20, 85);
		} else if (strokePhase < 50) {
			// orthogonaal
			g.drawLine(x + 20, 55, x + 20, 65);
			g.drawLine(x + 20, 95, x + 20, 85);
		} else if (strokePhase < 75) {
			// uittik
			g.drawLine(x + 26, 55, x + 20, 65);
			g.drawLine(x + 26, 95, x + 20, 85);
		} else {
			// orthogonaal
			g.drawLine(x + 20, 55, x + 20, 65);
			g.drawLine(x + 20, 95, x + 20, 85);
		}
		
		// Tegenstander
		g.setColor(Color.RED);
		g.fillRect(xAdv + 16, 115, 8, 20);
		polygon = new Polygon();
		polygon.addPoint(xAdv, 125);
		polygon.addPoint(xAdv + 16, 115);
		polygon.addPoint(xAdv + 16, 135);
		g.fillPolygon(polygon);
		polygon = new Polygon();
		polygon.addPoint(xAdv + 24, 115);
		polygon.addPoint(xAdv + 40, 125);
		polygon.addPoint(xAdv + 24, 135);
		g.fillPolygon(polygon);
		// Riemen
		strokeTime = this.elapsedTime * this.advTempo / 60.0;
		strokePhase = (int) Math.floor((strokeTime - Math.floor(strokeTime)) * 100);
		if (strokePhase < 25) {
			// intik
			g.drawLine(xAdv + 14, 105, xAdv + 20, 115);
			g.drawLine(xAdv + 14, 145, xAdv + 20, 135);
		} else if (strokePhase < 50) {
			// orthogonaal
			g.drawLine(xAdv + 20, 105, xAdv + 20, 115);
			g.drawLine(xAdv + 20, 145, xAdv + 20, 135);
		} else if (strokePhase < 75) {
			// uittik
			g.drawLine(xAdv + 26, 105, xAdv + 20, 115);
			g.drawLine(xAdv + 26, 145, xAdv + 20, 135);
		} else {
			// orthogonaal
			g.drawLine(xAdv + 20, 105, xAdv + 20, 115);
			g.drawLine(xAdv + 20, 145, xAdv + 20, 135);
		}
		
		g.setFont(new Font("SansSerif", Font.BOLD, 24));
		if (!started) {
			g.setColor(Color.WHITE);
			g.drawString("Begin met roeien om de race te starten...", 0, 250);
		} else {
			g.setColor(Color.WHITE);
			g.drawString(RaceLogic.secsToMinSecString(elapsedTime), 0, 250);
			g.drawString(distanceRaced + "m", 100, 250);
			g.drawString(this.avgSpeedString + " / 500m", 200, 250);
			g.drawString("Tempo " + this.tempo, 350, 250);
			g.setColor(Color.RED);
			g.drawString(distanceRacedByAdv + "m", 100, 300);
			g.drawString(this.advSpeedString + " / 500m", 200, 300);
			g.drawString("Tempo " + this.advTempo, 350, 300);
			Graphics2D g2 = (Graphics2D) g;
			g2.setStroke(new BasicStroke(10));
			if (this.distanceRaced > this.distanceRacedByAdv) {
				g2.setColor(Color.WHITE);
				g2.drawArc(550, 200, 100, 100, 180, 180);
			} else {
				g2.setColor(Color.RED);
				g2.drawArc(550, 250, 100, 100, 0, 180);
			}
		}
	}

	public void setDistanceToRace(int distanceToRace) {
		this.distanceToRace = distanceToRace;
	}

	public void setDistanceRaced(int distanceRaced) {
		this.distanceRaced = distanceRaced;
	}

	public void setDistanceRacedByAdv(int distanceRacedByAdv) {
		if (this.timeToRace > 0) {
			// Tijdrace: "tegenstander" roeit altijd door
			this.distanceRacedByAdv = distanceRacedByAdv;
		} else if (this.distanceToRace > 0) {
			// Afstandsrace: "tegenstander" stopt bij einde baan
			this.distanceRacedByAdv = Math.min(distanceToRace, distanceRacedByAdv);
		}
	}
	
	public void setTimeToRace(int timeToRace) {
		this.timeToRace = timeToRace;
	}
	
	public void setStarted() {
		this.started = true;
	}
	
	public void setElapsedTime(long timeElapsed) {
		this.elapsedTime = timeElapsed;
	}
	
	public void setTempo(int tempo) {
		this.tempo = tempo;
	}
	
	public void setAvgSpeedString(String avgSpeedString) {
		this.avgSpeedString = avgSpeedString;
	}
	
	public void setAdvSpeedString(String advSpeedString) {
		this.advSpeedString = advSpeedString;
	}
}
