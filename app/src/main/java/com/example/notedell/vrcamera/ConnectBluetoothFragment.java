package com.example.notedell.vrcamera;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notedell.vrcamera.Threads.BluetoothThread;

import java.util.Set;

/**
 * Created by Note Dell on 26/12/2016.
 */

public class ConnectBluetoothFragment extends Fragment implements ListView.OnItemClickListener {

    private BluetoothAdapter btAdapter;
    static BluetoothThread mBtt;
    private Set<BluetoothDevice> pairedDevices;
    private ListView lst_PairedDevices;
    private TextView txt_Status;
    public static int ENABLE_BLUETOOTH = 1;

    private Context context = getContext();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_connect_bt, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toast.makeText(context, "Hello World", Toast.LENGTH_SHORT).show();

        lst_PairedDevices = (ListView) view.findViewById(R.id.lst_devices);
        txt_Status = (TextView) view.findViewById(R.id.txt_Status);

        txt_Status.setText("Desconectado");

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        pairedDevices = btAdapter.getBondedDevices();

    /*  Cria um modelo para a lista e o adiciona à tela.
        Se houver dispositivos pareados, adiciona cada um à lista.
     */
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
        lst_PairedDevices.setAdapter(adapter);
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                adapter.add(device.getName() + "\n" + device.getAddress());
            }
        }


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

}
