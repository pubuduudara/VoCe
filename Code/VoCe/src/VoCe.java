import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class VoCe extends Thread {

    private static int mode = -1; // 1: 1st peer, 2: 2nd peer, 3: Multicast
    static int packetSize = 64;
    private int state = 0; //1: receiving , 2: recording and sending, 3: playback

    private static InetAddress server_address = null;
    private static InetAddress clientAddress = null;
    private static int clientPort = -1;
    private static DatagramSocket uplinkSocket = null;
    private static DatagramSocket downlinkSocket = null;
    private static MulticastSocket multicastSocket = null;
    private static RecordPlayback recordPlayback = new RecordPlayback();
    private static Serialization serial = new Serialization();

    private VoCe(int state) throws IOException {
        this.state = state;
    }

    public static void main(String[] args) throws IOException {
        recordPlayback = new RecordPlayback();
        //System.out.println("Threshold " + Serialization.threshold);

        Scanner sc = new Scanner(System.in);

        System.out.println("\nSelect the mode:\n1)Private call\n2)Group call\n");

        int choice = sc.nextInt();

        if(choice == 1){ //Private call
            System.out.println("1)Initiate call\n2)Call someone\n");
            choice = sc.nextInt();
            if(choice ==1){ //Initiate
                mode = 1;
            }else if(choice == 2){ // Call someone
                System.out.println("Enter peer's IP address: ");
                server_address = InetAddress.getByName(sc.next());
                mode = 2;
            }
        }else if(choice == 2){ // Group call
            System.out.println("Enter group's multicast IP address: ");
            server_address = InetAddress.getByName(sc.next());
            mode = 3;
        }else{
            System.out.println("\nInvalid input");
        }

        int server_port = 12000;

        if (mode == 1) {    //2nd peer

            try {

                downlinkSocket = new DatagramSocket(server_port);
                DatagramPacket packet = new DatagramPacket(new byte[packetSize], packetSize); // Prepare the packet for receive

                // Wait for a response from the server
                System.out.println("Waiting for peer...");
                downlinkSocket.receive(packet);
                System.out.println("Incoming call... Press Enter to answer");
                while (true) {
                    String s = sc.nextLine();
                    if (s.isEmpty()) break;
                }
                sc.close();

                clientAddress = packet.getAddress();
                ByteBuffer wrapped = ByteBuffer.wrap(packet.getData());
                clientPort = wrapped.getInt();

                byte[] data = "Client has Answered your call...".getBytes();

                uplinkSocket = new DatagramSocket();

                DatagramPacket packet_send = new DatagramPacket(data, data.length, clientAddress, clientPort);
                Thread.sleep(100);
                uplinkSocket.send(packet_send);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (mode == 2) {   //1st peer


            try {

                uplinkSocket = new DatagramSocket();
                downlinkSocket = new DatagramSocket();
                int downlinkPort = downlinkSocket.getLocalPort();

                /*Sending the downlinkPort port to other side for ask that side user to send data to this downlinkPort */
                ByteBuffer b = ByteBuffer.allocate(4);
                b.putInt(downlinkPort);
                byte[] data = b.array();

                DatagramPacket packet = new DatagramPacket(data, data.length, server_address, server_port);
                clientAddress = server_address;
                clientPort = server_port;

                uplinkSocket.send(packet);


                packet.setData(new byte[packetSize]);

                System.out.println("Waiting for the peer to answer...");
                downlinkSocket.receive(packet);
                System.out.println(new String(packet.getData()));


            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (mode == 3) { //multicast

            uplinkSocket = new DatagramSocket();
            clientAddress = server_address;
            clientPort = 8888;
            try {
                //Prepare to join multicast group
                multicastSocket = new MulticastSocket(8888);
                multicastSocket.joinGroup(server_address);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        Thread transmission = new Thread(new VoCe(1)); //transmission thread
        Thread receive = new Thread(new VoCe(2));      //thread reception thread
        Thread play = new Thread(new VoCe(3));         //thread playback thread

        transmission.start();
        receive.start();
        play.start();


    }


    public void run() {
        if (state == 2) {
            while (true) {

                byte[] data = recordPlayback.captureAudio();
                byte[] temp_data = serial.serialize(data);    //serialize the packet of audio to send the otherside whith sequence no.

                try {

                    DatagramPacket packet = new DatagramPacket(temp_data, temp_data.length, clientAddress, clientPort);

                    uplinkSocket.send(packet);    // Send the packet
                } catch (Exception e) {
                    System.out.println("sending error");
                    e.printStackTrace();
                    break;
                }


            }
        } else if (state == 1) {
            while (true) {
                try {
                    DatagramPacket packet = new DatagramPacket(new byte[packetSize], packetSize);       // Prepare the packet for receive
                    // Wait for a response from the other peer

                    if (mode == 3) {
                        multicastSocket.receive(packet);
                    } else {
                        downlinkSocket.receive(packet);

                    }

                    serial.deserialize(packet.getData());
                } catch (Exception e) {
                    System.out.println("Receiving error");
                    e.printStackTrace();
                    break;
                }

            }

        } else if (state == 3) {


            while (true) {

                byte[] temp = serial.getPacket();
                recordPlayback.playAudio(temp);


            }
        }
    }


}
