import java.util.Arrays;
import java.net.*;
import java.nio.ByteBuffer;


public class PacketNumberingAndData {

    private final static int packetSize = 64;
    private final static int recvBufferSize = 1024;
    private final static int packetThreshold = 32;
    private final static int resetNumberingInMillis = 1000;

    private static int recv_pkt_count = 0, not_in_order_pkt_count = 0, sent_pkt_count = 0;
    private int send_index, play_index = 0;

    private byte[][] recvBuffer = new byte[recvBufferSize][packetSize];

    private static volatile long startTime = System.currentTimeMillis();

    //add packet number
    byte[] addNumbers(byte[] packet) {

        byte[] numbered_packet = Arrays.copyOf(packet, packetSize);
        ByteBuffer bytebuffer = ByteBuffer.allocate(4);
        bytebuffer.putInt(send_index);
        byte[] data = bytebuffer.array();
        System.arraycopy(data, 0, numbered_packet, packetSize - 4, 4);
        sent_pkt_count++;
        send_index++;
        return numbered_packet;

    }

    //get packet number
    private int getNumber(byte[] packet) {

        byte[] temp = new byte[4];
        System.arraycopy(packet, packetSize - 4, temp, 0, 4);

        return ByteBuffer.wrap(temp).getInt();
    }

    //update the recvBuffer
    void appendPacket(byte[] packet) {

        recv_pkt_count++;


        int pktNumber = getNumber(packet);

        if (pktNumber > play_index) {
            recvBuffer[pktNumber % recvBufferSize] =  Arrays.copyOf(packet,packet.length);
        } else not_in_order_pkt_count++;

        if (System.currentTimeMillis()>startTime+resetNumberingInMillis) {
            System.out.println(" Losses " + (sent_pkt_count - recv_pkt_count) + " Not in order packets " + not_in_order_pkt_count);
            startTime = System.currentTimeMillis();
            sent_pkt_count = 0;
            recv_pkt_count = 0;
            not_in_order_pkt_count = 0;
        }

    }

    //To send the packets to play
    byte[] getPacket() {
        while (true) {
            int count = 0;
            for (int i = 0; i < recvBufferSize; i++) {
                if (recvBuffer[i] != null) count++;
            }
            if (count > packetThreshold) break;

        }

        byte[] bytesToPlay = new byte[(packetSize - 4)];

        for (int i = 0; i < recvBufferSize; i++) {
            if (recvBuffer[i] != null) {
                int recive_num = getNumber(recvBuffer[i]);

                if(recive_num>= play_index){
                    play_index = recive_num;
                    bytesToPlay = Arrays.copyOf(recvBuffer[i],recvBuffer[i].length-4);
                    recvBuffer[i] = null;
                    break ;
                }
                else{
                    recvBuffer[i] = null;
                }


            }

        }

        return bytesToPlay;
    }

    //To test the packet numbering
    public static void main(String[] args) {
        PacketNumberingAndData s1 = new PacketNumberingAndData();

        int server_port = 9876;

        try {
            InetAddress server_address = InetAddress.getByName("localhost");
            if (args.length == 0) {
                System.out.println("unit test Numbering");
                DatagramSocket socket = new DatagramSocket();
                try {
                    Thread.sleep(10);
                } catch (Exception ignored) {
                }
                for (int i = 2000; i < 2100; i++) {


                    ByteBuffer b = ByteBuffer.allocate(4);
                    b.putInt(i);
                    byte[] data = b.array();

                    byte[] data_serial = s1.addNumbers(data);
                    DatagramPacket packet = new DatagramPacket(data_serial, data_serial.length, server_address, server_port);
                    System.out.println("sending packet " + i);
                    socket.send(packet);
                }
            } else if (args.length == 1) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }

                System.out.println("AD");
                DatagramSocket socket = new DatagramSocket(server_port);
                while (true) {
                    System.out.println("receiving packet");
                    DatagramPacket packet = new DatagramPacket(new byte[packetSize], packetSize);                // Prepare the packet for receive


                    socket.receive(packet);


                    s1.appendPacket(packet.getData());
                    byte[] temp = s1.getPacket();

                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }

                    ByteBuffer wrapped = ByteBuffer.wrap(temp);
                    int a = wrapped.getInt();

                    System.out.println("Packet Contains : " + a);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}

