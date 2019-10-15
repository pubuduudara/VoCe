
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

public class TestPacketHandler {

	int sequenceNo;
	String user;
	ErrorDetails errorDetails;
	VoipDataPacket voipDataPacket;
	Random random;
	@Before
	public void setUp() {

		 random = new Random();

		sequenceNo = random.nextInt();
		user = "Testing";
		// Datapcaket for testing
		voipDataPacket = new VoipDataPacket();
		voipDataPacket.setUser(user);
		voipDataPacket.setSequenceNumber(sequenceNo);
		voipDataPacket.setData(user.getBytes());

		// ErrorDetails instance
		errorDetails = new ErrorDetails();
		errorDetails.setCurrentSequenceNo(sequenceNo);
		errorDetails.addSequenceNo(sequenceNo);
	}

	@Test
	public void test_putDetails() {

		PacketHandler.putDetails(user, errorDetails);
		ErrorDetails result = PacketHandler.getDetails(user);
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(sequenceNo);

		assertEquals("ErrorDetails Instance : sequence Number", sequenceNo, result.getCurrentSequenceNo());
		assertEquals("ErrorDetails Instance : ArrayList<Integer> loss ", list, result.getLoss());
		assertEquals("ErrorDetails Instance : OutOfOrder Number ", -1, result.getOut_of_order());

	}

}
