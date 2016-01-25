package com.riromain.oak.colorpickeroak2;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

/**
 * Created by Rinie Romain on 24/01/2016.
 */
public class HttpGETEntityAsString {

    public static HttpGetStringResp execute(URL myURL) {
        try {
            URLConnection connection = myURL.openConnection();
            connection.setRequestProperty("Accept-Charset", Charset.forName("utf-8").name());
            connection.connect();
            int responseCode = ((HttpURLConnection) connection).getResponseCode();
            if (200 != responseCode) {
                return HttpGetStringResp.errorWithCode("Wrong http resp", responseCode);
            }
            InputStream response = connection.getInputStream();
            return HttpGetStringResp.success(convertInputStreamToString(response));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return HttpGetStringResp.error("MalformedURLException for URL " + myURL + "\n" + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return HttpGetStringResp.error("IOException for URL " + myURL + "\n" + e.getMessage());
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
