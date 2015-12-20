package com.micrecorderfx.view.controller;

import com.micrecorderfx.media.AudioFormatEnum;
import com.micrecorderfx.media.MicControlObserver;
import com.micrecorderfx.media.MicControlService;
import com.micrecorderfx.media.Microphone;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.util.Duration;

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
    
    private final Timeline animation;
    private LineChart.Series<Number, Double> serie = new XYChart.Series<>();
    
    @FXML
    private NumberAxis xAxis;
    
    private double volume;
    int count = 1;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        MicControlService.getInstance().addObserver(this);
        //slVolume.setDisable(true);
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
    
    public MicRecorderController() {
        animation = new Timeline();
        
        animation.getKeyFrames()
                .add(new KeyFrame(Duration.millis(1000 / 60), (ActionEvent actionEvent) -> {
                    serie.getData().add(new XYChart.Data<>(count++, getVolume()));
                    
                    if (count > 100) {
                        serie.getData().remove(0);
                        xAxis.setLowerBound(xAxis.getLowerBound() + 1);
                        xAxis.setUpperBound(xAxis.getUpperBound() + 1);
                    }
                    
                }));
        animation.setCycleCount(Animation.INDEFINITE);
    }
    
    public void initTimelineChart() {
        ObservableList<XYChart.Series<Number, Double>> lineChartData = FXCollections.observableArrayList();
        serie.getData().add(new XYChart.Data<>(0, 0.0));
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(60);
        xAxis.setTickUnit(10.0);
        
        lineChartData.add(serie);
        chartVolume.setData(lineChartData);
        chartVolume.createSymbolsProperty();
    }
    
    private EventHandler<ActionEvent> getBtnPararAction() {
        return (ActionEvent e) -> {
            btnGravar.setDisable(false);
            //slVolume.setDisable(true);
            btnParar.setDisable(true);
            animation.stop();
            MicControlService.getInstance().stopCapture();
        };
    }
    
    private EventHandler<ActionEvent> getBtnGravarAction() {
        return (ActionEvent e) -> {
            //slVolume.setDisable(false);
            btnGravar.setDisable(true);
            btnParar.setDisable(false);
            txtDebug.appendText("Vai obter o valor do combo");
            animation.play();
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
        animation.play();
    }
    
    public void stop() {
        animation.pause();
    }
}
