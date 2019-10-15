
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

public class TestVoipClassMethods {

	VOIP voip = null;
	String Test = null;
	Random random;
	String user;
	@Before
	public void setUp() throws Exception {
		voip = new VOIP();
		Test = "Testing";
		random = new Random();
		user = InetAddress.getLocalHost().toString();
	}

	@Test
	public void testSerializeVoipPacket() {

		for (int i = 0; i < random.nextInt(300); i++) {
			VoipDataPacket voipDataPacket = new VoipDataPacket();

			voipDataPacket.setData(Test.getBytes());
			voipDataPacket.setSequenceNumber(random.nextInt());
			voipDataPacket.setUser(user);

			ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream;
			try {
				objectOutputStream = new ObjectOutputStream(arrayOutputStream);
				objectOutputStream.writeObject(voipDataPacket);

			} catch (IOException e) {
				e.printStackTrace();
			}
			// Expected Result
			byte[] data = arrayOutputStream.toByteArray();

			assertArrayEquals(data, voip.serializeVoipPacket(voipDataPacket));
		}
	}

	@Test
	public void testDeserializeVoipPacket() {

		for (int i = 0; i < random.nextInt(300); i++) {

			VoipDataPacket voipDataPacket = new VoipDataPacket();
			byte[] data = Test.getBytes();
			voipDataPacket.setData(data);
			voipDataPacket.setSequenceNumber(random.nextInt());
			voipDataPacket.setUser(user);

			byte packetData[] = voip.serializeVoipPacket(voipDataPacket);
			DatagramPacket packet = new DatagramPacket(packetData, packetData.length);

			

			assertEquals(voipDataPacket.getSequenceNumber(), voip.deserializeVoipPacket(packet).getSequenceNumber());
			assertEquals(voipDataPacket.getUser(), voip.deserializeVoipPacket(packet).getUser());
			assertEquals(data, voipDataPacket.getData());

		}
	}

}
