package com.sts.service;

import com.sts.net.HttpControl;
import static java.lang.String.format;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.Logger;

/**
 *
 * @author Ivan
 */
public class HttpService {
    
    private static HttpService INSTANCE;
    private final ServiceLoader<HttpControl> serviceLoader;
    private HttpControl httpControl;
    private static final Logger LOG = Logger.getLogger(HttpService.class.getName());

    public HttpService() {
        serviceLoader = ServiceLoader.load(HttpControl.class);
        
        Iterator<HttpControl> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            if (httpControl == null) {
                httpControl = iterator.next();
                LOG.info(format("USING HTTPCONTROL: %s", httpControl.getClass().getName()));
            } else {
                LOG.info(format("THIS HTTPCONTROL IS IGNORED: %s", iterator.next().getClass().getName()));
            }
        }
        
        if (httpControl == null) {
            LOG.severe("NO HTTPCONTROL IMPLEMENTATION COULD BE FOUND!");
        }
    }
    
     public static synchronized HttpService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HttpService();
        }
        return INSTANCE;
    }
     
     public void sendExercise(double[] exercise, long duration) {
        if (httpControl != null) {
            httpControl.sendExercise(exercise, duration);
        }
    }
    
}
