package com.bah.iotsap.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BluetoothDiscoveryService extends Service {

    /**
     * This public static String can be used to register receivers, used primarily
     * for receiving JSON data outside of this service from this service.
     */
    public  static final String RECEIVE_JSON = "com.bah.iotsap.services.RECEIVE_JSON";
    private static final String TAG = "BTDiscoveryService";

    private final BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive()");
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get information from discovered devices
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceMAC  = device.getAddress();
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                String date = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                Log.i(TAG, "receiver: " + " name: " + deviceName + " MAC: " + deviceMAC +
                           " RSSI: " + rssi + " Date: " + date);

                // Send information in local broadcast using JSON format
                JSONObject item = new JSONObject();
                try {
                    Log.i(TAG, "Creating JSONObject and sending through local Intent");
                    item.put("date", date);
                    item.put("mac", deviceMAC);
                    item.put("name", deviceName);
                    item.put("rssi", rssi);
                    Intent deviceInfo = new Intent();
                    deviceInfo.setAction(RECEIVE_JSON);
                    deviceInfo.putExtra("json", item.toString());
                    Log.i(TAG, item.toString());
                    //LocalBroadcastManager.getInstance(BluetoothDiscoveryService.this).sendBroadcast(deviceInfo);
                    sendBroadcast(deviceInfo);
                } catch(JSONException e) {
                    Log.i(TAG, "onReceive(): Caught JSON Exception");
                }

            } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i(TAG, "onReceive(): Restarting BT discovery");
                btAdapter.startDiscovery();
            }
        }
    };

    public BluetoothDiscoveryService() {
        Log.i(TAG, "Constructor");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate(): Entered");
        // Bluetooth discovery setup
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand()");
        // Check only continue if device supports Bluetooth
        if(btAdapter == null) {
            Log.i(TAG, "onStartCommand(): Device does not support BT, calling stopSelf()");
            stopSelf();
        } else if(!btAdapter.isEnabled()) {
            Log.i(TAG, "onStartCommand(): enabling bluetooth adapter");
            btAdapter.enable();
        }

        Log.i(TAG, "onStartCommand(): Discovering devices");
        btAdapter.startDiscovery();

        // This prevents the service from restarting after it is killed
        return START_NOT_STICKY;
        //return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy(): Entered");
        super.onDestroy();
        Log.i(TAG, "onDestroy(): Unregistering receiver");
        unregisterReceiver(receiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind(): Entered");
        return null;
    }
}
