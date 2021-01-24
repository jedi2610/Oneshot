package com.oneshot.activities;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.oneshot.R;
import com.oneshot.helper.UriData;
import com.oneshot.server.HttpServer;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SendFragment extends Fragment {

    private static final String TAG = "SendFragment";
    private HttpServer httpServer;
    private Button startButton;
    private Button stopButton;
    private TextView ipTextView;
    private String ipAddr;
    private static final int PICK_FILES_REQUEST_CODE = 1;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_send, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ipTextView = view.findViewById(R.id.ip_addr);
        startButton = view.findViewById(R.id.start_server);
        stopButton = view.findViewById(R.id.stop_server);
        Button shareUrl = view.findViewById(R.id.share_url);

        setIpTextView();
        startButton.setOnClickListener(v -> {
            startFilePickerActivity();
        });

        stopButton.setOnClickListener(v -> {
            stopServer();
            v.setEnabled(false);
            startButton.setEnabled(true);
        });

        shareUrl.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "http://" + ipAddr + ":9999");
            startActivity(Intent.createChooser(shareIntent, "Choose app to share URL"));
        });
    }

    private void setIpTextView() {
//        WifiManager wm = (WifiManager) SendFragment.this.getActivity().getApplicationContext().getSystemService(WIFI_SERVICE);
//        String ip = Integer.toString(wm.getConnectionInfo().getIpAddress());
//        Log.d("SendFragment", "onViewCreated: " + ip);
        ipAddr = getIpAddress(true);
        if (ipAddr != null) {
            ipTextView.setText(String.format("Serving files on: %s:9999", ipAddr));
        } else {
            ipTextView.setText(R.string.cant_get_ip);
        }
    }

    private void startFilePickerActivity() {
        Intent shareIntent = new Intent(Intent.ACTION_GET_CONTENT);
        shareIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        shareIntent.setType("*/*");
        startActivityForResult(shareIntent, PICK_FILES_REQUEST_CODE);
    }

    private void startServer(ArrayList<UriData> fileUris) {
        httpServer = new HttpServer(9999, getContext().getContentResolver(), fileUris);
        httpServer.startServer();
    }

    private void stopServer() {
        httpServer.stopServer();
        httpServer.interrupt();
    }

    private String getIpAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
            return;
        }
        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        Uri url = Uri.parse("http://" + ipAddr + ":9999");
        ClipData clipData = ClipData.newUri(getContext().getContentResolver(), "URL", url);
        clipboardManager.setPrimaryClip(clipData);
        Snackbar.make(getView(), "URL copied to clipboard", Snackbar.LENGTH_LONG).show();

        ArrayList<UriData> fileUris = new ArrayList<>();
        if (data.getClipData() != null) {
            ClipData uris = data.getClipData();
            for (int i=0; i < uris.getItemCount(); i++) {
                Uri uri = uris.getItemAt(i).getUri();
                fileUris.add(new UriData(uri));
            }
        } else {
            Uri uri = data.getData();
            fileUris.add(new UriData(uri));
        }
        startServer(fileUris);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}