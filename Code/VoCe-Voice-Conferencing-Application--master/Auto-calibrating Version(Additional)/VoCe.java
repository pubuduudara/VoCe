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

        public final static int packetsize = 500 ;
        public static boolean client=false;
        /*State =1 is for reciving loop state = 2 for recording and sending packet loop State = 3 for playback loop*/
        public int state = 0;

        public static InetAddress server_address = null;
        public static InetAddress client_address = null;
        public  static int server_port = 12000 ;
        public  static int client_port = -1 ;
        public  static int downlink_port = -1 ;
      
        public static DatagramSocket socket_uplink = null;
        public static DatagramSocket socket_downlink = null;
     
     	public static Audio audio = new Audio();
     	public static Serialization serial = new Serialization();


public VoCe(int state) throws IOException {
    
    this.state = state;

}

public static void main(String[] args) throws IOException{

       
    
      if( args.length > 1 )
      {
         System.out.println( "usage as Call reciver(Server) : java VoCe " ) ;
         System.out.println("usage as Call sender(Client) : java VoCe [ip address of other end]");
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
                
                System.out.println("Connecting Call to server.....");
                Thread.sleep(100);
                qa_sendPackets();
                Serialization.threshold = GetQuality();
                 System.out.println("Automatically selecting Threshold values based on your connection quality (1 -> Best connection 128 -> worst connection )  :" + Serialization.threshold);
                
                }
                
                 catch( Exception e )
                {
                    System.out.println( e ) ;
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
                               
                    DatagramPacket packet = new DatagramPacket ( data , data.length , server_address, server_port );
					client_address = server_address;
					client_port = server_port;
		    
                    socket_uplink.send( packet ) ;
               
                   
                    packet.setData( new byte[packetsize] ) ;
                    
                    System.out.println("Waiting for an answer from other end....."); 
                    socket_downlink.receive( packet ) ;
                    System.out.println( new String(packet.getData()) ) ;
                    
                    
                     
                    System.out.println("Connecting Call to client.....");
                    qa_FwdPackets();
                    System.out.println("Connected Call Successfully!");
                    
	                
                    
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
    public static void qa_sendPackets(){
        
                boolean timeout=true;
                byte [] data_test=new byte [64];
                DatagramPacket packet_sendr;
                try{
                for(int i=0;i<10;i++){
                        
                    packet_sendr = new DatagramPacket ( data_test , data_test.length , client_address , client_port );
                    socket_uplink.send( packet_sendr ) ;
                    Thread.sleep(1);
                
                }
                }catch(Exception e){
                    System.out.println(e);
                }
    }
    
    public static void qa_FwdPackets(){
                    try{
                        socket_downlink.setSoTimeout(1000);   // set the timeout in millisecounds.
                    
                    
                    int count=0;
                    DatagramPacket packet_test = new DatagramPacket ( new byte[64] , 64 );   
                    DatagramPacket packet_tmp=null;
                    while(count<10) {
                                // recieve data until timeout
                        try {
                            socket_downlink.receive(packet_test);
                            packet_tmp = new DatagramPacket ( packet_test.getData() , packet_test.getData().length , packet_test.getAddress() , packet_test.getPort() );
                            socket_uplink.send(packet_tmp) ;
                             System.out.println("Packet SEND "+count);                    
                        }
                        catch (SocketTimeoutException e) {
                            // timeout exception.
                          
                            
                            System.out.println("Timeout reached!!! " + e);
                        }
                        count++;              
                    
                    
                    }
                    
                    }catch(Exception ex){
                        System.out.println(ex);
                    };
    }                
    
    public static int GetQuality(){
                int count=0,missed=0;
                int quality=64;
                int timeTaken=0;
                
                try{
                socket_downlink.setSoTimeout(1000);   // set the timeout in millisecounds.
                DatagramPacket p = new DatagramPacket ( new byte[packetsize] , packetsize ); 
               
                
               
                while(count<10) {
                    int startTime = (int)System.currentTimeMillis(); 
                            // recieve data until timeout
                    try {
                        socket_downlink.receive(p);
                        timeTaken += System.currentTimeMillis()-startTime;                      
                    }
                    catch (SocketTimeoutException e) {
                        // timeout exception.
                        missed++;
                       // timeTaken += -2000;
                        System.out.println("Timeout reached!!! " + e);
                    }
                    count++;              
                    
                    
                }
                }catch(Exception ec){
                    System.out.println(ec);
                }
                System.out.println("TIME " +timeTaken +" "+missed);
                
                if(missed==0 && timeTaken<200){
                    quality = 1;
                }
                else if(missed<1 && timeTaken<400){
                    quality = 32;
                }
                
                else if(missed<3 && timeTaken<600){
                    quality = 48;
                }
                else if(missed>5 && timeTaken>800){
                    quality = 128;
                }

                else quality=64;            
                
                return quality;
    }
    
    
              

    public void run(){
        
        if(state ==2 ){
                 
                 if(!client){
                 try{  
                       
                        Thread.sleep(500);     
                        ByteBuffer bt = ByteBuffer.allocate(4);
                        bt.putInt(Serialization.threshold);    
                        byte [] data_q = bt.array();
                        for(int y=0;y<1000;y++){       
                        DatagramPacket packet_quality = new DatagramPacket ( data_q , data_q.length , server_address, server_port );
				        
				        socket_uplink.send( packet_quality ) ;	
				        }
				         Thread.sleep(1000); 
	                    System.out.println("Sending quality check values ...." +Serialization.threshold);
                   }catch( Exception e ){

                    System.out.println( e ) ;
                    }
                        }
                       
        	while(true){

				  byte [] data = audio.captureAudio();
				  byte [] temp_data = serial.serialize(data);  	//serialize the packet of audio to send the otherside whith sequence no.

              try{
             
              DatagramPacket packet = new DatagramPacket ( temp_data , temp_data.length , client_address , client_port );
              
              socket_uplink.send( packet ) ;    // Send the packet
              }catch( Exception e ){

                    System.out.println( e ) ;
              }
		   
        	}
    	}    


        else if (state == 1){
             DatagramPacket packet = new DatagramPacket ( new byte[packetsize] , packetsize ); 
                int quality=0;                
                if(client){
                try{
                        while(quality<=0 || quality>1024 ){
                        System.out.println("Reciving quality check values ....");
                        socket_downlink.setSoTimeout(10000); 
                        socket_downlink.receive( packet ) ;
                        ByteBuffer wrapped = ByteBuffer.wrap(packet.getData());
                        quality= wrapped.getInt();
                        System.out.println(quality);    
                    
                    }
                    
                    
                  
                   
                    
                    }catch(Exception e){
                         System.out.println("Timeout reached!!! " + e);
                    }
                    
                       
                    if(quality>-1 && quality<256 && quality!=64  && quality!=0){
                    
                    Serialization.threshold = quality;
                    System.out.println("Automatically selecting Threshold values based on your connection quality (1 -> Best connection 128 -> worst connection )  :" + quality);
                        
                    }
                }    
            while(true){  	
                 try{
                // DatagramPacket packet = new DatagramPacket ( new byte[packetsize] , packetsize  );       // Prepare the packet for receive   
                 // Wait for a response from the other peer
                 socket_downlink.receive( packet ) ;
                 serial.deSerialize(packet.getData());
                 }

                 catch( Exception e ){
                        System.out.println( e ) ;
                 }
                // System.out.println("RECIVNG "+recive);
                 
        	}
		
    	}

    	else if (state ==3){

    			try{

    				//Thread.sleep(500);	// initialy delay 1s for prepaire the queue

                }catch(Exception exec){
                	
                	System.out.println(exec);
                
                }

    		while(true){

    			byte [] temp = serial.getPacket();               
				audio.playAudio(temp);
				//System.out.println(temp.length);
    			    			try{

    				//Thread.sleep(1000);	// initialy delay 1s for prepaire the queue

                }catch(Exception exec){
                	
                	System.out.println(exec);
                
                }
    		}
    	}
	}

}
