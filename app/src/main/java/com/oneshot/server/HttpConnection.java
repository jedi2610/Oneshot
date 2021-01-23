package com.oneshot.server;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
    private static final String CRLF = "\r\n";

    private final Socket socket;
    private final ContentResolver contentResolver;
    private final Uri fileUri;

    public HttpConnection(Socket socket, ContentResolver contentResolver, Uri fileUri) {
        this.socket = socket;
        this.contentResolver = contentResolver;
        this.fileUri = fileUri;
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

    private StringBuilder constructHeader(int returnCode, String mimeType, long fileSize) {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 ");
        response.append(getReturnCodeString(returnCode)).append(CRLF);
        response.append("Connection: close").append(CRLF);
        response.append("Content-Type: ").append(mimeType).append(CRLF);
        response.append("Content-Length: ").append(fileSize).append(CRLF).append(CRLF);
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
                output.write(constructHeader(BAD_REQUEST, getMimeType(null), 0).toString().getBytes());
            } catch (IOException exp) {
                exp.printStackTrace();
                Log.d(TAG, "handleRequest: Unable to write to output stream");
            }
            return;
        }

//        String html = "<html><head><title>HTTP Server</title></head><body><h1>This is a basic HTTP Server written in Java</h1></body><html>";
        long size = 0;
        String name = null;
        try (Cursor metadataCursor = contentResolver.query(fileUri, new String[]{
                        OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE}, null,
                null, null)) {
            if (metadataCursor.moveToFirst()) {
                name = metadataCursor.getString(0);
                size = metadataCursor.getLong(1);
            }
        }
        StringBuilder response = constructHeader(OK, getMimeType(name), size);
        Log.i(TAG, "requestHandler: header: " + header);

        if (header.toUpperCase().startsWith("HEAD")) {
            try {
                output.write(response.toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "handleRequest: Unable to write to output stream");
            }
            return;
        } else if (!header.startsWith("GET")) {
            // methods other than GET and HEAD are not supported
            try {
                output.write(constructHeader(NOT_IMPLEMENTED, getMimeType(null), 0).toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "handleRequest: Unable to write to output stream");
            }
            return;
        }

        String[] words = header.split(" ");
        if (words[0].equals("GET") && words[1].equals("/favicon.ico")) {
            try {
                output.write(constructHeader(NOT_FOUND, getMimeType(null), 0).toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG, "handleRequest: Unable to write bytes to output stream");
            }
            return;
        }

        try {
            InputStream fileInputStream = contentResolver.openInputStream(fileUri);
            output.write(response.toString().getBytes());
            byte[] buffer = new byte[4096];
            for (int n; (n = fileInputStream.read(buffer)) != -1; ) {
                output.write(buffer, 0, n);
            }
            output.write(CRLF.getBytes());
            output.write(CRLF.getBytes());
            output.flush();
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            try {
                output.write(constructHeader(NOT_FOUND, getMimeType(null), 0).toString().getBytes());
                Log.i(TAG, "handleRequest: " + fileUri.toString());
            } catch (IOException exp) {
                exp.printStackTrace();
            }
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getMimeType(String fileName) {
        if (fileName == null)
            return "application/octet-stream";

        int pos = fileName.lastIndexOf(".");
        String ext = fileName.substring(pos + 1);
        switch (ext) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "mp4":
                return "video/mp4";
            case "avi":
                return "video/avi";
            case "mov":
                return "video/mov";
            case "mp3":
                return "audio/mpeg";
            case "aac":
                return "audio/aac";
            case "wav":
                return "audio/wav";
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "vcf":
                return "text/x-vcard";
            case "txt":
                return "text/plain";
            case "html":
                return "text/html";
            case "json":
                return "application/json";
            case "epub":
                return "application/epub+zip";
            default:
                return "application/octet-stream";
        }
    }

    @Override
    public void run() {

        processResponse();
    }
}