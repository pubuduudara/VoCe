/*
*	CO324-project 1 Iteration 2 
*	Unit test for Serialization class
*/

import java.util.Arrays;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.* ;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class SerializationTest{
	public static void main(String args[]){ 
		
		Serialization s1 = new Serialization();

		int serverport = 9876;
		int clientport = 8765;
			
		try{
		 	InetAddress server_address = InetAddress.getByName( "localhost" );
			if(args.length==0){
		 		System.out.println("Running unit testing client for testing Serialization and deSerialization");
		 		DatagramSocket socket = new DatagramSocket();
		 		
		 		try{
		 			Thread.sleep(10);
               			}catch(Exception exec){}
               			
				for(int i=2000;i<2100;i++){
			    
				ByteBuffer b = ByteBuffer.allocate(4);
                    		b.putInt(i);    
                    		byte [] data = b.array() ;

                    		byte [] data_serial = s1.serialize(data);
                    		
				 DatagramPacket packet = new DatagramPacket ( data_serial , data_serial.length , server_address , serverport );
					System.out.println("sending packet containing int value of" + i);
					socket.send(packet);
				}
			}	
			else if (args.length==1){
				try{
					Thread.sleep(1000);
              			}catch(Exception exec){}

				System.out.println("AD");
				DatagramSocket socket = new DatagramSocket( serverport ) ;
				
				while(true)
				{
					System.out.println("reciving packet" );
					DatagramPacket packet = new DatagramPacket ( new byte[VoCe.packetsize] , VoCe.packetsize );                			// Prepare the packet for receive

					 socket.receive( packet ) ;
					 				
					 s1.deSerialize(packet.getData());
					 byte [] temp = s1.getPacket();
					 
					 try{
					 	Thread.sleep(100);
					 }catch(Exception exec){}

					 ByteBuffer wrapped = ByteBuffer.wrap(temp);
				    	 int a = wrapped.getInt();
				    	
					 System.out.println("Packet Contains : "+a);
				}		
			}
		}catch(Exception ex){
			System.out.println(ex);
		};

	}
}
