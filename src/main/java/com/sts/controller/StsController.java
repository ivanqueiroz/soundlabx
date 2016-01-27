package com.sts.controller;

import com.sts.media.SoundControlObserver;
import com.sts.model.MicrophoneModel;
import com.sts.model.enums.AudioFormatEnum;
import com.sts.service.HttpService;
import com.sts.service.SoundControlService;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

public class StsController implements Initializable, SoundControlObserver {

    @FXML
    private ComboBox cmbMic;

    @FXML
    private Button btnRecordStop;

    @FXML
    private Button btnUpload;

    @FXML
    private Button btnMicGain;

    @FXML
    private Slider slVolume;

    @FXML
    private GridPane grdPaneChart;

    @FXML
    private Label lblVolMic;

    private final AnimationTimer animation;
    private List<Double> exercise;
    private boolean isRecording = false;
    private final ObservableList<XYChart.Series<Number, Double>> lineChartData;
    private long passedTime;
    private double sample;
    private final LineChart.Series<Number, Double> serie;
    private long startTime;
    private final LineChart<Number, Double> voiceChart;
    private final NumberAxis xAxis;
    private final NumberAxis yAxis;

    public StsController() {
        animation = new TimelineChartAnimation();
        lineChartData = FXCollections.observableArrayList();
        serie = new XYChart.Series<>();
        yAxis = new NumberAxis();
        xAxis = new NumberAxis();
        voiceChart = new LineChart(xAxis, yAxis);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        SoundControlService.getInstance().addObserver(this);
        slVolume.valueProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            SoundControlService.getInstance().setMicVolume(new_val.floatValue());
        });

    }

    public void initButtons() {
        btnRecordStop.setOnAction(createBtnRecordAction());
        btnUpload.setOnAction(createBtnUploadAction());
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
        resetVoiceChartData();
        configureXAxis();
        configureYAxis();
        configureAndAddVoiceChart();

    }

    private void configureAndAddVoiceChart() {
        lineChartData.add(serie);
        voiceChart.setData(lineChartData);
        voiceChart.createSymbolsProperty();
        voiceChart.setAnimated(false);
        voiceChart.setCache(true);
        voiceChart.setCacheShape(true);
        voiceChart.setCacheHint(CacheHint.SPEED);
        voiceChart.setCreateSymbols(false);
        voiceChart.setLegendVisible(false);
        grdPaneChart.add(voiceChart, 1, 1);
    }

    private void configureYAxis() {
        yAxis.setAutoRanging(false);
        yAxis.setCache(true);
        yAxis.setCacheHint(CacheHint.SPEED);
        yAxis.setLabel("Label");
        yAxis.setLowerBound(-40);
        yAxis.setTickUnit(10);
        yAxis.setUpperBound(0);
    }

    private void configureXAxis() {
        xAxis.setAutoRanging(false);
        xAxis.setCache(true);
        xAxis.setCacheHint(CacheHint.SPEED);
        xAxis.setLabel("Time");
        xAxis.setForceZeroInRange(false);
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
    }

    private class TimelineChartAnimation extends AnimationTimer {

        double sampleAux = 0;

        @Override
        public void handle(long now) {
            passedTime = now - startTime;
            final double sample = getSample();
            if (sampleAux != sample) {
                sampleAux = sample;
                System.out.println(sample);
                exercise.add(sample);
                XYChart.Data<Number, Double> data = new XYChart.Data<>(passedTime, sample);
                serie.getData().add(data);
                float volumeMic = SoundControlService.getInstance().getMicVolume();
                lblVolMic.setText((volumeMic * 100) + "%");
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

    }

    private EventHandler<ActionEvent> createBtnUploadAction() {
        return (ActionEvent e) -> {
            double[] exerciseDoubleArray = new double[exercise.size()];
            for (int i = 0; i < exerciseDoubleArray.length; i++) {
                exerciseDoubleArray[i] = exercise.get(i);
            }
            Thread thread = new Thread(() -> {
                HttpService.getInstance().sendExercise(exerciseDoubleArray, passedTime);
            });

            thread.start();
        };
    }

    private EventHandler<ActionEvent> createBtnRecordAction() {
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
        resetVoiceChartData();
        configureButtonsToStartRecord();
        initAndStartVoiceChartAnimation();
    }

    private void resetVoiceChartData() {
        exercise = new ArrayList<>();
        serie.getData().clear();
        serie.getData().add(new XYChart.Data<>(0, 0.0));
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(TimeUnit.SECONDS.toNanos(60));
    }

    private void configureButtonsToStartRecord() {
        btnRecordStop.setText("Stop");
        btnUpload.setDisable(true);
        btnMicGain.setDisable(true);
    }

    private void initAndStartVoiceChartAnimation() {
        startTime = System.nanoTime();
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
        configureButtonsToStopRecord();
        initAndStopVoiceChartAnimation();
    }

    private void configureButtonsToStopRecord() {
        btnRecordStop.setText("Record");
        btnUpload.setDisable(false);
        btnMicGain.setDisable(false);
    }

    private void initAndStopVoiceChartAnimation() {
        animation.stop();
        SoundControlService.getInstance().stopCapture();
    }

    @Override
    public void voiceSampleAsDouble(double sample) {
        this.sample = sample;
    }

    private double getSample() {
        return this.sample;
    }
}
