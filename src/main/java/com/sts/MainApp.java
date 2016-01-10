package com.sts;

import com.sts.controller.StsController;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;

    @Override
    public void start(Stage primaryStage) throws IOException {

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Speech Therapy System");
        initRootLayout();

    }

    public void initRootLayout() {
        try {
            URL arquivoFXML = getClass().getResource("/assets/sts.fxml");
            FXMLLoader loader = new FXMLLoader(arquivoFXML);
            rootLayout = (BorderPane) loader.load();
            StsController stsController = loader.getController();
            primaryStage.setScene(new Scene(rootLayout));

            if (stsController != null) {
                stsController.initCmbMicrophone();
                stsController.initButtons();
                stsController.initTimelineChart();
            }
            primaryStage.show();
        } catch (IOException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
