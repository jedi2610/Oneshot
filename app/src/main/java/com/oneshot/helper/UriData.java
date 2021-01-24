package com.oneshot.helper;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.IOException;
import java.io.InputStream;

public class UriData {

    private static ContentResolver contentResolver;
    private final Uri uri;
    private String fileName = null;
    private long size = 0;
    private String mimeType;
    private boolean isDirectory;

    public UriData(Uri uri) {
        this.uri = uri;
        processUri();
    }

    private void processUri() {

        try (Cursor metadataCursor = contentResolver.query(uri, new String[]{
                        OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE}, null,
                null, null)) {
            if (metadataCursor.moveToFirst()) {
                fileName = metadataCursor.getString(0);
                size = metadataCursor.getLong(1);
            }
        }
        mimeType = setMimeType();
    }

    private String setMimeType() {
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

    public String getFileName() {
        return fileName;
    }

    public long getSize() {
        return size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public InputStream getInputStream() throws IOException {
        return contentResolver.openInputStream(uri);
    }

    public static void setContentResolver(ContentResolver resolver) {
        contentResolver = resolver;
    }
}