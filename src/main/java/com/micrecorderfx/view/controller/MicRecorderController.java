package com.micrecorderfx.view.controller;

import com.micrecorderfx.media.AudioFormatEnum;
import com.micrecorderfx.media.MicControlService;
import com.micrecorderfx.media.Microphone;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;

/**
 * FXML Controller class
 *
 * @author Ivan Queiroz <ivanqueiroz@gmail.com>
 */
public class MicRecorderController implements Initializable {

    @FXML
    public ComboBox cmbMic;

    @FXML
    public Button btnGravar;

    @FXML
    public Button btnParar;

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

    private EventHandler<ActionEvent> getBtnPararAction() {
        return (ActionEvent e) -> {
            btnGravar.setDisable(false);
            btnParar.setDisable(true);
            MicControlService.getInstance().stopCapture();
        };
    }

    private EventHandler<ActionEvent> getBtnGravarAction() {
        return (ActionEvent e) -> {
            btnGravar.setDisable(true);
            btnParar.setDisable(false);
            System.out.println("Vai obter o valor do combo");
            Microphone mic = (Microphone) cmbMic.getValue();
            Task<Void> captureAudioTask = new Task<Void>() {

                @Override
                protected Void call() throws Exception {
                    System.out.println("Executando call()");
                    mic.setFormat(AudioFormatEnum.WAVE);
                    System.out.println("Definindo o mic " + mic.getName());
                    MicControlService.getInstance().setSelectedMic(mic);
                    System.out.println("Chamando captura.");
                    MicControlService.getInstance().captureAudio();

                    return null;
                }

            };
            System.out.println("Chamando getBtnGravarAction()");
            Thread t = new Thread(captureAudioTask);
            t.setDaemon(true);
            t.start();
        };
    }

}
