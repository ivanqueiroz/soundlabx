package com.micrecorderfx;

import com.micrecorderfx.view.controller.MicRecorderController;
import java.io.IOException;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage palco) throws IOException {
        URL arquivoFXML = getClass().getResource("MicRecorder.fxml");
        FXMLLoader loader = new FXMLLoader(arquivoFXML);
        Parent fxmlParent = (Parent) loader.load();
        MicRecorderController micRecorderController = loader.getController();

        palco.setScene(new Scene(fxmlParent));
        palco.setTitle("MicRecorder");
        palco.show();

        if (micRecorderController != null) {
            micRecorderController.fillcmbMic();
            micRecorderController.initButtons();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
