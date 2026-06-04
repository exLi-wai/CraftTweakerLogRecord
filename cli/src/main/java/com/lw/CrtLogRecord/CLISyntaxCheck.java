package com.lw.CrtLogRecord;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class CLISyntaxCheck {

    public static void main(String[] args) {
        if(args.length < 4) {
            System.err.println("Usage: crtcheck --rcon <host> <port> <password> [loaderName]");
            System.exit(1);
        }

        if(!args[0].equals("--rcon")) {
            System.err.println("Usage: crtcheck --rcon <host> <port> <password> [loaderName]");
            System.exit(1);
        }

        String host = args[1];
        int port;
        try {
            port = Integer.parseInt(args[2]);
        } catch(NumberFormatException e) {
            System.err.println("Invalid port: " + args[2]);
            System.exit(1);
            return;
        }
        String password = args[3];
        String loaderName = args.length > 4 ? args[4] : "";

        try {
            String result = rconCommand(host, port, password, loaderName);
            System.out.print(result);
        } catch(Exception e) {
            System.err.println("RCON error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static String rconCommand(String host, int port, String password, String loaderName) throws IOException {
        try(RCONClient rcon = new RCONClient(host, port, password)) {
            String cmd = loaderName.isEmpty() ? "/crtcheck" : "/crtcheck " + loaderName;
            return rcon.sendCommand(cmd);
        }
    }

    private static class RCONClient implements Closeable {
        private final Socket socket;
        private final DataInputStream in;
        private final DataOutputStream out;
        private int requestId;

        RCONClient(String host, int port, String password) throws IOException {
            socket = new Socket(host, port);
            socket.setSoTimeout(5000);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            int id = ++requestId;
            sendPacket(id, 3, password.getBytes("UTF-8"));
            RCONPacket pkt = readPacket();
            if(pkt == null || pkt.id == -1) {
                throw new IOException("RCON authentication failed (wrong password?)");
            }
        }

        String sendCommand(String command) throws IOException {
            int id = ++requestId;
            sendPacket(id, 2, command.getBytes("UTF-8"));

            StringBuilder result = new StringBuilder();
            socket.setSoTimeout(200);
            try {
                while(true) {
                    RCONPacket pkt = readPacket();
                    if(pkt == null) break;
                    if(pkt.type == 0 && pkt.id == id) {
                        result.append(pkt.payload);
                    }
                }
            } catch(SocketTimeoutException e) {
                // Expected - all packets received
            }
            socket.setSoTimeout(0);

            String output = result.toString();
            output = output.replaceAll("§[0-9a-fklmnor]", "");
            return output;
        }

        private void sendPacket(int id, int type, byte[] payload) throws IOException {
            int len = 8 + payload.length + 2; // id(4) + type(4) + payload + null(1) + padding(1)
            out.writeInt(Integer.reverseBytes(len));
            out.writeInt(Integer.reverseBytes(id));
            out.writeInt(Integer.reverseBytes(type));
            out.write(payload);
            out.write(0); // null terminator
            out.write(0); // padding byte
            out.flush();
        }

        private RCONPacket readPacket() throws IOException {
            int len = Integer.reverseBytes(in.readInt());
            if(len <= 0 || len > 65535) return null;
            int id = Integer.reverseBytes(in.readInt());
            int type = Integer.reverseBytes(in.readInt());
            int remaining = len - 8;
            byte[] payloadBytes = new byte[remaining];
            in.readFully(payloadBytes);
            int strLen = 0;
            while(strLen < remaining && payloadBytes[strLen] != 0) strLen++;
            String payload = new String(payloadBytes, 0, strLen, "UTF-8");
            return new RCONPacket(id, type, payload);
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }

        private static class RCONPacket {
            final int id, type;
            final String payload;
            RCONPacket(int id, int type, String payload) {
                this.id = id; this.type = type; this.payload = payload;
            }
        }
    }
}
