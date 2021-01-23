package com.oneshot.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.oneshot.R;
import com.oneshot.server.HttpServer;

public class SendFragment extends Fragment {

    private static final String TAG = "SendFragment";
    private HttpServer httpServer;
    private Button startButton;
    private Button stopButton;
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

        startButton = view.findViewById(R.id.start_server);
        stopButton = view.findViewById(R.id.stop_server);

        startButton.setOnClickListener(v -> {
//            WifiManager wm = (WifiManager) SendFragment.this.getActivity().getApplicationContext().getSystemService(WIFI_SERVICE);
//            String ip = Integer.toString(wm.getConnectionInfo().getIpAddress());
//            Log.d("SendFragment", "onViewCreated: " + ip);
            hideStartButton();
            startFilePickerActivity();
        });

        stopButton.setOnClickListener(v -> {
            stopServer();
            showStartButton();
        });
    }

    private void startFilePickerActivity() {
        Intent shareIntent = new Intent(Intent.ACTION_GET_CONTENT);
        shareIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        shareIntent.setType("*/*");
        startActivityForResult(shareIntent, PICK_FILES_REQUEST_CODE);
    }

    private void hideStartButton() {
        startButton.setVisibility(View.INVISIBLE);
        stopButton.setVisibility(View.VISIBLE);
    }

    private void showStartButton() {
        startButton.setVisibility(View.VISIBLE);
        stopButton.setVisibility(View.INVISIBLE);
    }

    private void startServer(Uri fileUri) {
        httpServer = new HttpServer(9999, getContext().getContentResolver(), fileUri);
        httpServer.startServer();
    }

    private void stopServer() {
        httpServer.stopServer();
        httpServer.interrupt();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK || data == null) {
            return;
        }
        Uri fileUri = data.getData();
        startServer(fileUri);
        Log.i(TAG, "onActivityResult: " + data.getData().toString());
    }
}