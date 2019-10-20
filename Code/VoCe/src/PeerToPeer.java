import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Scanner;


public class PeerToPeer extends Thread {

    enum MODE {PEER_1, PEER_2}

    enum STATE {RECV, REC_SEND, PLAY}

    private static MODE mode = MODE.PEER_1;
    private static int packetSize = 64;
    private STATE state;

    private static InetAddress server_address = null;
    private static InetAddress clientAddress = null;
    private static int clientPort = -1;
    private static DatagramSocket up_linkSocket = null;
    private static DatagramSocket down_linkSocket = null;
    private static RecordPlayback recordPlayback = new RecordPlayback();
    private static PacketNumbering packetNumbering = new PacketNumbering();

    private PeerToPeer(STATE state) {
        this.state = state;
    }

    public static void main(String[] args) throws IOException {
        recordPlayback = new RecordPlayback();
        //System.out.println("Threshold " + Serialization.threshold);

        String usage = "usage:  $java PeerToPeer peer1\nOR\n$java PeerToPeer peer2 <IP address>";
        if (args.length == 1) {
            if (args[0].equals("peer1")) {
                mode = MODE.PEER_1;
            } else {
                System.out.println("Invalid format\n" + usage);
            }
        } else if (args.length == 2) {
            if (args[0].equals("peer2")) {
                try {
                    server_address = InetAddress.getByName(args[1]);
                    mode = MODE.PEER_2;
                } catch (Exception e) {
                    System.out.println("Invalid IP address\n" + usage);
                }

            }
        } else {
            System.out.println("Invalid format\n" + usage);
        }
        Scanner sc = new Scanner(System.in);

        int server_port = 12000;

        if (mode == MODE.PEER_1) {

            try {

                down_linkSocket = new DatagramSocket(server_port);
                DatagramPacket packet = new DatagramPacket(new byte[packetSize], packetSize); // Prepare the packet for receive

                // Wait for a response from the server
                System.out.println("\nWaiting for peer...");


                System.out.println("Please share your IP address with peer2 ");

                down_linkSocket.receive(packet);
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

                up_linkSocket = new DatagramSocket();

                DatagramPacket packet_send = new DatagramPacket(data, data.length, clientAddress, clientPort);
                Thread.sleep(100);
                up_linkSocket.send(packet_send);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (mode == MODE.PEER_2) {


            try {

                up_linkSocket = new DatagramSocket();
                down_linkSocket = new DatagramSocket();
                int downlinkPort = down_linkSocket.getLocalPort();

                /*Sending the downlinkPort port to other side for ask that side user to send data to this downlinkPort */
                ByteBuffer b = ByteBuffer.allocate(4);
                b.putInt(downlinkPort);
                byte[] data = b.array();

                DatagramPacket packet = new DatagramPacket(data, data.length, server_address, server_port);
                clientAddress = server_address;
                clientPort = server_port;

                up_linkSocket.send(packet);


                packet.setData(new byte[packetSize]);

                System.out.println("Waiting for the peer to answer...");
                down_linkSocket.receive(packet);
                System.out.println(new String(packet.getData()));


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        Thread recv = new Thread(new PeerToPeer(STATE.RECV));
        Thread rec_send = new Thread(new PeerToPeer(STATE.REC_SEND));
        Thread play = new Thread(new PeerToPeer(STATE.PLAY));

        recv.start();
        rec_send.start();
        play.start();


    }

    public void run() {
        if (state == STATE.RECV) {
            while (true) {
                try {
                    DatagramPacket packet = new DatagramPacket(new byte[packetSize], packetSize);
                    down_linkSocket.receive(packet);
                    packetNumbering.removeNumber(packet.getData());
                } catch (Exception e) {
                    System.out.println("Receiving error");
                    e.printStackTrace();
                }

            }

        } else if (state == STATE.REC_SEND) {
            while (true) {

                byte[] data = recordPlayback.captureAudio();
                byte[] temp_data = packetNumbering.addNumbers(data);

                try {
                    DatagramPacket packet = new DatagramPacket(temp_data, temp_data.length, clientAddress, clientPort);
                    up_linkSocket.send(packet);
                } catch (Exception e) {
                    System.out.println("sending error");
                    e.printStackTrace();

                }


            }
        } else if (state == STATE.PLAY) {

            while (true) {

                byte[] temp = packetNumbering.getPacket();
                recordPlayback.playAudio(temp);

            }
        }
    }


}
