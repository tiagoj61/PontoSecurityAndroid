package com.example.pontosecurity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Bundle;

import com.example.pontosecurity.adapter.NumericButtonAdapter;
import com.example.pontosecurity.callback.IFutureCallback;
import com.example.pontosecurity.communicate.ComunicateToArduino;
import com.example.pontosecurity.communicate.ComunicateToServer;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.StrictMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.pontosecurity.databinding.ActivityPassBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class PassActivity extends AppCompatActivity {

    private ActivityPassBinding binding;
    private String numerosDaSenha;
    private int connectedDevices;
    private BluetoothAdapter mBluetoothAdapter;
    private String msg;
    TextView tv;
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        tv = (TextView)findViewById(R.id.totalConnected);
        try {
            ComunicateToArduino.sendAlertToBLE(this.getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
        StrictMode.setThreadPolicy(policy);
        numerosDaSenha = "";
        connectedDevices = 0;
        binding = ActivityPassBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        List<Integer> numeros = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
        NumericButtonAdapter numericButtonAdapter = new NumericButtonAdapter(this, numeros);
        binding.grid.setAdapter(numericButtonAdapter);
        binding.grid.setOnItemClickListener((adapterView, view, position, idView) -> {
            int clickedNumber = numeros.get(position);
            numerosDaSenha += String.valueOf(clickedNumber);
            System.out.println(numerosDaSenha);
            if(numerosDaSenha.length()>5)
                numerosDaSenha=numerosDaSenha.substring(numerosDaSenha.length()-2,numerosDaSenha.length()-1);
            if (numerosDaSenha.length() == 5) {
                try {
                    send();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//            else if (numerosDaSenha.length() > 5) {
//
//                ComunicateToArduino.send("1");
//            }

        });
    }


    public void send() throws IOException {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        List<String> macs = new ArrayList<>();
        for (BluetoothDevice bt : pairedDevices)
            macs.add(bt.getAddress());

        ComunicateToServer threadICPGetACs = new ComunicateToServer();
        threadICPGetACs.onFutureCallback(new IFutureCallback() {
            @Override
            public void onSuccess() {
                msg = "Connect";
                System.out.println(msg);
                connectedDevices++;
                numerosDaSenha = "";
                //changeTextConnectedDevices();
            }

            @Override
            public void onError(Exception exception) {
                numerosDaSenha = "";
                msg = "Not Connect";
                System.out.println(msg);
                connectedDevices = connectedDevices == 0 ? connectedDevices - 1 : 0;
                // changeTextConnectedDevices();
            }
        }, macs, "https://666e-2804-d49-4d33-d200-d18d-baa6-e7da-355e.ngrok.io/PontoSecurity/rest/", numerosDaSenha);
//        new Handler().post(new Runnable() {
//            @RequiresApi(api = Build.VERSION_CODES.N)
//            @Override
//            public void run() {
//

//            }
//        });

        Handler handler = new Handler();
        this.runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void run() {
                HttpURLConnection connection = null;
                try {
                    ComunicateToArduino.send("0");
                    URL url = new URL("https://666e-2804-d49-4d33-d200-d18d-baa6-e7da-355e.ngrok.io/PontoSecurity/rest/funcionario/login");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("charset", "utf-8");
                    connection.setUseCaches(false);
                    connection.setDoOutput(true);

                    JSONObject body = new JSONObject();
                    body.put("codigo", numerosDaSenha);
                    JSONArray mac = new JSONArray();
                    Stream.of(macs).parallel()
                            .forEach(e -> {
                                synchronized (mac) {
                                    mac.put(e);
                                }
                            });
                    body.put("enderecoMacDevices", mac);

                    String jsonInputString = body.toString();
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = jsonInputString.getBytes("utf-8");
                        os.write(input, 0, input.length);
                        os.close();
                    }
                    int statusCode = connection.getResponseCode();
                    System.out.println("Response code S3 Files Origins: " + statusCode);
                    if (statusCode == 200 || statusCode==202) {
                        msg = "Connect";
                        System.out.println(msg);
                        connectedDevices++;
                        numerosDaSenha = "";
                        Toast.makeText(PassActivity.this, msg, Toast.LENGTH_LONG).show();
                    } else if(statusCode != 403){
                        ComunicateToArduino.send("1");
                        numerosDaSenha = "";
                        msg = "Not Connect";
                        System.out.println(msg);
                        connectedDevices = connectedDevices == 0 ? connectedDevices - 1 : 0;
                        Toast.makeText(PassActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        //   threadICPGetACs.start();
    }

    public void changeTextConnectedDevices() {
        //Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        //binding.textConnect.setText(connectedDevices);
    }
}