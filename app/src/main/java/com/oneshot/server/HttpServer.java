package com.oneshot.server;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer extends Thread {

    public static final String TAG = "HttpServer";
    private final int port;
    private final ContentResolver contentResolver;
    private final Uri fileUri;
    private ServerSocket serverSocket = null;

    public HttpServer(int port, ContentResolver contentResolver, Uri fileUri) {
        this.port = port;
        this.contentResolver = contentResolver;
        this.fileUri = fileUri;
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
                new Thread(new HttpConnection(socket, contentResolver, fileUri)).start();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "run: " + e.getClass());
            }
        }
    }
}