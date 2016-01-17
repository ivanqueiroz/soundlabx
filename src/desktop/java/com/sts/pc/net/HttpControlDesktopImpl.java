package com.sts.pc.net;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.sts.net.HttpControl;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

public class HttpControlDesktopImpl implements HttpControl {

    private final HttpClient httpClient;
    private final String URL_SERVER = "http://localhost:8080/stsweb/exercise/save";
    private static final Logger LOG = Logger.getLogger(HttpControlDesktopImpl.class.getName());
    public HttpControlDesktopImpl() {
        httpClient = HttpClients.createDefault();
    }

    @Override
    public void sendExercise(double[] exercise) {
        try {
            HttpPost httppost = new HttpPost(URL_SERVER);
            List<NameValuePair> params = new ArrayList<>(1);
            JsonArray exerciseJson = Json.array(exercise);
            final String exerciseJsonString = exerciseJson.toString();
            LOG.log(Level.CONFIG, "JSON EXERCISE: {0}", exerciseJsonString);
            params.add(new BasicNameValuePair("exercise", exerciseJsonString));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            HttpResponse response = httpClient.execute(httppost);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                InputStream instream = entity.getContent();
                try {
                    LOG.info("CHAMOU 3");
                } finally {
                    instream.close();
                }
            }
        } catch (UnsupportedEncodingException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HttpControlDesktopImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
