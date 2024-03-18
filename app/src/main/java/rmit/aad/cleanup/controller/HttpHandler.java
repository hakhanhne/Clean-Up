package rmit.aad.cleanup.controller;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpHandler {
    private static final String BASE_URL = "http://localhost:3001/";
    private static final String REQUEST_CONTENT_TYPE = "application/json;charset=UTF-8";
    private static final String ACCEPT_TYPE = "application/json";

    public static  String getJson(String urlStr) {
        HttpURLConnection httpURLConnection = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            URL url = new URL(urlStr);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

            String line = "";
            while ((line= bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static String postJson(String urlStr) {
        String status = "";
        try {
            URL url = new URL(urlStr);
            HttpURLConnection httpURLConnection =  (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-type", REQUEST_CONTENT_TYPE);
            httpURLConnection.setRequestProperty("Accept", ACCEPT_TYPE);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", "Khanh");
            DataOutputStream os = new DataOutputStream(httpURLConnection.getOutputStream());
            os.writeBytes(jsonObject.toString());
            status = httpURLConnection.getResponseMessage();
            os.flush();
            os.close();
            return status;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
