package com.oneshot.server;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class HttpConnection implements Runnable {

    private static final int OK = 200;
    private static final int MOVED_TEMPORARILY = 302;
    private static final int BAD_REQUEST = 400;
    private static final int FORBIDDEN = 403;
    private static final int NOT_FOUND = 404;
    private static final int INTERNAL_SERVER_ERROR = 500;
    private static final int NOT_IMPLEMENTED = 501;
    private static final String TAG = "HttpConnection";
    private static final String CRLF = "\n\r";

    private final Socket socket;

    public HttpConnection(Socket socket) {
        this.socket = socket;
        Log.i(TAG, "HttpConnection: Inside constructor");
    }

    private String getReturnCodeString(int returnCode) {
        switch (returnCode) {
            case OK:
                return "200 OK";
            case MOVED_TEMPORARILY:
                return "302 Moved Temporarily";
            case BAD_REQUEST:
                return "400 Bad Request";
            case FORBIDDEN:
                return "403 Forbidden";
            case NOT_FOUND:
                return "404 Not Found";
            case INTERNAL_SERVER_ERROR:
                return "500 Internal Server Error";
            case NOT_IMPLEMENTED:
            default:
                return "501 Not Implemented";
        }
    }

    private StringBuilder constructResponse(int returnCode, int fileSize) {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 ");
        response.append(getReturnCodeString(returnCode)).append(CRLF);
        response.append("Connection: close").append(CRLF);
        response.append("Content-Type: text/html").append(CRLF);
        response.append("Content-Length: ").append(fileSize).append(CRLF).append(CRLF);
        return response;
    }

    private void sendResponse() {
        BufferedReader input;
        OutputStream output;
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "getResponse: Unable to get input stream from socket");
            return;
        }
        try {
            output = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "getResponse: Unable to get output stream from socket");
            e.printStackTrace();
            return;
        }

        String html = "<html><head><title>HTTP Server</title></head><body><h1>This is a basic HTTP Server written in Java</h1></body><html>";
        StringBuilder response = constructResponse(OK, html.getBytes().length);
        response.append(html).append(CRLF).append(CRLF);

        try {
            output.write(response.toString().getBytes());
            Log.i(TAG, "sendResponse: Flushed to output stream");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "getResponse: Unable to write bytes to output stream" + e.getMessage());
        }
        try {
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestHandler(BufferedReader input, OutputStream output) {

    }

    @Override
    public void run() {

        Log.i(TAG, "run: inside run");
        sendResponse();
    }
}