package com.example.pontosecurity.communicate;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.Toast;

import com.example.pontosecurity.TextUtil;
import com.example.pontosecurity.communicate.ble.SerialService;
import com.example.pontosecurity.communicate.ble.SerialSocket;

import java.io.IOException;


public class ComunicateToArduino {
    public static SerialService service;

    public static void sendAlertToBLE(Context context) throws IOException {
        connect(context);
    }

    private static void connect(Context context) throws IOException {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice("18:93:D7:34:77:51");
        System.out.println(device);
        SerialSocket socket = new SerialSocket(context, device);
        service = new SerialService();
        service.connect(socket);
    }

    public static void send(String str) {
        System.out.println(str);
        try {
            String newline = TextUtil.newline_crlf;
            String msg;
            byte[] data;
            msg = str;
            data = (str + newline).getBytes();

            SpannableStringBuilder spn = new SpannableStringBuilder(msg + '\n');
            service.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
