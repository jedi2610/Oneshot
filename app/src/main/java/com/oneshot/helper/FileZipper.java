package com.oneshot.helper;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileZipper implements Runnable {

    private final OutputStream output;
    private final ArrayList<UriData> fileUris;
    private ZipOutputStream zipFile;

    public FileZipper(OutputStream output, ArrayList<UriData> fileUris) {
        this.output = output;
        this.fileUris = fileUris;
    }

    @Override
    public void run() {
        CheckedOutputStream outputStream = new CheckedOutputStream(output, new Adler32());
        zipFile = new ZipOutputStream(outputStream);

        for (UriData data : fileUris) {
            addFile(data);
        }

        try {
            zipFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addFile(UriData data) {
        try {
            ZipEntry entry = new ZipEntry(data.getFileName());
            Log.i("FileZipper", "addFile: " + data.getFileName());
            zipFile.putNextEntry(entry);
            InputStream file = data.getInputStream();
            byte[] buffer = new byte[4096];
            for (int n; (n = file.read(buffer)) != -1; ) {
                zipFile.write(buffer, 0, n);
            }
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
