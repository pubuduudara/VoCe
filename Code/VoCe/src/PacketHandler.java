
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class PacketHandler {

	private static Timer timer = new Timer();
	static Map<String, ArrayList<VoipDataPacket>> queue = new HashMap<String, ArrayList<VoipDataPacket>>();;
	static Map<String, ErrorDetails> errorDetails = new HashMap<String, ErrorDetails>();
	static int statusReportTime = 60000;
	public PacketHandler() {

	}

	private static synchronized int sizeOferrorDetails() {

		return errorDetails.size();
	}
	private static synchronized int sizeOfQueue() {

		return queue.size();
	}
	private static synchronized Set<String> getKeyset_Queue(){
		return queue.keySet();
	}
/*This is static method will execute every min 
	calculate loss and out of order packets
	printing result*/ 
	public static void setTimer() {

		timer.schedule(new TimerTask() {

			@Override
			public void run() {
			
				
				for (String user : getKeyset_ErrorDetails()) {
					ErrorDetails details = getDetails(user);
					ArrayList<Integer> list = details.getLoss();
					int size;
					synchronized(list){					
					Collections.sort(list);
					size = list.size();
					}
					double noOfPacketsSent=(list.get(size-1)-list.get(0) );
					double totalLoss = ((noOfPacketsSent-size)/noOfPacketsSent)*100.0;
					System.out.println(user+" Loss :"+totalLoss+"%"+" NO of out of order packets:"+details.getOut_of_order());
					details.clear();
					
				}
				
			}
		}, statusReportTime,statusReportTime);
	}
	/*
	*This method will store packet in queue and put metadata in database
	*/
	public static void hanldePacket(VoipDataPacket dataPacket) {

		String user = dataPacket.getUser();
		int sequenceNo = dataPacket.getSequenceNumber();
		ErrorDetails details=null, detailstmp = getDetails(user);
		if(detailstmp==null){
			details = new ErrorDetails();
			details.setCurrentSequenceNo(sequenceNo);
			
		}else{
			details = detailstmp;
		}
		int currentSequenceNo =details.getCurrentSequenceNo();
		//check whether sequence is correct or wrong 
		if ((sequenceNo - currentSequenceNo) != 1){
			details.incrementOutOfOrder();
		}
			
		details.setCurrentSequenceNo(sequenceNo);
		details.addSequenceNo(sequenceNo);
		putDetails(user, details);
		put(dataPacket, user);
	}
	/*This method is saving packet in the queue and its synchronized since timer threads are also accessing */
	public static synchronized void put(VoipDataPacket packet, String user) {
		
		if(queue.containsKey(user))
			queue.get(user).add(packet);
		else{
			ArrayList<VoipDataPacket> list = new ArrayList<VoipDataPacket>();
			list.add(packet);
			queue.put(user, list);
		} 
			

	}
	
	public static void sort() {

		
		
		Set<String> keySet = getKeyset_Queue();
		
		for (String key :keySet) {
			//loading data from the queue
			ArrayList<VoipDataPacket> list;
			synchronized (queue) {
				list = queue.get(key);
				Collections.sort(list);
			
			}
			
		}
	
	}

	private static synchronized Set<String> getKeyset_ErrorDetails(){
		return errorDetails.keySet();
	} 
	public static synchronized ErrorDetails getDetails(String user){
		
		return errorDetails.get(user);
	}

	public static synchronized void putDetails(String user ,ErrorDetails details){
		
		errorDetails.put(user, details);
	}
	public static void clear(){
		
		synchronized (queue) {
			queue.clear();
		}
		
	}
	public static ArrayList<VoipDataPacket> getData(){
		
		ArrayList<VoipDataPacket> data = new ArrayList<VoipDataPacket>();
		
		Set<String> keySet = getKeyset_Queue();
		for (String key :keySet) {
			ArrayList<VoipDataPacket> tmp;
			synchronized (queue) {
				
				tmp = queue.get(key);
				for(VoipDataPacket packet : tmp) data.add(packet);
			}
			
			
		}
		clear();
		return data;
	}
	
}
