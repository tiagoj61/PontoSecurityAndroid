package com.example.pontosecurity.communicate;


import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.pontosecurity.callback.IFutureCallback;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.IOUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ComunicateToServer extends Thread {
    private IFutureCallback callback;
    private List<String> macs ;
    private String url ;
    private String codigo ;

    public void onFutureCallback(IFutureCallback callback, List<String> macs, String string,String codigo) {
        this.callback = callback;
        this.macs = macs;
        this.url = string;
        this.codigo = codigo;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void run() {


        HttpURLConnection connection = null;
        try {
            URL url = new URL(this.url+"funcionario/login");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("charset", "utf-8");
            connection.setUseCaches(false);
            connection.setDoOutput(true);

            JSONObject body = new JSONObject();
            body.put("codigo", codigo);
            JSONArray mac = new JSONArray();
            Stream.of(macs).parallel()
                    .forEach(e -> {
                        synchronized(mac) {
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
            if(statusCode==200){
                callback.onSuccess();
            }else{
                callback.onError(new Exception());
            }
           /* InputStream in = new BufferedInputStream(connection.getInputStream());
            String resultado = IOUtils.toString(in, "UTF-8");



            JSONObject result = new JSONObject(resultado);

            JSONArray responseFiles = new JSONArray(result.get("blobs").toString());
            for (int i = 0; i < responseFiles.length(); i++) {

                JSONObject file = new JSONObject(responseFiles.get(i).toString());
                String nomeArquivo = file.getString("name");
                String caminhoUrl = file.getString("url");
                //String caminhoUrl = file.getString("size");

            }

            JSONObject response = new JSONObject(resultado);
*/


            //in.close();
            callback.onSuccess();
        } catch (IOException | JSONException e) {

            callback.onError(e);
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }
}
