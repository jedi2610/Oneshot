package com.oneshot.activities;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.oneshot.R;
import com.oneshot.server.FileDownloader;

import java.net.MalformedURLException;
import java.net.URL;

public class ReceiveFragment extends Fragment {

    private String addressText;
    private int portNumber;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_receive, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText address = view.findViewById(R.id.ip_address);
        TextInputEditText port = view.findViewById(R.id.port);
        Button downloadButton = view.findViewById(R.id.download_button);

        Log.i("ReceiveFragment", "onViewCreated: " + address + port);

        downloadButton.setOnClickListener(v -> {
            addressText = address.getText().toString();
            portNumber = Integer.parseInt(port.getText().toString());
            Log.i("ReceiveFragment", "onViewCreated: " + addressText + isValidIp(addressText));
            if (isValidIp(addressText))
                downloadFiles();
            else
                Snackbar.make(getView(), "Not a valid IP address", Snackbar.LENGTH_LONG).show();
        });
    }

    private boolean isValidIp(String ip) {
        try {
            if (ip == null || ip.isEmpty() || ip.endsWith(".")) {
                return false;
            }

            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                return false;
            }

            for (String s : parts) {
                int i = Integer.parseInt(s);
                if ((i < 0) || (i > 255)) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void downloadFiles() {
        String url = "http://" + addressText + ":" + portNumber;
        Log.i("ReceiveFragment", "downloadFiles: " + url);
        try {
            new FileDownloader(new URL(url), getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)).start();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}