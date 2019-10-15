
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.*;
import java.net.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class VOIP {

	boolean stopCapture = false;
	ByteArrayOutputStream byteArrayOutputStream;
	AudioFormat audioFormat;
	TargetDataLine targetDataLine;
	AudioInputStream audioInputStream;
	SourceDataLine sourceDataLine;
	static InetAddress host;
	static int port;
	
	private final static int packetsize = 1400;
	private final static int soundBuffer = 1100;
	private final static long Waiting_time = 300;// wait for 300ms
	private static String userName = null;
	
	
	private AudioFormat getAudioFormat() {
		float sampleRate = 16000.0F;
		int sampleSizeInBits = 8;
		int channels = 2;
		boolean signed = true;
		boolean bigEndian = true;
		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
	}

	private void get_and_play_audioStreme(InetAddress host, int port) {
		
		byte got_tempBuffer[] = new byte[packetsize];
		
		stopCapture = false;

		try {
			//MulticastSocket socket = new MulticastSocket(port);
			DatagramSocket socket = new DatagramSocket(port);
			DatagramPacket packet = new DatagramPacket(got_tempBuffer, packetsize);
			System.out.println("Sever is ready to recieve data");

			try {

				audioFormat = getAudioFormat(); // get the audio format

				DataLine.Info dataLineInfo1 = new DataLine.Info(SourceDataLine.class, audioFormat);
				sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo1);
				sourceDataLine.open(audioFormat);
				sourceDataLine.start();

				// Setting the maximum volume
				FloatControl control = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
				control.setValue(control.getMaximum());
				//join multicast group
				//socket.joinGroup(host);
				//Timer for sorting and play
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					
					@Override
					public void run() {
						
						PacketHandler.sort();//sorting packets in the queue
						ArrayList<VoipDataPacket> data = PacketHandler.getData();
						
						for(VoipDataPacket packet : data) sourceDataLine.write(packet.getData(), 0, soundBuffer);//write packets into the source Dataline
					}
				}, Waiting_time,Waiting_time);
				while (true) {
					socket.receive(packet);
					
					VoipDataPacket dataPacket = deserializeVoipPacket(packet);
					PacketHandler.hanldePacket(dataPacket);
					
				}

			} catch (LineUnavailableException e) {
				e.printStackTrace();
				System.exit(0);
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		
	}
	/*This method will convert DatagramPacket into VoipDataPacket object*/
	public VoipDataPacket deserializeVoipPacket(DatagramPacket packet){
		ByteArrayInputStream inputStream = new ByteArrayInputStream(packet.getData());
		ObjectInputStream objectInputStream = null;
		try {
			objectInputStream = new ObjectInputStream(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		VoipDataPacket dataPacket = null;
		try {
			dataPacket = (VoipDataPacket) objectInputStream.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dataPacket;
	}
	private void capture_and_send_audioStreme(InetAddress host, int port) {

		stopCapture = false;

		try {

			Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo(); // get
																	// available
																	// mixers
			System.out.printf("%d\n", mixerInfo.length);
			System.out.println("Available mixers:");
			Mixer mixer = null;
			for (int cnt = 0; cnt < mixerInfo.length; cnt++) {

				System.out.println(cnt + " " + mixerInfo[cnt].getName());
				mixer = AudioSystem.getMixer(mixerInfo[cnt]);

				Line.Info[] lineInfos = mixer.getTargetLineInfo();

				if (lineInfos.length >= 1 && lineInfos[0].getLineClass().equals(TargetDataLine.class)) {
					System.out.println(cnt + " Mic is supported!");
					System.out.println("Now you are able to talk");
					break;
				}

			}

			audioFormat = getAudioFormat(); // get the audio format

			DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
			targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
			targetDataLine.open(audioFormat);
			targetDataLine.start();

		
			try {
				//MulticastSocket socket = new MulticastSocket(port);
				//socket.setLoopbackMode(true);
				//socket.joinGroup(host);
				DatagramSocket socket = new DatagramSocket();
				int readCount;
				
				byte[] tempBuffer = new byte[soundBuffer];
				int i = 1;

				while (!stopCapture) {
					readCount = targetDataLine.read(tempBuffer, 0, tempBuffer.length); // capture
																						// sound

					if (readCount > 0) {
						

						VoipDataPacket voipDataPacket = new VoipDataPacket();
						voipDataPacket.setData(tempBuffer);
						voipDataPacket.setSequenceNumber(i);
						voipDataPacket.setUser(userName);
					

						byte[] data =	serializeVoipPacket(voipDataPacket);
						
						
						DatagramPacket packet = new DatagramPacket(data, packetsize, host, port);
						
						socket.send(packet);

						i++;// seqeunce number

					}
				}

			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}

		} catch (LineUnavailableException e) {
			System.out.println(e);
			System.exit(0);
		}

	}
	/*This method will convert VoipDataPacket byte array*/
public byte[] serializeVoipPacket(VoipDataPacket voipDataPacket){
	
	ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
	ObjectOutputStream objectOutputStream;
	try {
		objectOutputStream = new ObjectOutputStream(arrayOutputStream);
		objectOutputStream.writeObject(voipDataPacket);
	
	} catch (IOException e) {
		e.printStackTrace();
	}

	byte[] data = arrayOutputStream.toByteArray();

	return data;
}
	public static void main(String args[]) {

		 if (args.length != 2) {
			 System.out.println("usage: Please Enter host and then port");
			 return;
		 }

		try {
			// Convert the arguments to ensure that they are valid
			 host = InetAddress.getByName(args[0]);
			
			port = Integer.parseInt(args[1]);
			//set user name to host ip address
			userName =InetAddress.getLocalHost().toString();
		
			final VOIP w = new VOIP();
			// define two threads which will execute capturing sound,sending and receive,play
			Thread t1 = new Thread(new Runnable() {
				public void run() {
					w.capture_and_send_audioStreme(host, port);

				}

			});

			Thread t2 = new Thread(new Runnable() {
				public void run() {
					w.get_and_play_audioStreme(host, port);

				}

			});

			// Starting the threads
			t1.start();
			Thread.sleep(1000);
			t2.start();
			Thread.sleep(1000);
			//enable status handling
			PacketHandler.setTimer();
		} catch (Exception e) {
			System.out.println(e);
		}

	}

}
