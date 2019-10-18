import java.util.Arrays;
import java.net.*;
import java.nio.ByteBuffer;


public class Serialization {


    static int threshold = 32;

    private static int no_pkt_recv = 0, packet_reorder = 0, no_pkt_sent = 0;

    private int send_i = 0, play_i = -1;

    private byte[][] tempBuffer = new byte[1024][VoCe.packetSize];

    private static volatile long startTime = System.currentTimeMillis();

    //append sequence a number to the packet
    byte[] serialize(byte[] packet) {

        byte[] copy_packet = Arrays.copyOf(packet, VoCe.packetSize);
        ByteBuffer bytebuffer = ByteBuffer.allocate(4);
        bytebuffer.putInt(send_i);
        byte[] data = bytebuffer.array();
        System.arraycopy(data, 0, copy_packet, VoCe.packetSize - 4, 4);
        no_pkt_sent++;
        send_i++;
        return copy_packet;

    }

    //remove the sequence number from the packet and check errors
    void deserialize(byte[] packet) {


        no_pkt_recv++;

        byte[] temp = new byte[4];
        System.arraycopy(packet, VoCe.packetSize - 4, temp, 0, 4);
        int seq_num = ByteBuffer.wrap(temp).getInt();

        if (seq_num > play_i) {
            tempBuffer[seq_num % 1024] = Arrays.copyOf(packet, packet.length);
        } else packet_reorder++;

        if (System.currentTimeMillis() > startTime + 10000) {
            System.out.println("Packet Size " + VoCe.packetSize + " Packet Loss " + (no_pkt_sent - no_pkt_recv) + " Reordered Packets " + packet_reorder);
            startTime = System.currentTimeMillis();
            no_pkt_sent = 0;
            no_pkt_recv = 0;
            packet_reorder = 0;

        }


    }

    //returns the first packet from the audio packet buffer which contains the packets received.
    byte[] getPacket() {

        byte[] buff = new byte[VoCe.packetSize - 4];
        int i = play_i + 1;
        int k = 0;
        //System.out.println("A");
        while (true) {
            int counter_buff = 0;
            for (int j = 0; j < 1024; j++) {
                if (tempBuffer[j] != null) counter_buff++;
            }
            if (counter_buff > threshold) break;


        }


        for (int p = 0; p < 1024; p++) {
            //System.out.println("A");

            if (tempBuffer[p] != null) {

                //System.out.println("B");
                int receive_num;

                byte[] temp = new byte[4];
                System.arraycopy(tempBuffer[p], VoCe.packetSize - 4, temp, 0, 4);
                ByteBuffer wrapped = ByteBuffer.wrap(temp);

                receive_num = wrapped.getInt();
                //System.out.println("IMPORTANT rec_no "+receive_num +"  "+curr_playing);


                if (receive_num >= play_i) {
                    play_i = receive_num;
                    buff = Arrays.copyOf(tempBuffer[p], tempBuffer[p].length - 4);

                    tempBuffer[p] = null;
                    //System.out.println("Receive number HERE = "   +curr_playing + "  " +played_loops );

                    break;
                } else {
                    //System.out.println("else");
                    tempBuffer[p] = null;

                }
            }


            //System.out.println(played_loops);
        }


        return buff;
    }

/*
	main class written for unit test the serialization and deserialization part
*/

    public static void main(String[] args) {
        Serialization s1 = new Serialization();

        int server_port = 9876;

        try {
            InetAddress server_address = InetAddress.getByName("localhost");
            if (args.length == 0) {
                System.out.println("Running unit testing client for testing Serialization and deSerialization");
                DatagramSocket socket = new DatagramSocket();
                try {
                    Thread.sleep(10);
                } catch (Exception ignored) {
                }
                for (int i = 2000; i < 2100; i++) {


                    ByteBuffer b = ByteBuffer.allocate(4);
                    b.putInt(i);
                    byte[] data = b.array();

                    byte[] data_serial = s1.serialize(data);
                    DatagramPacket packet = new DatagramPacket(data_serial, data_serial.length, server_address, server_port);
                    System.out.println("sending packet containing int value of" + i);
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
                    DatagramPacket packet = new DatagramPacket(new byte[VoCe.packetSize], VoCe.packetSize);                // Prepare the packet for receive


                    socket.receive(packet);


                    s1.deserialize(packet.getData());
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

