import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Scanner;


public class Unicast extends Thread {

    enum MODE {PEER_1, PEER_2}

    enum STATUS {RECV, REC_SEND, PLAY}

    private static MODE mode;
    private static int packetSize = 64;
    private STATUS state;

    private static InetAddress server_address = null;
    private static InetAddress clientAddress = null;
    private static int clientPort = -1;
    private static DatagramSocket uplinkSocket = null;
    private static DatagramSocket downlinkSocket = null;
    private static RecordPlayback recordPlayback = new RecordPlayback();
    private static PacketNumbering serial = new PacketNumbering();

    private static String usage = "usage:  $java Unicast peer1\nOR\n$java Unicast peer2 <IP address>";

    private Unicast(STATUS state) throws IOException {
        this.state = state;
    }

    public static void main(String[] args) throws IOException {
        recordPlayback = new RecordPlayback();
        //System.out.println("Threshold " + Serialization.threshold);

        if (args.length == 1) {
            if (args[0].equals("peer1")) {
                mode = MODE.PEER_1;
            } else {
                System.out.println("Invalid format\n"+usage);
            }
        }else if(args.length == 2){
            if(args[0].equals("peer2")){
                try {
                    server_address = InetAddress.getByName(args[1]);
                }catch (Exception e){
                    System.out.println("Invalid IP address\n"+usage);
                }

            }
        }else {
            System.out.println("Invalid format\n"+usage);
        }
        Scanner sc = new Scanner(System.in);

        int server_port = 12121;

        if (mode == MODE.PEER_1) {

            try {

                downlinkSocket = new DatagramSocket(server_port);
                DatagramPacket packet = new DatagramPacket(new byte[packetSize], packetSize); // Prepare the packet for receive

                // Wait for a response from the server
                System.out.println("Waiting for peer...");

                //Getting my IP Address
                InetAddress localHost = InetAddress.getLocalHost();
                String myIPAddress = "";
                try {
                    URL urlName = new URL("http://bot.whatismyipaddress.com");
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlName.openStream()));

                    myIPAddress = bufferedReader.readLine().trim();
                    System.out.println("\nYour IP: " + myIPAddress);
                } catch (Exception e) {

                }

                System.out.println("\nPlease share your IP address with peer2 ");

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

        } else if (mode == MODE.PEER_2) {


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

        }

        Thread recv = new Thread(new Unicast(STATUS.RECV));
        Thread rec_send = new Thread(new Unicast(STATUS.REC_SEND));
        Thread play = new Thread(new Unicast(STATUS.PLAY));

        recv.start();
        rec_send.start();
        play.start();


    }

    public void run() {
        if (state == STATUS.RECV) {
            while (true) {
                try {
                    DatagramPacket packet = new DatagramPacket(new byte[packetSize], packetSize);
                    downlinkSocket.receive(packet);
                    serial.deserialize(packet.getData());
                } catch (Exception e) {
                    System.out.println("Receiving error");
                    e.printStackTrace();
                    break;
                }

            }

        } else if (state == STATUS.REC_SEND) {
            while (true) {

                byte[] data = recordPlayback.captureAudio();
                byte[] temp_data = serial.serialize(data);

                try {
                    DatagramPacket packet = new DatagramPacket(temp_data, temp_data.length, clientAddress, clientPort);
                    uplinkSocket.send(packet);
                } catch (Exception e) {
                    System.out.println("sending error");
                    e.printStackTrace();
                    break;
                }


            }
        } else if (state == STATUS.PLAY) {

            while (true) {

                byte[] temp = serial.getPacket();
                recordPlayback.playAudio(temp);

            }
        }
    }


}
