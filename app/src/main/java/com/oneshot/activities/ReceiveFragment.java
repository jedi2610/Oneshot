package com.oneshot.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.oneshot.R;

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
            downloadFiles();
        });
    }

    private void downloadFiles() {
    }
}