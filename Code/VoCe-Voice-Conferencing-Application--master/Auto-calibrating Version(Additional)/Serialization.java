/*
 * CO 324 - Network and Web programming
 * Project I
 * Serialization and Deserialization plus error correction  class
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




public class Serialization{

    public static int threshold=64;
	private int curr_sending=0,curr_playing=-1;

	private byte tempBuffer[][] = new byte[1024][VoCe.packetsize];		

	int played = 0;
    
    
    //System.out.println("A");
    
	public  byte [] serialize(byte [] buff){		//method fro serialization of data; it adds sequence number to the packet
		 

		byte [] temp = Arrays.copyOf(buff, VoCe.packetsize);
		ByteBuffer bytebuff = ByteBuffer.allocate(4);
                    bytebuff.putInt(curr_sending);    
                    byte [] data = bytebuff.array() ;
        System.arraycopy(data, 0, temp, VoCe.packetsize-4, 4);
	
		curr_sending++;
		return temp;

	}

/*
	*mwthod for deserialization split the original sound packet and sequence no. then check for errors and if no 
		error it will add the current packet to the queue.if unrecoverable error happens it will drop the packet
*/
	public  void deSerialize(byte []buff){			

		int recive_num;
		
		byte [] temp = new byte[4];
		System.arraycopy(buff, VoCe.packetsize-4, temp, 0, 4);
		ByteBuffer wrapped = ByteBuffer.wrap(temp);
        
        recive_num = wrapped.getInt();
        //System.out.println("Recive number = " + recive_num + "  "+curr_playing + "  " +played_loops );
        //System.out.println("RECIVE " +recive_num + " "+ curr_playing);
		if(recive_num>curr_playing){
			tempBuffer [recive_num%1024] = Arrays.copyOf(buff,buff.length);
			
		}
	    //System.out.println("C");
	}

	public byte [] getPacket(){		//returns the first packet from the audio packet buffer wich contains the packets recived.
		
		byte [] buff = new byte [VoCe.packetsize-4];
		int i=curr_playing+1;
		int k=0;
		//System.out.println("A");
		while(true){
			        int counter_buff=0;
			        for(int j=0;j<1024;j++){
			            if(tempBuffer[j] != null)counter_buff++;
			        }     
			        if(counter_buff>threshold)break;
			        
			    
	    }   
			    
    
		
		for(int p=0;p<1024;p++){
		//System.out.println("A");
		
			if(tempBuffer[p] != null){
				
				//System.out.println("B");
                int recive_num;
		
		        byte [] temp = new byte[4];
		        System.arraycopy(tempBuffer[p], VoCe.packetsize-4, temp, 0, 4);
		        ByteBuffer wrapped = ByteBuffer.wrap(temp);
        
                recive_num = wrapped.getInt();
                //System.out.println("IMPORTENT rec_no "+recive_num +"  "+curr_playing);
                
                
                
                if(recive_num>=curr_playing){
                curr_playing = recive_num;
				buff = Arrays.copyOf(tempBuffer[p],tempBuffer[p].length-4);
				
				tempBuffer[p] = null;
				//System.out.println("Recive number HERE = "   +curr_playing + "  " +played_loops );
				
				break ;
				}
				else{
				    //System.out.println("else");
				    tempBuffer[p] = null;
				
				}
			}
			
				
			//System.out.println(played_loops);
		}
		
	
		return buff;
	}

/*
	main class written for unint test the serialization and deserialization part
*/

	public static void main(String args[]){ 
		Serialization s1 = new Serialization();

		int serverport = 9876;int clientport = 8765;
			
		try{
		 InetAddress server_address = InetAddress.getByName( "localhost" );
		 if(args.length==0){
		 		System.out.println("Running unit testing client for testing Serialization and deSerialization");
		 		DatagramSocket socket = new DatagramSocket(  ) ;
		 		try{Thread.sleep(10);
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
		try{Thread.sleep(1000);
                }catch(Exception exec){}

				System.out.println("AD");
				DatagramSocket socket = new DatagramSocket( serverport ) ;
				while(true)
				{
					System.out.println("reciving packet" );
					DatagramPacket packet = new DatagramPacket ( new byte[VoCe.packetsize] , VoCe.packetsize );                // Prepare the packet for receive


                 socket.receive( packet ) ;
                 
                
                 s1.deSerialize(packet.getData());
                 byte [] temp = s1.getPacket();
                 
                 try{Thread.sleep(100);
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

