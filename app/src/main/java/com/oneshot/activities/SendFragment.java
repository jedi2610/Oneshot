package com.oneshot.activities;

import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.oneshot.R;
import com.oneshot.server.HttpServer;

import static android.content.Context.WIFI_SERVICE;

public class SendFragment extends Fragment {

    private HttpServer httpServer;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_send, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button startButton = view.findViewById(R.id.start_server);
        Button stopButton = view.findViewById(R.id.stop_server);

        startButton.setOnClickListener(v -> {
            WifiManager wm = (WifiManager) SendFragment.this.getActivity().getApplicationContext().getSystemService(WIFI_SERVICE);
            String ip = Integer.toString(wm.getConnectionInfo().getIpAddress());
            startServer();
            Log.d("SendFragment", "onViewCreated: " + ip);
            v.setVisibility(View.INVISIBLE);
            stopButton.setVisibility(View.VISIBLE);
        });

        stopButton.setOnClickListener(v -> {
            stopServer();
            v.setVisibility(View.INVISIBLE);
            startButton.setVisibility(View.VISIBLE);
        });
    }

    private void startServer() {
        httpServer = new HttpServer(9999);
        httpServer.startServer();
    }

    private void stopServer() {
        httpServer.stopServer();
    }
}