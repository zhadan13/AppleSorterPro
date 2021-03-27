package project;

import com.sun.javafx.application.LauncherImpl;
import javafx.animation.AnimationTimer;
import javafx.application.Preloader;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.VBox;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

import java.awt.image.BufferedImage;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import com.github.sarxos.webcam.Webcam;
import com.jfoenix.controls.*;

public class ApplicationGui extends javafx.application.Application {

    private static class WebCamInfo {

        private String webCamName;
        private int webCamIndex;

        public String getWebCamName() {
            return webCamName;
        }

        public void setWebCamName(String webCamName) {
            this.webCamName = webCamName;
        }

        public int getWebCamIndex() {
            return webCamIndex;
        }

        public void setWebCamIndex(int webCamIndex) {
            this.webCamIndex = webCamIndex;
        }

        @Override
        public String toString() {
            return webCamName;
        }
    }

    private final VBox topPane = new VBox(5);
    private final FlowPane top1Pane = new FlowPane(30, 20);
    private final FlowPane top2Pane = new FlowPane(30, 20);
    private final FlowPane topSecurityPane = new FlowPane(30, 20);
    private final VBox bottomPane = new VBox(1);
    private final BorderPane cameraPane = new BorderPane();
    private final BorderPane rootPane = new BorderPane();
    private final StackPane mainPane = new StackPane();

    private final JFXComboBox<WebCamInfo> cameraOptions = new JFXComboBox<>();
    private static final ImageView imgWebCamCapturedImage = new ImageView();
    private Webcam webCam = null;
    private final ObjectProperty<Image> imageProperty = new SimpleObjectProperty<>();

    private final JFXToggleButton cameraStatus = new JFXToggleButton();
    private final JFXButton cameraClose = new JFXButton();
    private final Label cameraStatusLabel = new Label();
    private boolean stopCamera = true;

    private final JFXToggleButton scanStatus = new JFXToggleButton();
    private final JFXSpinner runIndicator = new JFXSpinner();
    private volatile boolean stopScan = true;

    private final JFXButton cleanStat = new JFXButton("Clear statistics");

    private final VBox timerVbox = new VBox(10);
    private final Label timer1Label = new Label("Scanning time");
    private final Label timer2Label = new Label();
    private AnimationTimer timer = timer();

    private final Label currentApple = new Label();

    private final VBox applesVbox = new VBox(10);
    private final VBox greenVbox = new VBox(10);
    private final VBox redVbox = new VBox(10);
    private final Label labelApples = new Label("All apples");
    private final Label labelGreen = new Label("Only bad apples");
    private final Label labelRed = new Label("Only good apples");
    private final Label labelApplesCount = new Label("0");
    private final Label labelGreenCount = new Label("0");
    private final Label labelRedCount = new Label("0");
    private int counterApples = 0;
    private int counterGreen = 0;
    private int counterRed = 0;
    private final ProgressBar applesProgressBar = new ProgressBar(0);
    private final ProgressBar greenProgressBar = new ProgressBar(0);
    private final ProgressBar redProgressBar = new ProgressBar(0);

    private final JFXToggleButton theme = new JFXToggleButton();
    private final Label themeIcon = new Label();

    private final JFXPasswordField passwordField = new JFXPasswordField();

    private final Separator separator0 = new Separator(Orientation.HORIZONTAL);
    private final BoxBlur blur = new BoxBlur(5, 5, 1);

    private volatile int position = 0;
    private final ArduinoClass arduino = new ArduinoClass();

    @Override
    public void init() throws Exception {
        for (int i = 0; i <= 100; i++) {
            double progress = (double) i / 100;
            LauncherImpl.notifyPreloader(this, new Preloader.ProgressNotification(progress));
            Thread.sleep(50);
        }
    }

