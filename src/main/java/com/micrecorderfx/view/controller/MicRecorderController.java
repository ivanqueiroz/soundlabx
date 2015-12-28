package com.micrecorderfx.view.controller;

import com.micrecorderfx.media.AudioFormatEnum;
import com.micrecorderfx.media.MicControlObserver;
import com.micrecorderfx.media.MicControlService;
import com.micrecorderfx.media.Microphone;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
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
import javafx.scene.control.TextArea;
import javafx.util.StringConverter;

/**
 * FXML Controller class
 *
 * @author Ivan Queiroz <ivanqueiroz@gmail.com>
 */
public class MicRecorderController implements Initializable, MicControlObserver {

    @FXML
    public ComboBox cmbMic;

    @FXML
    public Button btnGravar;

    @FXML
    public Button btnParar;

    @FXML
    public TextArea txtDebug;

    @FXML
    public LineChart<Number, Double> chartVolume;

    @FXML
    public Slider slVolume;

    private final AnimationTimer animation;
    private final LineChart.Series<Number, Double> serie = new XYChart.Series<>();

    @FXML
    private NumberAxis xAxis;

    @FXML
    public Label lblVolMic;

    private double volume;
    int count = 0;
    private long startTime;
    private long passedTime;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        MicControlService.getInstance().addObserver(this);

        slVolume.valueProperty().addListener((ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            txtDebug.appendText("" + new_val.floatValue());
            MicControlService.getInstance().setMicVolume(new_val.floatValue());
        });

    }

    public void initButtons() {
        btnGravar.setOnAction(getBtnGravarAction());
        btnParar.setOnAction(getBtnPararAction());
    }

    public void initSlider() {
        cmbMic.setOnAction((event) -> {
            //slVolume.setValue(MicControlService.getInstance().gettMicVolume());
        });
    }

    public void fillcmbMic() {
        cmbMic.getItems().clear();
        cmbMic.getItems().addAll(MicControlService.getInstance().listAllMics());
        cmbMic.valueProperty().addListener((ObservableValue observable, Object oldValue, Object newValue) -> {
            Microphone mic = (Microphone) cmbMic.getValue();
            mic.setFormat(AudioFormatEnum.WAVE);
            MicControlService.getInstance().setSelectedMic(mic);
            float volumeMic = MicControlService.getInstance().gettMicVolume();
            txtDebug.appendText("Vai obter o valor do slider: " + volumeMic);
            slVolume.setValue(volumeMic * 100);
        });
    }

    private class ChartAnimation extends AnimationTimer {

        @Override
        public void handle(long now) {
            passedTime = now - startTime;
            serie.getData().add(new XYChart.Data<>(passedTime, getVolume()));
            
            float volumeMic = MicControlService.getInstance().gettMicVolume();
                    lblVolMic.setText("Volume atual: " + volumeMic);

            //Se o tempo passado for maior que o limite superior do eixo X
            if (passedTime > xAxis.getUpperBound()) {
                
                //Remove o dado antigo
                serie.getData().remove(0);
                //O limite inferior do eixo X iguala ao superior
                xAxis.setLowerBound(xAxis.getUpperBound());
                //O limite superior do eixo X é atualizado para mais 10 segundos.
                xAxis.setUpperBound(xAxis.getUpperBound() + TimeUnit.SECONDS.toNanos(10));
            }

        }

    }

    public MicRecorderController() {
        animation = new ChartAnimation();
    }

    public void initTimelineChart() {
        //Configura o gráfico
        ObservableList<XYChart.Series<Number, Double>> lineChartData = FXCollections.observableArrayList();
        serie.getData().add(new XYChart.Data<>(0, 0.0));
        xAxis.setForceZeroInRange(false);
        //Inicia o eixo X com 0
        xAxis.setLowerBound(0);
        //Inicia o final do eixo X com 10 segundos em nanos
        xAxis.setUpperBound(TimeUnit.SECONDS.toNanos(10));
        xAxis.setTickUnit(TimeUnit.SECONDS.toNanos(1));
        xAxis.setMinorTickCount(1);
        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                return String.format("%02d:%02d",
                        TimeUnit.NANOSECONDS.toMinutes(object.longValue()),
                        TimeUnit.NANOSECONDS.toSeconds(object.longValue())
                        - TimeUnit.NANOSECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(object.longValue()))
                );
            }

            @Override
            public Number fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });

        lineChartData.add(serie);
        chartVolume.setData(lineChartData);
        chartVolume.createSymbolsProperty();
        chartVolume.setAnimated(false);
        chartVolume.setCache(true);
        chartVolume.setCacheShape(true);
        chartVolume.setCacheHint(CacheHint.SPEED);
    }

    private EventHandler<ActionEvent> getBtnPararAction() {
        return (ActionEvent e) -> {
            btnGravar.setDisable(false);
            btnParar.setDisable(true);
            animation.stop();
            MicControlService.getInstance().stopCapture();
            
        };
    }

    private EventHandler<ActionEvent> getBtnGravarAction() {
        return (ActionEvent e) -> {
            btnGravar.setDisable(true);
            btnParar.setDisable(false);
            txtDebug.appendText("Vai obter o valor do combo");
            startTime = System.nanoTime();
            animation.start();
            Task<Void> captureAudioTask = new Task<Void>() {

                @Override
                protected Void call() throws Exception {
                    MicControlService.getInstance().captureAudio();
                    return null;
                }

            };
            Thread t = new Thread(captureAudioTask);
            t.setDaemon(true);
            t.start();
        };
    }

    @Override
    public void update(double volume) {
        this.volume = volume;
        Platform.runLater(() -> txtDebug.appendText("\nVolume: " + volume));
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
