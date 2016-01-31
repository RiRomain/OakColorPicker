package com.riromain.oak.colorpickeroak2.http.helper;

import com.riromain.oak.colorpickeroak2.http.result.ObjectWithPotentialError;
import com.riromain.oak.colorpickeroak2.http.result.ObjectWithPotentialErrorImpl;
import com.riromain.oak.colorpickeroak2.object.SetNewValueInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

/**
 * Created by rrinie on 25.01.16.
 */
public class HttpPostNewValue {
    private HttpPostNewValue() {
    }

    public static ObjectWithPotentialError<String> execute(final SetNewValueInfo info) {
        URL url = null;
        try {
            url = new URL("https://api.particle.io/v1/devices/" + info.getDeviceId() + "/" + info.getServiceId() + "/?access_token=" + info.getAccessToken());
            //Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.100.4", 8080));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();//proxy);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            bufferedWriter.write("args=" + info.getNewValue());
            bufferedWriter.flush();
            bufferedWriter.close();
            int responseCode =  connection.getResponseCode();
            if (200 != responseCode) {
                String error = convertInputStreamToString(connection.getErrorStream());
                return new ObjectWithPotentialErrorImpl<>(responseCode, error);
            }
            InputStream response = connection.getInputStream();
            return new ObjectWithPotentialErrorImpl<>(convertInputStreamToString(response));
        } catch (IOException e) {
            e.printStackTrace();
            return new ObjectWithPotentialErrorImpl<>(0, "IOException for URL " + url + "\n" + e.getMessage());
        }
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}
