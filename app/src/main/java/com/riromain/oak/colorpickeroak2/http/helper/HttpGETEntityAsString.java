package com.riromain.oak.colorpickeroak2.http.helper;

import android.support.annotation.NonNull;

import com.riromain.oak.colorpickeroak2.http.result.ObjectWithPotentialError;
import com.riromain.oak.colorpickeroak2.http.result.ObjectWithPotentialErrorImpl;
import com.riromain.oak.colorpickeroak2.object.GetVariableInfo;
import com.riromain.oak.colorpickeroak2.object.OakInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

/**
 * Created by Rinie Romain on 24/01/2016.
 */
public class HttpGETEntityAsString {
    private HttpGETEntityAsString() {
    }

    public static ObjectWithPotentialError<String> getName(final OakInfo oakInfo) {

        URL myURL = null;
        try {
            myURL = new URL("https://api.particle.io/v1/devices/" + oakInfo.getDeviceId() + "/?access_token=" + oakInfo.getAccessToken());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return new ObjectWithPotentialErrorImpl<>(0, "MalformedURLException for URL\n" + e.getMessage());
        }
        return processPost(myURL);
    }
    public static ObjectWithPotentialError<String> getVariable(final GetVariableInfo getVariableInfo) {
        URL myURL = null;
        try {
            myURL = new URL(createGetVariableUrl(getVariableInfo));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return new ObjectWithPotentialErrorImpl<>(0, "MalformedURLException for URL\n" + e.getMessage());
        }
        return processPost(myURL);
    }

    @NonNull
    private static ObjectWithPotentialError<String> processPost(final URL myURL) {
        try {
          //  Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.100.4", 8080));
            URLConnection connection = myURL.openConnection();//proxy);
            connection.setRequestProperty("Accept-Charset", Charset.forName("utf-8").name());
            connection.connect();
            int responseCode = ((HttpURLConnection) connection).getResponseCode();
            if (200 != responseCode) {
                String error = convertInputStreamToString(((HttpURLConnection) connection).getErrorStream());
                return new ObjectWithPotentialErrorImpl<>(responseCode, error);
            }
            InputStream response = connection.getInputStream();
            return new ObjectWithPotentialErrorImpl<>(convertInputStreamToString(response));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return new ObjectWithPotentialErrorImpl<>(0, "MalformedURLException for URL " + myURL + "\n" + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return new ObjectWithPotentialErrorImpl<>(0, "IOException for URL " + myURL + "\n" + e.getMessage());
        }
    }

    @NonNull
    private static String createGetVariableUrl(final GetVariableInfo getVariableInfo) {
        return "https://api.particle.io/v1/devices/" + getVariableInfo.getDeviceId()
                + "/" + getVariableInfo.getVariableId() + "/?access_token="
                + getVariableInfo.getAccessToken();
    }


    private static String convertInputStreamToString(final InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }
}
