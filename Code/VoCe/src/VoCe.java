
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class VoCe extends Thread {

    private static int Mode = -1; //Normal Mode as 1st peer is Mode=1 , Normal Mode as 2st client is Mode=2, Multicast is Mode=3
    static int packet_size = 64;

    /*State =1 is for receiving loop state = 2 for recording and sending packet loop State = 3 for playback loop*/
    private int state = 0;

    private static InetAddress server_address = null;
    private static InetAddress client_address = null;
    private static int client_port = -1;
    private static DatagramSocket socket_uplink = null;
    private static DatagramSocket socket_downlink = null;
    private static MulticastSocket socket_multicast = null;
    private static RecordPlayback audio = new RecordPlayback();
    private static Serialization serial = new Serialization();


    private VoCe(int state) throws IOException {

        this.state = state;

    }

    public static void main(String[] args) throws IOException {

        System.out.println("Threshold " + Serialization.threshold);


        if (args.length == 1) {
            Mode = 2;
            server_address = InetAddress.getByName(args[0]);

        } else if (args.length == 0) {
            Mode = 1;
        } else if (args.length == 2 && args[1].equalsIgnoreCase("-Multicast")) {
            Mode = 3;
            server_address = InetAddress.getByName(args[0]);
        } else {
            System.out.println("usage: java Client host or java Server");
            return;
        }


        int server_port = 12000;
        int downlink_port = -1;
        if (Mode == 1) {    //application run as Server

            try {

                socket_downlink = new DatagramSocket(server_port);
                DatagramPacket packet = new DatagramPacket(new byte[packet_size], packet_size);                // Prepare the packet for receive

                // Wait for a response from the server
                System.out.println("Waiting for a call...... ");
                socket_downlink.receive(packet);
                System.out.println("Incomming call.... Press Eneter key to answer");


                // Asking user to answer the call 
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String readString = scanner.nextLine();
                    if (readString.isEmpty()) break;
                }
                scanner.close();


                client_address = packet.getAddress();
                ByteBuffer wrapped = ByteBuffer.wrap(packet.getData());
                client_port = wrapped.getInt();

                byte[] data = "Client has Answered your call...".getBytes();


                socket_uplink = new DatagramSocket();

                DatagramPacket packet_send = new DatagramPacket(data, data.length, client_address, client_port);
                Thread.sleep(100);
                socket_uplink.send(packet_send);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Mode == 2) {   //application run as client


            try {

                socket_uplink = new DatagramSocket();
                socket_downlink = new DatagramSocket();
                downlink_port = socket_downlink.getLocalPort();

                /*Sending the down-link port to other side for ask that side user to send data to this downlink_port */
                ByteBuffer b = ByteBuffer.allocate(4);
                b.putInt(downlink_port);
                byte[] data = b.array();

                DatagramPacket packet = new DatagramPacket(data, data.length, server_address, server_port);
                client_address = server_address;
                client_port = server_port;

                socket_uplink.send(packet);


                packet.setData(new byte[packet_size]);

                System.out.println("Waiting for an answer from other end.....");
                socket_downlink.receive(packet);
                System.out.println(new String(packet.getData()));


            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (Mode == 3) {


            socket_uplink = new DatagramSocket();
            client_address = server_address;
            client_port = 8888;
            try {
                //Prepare to join multicast group
                socket_multicast = new MulticastSocket(8888);
                socket_multicast.joinGroup(server_address);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        Thread transmission = new Thread(new VoCe(1)); //new thread started for transmission
        Thread receive = new Thread(new VoCe(2));        //new thread started for receive packets
        Thread play = new Thread(new VoCe(3));            //new thread started for playback

        transmission.start();
        receive.start();
        play.start();


    }


    public void run() {
        if (state == 2) {
            while (true) {

                byte[] data = audio.captureAudio();
                byte[] temp_data = serial.serialize(data);    //serialize the packet of audio to send the otherside whith sequence no.

                try {

                    DatagramPacket packet = new DatagramPacket(temp_data, temp_data.length, client_address, client_port);

                    socket_uplink.send(packet);    // Send the packet
                } catch (Exception e) {
                    System.out.println("sending error");
                    e.printStackTrace();
                    break;
                }


            }
        } else if (state == 1) {
            while (true) {
                try {
                    DatagramPacket packet = new DatagramPacket(new byte[packet_size], packet_size);       // Prepare the packet for receive
                    // Wait for a response from the other peer

                    if (Mode == 3) {
                        socket_multicast.receive(packet);
                    } else {
                        socket_downlink.receive(packet);

                    }

                    serial.deSerialize(packet.getData());
                } catch (Exception e) {
                    System.out.println("Receiving error");
                    e.printStackTrace();
                    break;
                }

            }

        } else if (state == 3) {


            while (true) {

                byte[] temp = serial.getPacket();
                audio.playAudio(temp);


            }
        }
    }


}
