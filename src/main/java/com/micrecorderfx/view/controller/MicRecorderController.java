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
    
    private final Timeline animation;
    private LineChart.Series<Number, Double> serie = new XYChart.Series<>();
    
    @FXML
    private NumberAxis xAxis;

    private double volume;
    int count = 1;
    double teste = 3;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public void initButtons() {
        btnGravar.setOnAction(getBtnGravarAction());
        btnParar.setOnAction(getBtnPararAction());
    }

    public void fillcmbMic() {
        cmbMic.getItems().clear();
        cmbMic.getItems().addAll(MicControlService.getInstance().listAllMics());
    }

    public MicRecorderController() {
        animation = new Timeline();

        animation.getKeyFrames()
                .add(new KeyFrame(Duration.millis(1000 / 60), (ActionEvent actionEvent) -> {
                    serie.getData().add(new XYChart.Data<>(count++, getVolume()));

                    if (count % 100 == 0) {
                        serie.getData().remove(0);
                        xAxis.setLowerBound(xAxis.getLowerBound() + 100);
                        xAxis.setUpperBound(xAxis.getUpperBound() + 100);
                    }

                }));
        animation.setCycleCount(Animation.INDEFINITE);
    }

    public void initTimelineChart() {
        ObservableList<XYChart.Series<Number, Double>> lineChartData = FXCollections.observableArrayList();
        serie.getData().add(new XYChart.Data<>(0, 0.0));

        lineChartData.add(serie);
        chartVolume.setData(lineChartData);
        chartVolume.createSymbolsProperty();
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
            animation.play();
            Microphone mic = (Microphone) cmbMic.getValue();
            MicControlService.getInstance().addObserver(this);
            Task<Void> captureAudioTask = new Task<Void>() {

                @Override
                protected Void call() throws Exception {
                    mic.setFormat(AudioFormatEnum.WAVE);
                    MicControlService.getInstance().setSelectedMic(mic);
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
