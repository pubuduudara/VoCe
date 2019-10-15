
import java.io.Serializable;

public class VoipDataPacket implements Serializable, Comparable<VoipDataPacket> {

	
	private static final long serialVersionUID = 1L;

	private int sequenceNumber;
	private byte[] data;
	private String user;

	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String ip) {
		this.user = ip;
	}
	//This is used for set sorting parameter
	public int compareTo(VoipDataPacket o) {
		if (this.getSequenceNumber() > o.getSequenceNumber())
			return 1;
		else if (this.getSequenceNumber() == o.getSequenceNumber())
			return 0;
		else
			return -1;
	}

}
