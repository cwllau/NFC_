package com.bah.iotsap;

import android.Manifest;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.bah.iotsap.services.BleDiscoveryService;
import com.bah.iotsap.services.BluetoothDiscoveryService;
import com.bah.iotsap.services.ServiceManager;

/**
 * This activity is basically a container for various fragments. It consists
 * only of a ViewPager, which is populated upon creation with various fragments
 * which can be swiped between.
 */
public class MainActivity extends FragmentActivity {

    private static final String TAG = "MainActivity";
    private static final int NUM_FRAGMENTS = 5;
    private static final int START_INDEX   = 1;
    private static final int PREF_INDEX    = 0;
    private static final int MAP_INDEX     = 1;
    private static final int BT_INDEX      = 2;
    private static final int BLE_INDEX     = 3;
    private static final int NFC_INDEX     = 4;

    PagerAdapter pagerAdapter;
    ViewPager viewPager;

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate: setContentView");
        setContentView(R.layout.activity_main);

        /**
         * Attach the pager adapter to the viewPager layout of the activity.
         * This allows us to swipe between fragments on the same activity.
         */
        Log.i(TAG, "onCreate: Setting up pagerAdapter / viewPager");
        pagerAdapter = new PagerAdapter(getFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.fragment_pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(START_INDEX);

        //Set up NFC Adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if (nfcAdapter == null)
        {
            Log.i(TAG, "NFC CAT");
            Toast.makeText(this, "No NFC capability", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (!nfcAdapter.isEnabled())
        {
            Log.i(TAG, "onCreate(): nfcAdapter is not enabled");
            Toast.makeText(this, "Please enable NFC Adapter", Toast.LENGTH_SHORT).show();
            finish();
        }

        /**
         * Request permission for fine location if it is not already granted
         */
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "onCreate(): Requesting FINE LOCATION permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        // Service manager handles launching services that are able to run on device
        startService(new Intent(ServiceManager.START, null, this, ServiceManager.class));

    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy(): stopping ServiceManager");
        stopService(new Intent(ServiceManager.STOP, null, this, ServiceManager.class));
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent == null)
        {
            Log.i(TAG, "onNewIntent(): NULL INTENT");
            return;
        }

        Log.i(TAG, "onNewIntent(): action + " + intent.getAction() + ", Type = " + intent.getType());
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()))
        {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Log.i(TAG, "onNewIntent(): tag to string: " + tag.toString());

            Parcelable [] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null)
            {
                //Parsing through ndef msg?
                NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i<msgs.length; ++i)
                {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                    Log.i(TAG, "onNewIntent() : Message" + i + " = " + msgs[i].toString());
                }
            }
        }
    }

    /**
     * Class used to swipe between different fragments.
     */
    private static class PagerAdapter extends FragmentPagerAdapter {

        SparseArray<Fragment> registeredFragments = new SparseArray<>(NUM_FRAGMENTS);

        public PagerAdapter(android.app.FragmentManager fm) {
            super(fm);
            Log.i(TAG, "PagerAdapter: constructor");
        }

        /**
         * Instantiates fragments for each position and stores them in a SparseArray
         * for later retrieval.
         * @param position
         * @return fragment belonging to position
         */
        @Override
        public Fragment getItem(int position) {
            Log.i(TAG, "PagerAdapter: getItem, position = " + position);
            switch(position) {
                case PREF_INDEX: registeredFragments.put(position, new SettingsFragment());    break;
                case MAP_INDEX : registeredFragments.put(position, MapFragment.newInstance()); break;
                case BT_INDEX  : registeredFragments.put(position, ItemFragment.newInstance(
                        BluetoothDiscoveryService.RECEIVE_JSON)); break;
                case BLE_INDEX : registeredFragments.put(position, ItemFragment.newInstance(
                        BleDiscoveryService.RECEIVE_JSON)); break;
                case NFC_INDEX : registeredFragments.put(position, new NfcFragment()); break;
                default: break;
            }
            return registeredFragments.get(position);
        }

        /**
         * This function dictates how many screens you can swipe through.
         * @return number of fragment pages to have as an int
         */
        @Override
        public int getCount() {
            return NUM_FRAGMENTS;
        }

        /**
         * Use to retrieve a fragment at a particular position.
         * Usage: MapFragment frag = (MapFragment) pagerAdapter.getFragmentAt(MAP_INDEX);
         * @param position
         * @return Fragment
         */
        @Nullable
        public Fragment getFragmentAt(int position) {
            Log.i(TAG, "PagerAdapter: getFragmentAt(" + position + ")");
            return registeredFragments.get(position);
        }
    }
}