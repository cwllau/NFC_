package com.bah.iotsap;

import 'android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * ItemFragment is a simple fragment that receives messages / data from any intent that it is
 * passed on instantiation.
 * Example: ItemFragment.getInstance(BleDiscoveryService.RECEIVE_JSON)
 * Result : This fragment receives all intents sent from the service with the RECEIVE_JSON action.
 */
public class ItemFragment extends ListFragment {

    private static final String TAG = "ItemFragment";

    private String               action;
    private ArrayAdapter<String> adapter;
    private List<String>         list;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive()");
            if(intent.getAction().equals(action)) {
                Log.i(TAG, "onReceive(): received action " + action);
                list.add(intent.getStringExtra("json"));
                Log.i(TAG, "onReceive(): JSON: " + intent.getStringExtra("json"));
                adapter.notifyDataSetChanged();
            }
        }
    };

    public static ItemFragment newInstance() {
        return new ItemFragment();
    }

    /**
     * Use this method to instantiate new ItemFragments. It is very useful for
     * passing in any String to control what the fragment will listen to in
     * its receiver.
     *
     * @param action a String that the fragment will listen to in a receiver
     * @return new ItemFragment object with passed arguments available
     */
    public static ItemFragment newInstance(String action) {

        Log.i(TAG, "newInstance(): action: " + action);
        Bundle args = new Bundle();
        args.putString("action", action);
        ItemFragment fragment = new ItemFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.get("action") != null) {
            action = (String) savedInstanceState.get("action");
            Log.i(TAG, "onCreate(): received: " + action);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");
        return inflater.inflate(R.layout.fragment_item, container, false);
    }

    /**
     * This method is called once a view is created (duh).
     * This is where any object needs to be instantiated if they
     * do not currently exist.
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "onViewCreated()");

        // Set up adapter with the ListView
        if(list == null) list = new ArrayList<>();
        adapter = new ArrayAdapter<>(view.getContext(), R.layout.list_item, list);
        setListAdapter(adapter);

        // Check the bundle for saved information or Strings
        Bundle bundle = getArguments();
        if(bundle != null && bundle.get("action") != null) {
            action = (String) bundle.get("action");
            Log.i(TAG, "onViewCreated(): action = " + action);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        if(action != null) {
            IntentFilter filter = new IntentFilter(action);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver, filter);
            Log.i(TAG, "onResume(): Registering receiver for action " + action);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            Log.i(TAG, "onPause(): unregister Receiver");
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        } catch(Exception e) {
            Log.i(TAG, "onPause(): Caught exception unregister receiver");
        }
    }
}
