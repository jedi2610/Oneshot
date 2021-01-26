package com.oneshot.server;

import android.util.Log;

import com.oneshot.helper.FileZipper;
import com.oneshot.helper.UriData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HttpConnection implements Runnable {

    private static final int OK = 200;
    private static final int MOVED_TEMPORARILY = 302;
    private static final int BAD_REQUEST = 400;
    private static final int FORBIDDEN = 403;
    private static final int NOT_FOUND = 404;
    private static final int INTERNAL_SERVER_ERROR = 500;
    private static final int NOT_IMPLEMENTED = 501;
    private static final String TAG = "HttpConnection";
    private static final String CRLF = "\r\n";

    private final Socket socket;
    private boolean sendHeaderOnly = false;
    private final ArrayList<UriData> fileUris;

    public HttpConnection(Socket socket, ArrayList<UriData> fileUris) {
        this.socket = socket;
        this.fileUris = fileUris;
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

    private StringBuilder constructHeader(int returnCode, String mimeType, long fileSize, String fileName) {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 ");
        response.append(getReturnCodeString(returnCode)).append(CRLF);
        response.append("Connection: close").append(CRLF);
        if (fileUris.size() == 1) {
            response.append("Content-Length: ").append(fileSize).append(CRLF);
        }
        response.append("Content-Disposition: attachment; filename=\"").append(fileName).append("\"").append(CRLF);
        response.append("Content-Type: ").append(mimeType).append(CRLF).append(CRLF);
        return response;
    }

    private void processResponse() {
        BufferedReader input;
        OutputStream output;
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "getResponse: Unable to get input or output stream from socket");
            return;
        }

        handleRequest(input, output);
        try {
            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(BufferedReader input, OutputStream output) {

        String header;
        try {
            header = input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                output.write(constructHeader(BAD_REQUEST, null, 0, null).toString().getBytes());
            } catch (IOException exp) {
                exp.printStackTrace();
                Log.d(TAG, "handleRequest: Unable to write to output stream");
            }
            return;
        }

        if (header.toUpperCase().startsWith("HEAD")) {
            sendHeaderOnly = true;
        } else if (!header.startsWith("GET")) {
            // methods other than GET and HEAD are not supported
            try {
                output.write(constructHeader(NOT_IMPLEMENTED, null, 0, null).toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "handleRequest: Unable to write to output stream");
            }
            return;
        }

        String[] words = header.split(" ");
        if (words[0].equals("GET") && words[1].equals("/favicon.ico")) {
            try {
                output.write(constructHeader(NOT_FOUND, null, 0, null).toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "handleRequest: Unable to write bytes to output stream");
            }
            return;
        }

        if (fileUris.size() > 1) {
            shareMultipleFiles(output);
        } else {
            shareOneFile(output);
        }
    }

    private void shareOneFile(OutputStream outputStream) {
        UriData data = fileUris.get(0);
        String response = constructHeader(OK, data.getMimeType(), data.getSize(), data.getFileName()).toString();
        try {
            outputStream.write(response.getBytes());
            if (sendHeaderOnly) {
                return;
            }
            InputStream fileInputStream = data.getInputStream();
            byte[] buffer = new byte[4096];
            for (int n; (n = fileInputStream.read(buffer)) != -1; ) {
                outputStream.write(buffer, 0, n);
            }
            outputStream.write(CRLF.getBytes());
            outputStream.write(CRLF.getBytes());
            outputStream.flush();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "shareOneFile: cannot write to output stream");
        }
    }

    private void shareMultipleFiles(OutputStream outputStream) {
        String mimeType = "multipart/x-zip";
        String fileName = "oneshot-" + new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss", Locale.getDefault()).format(new Date()) + ".zip";
        String response = constructHeader(OK, mimeType, getFileSize(), fileName).toString();
        try {
            outputStream.write(response.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "shareMultipleFiles: " + e.getMessage());
        }
        FileZipper zipper = new FileZipper(outputStream, fileUris);
        zipper.run();
    }

    private long getFileSize() {
        long fileSize = 0;
        for (UriData fileUri : fileUris) {
            fileSize += fileUri.getSize();
        }
        return fileSize;
    }

    @Override
    public void run() {
        processResponse();
    }
}