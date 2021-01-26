package com.oneshot.server;

import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileDownloader extends Thread{

    private final URL url;
    private final File baseDir;

    public FileDownloader(URL url, File baseDir) {
        this.url = url;
        this.baseDir = baseDir;
    }

    @Override
    public void run() {
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            String fileName = null;
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                fileName = conn.getHeaderField("Content-Disposition").split("=")[1];
                Log.i("ReceiveFragment", "downloadFiles: " + fileName + " " + baseDir.getPath());
            } else {
                return;
            }
            InputStream is = conn.getInputStream();
            DataInputStream dis = new DataInputStream(is);

            byte[] buffer = new byte[4096];
            int length;

            if (!baseDir.exists()) {
                baseDir.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(new File(baseDir, fileName));
            while ((length = dis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            fos.flush();
            fos.close();
            dis.close();
            is.close();
        } catch (IOException ioe) {
            Log.e("SYNC getUpdate", "io error", ioe);
        } catch (SecurityException se) {
            Log.e("SYNC getUpdate", "security error", se);
        }
    }
}
