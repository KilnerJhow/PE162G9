package com.example.notedell.vrcamera;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class BluetoothConnect2 extends Fragment {

    BluetoothAdapter btAdapter;

    private static final int ENABLE_BLUETOOTH = 1 ;

    @Nullable
    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstaceState) {
        return inflater.inflate(R.layout.activity_connect_bluetooth, container, false);
    }

}


