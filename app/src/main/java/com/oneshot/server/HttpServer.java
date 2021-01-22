package com.oneshot.server;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer extends Thread {

    public static final String TAG = "HttpServer";
    private int port;
    private ServerSocket serverSocket = null;

    public HttpServer(int port) {
        this.port = port;
    }
    public void setPort(int port) {
        this.port = port;
    }

    public synchronized void startServer() {
        this.start();
    }

    public synchronized void stopServer() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean bindToPort() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Unable to connect to port");
            return false;
        }
        return true;
    }

    @Override
    public void run() {

        if (!bindToPort())
            return;

        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                InetAddress client = socket.getInetAddress();
                Log.d(TAG, client.getHostAddress() + " " + client.getHostName());
                new Thread(new HttpConnection(socket)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}