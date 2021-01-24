package com.oneshot.server;

import android.content.ContentResolver;
import android.util.Log;

import com.oneshot.helper.UriData;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class HttpServer extends Thread {

    public static final String TAG = "HttpServer";
    private final int port;
    private final ArrayList<UriData> fileUris;
    private ServerSocket serverSocket = null;

    public HttpServer(int port, ArrayList<UriData> fileUris) {
        this.port = port;
        this.fileUris = fileUris;
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
                Log.d(TAG, client.getHostAddress());
                new Thread(new HttpConnection(socket, fileUris)).start();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "run: " + e.getClass());
            }
        }
    }
}