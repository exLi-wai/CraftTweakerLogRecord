package com.lw.CrtLogRecord;

import net.minecraft.server.MinecraftServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class LocalCheckServer implements Closeable {

    public static final int DEFAULT_PORT = 26743;

    private static LocalCheckServer instance;

    private final MinecraftServer minecraftServer;
    private final ServerSocket serverSocket;
    private final Thread acceptThread;
    private volatile boolean running;

    private LocalCheckServer(MinecraftServer minecraftServer, int port) throws IOException {
        this.minecraftServer = minecraftServer;
        this.serverSocket = new ServerSocket();
        this.serverSocket.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), port));
        this.acceptThread = new Thread(this::acceptLoop, "CrtLogRecord Local Check");
        this.acceptThread.setDaemon(true);
    }

    public static synchronized void start(MinecraftServer minecraftServer) {
        stop();

        int port = getConfiguredPort();
        try {
            instance = new LocalCheckServer(minecraftServer, port);
            instance.running = true;
            instance.acceptThread.start();
            CrtLogUtil.log(CrtLogUtil.INFO, "Local crtcheck server listening on 127.0.0.1:" + port);
        } catch(IOException e) {
            instance = null;
            CrtLogUtil.log(CrtLogUtil.ERROR, "Failed to start local crtcheck server: " + e.getMessage());
        }
    }

    public static synchronized void stop() {
        if(instance != null) {
            try {
                instance.close();
            } catch(IOException ignored) {
            }
            instance = null;
        }
    }

    private static int getConfiguredPort() {
        String value = System.getProperty("crtlogrecord.cli.port");
        if(value == null || value.trim().isEmpty()) {
            return DEFAULT_PORT;
        }

        try {
            int port = Integer.parseInt(value.trim());
            if(port < 1 || port > 65535) {
                throw new NumberFormatException("out of range");
            }
            return port;
        } catch(NumberFormatException e) {
            CrtLogUtil.log(CrtLogUtil.WARN, "Invalid crtlogrecord.cli.port \"" + value + "\", using " + DEFAULT_PORT);
            return DEFAULT_PORT;
        }
    }

    private void acceptLoop() {
        while(running) {
            try {
                Socket socket = serverSocket.accept();
                Thread worker = new Thread(() -> handleClient(socket), "CrtLogRecord Local Check Client");
                worker.setDaemon(true);
                worker.start();
            } catch(SocketException e) {
                if(running) {
                    CrtLogUtil.log(CrtLogUtil.ERROR, "Local crtcheck socket error: " + e.getMessage());
                }
            } catch(IOException e) {
                CrtLogUtil.log(CrtLogUtil.ERROR, "Local crtcheck accept failed: " + e.getMessage());
            }
        }
    }

    private void handleClient(Socket socket) {
        try(Socket client = socket;
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8))) {
            client.setSoTimeout(10000);
            String response = handleRequest(reader.readLine());
            writer.write(response);
            writer.flush();
        } catch(Exception e) {
            CrtLogUtil.log(CrtLogUtil.ERROR, "Local crtcheck request failed: " + e.getMessage());
        }
    }

    private String handleRequest(String request) throws Exception {
        if(request == null) {
            return "ERROR: empty request\n";
        }

        String trimmed = request.trim();
        if(trimmed.isEmpty()) {
            return runOnServerThread(SyntaxCheckRunner.DEFAULT_LOADER);
        }

        if(!trimmed.equals("CHECK") && !trimmed.startsWith("CHECK ")) {
            return "ERROR: unsupported request. Expected: CHECK [loaderName]\n";
        }

        String loaderName = trimmed.length() > 5 ? trimmed.substring(5).trim() : SyntaxCheckRunner.DEFAULT_LOADER;
        return runOnServerThread(loaderName);
    }

    private String runOnServerThread(String loaderName) throws Exception {
        FutureTask<String> task = new FutureTask<>(new Callable<String>() {
            @Override
            public String call() {
                return SyntaxCheckRunner.run(loaderName).toTerminalText();
            }
        });

        if(minecraftServer.isCallingFromMinecraftThread()) {
            task.run();
        } else {
            minecraftServer.addScheduledTask(task);
        }

        try {
            return task.get(30, TimeUnit.SECONDS);
        } catch(TimeoutException e) {
            return "ERROR: syntax check timed out\n";
        }
    }

    @Override
    public void close() throws IOException {
        running = false;
        serverSocket.close();
    }
}
