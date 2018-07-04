import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.comm.CommPortIdentifier;
import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;

public class ErgoCommunicator implements Runnable {

	private int distance = 0;
	private int elapsedTime = 0;
	private int strokeRate = 0;
	private float secondsPerMeter = (float)0.0;
	private SerialPort serialPort = null;
	private OutputStream outputStream = null;
	private InputStream inputStream = null;
	
	@Override
	public void run() {
		while (true) {
			readDistance();
			if (readTime()) {
				readPace();
			}
		}
	}
	
	public boolean setUpConnection() {
		try {
			CommPortIdentifier portId1 = CommPortIdentifier.getPortIdentifier("COM1");
			this.serialPort = (SerialPort) portId1.open("ErgoCommunicator", 2000);
			this.serialPort.setSerialPortParams(9600,
	                SerialPort.DATABITS_8,
	                SerialPort.STOPBITS_1,
	                SerialPort.PARITY_NONE);
		} catch (NoSuchPortException e) {
			e.printStackTrace();
			return false;
		} catch (PortInUseException e) {
			e.printStackTrace();
			return false;
		} catch (UnsupportedCommOperationException e) {
			e.printStackTrace();
			return false;
		}
		try {
			this.outputStream = this.serialPort.getOutputStream();
			this.inputStream = this.serialPort.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		// Testen of er wel iets binnenkomt:
		byte[] bytes = new byte[2];
		bytes[0] = (byte) (0xb3);
		bytes[1] = (byte) (0x00);
		try {
			outputStream.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		try {
			byte[] recBytes = new byte[1];
			inputStream.read(recBytes, 0, 1); // blokkeert tot er iets binnenkomt
			// Rest ook inlezen
			recBytes = new byte[4];
			inputStream.read(recBytes, 0, 4);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void shutDownConnection() {
		this.serialPort.close();
	}

	private void readPace() {
		byte[] bytes = new byte[2];
		bytes[0] = (byte) (0xb1);
		bytes[1] = (byte) (0x00);
		try {
			outputStream.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			this.strokeRate = inputStream.read();
			
			byte[] recBytes = new byte[4];
			inputStream.read(recBytes, 0, 4);
			ByteBuffer buffer = ByteBuffer.wrap(recBytes);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			this.secondsPerMeter = buffer.getFloat();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean readTime() {
		boolean endOfStroke = false;
		byte[] bytes = new byte[2];
		bytes[0] = (byte) (0xb3);
		bytes[1] = (byte) (0x00);
		try {
			outputStream.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			byte[] recBytes = new byte[1];
			inputStream.read(recBytes, 0, 1);
			byte statusByte = recBytes[0];
			if ((statusByte & (byte)(0x02)) != 0) { // klopt dit?
				endOfStroke = true;
			}
			recBytes = new byte[4];
			inputStream.read(recBytes, 0, 4);
			ByteBuffer buffer = ByteBuffer.wrap(recBytes);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			this.elapsedTime = Math.round(buffer.getFloat());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return endOfStroke;
	}

	private void readDistance() {
		byte[] bytes = new byte[2];
		bytes[0] = (byte) (0xb0);
		bytes[1] = (byte) (0x00);
		try {
			outputStream.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			byte[] recBytes = new byte[1];
			inputStream.read(recBytes, 0, 1);
			//byte statusByte = recBytes[0];
			recBytes = new byte[4];
			inputStream.read(recBytes, 0, 4);
			ByteBuffer buffer = ByteBuffer.wrap(recBytes);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			this.distance = Math.round(buffer.getFloat());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getDistance() {
		return distance;
	}

	public int getElapsedTime() {
		return elapsedTime;
	}

	public int getStrokeRate() {
		return strokeRate;
	}

	public int getCurrent500mTime() {
		return Math.round(this.secondsPerMeter * 500);
	}

	public int getAve500mTime() {
		if (this.distance == 0) {
			return 0;
		}
		return Math.round(this.elapsedTime * 500 / (float)this.distance);
	}
	
}
