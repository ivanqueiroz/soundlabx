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
import javafx.scene.Node;
import javafx.scene.layout.GridPane;

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
    private Slider slVolume;
    
    @FXML
    private GridPane grdPaneChart;
    
    private final NumberAxis xAxis;
    private final NumberAxis yAxis;

    @FXML
    private Label lblVolMic;

    /*
    * Internal properties
     */
    private final AnimationTimer animation;
    private final ObservableList<XYChart.Series<Number, Double>> lineChartData;
    private final LineChart.Series<Number, Double> serie;
    private double volume;
    private long startTime;
    private long passedTime;
    private boolean isRecording = false;
    private final LineChart<Number, Double> voiceChart;

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
        serie.getData().add(new XYChart.Data<>(0, 0.0));
        initLineChart();

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

        yAxis.setAutoRanging(false);
        yAxis.setCache(true);
        yAxis.setCacheHint(CacheHint.SPEED);
        yAxis.setLabel("Label");
        yAxis.setLowerBound(0);
        yAxis.setTickUnit(5);
        yAxis.setUpperBound(100);
        
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

    private class TimelineChartAnimation extends AnimationTimer {

        @Override
        public void handle(long now) {
            passedTime = now - startTime;
            XYChart.Data<Number, Double> data = new XYChart.Data<>(passedTime, getVolume());
            data.nodeProperty().addListener((ObservableValue<? extends Node> observable, Node oldValue, Node newValue) -> {
                if (newValue != null) {
                    setNodeStyle(data);
                }
            });

            serie.getData().add(data);

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

    private void setNodeStyle(XYChart.Data<Number, Double> data) {
        Node node = data.getNode();
        if (data.getYValue().intValue() > 8) {
            node.setStyle(".default-color0.chart-series-line { -fx-stroke: #e9967a; }");
        } else if (data.getYValue().intValue() > 5) {
            node.setStyle(".default-color1.chart-series-line { -fx-stroke: #f0e68c; }");
        } else {
            node.setStyle(".default-color2.chart-series-line { -fx-stroke: #dda0dd; }");
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
        initLineChart();
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

    private void initLineChart() {
        serie.getData().clear();
        serie.getData().add(new XYChart.Data<>(0, 0.0));
        //Inicia o eixo X com 0
        xAxis.setLowerBound(0);
        //Inicia o final do eixo X com 10 segundos em nanos
        xAxis.setUpperBound(TimeUnit.SECONDS.toNanos(60));
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
