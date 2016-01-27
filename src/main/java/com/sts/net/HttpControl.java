package com.sts.net;

/**
 *
 * @author Ivan
 */
public interface HttpControl {

    public void sendExercise(double[] exercise, long duration);

    public void addObserver(HttpServerObserver observer);

    public void removeObserver(HttpServerObserver observer);

}
