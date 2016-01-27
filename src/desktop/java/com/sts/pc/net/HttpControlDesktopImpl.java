package com.sts.pc.net;

import com.sts.net.HttpControl;
import com.sts.net.HttpServerObserver;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpControlDesktopImpl implements HttpControl {

    private final CloseableHttpClient httpClient;
    private final String URL_SERVER = "http://sts-ivanqueiroz.rhcloud.com/exercise/save";
    //private final String URL_SERVER = "http://localhost:8080/stsweb/exercise/save";
    private static final Logger LOG = Logger.getLogger(HttpControlDesktopImpl.class.getName());
    private final List<HttpServerObserver> observers = new ArrayList<>();

    private String serverResult;

    public HttpControlDesktopImpl() {
        httpClient = HttpClients.createDefault();
    }

    @Override
    public void sendExercise(double[] exercise, long duration) {
        try {
            HttpPost httppost = new HttpPost(URL_SERVER);
            List<NameValuePair> params = new ArrayList<>(2);
            params.add(new BasicNameValuePair("exercise", getExerciseAsString(exercise)));
            params.add(new BasicNameValuePair("duration", String.valueOf(duration)));

            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            try (CloseableHttpResponse response = httpClient.execute(httppost)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {

                    if (response.getStatusLine().getStatusCode() != 200) {
                        setServerResult("Status Error: " + response.getStatusLine().getStatusCode());
                    } else {
                        setServerResult(EntityUtils.toString(entity));
                    }
                }
            }

        } catch (UnsupportedEncodingException ex) {
            LOG.log(Level.SEVERE, null, ex);
            setServerResult("Execution Error: " + ex.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(HttpControlDesktopImpl.class.getName()).log(Level.SEVERE, null, ex);
            setServerResult("Execution Error: " + ex.getMessage());
        }
    }

    private String getExerciseAsString(double[] exercise) {

        StringBuilder stmExercise = new StringBuilder("[");
        int i = 0;

        for (double valor : exercise) {

            stmExercise.append(String.format(Locale.US, "%.1f", valor)).append(",");
        }
        stmExercise.deleteCharAt(stmExercise.lastIndexOf(",")).append("]");
        return stmExercise.toString();
    }

    @Override
    public void addObserver(HttpServerObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(HttpServerObserver observer) {
        observers.remove(observer);
    }
    
    private void setServerResult(String serverResult){
        this.serverResult = serverResult;
        notifyObservers();
    }

    private void notifyObservers() {
        observers.stream().forEach((observer) -> {
            observer.httpResult(serverResult);
        });
    }

}
