/*
*	CO324-project 1 Iteration 2 
*	Unit test for Audio class
*/

public class AudioTest{
	public static void main(String [] args){
		Audio test = new Audio();
		while(true){
			byte [] testbuff = test.captureAudio();
			test.playAudio(testbuff);		
		}
	}
}