    @Override
    public void start(Stage stage) {

        stage.setTitle("Apple Sorter Pro 1.3 X");

        createTopPanel();
        createControls();
        rootPane.setTop(topPane);

        cameraPane.setCenter(imgWebCamCapturedImage);
        rootPane.setCenter(cameraPane);

        createBottomPanel();
        rootPane.setBottom(bottomPane);

        BackgroundImage backgroundImage = new BackgroundImage(new Image("file:src/main/resources/wallpaper/mainBackground.jpg", 1920, 1080, true, true), BackgroundRepeat.SPACE, BackgroundRepeat.SPACE, BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
        mainPane.setBackground(new Background(backgroundImage));
        mainPane.getChildren().add(rootPane);

        Scene scene = new Scene(mainPane);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setHeight(800);
        stage.setWidth(1200);
        stage.getIcons().add(new Image("file:src/main/resources/wallpaper/icon.png"));
        stage.setOnCloseRequest(event -> closeWebCamCamera());
        stage.show();

        Platform.runLater(this::setImageViewSize);
    }

    private void createTopPanel() {
        int webCamCounter = 0;

        ObservableList<WebCamInfo> options = FXCollections.observableArrayList();
        for (Webcam webcam : Webcam.getWebcams()) {
            WebCamInfo webCamInfo = new WebCamInfo();
            webCamInfo.setWebCamIndex(webCamCounter);
            webCamInfo.setWebCamName(webcam.getName());
            options.add(webCamInfo);
            webCamCounter++;
        }

        cameraOptions.setItems(options);
        cameraOptions.setPromptText("Select Your camera");
        cameraOptions.setFocusColor(Color.LIGHTSEAGREEN);
        cameraOptions.setUnFocusColor(Color.LIGHTSEAGREEN);
        Tooltip comboBoxTooltip = new Tooltip("Click and choose Your camera");
        comboBoxTooltip.setGraphic(new ImageView("file:src/main/resources/wallpaper/comboBoxTooltip.png"));
        cameraOptions.setTooltip(comboBoxTooltip);
        cameraOptions.getSelectionModel().selectedItemProperty().addListener((arg0, arg1, arg2) -> {
            if (arg2 != null) {
                initializeWebCam(arg2.getWebCamIndex());
            }
        });

        top1Pane.getChildren().add(cameraOptions);
    }

    private void initializeWebCam(final int webCamIndex) {
        Task<Void> webCamTask = new Task<Void>() {
            @Override
            protected Void call() {
                if (webCam != null) {
                    closeWebCamCamera();
                }
                webCam = Webcam.getWebcams().get(webCamIndex);
                webCam.open();
                startWebCamStream();
                return null;
            }
        };
        Thread webCamThread = new Thread(webCamTask);
        webCamThread.setDaemon(true);
        webCamThread.start();
    }

    private void startWebCamStream() {
        stopCamera = false;
        Task<Void> webCamStreamTask = new Task<Void>() {
            @Override
            protected Void call() {
                final AtomicReference<WritableImage> reference = new AtomicReference<>();
                BufferedImage img;
                while (!stopCamera) {
                    try {
                        if ((img = webCam.getImage()) != null) {
                            reference.set(SwingFXUtils.toFXImage(img, reference.get()));
                            img.flush();
                            Platform.runLater(() -> imageProperty.set(reference.get()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };
        Thread webCamStreamThread = new Thread(webCamStreamTask);
        webCamStreamThread.setDaemon(true);
        webCamStreamThread.start();

        imgWebCamCapturedImage.setVisible(true);
        imgWebCamCapturedImage.imageProperty().bind(imageProperty);

        cameraStatus.setDisable(false);
        cameraStatus.setSelected(true);
        cameraStatusLabel.setVisible(true);
        cameraClose.setDisable(false);
        scanStatus.setDisable(false);
    }

    private void createControls() {
        top1Pane.setEffect(blur);
        top2Pane.setEffect(blur);
        bottomPane.setEffect(blur);
        top1Pane.setDisable(true);
        top2Pane.setDisable(true);
        bottomPane.setDisable(true);

        cameraStatusLabel.setVisible(false);
        cameraStatusLabel.setPrefSize(32, 32);
        cameraStatusLabel.setGraphic(new ImageView("file:src/main/resources/wallpaper/cameralive.png"));
        Tooltip cameraStatusLabelTooltip = new Tooltip("Camera is in action!");
        cameraStatusLabelTooltip.setGraphic(new ImageView("file:src/main/resources/wallpaper/cameraStatusLabelTooltip.png"));
        cameraStatusLabel.setTooltip(cameraStatusLabelTooltip);

        runIndicator.setVisible(false);
        runIndicator.setPrefSize(32, 32);
        Tooltip runIndicatorTooltip = new Tooltip("Scanning is in action!");
        runIndicatorTooltip.setGraphic(new ImageView("file:src/main/resources/wallpaper/runIndicatorTooltip.png"));
        runIndicator.setTooltip(runIndicatorTooltip);

        currentApple.setVisible(false);
        currentApple.setPrefSize(32, 32);
        Tooltip currentAppleTooltip = new Tooltip("Current apple in camera!");
        currentAppleTooltip.setGraphic(new ImageView("file:src/main/resources/wallpaper/currentAppleTooltip.png"));
        currentApple.setTooltip(currentAppleTooltip);

        cameraStatus.setDisable(true);
        cameraStatus.setText("Turn OFF camera");
        Tooltip cameraStatusTooltip = new Tooltip("Switch camera status");
        cameraStatusTooltip.setGraphic(new ImageView("file:src/main/resources/wallpaper/cameraStatusTooltip.png"));
        cameraStatus.setTooltip(cameraStatusTooltip);
        cameraStatus.setOnAction(event -> {
            if (cameraStatus.isSelected()) {
                cameraStatus.setText("Turn OFF camera");
                cameraStatusLabel.setVisible(true);
                startWebCamCamera();
            } else {
                cameraStatus.setText("Turn ON camera");
                cameraStatusLabel.setVisible(false);
                stopWebCamCamera();
            }
        });

        cameraClose.setDisable(true);
        cameraClose.setGraphic(new ImageView("file:src/main/resources/wallpaper/cameraclose.png"));
        Tooltip cameraCloseTooltip = new Tooltip("Shutdown the camera");
        cameraCloseTooltip.setGraphic(new ImageView("file:src/main/resources/wallpaper/cameraCloseTooltip.png"));
        cameraClose.setTooltip(cameraCloseTooltip);
        cameraClose.setOnAction(event -> {
            cameraOptions.getSelectionModel().clearSelection();
            closeWebCamCamera();
        });

        scanStatus.setDisable(true);
        scanStatus.setText("Start scanning");
        Tooltip scanStatusTooltip = new Tooltip("Switch scanning status");
        scanStatusTooltip.setGraphic(new ImageView("file:src/main/resources/wallpaper/scanStatusTooltip.png"));
        scanStatus.setTooltip(scanStatusTooltip);
        scanStatus.setOnAction(event -> {
            if (scanStatus.isSelected()) {
                scanStatus.setText("Stop scanning");
                runIndicator.setVisible(true);
                startScan();
            } else {
                scanStatus.setText("Start scanning");
                runIndicator.setVisible(false);
                stopScan();
            }
        });

        theme.setText("Change theme");
        theme.setUnToggleColor(Color.rgb(255, 111, 97));
        theme.setUnToggleLineColor(Color.rgb(255, 111, 97));
        theme.setToggleColor(Color.rgb(107, 91, 149));
        theme.setToggleLineColor(Color.rgb(107, 91, 149));
        Tooltip themeTooltip = new Tooltip("Switch theme!");
        themeTooltip.setGraphic(new ImageView("file:src/main/resources/wallpaper/themeTooltip.png"));
        theme.setTooltip(themeTooltip);
        themeIcon.setGraphic(new ImageView("file:src/main/resources/wallpaper/bright.png"));
        Tooltip themeIconTooltip = new Tooltip("Bright theme!");
        themeIconTooltip.setGraphic(new ImageView("file:src/main/resources/wallpaper/themeIconTooltipBright.png"));
        themeIcon.setTooltip(themeIconTooltip);
        theme.setOnAction(event -> {
            if (theme.isSelected()) {
                themeIconTooltip.setText("Dark theme!");
                themeIconTooltip.setGraphic(new ImageView("file:src/main/resources/wallpaper/themeIconTooltipDark.png"));
                setDarkTheme();
            } else {
                themeIconTooltip.setText("Bright theme!");
                themeIconTooltip.setGraphic(new ImageView("file:src/main/resources/wallpaper/themeIconTooltipBright.png"));
                setBrightTheme();
            }
        });

        top1Pane.getChildren().addAll(cameraStatus, cameraClose, scanStatus, theme, themeIcon);
        top1Pane.setAlignment(Pos.CENTER);

        passwordField.setLabelFloat(true);
        passwordField.setFocusColor(Color.LIGHTSEAGREEN);
        passwordField.setUnFocusColor(Color.RED);
        passwordField.setPrefWidth(200);
        passwordField.setPromptText("Enter the password to unlock");
        passwordField.setAlignment(Pos.CENTER);
        passwordField.setOnAction(event -> {
            try {
                validation();
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        });

        topSecurityPane.getChildren().add(passwordField);
        topSecurityPane.setAlignment(Pos.CENTER);

        labelApples.setPrefSize(150, 20);
        applesProgressBar.setPrefWidth(120);
        labelApplesCount.setPrefSize(150, 20);
        applesProgressBar.getStylesheets().add("progressBarApples.css");
        labelApples.setAlignment(Pos.CENTER);
        labelApplesCount.setAlignment(Pos.CENTER);
        applesVbox.getChildren().addAll(labelApples, applesProgressBar, labelApplesCount);
        applesVbox.setAlignment(Pos.CENTER);
        Tooltip applesTooltip = new Tooltip("Amount of scanned apples");
        applesTooltip.setGraphic(new ImageView("file:src/main/resources/wallpaper/applesTooltip.png"));
        labelApples.setTooltip(applesTooltip);
        applesProgressBar.setTooltip(applesTooltip);
        labelApplesCount.setTooltip(applesTooltip);

        labelGreen.setPrefSize(150, 20);
        greenProgressBar.setPrefWidth(120);
        labelGreenCount.setPrefSize(150, 20);
        greenProgressBar.getStylesheets().add("progressBarGreen.css");
        labelGreen.setAlignment(Pos.CENTER);
        labelGreenCount.setAlignment(Pos.CENTER);
        greenVbox.getChildren().addAll(labelGreen, greenProgressBar, labelGreenCount);
        greenVbox.setAlignment(Pos.CENTER);
        Tooltip applesGreenTooltip = new Tooltip("Amount of bad apples");
        applesGreenTooltip.setGraphic(new ImageView("file:src/main/resources/wallpaper/applesGreenTooltip.png"));
        labelGreen.setTooltip(applesGreenTooltip);
        greenProgressBar.setTooltip(applesGreenTooltip);
        labelGreenCount.setTooltip(applesGreenTooltip);

        labelRed.setPrefSize(150, 20);
        redProgressBar.setPrefWidth(120);
        labelRedCount.setPrefSize(150, 20);
        redProgressBar.getStylesheets().add("progressBarRed.css");
        labelRed.setAlignment(Pos.CENTER);
        labelRedCount.setAlignment(Pos.CENTER);
        redVbox.getChildren().addAll(labelRed, redProgressBar, labelRedCount);
        redVbox.setAlignment(Pos.CENTER);
        Tooltip applesRedTooltip = new Tooltip("Amount of good apples");
        applesRedTooltip.setGraphic(new ImageView("file:src/main/resources/wallpaper/applesRedTooltip.png"));
        labelRed.setTooltip(applesRedTooltip);
        redProgressBar.setTooltip(applesRedTooltip);
        labelRedCount.setTooltip(applesRedTooltip);

        cleanStat.setPrefSize(130, 30);
        cleanStat.setStyle("-jfx-button-type: RAISED; -fx-background-color: rgb(160, 160, 160)");
        Tooltip clearButtonTooltip = new Tooltip("Clear all statistics: number of apples and time");
        clearButtonTooltip.setGraphic(new ImageView("file:src/main/resources/wallpaper/clearButtonTooltip.png"));
        cleanStat.setTooltip(clearButtonTooltip);
        cleanStat.setOnAction(event -> cleanStatistics());

        Tooltip timerTooltip = new Tooltip("Scanning time");
        timerTooltip.setGraphic(new ImageView("file:src/main/resources/wallpaper/timerTooltip.png"));
        timer1Label.setTooltip(timerTooltip);
        timer2Label.setTooltip(timerTooltip);
        timerVbox.getChildren().addAll(timer1Label, timer2Label);

        top2Pane.getChildren().addAll(currentApple, runIndicator, applesVbox, greenVbox, redVbox, cleanStat, timerVbox, cameraStatusLabel);
        top2Pane.setAlignment(Pos.CENTER);

        Separator separator = new Separator(Orientation.HORIZONTAL);
        topPane.getChildren().addAll(top1Pane, separator, topSecurityPane, separator0, top2Pane);
        topPane.setAlignment(Pos.CENTER);
    }

    private void createBottomPanel() {
        Label bottom1Label = new Label("APPLE SORTER PRO");
        Label bottom2Label = new Label("version 1.3 X");
        Label bottom3Label = new Label("Copyright © 2020 Zhadan Artem. All rights reserved.");
        bottom1Label.setStyle("-fx-font-size: 16px");
        bottom2Label.setStyle("-fx-font-size: 13px");
        bottom3Label.setStyle("-fx-font-size: 13px");
        bottom2Label.setPadding(new Insets(5, 0, 0, 0));
        bottom1Label.setGraphic(new ImageView("file:src/main/resources/wallpaper/apple.png"));
        bottomPane.getChildren().addAll(bottom1Label, bottom2Label, bottom3Label);
        bottomPane.setAlignment(Pos.CENTER);
    }

    private AnimationTimer timer() {
        DoubleProperty time = new SimpleDoubleProperty();
        timer2Label.textProperty().bind(time.asString("%.2f seconds"));
        BooleanProperty running = new SimpleBooleanProperty();
        return new AnimationTimer() {
            private long startTime;

            @Override
            public void start() {
                startTime = System.currentTimeMillis();
                running.set(true);
                super.start();
            }

            @Override
            public void stop() {
                running.set(false);
                super.stop();
            }

            @Override
            public void handle(long timestamp) {
                long now = System.currentTimeMillis();
                time.set((now - startTime) / 1000.0);
            }
        };
    }

    private void startWebCamCamera() {
        startWebCamStream();
    }

    private void stopWebCamCamera() {
        stopCamera = true;
        cameraStatusLabel.setVisible(false);
    }

    private void closeWebCamCamera() {
        if (!stopScan) {
            stopScan();
        }
        stopScan = true;
        stopCamera = true;
        if (webCam != null)
            webCam.close();
        webCam = null;
        scanStatus.setSelected(false);
        scanStatus.setDisable(true);
        cameraStatusLabel.setVisible(false);
        cameraStatus.setSelected(false);
        cameraStatus.setDisable(true);
        cameraClose.setDisable(true);
        imgWebCamCapturedImage.setVisible(false);
        if (cameraOptions.isDisabled() && !top1Pane.isDisabled()) {
            restart();
        }
    }

    private void restart() {
        createTopPanel();
    }

    private void setImageViewSize() {
        imgWebCamCapturedImage.setPreserveRatio(true);
        imgWebCamCapturedImage.setSmooth(true);
        imgWebCamCapturedImage.setCache(true);
        imgWebCamCapturedImage.fitHeightProperty().bind(cameraPane.heightProperty().subtract(20));
        imgWebCamCapturedImage.fitWidthProperty().bind(cameraPane.widthProperty().subtract(20));
    }

    private void startScan() {
        stopScan = false;
        runIndicator.setVisible(true);
        currentApple.setVisible(true);
        timer.start();
        arduino.arduinoOpenConnection();

        ConcurrentLinkedQueue<Integer> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);

        Runnable algoTask = () -> {
            if (stopScan) {
                scheduledExecutorService.shutdown();
                try {
                    if (!scheduledExecutorService.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                        scheduledExecutorService.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    scheduledExecutorService.shutdownNow();
                }
            }
            int R = Application.result();
            concurrentLinkedQueue.add(R);
            if (R == 1) {
                Platform.runLater(this::addRed);
            }
            if (R == -1) {
                Platform.runLater(this::addGreen);
            }
            if (R == 0) {
                Platform.runLater(this::noApple);
            }
        };

        ScheduledExecutorService scheduledExecutorService2 = Executors.newSingleThreadScheduledExecutor();

        Runnable arduinoTask = () -> {
            if (stopScan) {
                scheduledExecutorService2.shutdown();
                try {
                    if (!scheduledExecutorService2.awaitTermination(3000, TimeUnit.MILLISECONDS)) {
                        scheduledExecutorService2.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    scheduledExecutorService2.shutdownNow();
                }
                arduino.arduinoCloseConnection();
            }
            if (concurrentLinkedQueue.peek() != null) {
                sendMessage(concurrentLinkedQueue.poll());
            }
        };

        scheduledExecutorService.scheduleAtFixedRate(algoTask, 1000, 500, TimeUnit.MILLISECONDS);

        scheduledExecutorService2.scheduleAtFixedRate(arduinoTask, 2000, 500, TimeUnit.MILLISECONDS);
    }

    private void stopScan() {
        timer.stop();
        stopScan = true;
        currentApple.setVisible(false);
        runIndicator.setVisible(false);
    }

    private void addGreen() {
        currentApple.setVisible(true);
        currentApple.setGraphic(new ImageView("file:src/main/resources/wallpaper/green.png"));
        counterApples++;
        counterGreen++;
        applesProgressBar.setProgress(applesProgressBar.getProgress() + 0.01);
        labelApplesCount.setText(String.valueOf((int) (applesProgressBar.getProgress() * 100)));
        greenProgressBar.setProgress(greenProgressBar.getProgress() + 0.01);
        labelGreenCount.setText(String.valueOf((int) (greenProgressBar.getProgress() * 100)));
    }

    private void addRed() {
        currentApple.setVisible(true);
        currentApple.setGraphic(new ImageView("file:src/main/resources/wallpaper/red.png"));
        counterApples++;
        counterRed++;
        applesProgressBar.setProgress(applesProgressBar.getProgress() + 0.01);
        labelApplesCount.setText(String.valueOf((int) (applesProgressBar.getProgress() * 100)));
        redProgressBar.setProgress(redProgressBar.getProgress() + 0.01);
        labelRedCount.setText(String.valueOf((int) (redProgressBar.getProgress() * 100)));
    }

    private void noApple() {
        currentApple.setVisible(false);
    }

    private void cleanStatistics() {
        counterApples = 0;
        counterGreen = 0;
        counterRed = 0;
        applesProgressBar.setProgress(0);
        greenProgressBar.setProgress(0);
        redProgressBar.setProgress(0);
        labelApplesCount.setText(String.valueOf(counterApples));
        labelGreenCount.setText(String.valueOf(counterGreen));
        labelRedCount.setText(String.valueOf(counterRed));
        if (!stopScan) {
            timer.start();
        } else {
            timer = timer();
        }
    }

    private void setBrightTheme() {
        themeIcon.setGraphic(new ImageView("file:src/main/resources/wallpaper/bright.png"));
        mainPane.setStyle("-fx-base: rgba(230, 230, 230, 255)");
        cameraOptions.setStyle("-fx-prompt-text-fill: rgba(0, 0, 0, 255)");
    }

    private void setDarkTheme() {
        themeIcon.setGraphic(new ImageView("file:src/main/resources/wallpaper/dark.png"));
        mainPane.setStyle("-fx-base: rgba(40, 40, 40, 255)");
        cameraOptions.setStyle("-fx-prompt-text-fill: rgba(255, 255, 255, 255)");
    }

    private void sendMessage(final int message) {
        if (position != message) {
            if (message == 1) {
                arduino.arduinoMessage('1');
            }
            if (message == -1) {
                arduino.arduinoMessage('0');
            }
            position = message;
        }
    }

    private void validation() throws InvalidKeySpecException, NoSuchAlgorithmException {
        char[] password = passwordField.getText().toCharArray();
        if (Encryption.encrypting(password)) {
            initializeProject();
        } else {
            BoxBlur blur0 = new BoxBlur(10, 10, 2);
            JFXDialogLayout dialogLayout = new JFXDialogLayout();
            JFXButton dialogButton = new JFXButton("OK");
            JFXDialog dialog = new JFXDialog(mainPane, dialogLayout, JFXDialog.DialogTransition.CENTER);
            dialogLayout.setBody(new Label("Wrong password, please re-enter."));
            dialogLayout.setActions(dialogButton);
            dialogButton.setPrefSize(100, 30);
            dialogButton.setStyle("-jfx-button-type: RAISED; -fx-background-color: rgb(160, 160, 160)");
            dialogButton.setOnAction(event -> dialog.close());
            dialog.setOverlayClose(true);
            dialog.show();
            dialog.setOnDialogClosed(event -> {
                top1Pane.setEffect(blur);
                top2Pane.setEffect(blur);
                bottomPane.setEffect(blur);
                topSecurityPane.setEffect(null);
            });
            top1Pane.setEffect(blur0);
            top2Pane.setEffect(blur0);
            bottomPane.setEffect(blur0);
            topSecurityPane.setEffect(blur0);
        }
    }

    private void initializeProject() {
        topPane.getChildren().removeAll(topSecurityPane, separator0);
        top1Pane.setDisable(false);
        top2Pane.setDisable(false);
        bottomPane.setDisable(false);
        top1Pane.setEffect(null);
        top2Pane.setEffect(null);
        bottomPane.setEffect(null);
    }

    protected static BufferedImage grabbedImage() {
        return SwingFXUtils.fromFXImage(imgWebCamCapturedImage.getImage(), null);
    }

    public static void main(String[] args) {
        LauncherImpl.launchApplication(ApplicationGui.class, MyPreloader.class, args);
    }
}
