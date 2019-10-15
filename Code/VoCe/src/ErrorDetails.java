
import java.util.ArrayList;
/*This class hold information of each client's packet loss and no outof order packets*/ 
public class ErrorDetails {

	private ArrayList<Integer> loss;
	private int currentSequenceNo;

	private int out_of_order = -1;
	
	
	
	public ErrorDetails() {
		loss = new ArrayList<Integer>();
	}
	
	
	
	public int getOut_of_order() {
		return out_of_order;
	}
	public void setOut_of_order(int out_of_order) {
		this.out_of_order = out_of_order;
	}

	public int getCurrentSequenceNo() {
		return currentSequenceNo;
	}
	public void setCurrentSequenceNo(int currentSequenceNo) {
		this.currentSequenceNo = currentSequenceNo;
	}
	public void incrementOutOfOrder() {
		
		out_of_order++;
	}
	public void addSequenceNo(int sequence){
		
		loss.add(sequence);
	}
	
	public synchronized ArrayList<Integer> getLoss(){
		return loss;
	}
	
	public void clear(){
		
		out_of_order = 0;
		loss.clear();
	}
}
