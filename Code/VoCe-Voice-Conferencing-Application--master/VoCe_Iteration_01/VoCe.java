/*
 * CO 324 - Network and Web programming
 * Project I
 * Main program which is used to connect between two clients  
 */



import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.* ;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class VoCe extends Thread {

        public static boolean client=false;
        /*State =1 is for reciving loop state = 2 for recording and sending packet loop State = 3 for playback loop*/
        public int state = 0;
        public static int packetsize = 500 ;       
        public static InetAddress uplink_address = null;
        public static InetAddress downlink_address = null;
        public static InetAddress server_address = null;
        public static InetAddress client_address = null;
        public  static int server_port = 12000 ;
        public  static int client_port = -1 ;
        public  static int downlink_port = -1 ;
        public  static int uplink_port = -1 ;
        public static DatagramSocket socket_uplink = null;
        public static DatagramSocket socket_downlink = null;
     	public static Audio audio = new Audio();
     	public static Serialization serial = new Serialization();


public VoCe(int state) throws IOException {
    
    this.state = state;

}

public static void main(String[] args) throws IOException{

        System.out.println("Threshold "+Serialization.threshold);
    
      if( args.length > 1 )
      {
         System.out.println( "usage: java Client host or java Server" ) ;
         return ;
      }
      
      else if(args.length == 1){
            client = true;
            server_address = InetAddress.getByName( args[0] ) ;
            
     }
      
     else{
            client=false;
     }

     
     if(!client){    //application run as Server 

	        try{
	            
	            socket_downlink = new DatagramSocket( server_port ) ;
	            downlink_port = server_port;
	            DatagramPacket packet = new DatagramPacket ( new byte[packetsize] , packetsize );                // Prepare the packet for receive

                // Wait for a response from the server
                System.out.println("Waiting for a call...... ");
                 socket_downlink.receive( packet ) ;
                System.out.println("Incomming call.... Press Eneter key to answer");            

              
                // Asking user to answer the call 
                Scanner scanner = new Scanner(System.in);
                while(true){
                    String readString = scanner.nextLine();
                        if(readString.isEmpty())break;
                }
                scanner.close(); 
                
                
                client_address = packet.getAddress();
                ByteBuffer wrapped = ByteBuffer.wrap(packet.getData());
                client_port = wrapped.getInt();
                
                           byte [] data = "Client has Answerd your call...".getBytes() ;
                 
                                             
                socket_uplink = new DatagramSocket(); 
               
                DatagramPacket packet_send = new DatagramPacket ( data , data.length , client_address , client_port );
		        Thread.sleep(100);
                socket_uplink.send( packet_send ) ;
                //System.out.println("Connecting Call.....");
                }
                 catch( Exception e )
                {
                    //System.out.println( e ) ;
                }
     
	    }
	    
	    else{   //application run as client
	    
             
	            try{            
                    	            
	                socket_uplink = new DatagramSocket();
                    socket_downlink = new DatagramSocket();
                    downlink_port = socket_downlink.getLocalPort();
                    
                    /*Sending the downlink port to other side for ask that side user to send data to this downlink_port */
                    ByteBuffer b = ByteBuffer.allocate(4);
                    b.putInt(downlink_port);    
                    byte [] data = b.array();
                               
                    DatagramPacket packet = new DatagramPacket ( data , data.length , server_address , server_port );
					client_address = server_address;
					client_port = server_port;
		    
                    socket_uplink.send( packet ) ;
               
                   
                    packet.setData( new byte[packetsize] ) ;
                    
                    System.out.println("Waiting for an answer from other end....."); 
                    socket_downlink.receive( packet ) ;
                    System.out.println( new String(packet.getData()) ) ;
                
                
                }
                catch( Exception e ){
                    System.out.println( e ) ;
                }
     
	    }
	    
	    
	    Thread transmission = new Thread(new VoCe(1)); //new thread started for transmission
		Thread recive = new Thread(new VoCe(2));     	//new thread started for recive packets
		Thread play = new Thread(new VoCe(3));    	 	//new thread started for playback
	    
	    transmission.start();
	    recive.start();
	    play.start();
             
   
      
    }   


    public void run(){
        int send=0,recive=0;
        if(state ==2 ){
        	while(true){

				  byte [] data = audio.captureAudio();
				  byte [] temp_data = serial.serialize(data);  	//serialize the packet of audio to send the otherside whith sequence no.

              try{
             
              DatagramPacket packet = new DatagramPacket ( temp_data , temp_data.length , client_address , client_port );
              
              socket_uplink.send( packet ) ;    // Send the packet
              }catch( Exception e ){

                    System.out.println( e ) ;
              }

              send++;
 				   
        	}
    	}    


        else if (state == 1){
            while(true){  	
                 try{
                 DatagramPacket packet = new DatagramPacket ( new byte[packetsize] , packetsize  );       // Prepare the packet for receive   
                 // Wait for a response from the other peer
                 socket_downlink.receive( packet ) ;
                 serial.deSerialize(packet.getData());
                 }

                 catch( Exception e ){
                        System.out.println( e ) ;
                 }

                 recive++;
        	}
		
    	}

    	else if (state ==3){


    		while(true){

    			byte [] temp = serial.getPacket();               
				audio.playAudio(temp);

    		}
    	}
	}

}
