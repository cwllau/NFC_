package com.bah.iotsap;



import android.app.Fragment;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;


public class NfcFragment extends Fragment {

    private static final String TAG = "NfcFragment";
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    public NfcFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nfc, container, false);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "OnCreate () from NFC frag");

        nfcAdapter = NfcAdapter.getDefaultAdapter(this.getActivity());
        if (nfcAdapter == null)
        {
            Log.i(TAG, "NFC CAT");
            Toast.makeText(this.getActivity(), "NFC not available", Toast.LENGTH_SHORT).show();


        }
        pendingIntent = PendingIntent.getActivity(
                this.getActivity(), 0, new Intent(this.getActivity(), getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        Log.i(TAG, "onCreate\n");

        //onClickListener, store data into array somewhere
        BluetoothDevice device = result.getDevice();
        String deviceName = device.getName();
        String deviceMac  = device.getAddress();
        int    rssi       = result.getRssi();
        String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

    }

}
