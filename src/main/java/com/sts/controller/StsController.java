package com.sts.controller;

import com.sts.model.enums.AudioFormatEnum;
import com.sts.service.SoundControlService;
import com.sts.model.MicrophoneModel;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import javafx.animation.AnimationTimer;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.CacheHint;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.util.StringConverter;
import com.sts.media.SoundControlObserver;

public class StsController implements Initializable, SoundControlObserver {

    /**
     * FXML Components
     */
    @FXML
    private ComboBox cmbMic;

    @FXML
    private Button btnRecordStop;

    @FXML
    private Button btnUpload;

    @FXML
    private Button btnMicGain;

    @FXML
    private LineChart<Number, Double> voiceChart;

    @FXML
    private Slider slVolume;

    @FXML
    private NumberAxis xAxis;

    @FXML
    private Label lblVolMic;

    /*
    * Internal properties
     */
    // Reference to the main application.
    private final AnimationTimer animation;
    private final LineChart.Series<Number, Double> serie;
    private double volume;
    private long startTime;
    private long passedTime;
    private boolean isRecording = false;

    public StsController() {
        animation = new TimelineChartAnimation();
        serie = new XYChart.Series<>();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        SoundControlService.getInstance().addObserver(this);
        slVolume.valueProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            SoundControlService.getInstance().setMicVolume(new_val.floatValue());
        });

    }

    public void initButtons() {
        
        /* Record/Stop Button */
        btnRecordStop.setOnAction(getBtnGravarAction());
        
        /* Upload Button */
        btnUpload.setOnAction(getBtnPararAction());
        btnUpload.setDisable(true);
        btnMicGain.setDisable(true);
        
    }


    public void initCmbMicrophone() {
        cmbMic.getItems().clear();
        cmbMic.getItems().addAll(SoundControlService.getInstance().listAllMics());
        cmbMic.valueProperty().addListener((ObservableValue observable, Object oldValue, Object newValue) -> {
            MicrophoneModel mic = (MicrophoneModel) cmbMic.getValue();
            mic.setRecordFormat(AudioFormatEnum.WAVE);
            SoundControlService.getInstance().setSelectedMic(mic);
            initSlider();
        });
    }

    private void initSlider() {
        float volumeMic = SoundControlService.getInstance().getMicVolume();
        slVolume.setValue(volumeMic * 100);
    }
    
    public void initTimelineChart() {
        //Configura o gráfico
        ObservableList<XYChart.Series<Number, Double>> lineChartData = FXCollections.observableArrayList();
        serie.getData().add(new XYChart.Data<>(0, 0.0));
        xAxis.setForceZeroInRange(false);
        //Inicia o eixo X com 0
        xAxis.setLowerBound(0);
        //Inicia o final do eixo X com 10 segundos em nanos
        xAxis.setUpperBound(TimeUnit.SECONDS.toNanos(60));
        xAxis.setTickUnit(TimeUnit.SECONDS.toNanos(5));
        xAxis.setMinorTickCount(1);
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                return String.format("%02d:%02d",
                        TimeUnit.NANOSECONDS.toMinutes(object.longValue()),
                        TimeUnit.NANOSECONDS.toSeconds(object.longValue())
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.NANOSECONDS.toMinutes(object.longValue()))
                );
            }

            @Override
            public Number fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });

        lineChartData.add(serie);
        voiceChart.setData(lineChartData);
        voiceChart.createSymbolsProperty();
        voiceChart.setAnimated(false);
        voiceChart.setCache(true);
        voiceChart.setCacheShape(true);
        voiceChart.setCacheHint(CacheHint.SPEED);
    }

    private class TimelineChartAnimation extends AnimationTimer {

        @Override
        public void handle(long now) {
            passedTime = now - startTime;
            serie.getData().add(new XYChart.Data<>(passedTime, getVolume()));

            float volumeMic = SoundControlService.getInstance().getMicVolume();
            lblVolMic.setText((volumeMic * 100) + "%");

            //Se o tempo passado for maior que o limite superior do eixo X
            if (passedTime > xAxis.getUpperBound()) {

                //Remove o dado antigo
                serie.getData().remove(0);
                //O limite inferior do eixo X iguala ao superior
                xAxis.setLowerBound(xAxis.getLowerBound() + TimeUnit.SECONDS.toNanos(1));
                //O limite superior do eixo X é atualizado para mais 10 segundos.
                xAxis.setUpperBound(xAxis.getUpperBound() + TimeUnit.SECONDS.toNanos(1));
            }

        }

    }

    private EventHandler<ActionEvent> getBtnPararAction() {
        return (ActionEvent e) -> {
            stopRecord();
        };
    }

    private EventHandler<ActionEvent> getBtnGravarAction() {
        return (ActionEvent e) -> {
            isRecording = btnRecordStop.getText().equalsIgnoreCase("Record");
            if (isRecording) {
                startRecord();
            } else {
                stopRecord();
            }
        };
    }

    private void startRecord() {
        btnRecordStop.setText("Stop");
        startTime = System.nanoTime();
        btnUpload.setDisable(true);
        btnMicGain.setDisable(true);
        animation.start();
        Task<Void> captureAudioTask = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                SoundControlService.getInstance().captureAudio();
                return null;
            }

        };
        Thread t = new Thread(captureAudioTask);
        t.setDaemon(true);
        t.start();
    }

    private void stopRecord() {
        btnRecordStop.setText("Record");
        btnUpload.setDisable(false);
        btnMicGain.setDisable(false);
        animation.stop();
        SoundControlService.getInstance().stopCapture();
    }

    @Override
    public void voiceSampleAsDouble(double volume) {
        this.volume = volume;
    }

    private double getVolume() {
        return this.volume;
    }

    public void play() {
        animation.start();
    }

    public void stop() {
        animation.stop();
    }

}
