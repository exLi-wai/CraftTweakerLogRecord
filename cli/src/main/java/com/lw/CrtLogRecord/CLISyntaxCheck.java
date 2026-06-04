package com.lw.CrtLogRecord;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class CLISyntaxCheck {

    private static final String DEFAULT_LOCAL_HOST = "127.0.0.1";
    private static final int DEFAULT_LOCAL_PORT = 26743;
    private static final Pattern NON_ZERO_ERRORS = Pattern.compile("(^|\\D)[1-9][0-9]* errors");

    public static void main(String[] args) {
        if(args.length < 1) {
            printUsage();
            System.exit(1);
        }

        try {
            String result;
            if(args[0].equals("--local")) {
                result = runLocal(args);
            } else if(args[0].equals("--rcon")) {
                result = runRcon(args);
            } else {
                printUsage();
                System.exit(1);
                return;
            }
            String output = stripFormatting(result);
            System.out.print(output);
            if(isCheckFailure(output)) {
                System.exit(2);
            }
        } catch(Exception e) {
            System.err.println("crtcheck error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static String runLocal(String[] args) throws IOException {
        String host = DEFAULT_LOCAL_HOST;
        int port = DEFAULT_LOCAL_PORT;
        String loaderName = "";

        if(args.length == 2) {
            loaderName = args[1];
        } else if(args.length >= 3) {
            host = args[1];
            try {
                port = Integer.parseInt(args[2]);
            } catch(NumberFormatException e) {
                throw new IOException("Invalid port: " + args[2]);
            }
            loaderName = args.length > 3 ? args[3] : "";
        }

        return localCommand(host, port, loaderName);
    }

    private static String runRcon(String[] args) throws IOException {
        if(args.length < 4) {
            printUsage();
            System.exit(1);
        }

        String host = args[1];
        int port;
        try {
            port = Integer.parseInt(args[2]);
        } catch(NumberFormatException e) {
            throw new IOException("Invalid port: " + args[2]);
        }
        String password = args[3];
        String loaderName = args.length > 4 ? args[4] : "";

        return rconCommand(host, port, password, loaderName);
    }

    private static String localCommand(String host, int port, String loaderName) throws IOException {
        try(Socket socket = new Socket(host, port);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            socket.setSoTimeout(35000);
            writer.write(loaderName == null || loaderName.isEmpty() ? "CHECK" : "CHECK " + loaderName);
            writer.newLine();
            writer.flush();

            StringBuilder result = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                result.append(line).append(System.lineSeparator());
            }
            return result.toString();
        }
    }

    private static String rconCommand(String host, int port, String password, String loaderName) throws IOException {
        try(RCONClient rcon = new RCONClient(host, port, password)) {
            String cmd = loaderName.isEmpty() ? "/crtcheck" : "/crtcheck " + loaderName;
            return rcon.sendCommand(cmd);
        }
    }

    private static String stripFormatting(String output) {
        return output.replaceAll("[\\u00a7搂][0-9a-fk-orA-FK-OR]", "");
    }

    private static boolean isCheckFailure(String output) {
        return output.contains("ERROR:")
                || output.contains("Syntax check crashed")
                || NON_ZERO_ERRORS.matcher(output).find();
    }

    private static void printUsage() {
        System.err.println("Usage:");
        System.err.println("  crtcheck --local [loaderName]");
        System.err.println("  crtcheck --local <host> <port> [loaderName]");
        System.err.println("  crtcheck --rcon <host> <port> <password> [loaderName]");
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
                // Expected - all packets received.
            }
            socket.setSoTimeout(0);

            return result.toString();
        }

        private void sendPacket(int id, int type, byte[] payload) throws IOException {
            int len = 8 + payload.length + 2; // id(4) + type(4) + payload + null(1) + padding(1)
            out.writeInt(Integer.reverseBytes(len));
            out.writeInt(Integer.reverseBytes(id));
            out.writeInt(Integer.reverseBytes(type));
            out.write(payload);
            out.write(0);
            out.write(0);
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
            final int id;
            final int type;
            final String payload;

            RCONPacket(int id, int type, String payload) {
                this.id = id;
                this.type = type;
                this.payload = payload;
            }
        }
    }
}
