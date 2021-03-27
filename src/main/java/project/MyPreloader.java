package project;

import java.io.File;
import java.net.URL;

import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MyPreloader extends Preloader {

    private Stage preloaderStage;
    private Scene preloaderScene;

    @Override
    public void init() throws Exception {
        URL url = new File("src/main/resources/splashScreen.fxml").toURI().toURL();
        Parent preloaderRootPane = FXMLLoader.load(url);
        preloaderScene = new Scene(preloaderRootPane);
    }

    @Override
    public void start(Stage primaryStage) {
        this.preloaderStage = primaryStage;
        preloaderStage.setScene(preloaderScene);
        preloaderStage.initStyle(StageStyle.UNDECORATED);
        preloaderStage.getIcons().add(new Image("file:src/main/resources/wallpaper/icon.png"));
        preloaderStage.show();
    }

    @Override
    public void handleApplicationNotification(Preloader.PreloaderNotification info) {
        if (info instanceof ProgressNotification) {
            project.FXMLDocumentController.statProgressBar.setProgress(((ProgressNotification) info).getProgress());
        }
    }

    @Override
    public void handleStateChangeNotification(Preloader.StateChangeNotification info) {
        StateChangeNotification.Type type = info.getType();
        if (type == StateChangeNotification.Type.BEFORE_START) {
            preloaderStage.close();
        }
    }
}
