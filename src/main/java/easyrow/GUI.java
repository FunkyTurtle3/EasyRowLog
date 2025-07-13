package easyrow;

import easyrow.compat.MacIconSetter;
import easyrow.data.Club;
import easyrow.data.Prio;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import org.json.JSONObject;


public class GUI extends Application {

    private static final Logger log = LoggerFactory.getLogger(GUI.class);
    private final EasyRowLog easyRowLog;
    private static final String API_KEY = "2c4efc9c04e72b05668e99873bae5cdb";
    private double latitude;
    private double longitude;
    private ImageView weatherImgView;
    private Label descriptionLabel;
    private double width;
    private double height;
    private static final RadialGradient clearSkyGradient = new RadialGradient(0, 0, 0.5, 1, 1,  true, CycleMethod.NO_CYCLE, getStops(new Stop(0, Color.web("C7FFF4")), new Stop(1, Color.web("0096C7"))));
    private static final RadialGradient sunsetSunriseGradient = new RadialGradient(0, 0, 0.5, 1, 1, true, CycleMethod.NO_CYCLE, getStops(new Stop(0, Color.web("FCBF49")), new Stop(1, Color.web("D62828"))));
    private static final RadialGradient nightSkyGradient = new RadialGradient(0, 0, 0.5, 1, 1, true, CycleMethod.NO_CYCLE, getStops(new Stop(0, Color.web("03045E")), new Stop(1, Color.web("0D1B2A"))));
    private static final RadialGradient fogSkyGradient = new RadialGradient(0, 0, 0.5, 1, 1, true, CycleMethod.NO_CYCLE, getStops(new Stop(0, Color.web("FFFFFF")), new Stop(1, Color.web("7F7F7F"))));



    public static void main(String[] args) {
        launch(args);
    }

    public GUI() {
        easyRowLog = new EasyRowLog();
        latitude = getCoordinatesFromIP()[0];
        longitude = getCoordinatesFromIP()[1];
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Easy Row Log");

        this.width = Screen.getPrimary().getBounds().getWidth();
        this.height = Screen.getPrimary().getBounds().getHeight();
        stage.setWidth(width);
        stage.setHeight(height);
        stage.setAlwaysOnTop(true);
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        weatherImgView = new ImageView(getCurrentWeatherSymbol());
        weatherImgView.setFitHeight(width / 40);
        weatherImgView.setPreserveRatio(true);
        weatherImgView.setEffect(new DropShadow(width / 200, 0, 0, Color.web("#000000", 0.20)));

        descriptionLabel = getInfoLabel("");
        descriptionLabel.setAlignment(Pos.CENTER);
        descriptionLabel.setLayoutX(width / 4);
        descriptionLabel.setLayoutY(height / 3);

        BorderPane mainPane = new BorderPane();

        stage.getIcons().add(Resource.getTexture("/UI/logo_500x500.png"));
        MacIconSetter.setDockIcon();

        // --- Left Panel ---
        StackPane leftPanel = new StackPane();
        leftPanel.setPrefWidth(width / 4);

        Circle addCircle = getInfoCircle(true);
        HBox addIconBox = getIconBox("/UI/add_symbol.png", -1);
        StackPane addPane = new StackPane(addCircle, addIconBox);
        StackPane addAthletePane = new StackPane(getInfoCircle(true), getIconBox("/UI/athlete_symbol.png", -1));
        addAthletePane.setOnMouseClicked(event -> showAthleteEntryWindow(stage));
        StackPane addBoatPane = new StackPane(getInfoCircle(true), getIconBox("/UI/boat_symbol.png", -1));
        StackPane addLocationPane = new StackPane(getInfoCircle(true), getIconBox("/UI/location_marker_symbol.png", -1));

        addCircle.setOnMouseClicked(mouseEvent -> {
            RotateTransition rotateTransition = new RotateTransition(Duration.millis(300), addIconBox);
            rotateTransition.setToAngle(addAthletePane.getTranslateY() > addPane.getTranslateY() ? 0 : 45);
            rotateTransition.play();
        });

        StackPane addDropdown = getDropdown(0.2, addPane, addAthletePane, addBoatPane, addLocationPane);
        addDropdown.setPadding(new Insets(width / 60));

        // --- Center Panel ---
        Pane centerPane = new Pane();
        centerPane.setPrefWidth(width / 2);

        Rectangle clearSky = new Rectangle(width, height);
        clearSky.setFill(clearSkyGradient);

        Rectangle sunsetSunriseSky = new Rectangle(width, height);
        sunsetSunriseSky.setFill(sunsetSunriseGradient);
        sunsetSunriseSky.setOpacity(0);

        if(getCurrentSunProgress() > 0.8 ) {
            sunsetSunriseSky.setOpacity((getCurrentSunProgress() - 0.8) * 5);
        }

        Rectangle nightSky = new Rectangle(width, height);
        nightSky.setFill(nightSkyGradient);
        nightSky.setOpacity(0);
        if (getCurrentSunProgress() == -1) {
            nightSky.setOpacity(1);
        }

        Rectangle fogSky = new Rectangle(width, height);
        fogSky.setFill(fogSkyGradient);
        fogSky.setOpacity(0);
        if (getWeatherJson().getJSONObject("clouds").getInt("all") > 90) {
            fogSky.setOpacity(1);
        }

        Circle sunDisplay = new Circle(width / 20);
        sunDisplay.setFill(new RadialGradient(0, 0, 0.5, 0.5, 1,  true,
                CycleMethod.NO_CYCLE, getStops(new Stop(0, Color.web("#FFF0D7")), new Stop(1, Color.web("#FFDB72")))));
        sunDisplay.setCenterX(width / 2);
        sunDisplay.setTranslateY(getCurrentSunPos() + sunDisplay.getRadius());
        sunDisplay.setEffect(new DropShadow(75, Color.web("#F7B801", 0.75)));
        TranslateTransition sunTranslate = new TranslateTransition(Duration.seconds(60), sunDisplay);


        Label timeLabel = getInfoLabel(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

        StackPane timePane = new StackPane(getInfoRectangle(), timeLabel);
        timePane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        Label tempLabel = getInfoLabel(getTemperature("Berlin"));

        HBox weatherSymbol = new HBox(weatherImgView);
        weatherSymbol.setPadding(new Insets(0, 0, 0, 0));
        weatherSymbol.setAlignment(Pos.CENTER);

        Circle offCircle = getInfoCircle(true);
        offCircle.setOnMouseClicked(MouseEvent -> System.exit(0));

        StackPane tempPane = new StackPane(getInfoRectangle(), tempLabel);
        tempPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        tempPane.setAlignment(Pos.CENTER);

        ImageView windDirectionImgView = getSimpleIconImgView("/UI/compass_symbol.png");
        windDirectionImgView.setRotate(Objects.requireNonNull(getWeatherJson("Berlin")).getJSONObject("wind").getInt("deg") + 180);


        Label windSpeedLabel = new Label(((int) getWeatherJson().getJSONObject("wind").getDouble("speed")) + "");
        windSpeedLabel.setTooltip(new Tooltip("Windgeschwindigkeit: " + windSpeedLabel.getText() + "km/h"));
        windSpeedLabel.setFont(Resource.getFont("/anta_regular.otf", (int) (height / 30)));
        windSpeedLabel.setTextFill(Color.WHITE);
        windSpeedLabel.setAlignment(Pos.CENTER);
        windSpeedLabel.setEffect(new DropShadow(width / 200, 0, 0, Color.web("#000000", 0.20)));

        StackPane windDirectionPane = new StackPane(getInfoCircle(false), windDirectionImgView);
        String firstWindDirection = "W";
        if (Objects.requireNonNull(getWeatherJson()).getJSONObject("wind").getInt("deg") < 270) {
            firstWindDirection = "S";
        }
        if (Objects.requireNonNull(getWeatherJson()).getJSONObject("wind").getInt("deg") < 180) {
            firstWindDirection = "O";
        }
        if (Objects.requireNonNull(getWeatherJson()).getJSONObject("wind").getInt("deg") < 90) {
            firstWindDirection = "N";
        }
        Tooltip.install(windDirectionPane, new Tooltip(firstWindDirection));

        HBox symbolPane = new HBox(new StackPane(getInfoCircle(false), weatherSymbol), new StackPane(getInfoCircle(false), windSpeedLabel), windDirectionPane, new StackPane(offCircle, getIconBox("/UI/shutdown_symbol.png", -1)));
        symbolPane.setAlignment(Pos.CENTER);
        symbolPane.setSpacing(18 * width / height);


        VBox infobox = new VBox(height / 30, timePane, tempPane, symbolPane);
        infobox.setLayoutX((width / 2 - infobox.prefWidth(-1)) / 2);
        infobox.setLayoutY(height / 2);
        infobox.setAlignment(Pos.CENTER);

        Image waveImg = Resource.getTexture("/waves/normal.png");
        ImageView wavesImageView = new ImageView(waveImg);

        wavesImageView.setPreserveRatio(true);
        wavesImageView.setFitWidth(width / 2);
        wavesImageView.setEffect(new DropShadow(width / 200, 0, -2, Color.web("#FFFFFF", 0.25)));
        wavesImageView.setX(0);
        wavesImageView.setOpacity(0);
        wavesImageView.setY(height - waveImg.getHeight() / 2);
        wavesImageView.setEffect(new DropShadow(width / 200, Color.WHITE));

        // --- Right Panel ---
        StackPane rightPanel = new StackPane();
        rightPanel.setPrefWidth(width / 4);


        // Alles hinzufügen
        centerPane.getChildren().addAll(infobox, wavesImageView, addDropdown, descriptionLabel);

        Rectangle leftSidebar = getInfoRectangle((width / 4) - (width / 30), height - width / 30);
        leftPanel.setPadding(new Insets(width / 60));

        leftPanel.getChildren().addAll(leftSidebar);

        Rectangle rightSidebar = getInfoRectangle((width / 4) - (width / 30), height - width / 30);
        rightPanel.setPadding(new Insets(width / 60));

        rightPanel.getChildren().add(rightSidebar);


        mainPane.getChildren().addAll(clearSky, sunsetSunriseSky, nightSky, sunDisplay, fogSky);

        // Add Panels to main layout
        mainPane.setLeft(leftPanel);
        mainPane.setRight(rightPanel);
        mainPane.setCenter(centerPane);

        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> timeLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")))));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        Timeline weatherCycle = new Timeline(new KeyFrame(Duration.seconds(60), e -> {
            tempLabel.setText(getTemperature("Berlin"));
            weatherImgView.setImage(getCurrentWeatherSymbol());

                sunTranslate.setToY(getCurrentSunPos() + sunDisplay.getRadius());
                sunTranslate.play();



                sunTranslate.setToY(getCurrentSunPos() + sunDisplay.getRadius());
                sunTranslate.play();

            if(getCurrentSunProgress() > 0.8) {
                FadeTransition sunsetSunriseAnimation = new FadeTransition(Duration.seconds(60), sunsetSunriseSky);
                sunsetSunriseAnimation.setToValue((getCurrentSunProgress() - 0.8) * 5);
                sunsetSunriseAnimation.play();
            } else if(getCurrentSunProgress() < 0.2 && getCurrentSunProgress() > 0) {
                FadeTransition sunsetSunriseAnimation = new FadeTransition(Duration.seconds(60), sunsetSunriseSky);
                sunsetSunriseAnimation.setToValue(getCurrentSunProgress() * 5);
                sunsetSunriseAnimation.play();
            }

            if (getWeatherJson().getJSONObject("clouds").getInt("all") > 90) {
                FadeTransition fogSkyFadeTransition = new FadeTransition(Duration.seconds(60), fogSky);
                fogSkyFadeTransition.setToValue(1);
                fogSkyFadeTransition.play();
            } else {
                FadeTransition fogSkyFadeTransition = new FadeTransition(Duration.seconds(60), fogSky);
                fogSkyFadeTransition.setToValue(0);
                fogSkyFadeTransition.play();
            }

            if (getCurrentSunProgress() == -1 && nightSky.getOpacity() == 0) {
                FadeTransition nightAnimation = new FadeTransition(Duration.minutes(4), nightSky);
                nightAnimation.setToValue(1);
                nightAnimation.play();
            }

            RotateTransition compassRotate = new RotateTransition(Duration.seconds(2) , windDirectionImgView);
            compassRotate.setToAngle(Objects.requireNonNull(getWeatherJson()).getJSONObject("wind").getInt("deg") + 180);
            compassRotate.play();
            String windDirection = "W";
            if (Objects.requireNonNull(getWeatherJson()).getJSONObject("wind").getInt("deg") < 270) {
                windDirection = "S";
            }
            if (Objects.requireNonNull(getWeatherJson()).getJSONObject("wind").getInt("deg") < 180) {
                windDirection = "O";
            }
            if (Objects.requireNonNull(getWeatherJson()).getJSONObject("wind").getInt("deg") < 90) {
                windDirection = "N";
            }
            Tooltip.install(windDirectionPane, new Tooltip(windDirection));
        }));
        weatherCycle.setCycleCount(Animation.INDEFINITE);
        weatherCycle.play();

        Scene scene = new Scene(mainPane);
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();
    }

    private void showAthleteEntryWindow(Stage parentStage) {
        TextField firstNameTextField = getSimpleTextField();
        firstNameTextField.setPromptText("First Name");
        firstNameTextField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[a-zA-Z]*")) {
                return change;
            }
            return null;
        }));

        TextField lastNameTextField = getSimpleTextField();
        lastNameTextField.setPromptText("Last Name");
        lastNameTextField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[a-zA-Z]*")) {
                return change;
            }
            return null;
        }));
        TextField clubTextField = getSimpleTextField();
        clubTextField.setPromptText("Club");

        TextField prioTextField = getSimpleTextField();
        prioTextField.setPromptText("Priority");

        DatePicker dateOfBirthPicker = new DatePicker();

        dateOfBirthPicker.getEditor().setPrefHeight(height / 20);
        dateOfBirthPicker.getEditor().setFont(Resource.getFont("/anta_regular.otf", (int) (height / 40)));
        dateOfBirthPicker.setStyle(
                "-fx-background-radius: " + width / 100 + ";" +
                        "-fx-border-radius: " + width / 100 + ";" +
                        "-fx-border-color: rgba(255, 255, 255, 0.5);" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-background-color: rgba(255, 255, 255, 0.2);" +
                        "-fx-border-width: 2;"
        );
        dateOfBirthPicker.getEditor().setStyle(
                "-fx-background-radius: " + width / 100 + ";" +
                        "-fx-border-radius: " + width / 100 + ";" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-background-color: rgba(255, 255, 255, 0);" +
                        "-fx-border-width: 0;"
        );
        /**
        TextField licenseNumberTextField = getSimpleTextField();
        licenseNumberTextField.setPromptText("License Number (optional)");
        licenseNumberTextField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*") && newText.length() <= 10) {
                return change;
            }
            return null;
        }));
         */

        Button submitButton = new Button();
        submitButton.setGraphic(getSimpleIconImgView("/UI/save_symbol.png"));
        submitButton.setOnAction(e -> {
            try {
                easyRowLog.saveAthlete(new Athlete(firstNameTextField.getText(), lastNameTextField.getText(), Club.valueOf(clubTextField.getText().toUpperCase()), Prio.getPrioByInt(Integer.parseInt(prioTextField.getText())), dateOfBirthPicker.getValue(), 0));
            } catch (Exception exception) {
                submitButton.setStyle("-fx-background-radius: " + width / 100 + ";" +
                        "-fx-border-radius: " + width / 100 + ";" +
                        "-fx-border-color: rgba(255, 0, 0, 0.5);" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-background-color: rgba(255, 0, 0, 0.2);" +
                        "-fx-border-width: 2;");
            }
        });
        submitButton.setPrefWidth(width / 12 - width / 48);
        submitButton.setPrefHeight(height / 40);
        submitButton.setFont(Resource.getFont("/anta_regular.otf", (int) (height / 40)));
        submitButton.setStyle(
                "-fx-background-radius: " + width / 100 + ";" +
                        "-fx-border-radius: " + width / 100 + ";" +
                        "-fx-border-color: rgba(255, 255, 255, 0.5);" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-background-color: rgba(255, 255, 255, 0.2);" +
                        "-fx-border-width: 2;"
        );
        submitButton.setOnMousePressed(mouseEvent -> {
            ScaleTransition animation = new ScaleTransition(Duration.millis(200), submitButton);
            animation.setToX(0.9);
            animation.setToY(0.9);
            animation.play();
        });
        submitButton.setOnMouseReleased(mouseEvent -> {
            ScaleTransition animation = new ScaleTransition(Duration.millis(200), submitButton);
            animation.setToX(1);
            animation.setToY(1);
            animation.play();
        });

        Button cancelButton = new Button();
        ImageView cancelImgView = getSimpleIconImgView("/UI/add_symbol.png");
        cancelImgView.setRotate(45);
        cancelButton.setGraphic(cancelImgView);
        cancelButton.setOnAction(e -> {
            ((Stage) cancelButton.getScene().getWindow()).close();
        });
        cancelButton.setPrefWidth(width / 12 - width / 48);
        cancelButton.setPrefHeight(height / 40);
        cancelButton.setFont(Resource.getFont("/anta_regular.otf", (int) (height / 40)));
        cancelButton.setStyle(
                "-fx-background-radius: " + width / 100 + ";" +
                        "-fx-border-radius: " + width / 100 + ";" +
                        "-fx-border-color: rgba(255, 255, 255, 0.5);" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-background-color: rgba(255, 255, 255, 0.2);" +
                        "-fx-border-width: 2;"
        );
        cancelButton.setOnMousePressed(mouseEvent -> {
            ScaleTransition animation = new ScaleTransition(Duration.millis(200), cancelButton);
            animation.setToX(0.9);
            animation.setToY(0.9);
            animation.play();
        });
        cancelButton.setOnMouseReleased(mouseEvent -> {
            ScaleTransition animation = new ScaleTransition(Duration.millis(200), cancelButton);
            animation.setToX(1);
            animation.setToY(1);
            animation.play();
        });

        VBox layout = new VBox(10,
                firstNameTextField,
                lastNameTextField,
                clubTextField,
                prioTextField,
                dateOfBirthPicker,
                new HBox(width / 24, submitButton, cancelButton)
        );
        layout.setStyle("-fx-padding: 20;");
        layout.setBackground(new Background(new BackgroundFill(clearSkyGradient, CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(layout);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/assets/style/datePickerStyle.css")).toExternalForm());

        Stage formStage = new Stage();
        formStage.setTitle("Neuer Eintrag");
        formStage.setScene(scene);
        formStage.initOwner(parentStage);                // Setzt Hauptfenster als Owner
        formStage.initModality(Modality.WINDOW_MODAL);   // Blockiert nur das Hauptfenster
        formStage.setResizable(false);
        formStage.show();
    }

    public TextField getSimpleTextField() {
        TextField textField = new TextField();
        textField.setPrefWidth(width / 6);
        textField.setPrefHeight(height / 40);
        textField.setFont(Resource.getFont("/anta_regular.otf", (int) (height / 40)));
        textField.setStyle(
                "-fx-background-radius: " + width / 100 + ";" +
                        "-fx-border-radius: " + width / 100 + ";" +
                        "-fx-border-color: rgba(255, 255, 255, 0.5);" +
                        "-fx-text-fill: #ffffff;" +
                        "-fx-background-color: rgba(255, 255, 255, 0.2);" +
                        "-fx-prompt-text-fill: #eeeeee;" +
                        "-fx-border-width: 2;"
        );
        return textField;
    }

    public Image getCurrentWeatherSymbol() {
        if(!getWeather("Berlin").equalsIgnoreCase("clear")) {
            return Resource.getTexture("/UI/" + getWeather("Berlin").toLowerCase() + "_symbol.png");
        } else if (getCurrentSunProgress() == -1) {
            return Resource.getTexture("/UI/moon_symbol.png");
        } else {
            return Resource.getTexture("/UI/sun_symbol.png");
        }
    }

    public Rectangle getInfoRectangle() {
        return getInfoRectangle(width / 5, height / 15);
    }

    public Rectangle getInfoRectangle(double width, double height) {
        Rectangle rectangle = new Rectangle(width, height);
        rectangle.prefWidth(width);
        rectangle.prefHeight(height);
        rectangle.setArcWidth(this.height / 15);
        rectangle.setArcHeight(this.height / 15);
        rectangle.setFill(Color.web("#FFFFFF", 0.2));
        rectangle.setStroke(Color.web("#FFFFFF", 0.5));
        rectangle.setStrokeWidth(2);
        rectangle.setEffect(new DropShadow(this.width / 200, 2, 2, Color.web("#FFFFFF", 1)));
        return rectangle;
    }

    public Rectangle getInfoCircleExpandable(boolean hoverEffect, String content) {
        Rectangle rectangle = getInfoRectangle(height / 15,height / 15);
        Label text = getInfoLabel(content);
        text.setOpacity(0);
        rectangle.setOnMouseEntered(mouseEvent -> {
            FillTransition animation = new FillTransition(Duration.millis(300), rectangle, (Color) rectangle.getFill(), Color.web("#8E8E8E", 0.2));
            animation.play();
        });
        rectangle.setOnMouseExited(mouseEvent -> {
            FillTransition animation = new FillTransition(Duration.millis(300), rectangle, (Color) rectangle.getFill(), Color.web("#FFFFFF", 0.2));
            animation.play();
        });
        rectangle.setOnMousePressed(mouseEvent -> {
            ScaleTransition animation = new ScaleTransition(Duration.millis(200), rectangle);
            animation.setToX(0.9);
            animation.setToY(0.9);
            animation.play();
        });
        rectangle.setOnMouseReleased(mouseEvent -> {
            ScaleTransition animation = new ScaleTransition(Duration.millis(200), rectangle);
            animation.setToX(1);
            animation.setToY(1);
            animation.play();
        });
        return rectangle;
    }

    public Circle getInfoCircle(boolean hoverEffect) {
        Circle circle = new Circle(height / 30);
        circle.setFill(Color.web("#FFFFFF", 0.2));
        circle.setStroke(Color.web("#FFFFFF", 0.5));
        circle.setStrokeWidth(2);
        circle.setEffect(new DropShadow(width / 200, 2, 2, Color.web("#FFFFFF", 1)));

        if (hoverEffect) {
            circle.setOnMouseEntered(mouseEvent -> {
                FillTransition animation = new FillTransition(Duration.millis(300), circle, (Color) circle.getFill(), Color.web("#8E8E8E", 0.2));
                animation.play();
            });
            circle.setOnMouseExited(mouseEvent -> {
                FillTransition animation = new FillTransition(Duration.millis(300), circle, (Color) circle.getFill(), Color.web("#FFFFFF", 0.2));
                animation.play();
            });
            circle.setOnMousePressed(mouseEvent -> {
                ScaleTransition animation = new ScaleTransition(Duration.millis(200), circle);
                animation.setToX(0.9);
                animation.setToY(0.9);
                animation.play();
            });
            circle.setOnMouseReleased(mouseEvent -> {
                ScaleTransition animation = new ScaleTransition(Duration.millis(200), circle);
                animation.setToX(1);
                animation.setToY(1);
                animation.play();
            });
        }
        return circle;
    }

    public Label getInfoLabel(String text) {
        Label label = new Label();
        label.setTextFill(Color.WHITE);
        label.setFont(Resource.getFont("/anta_regular.otf", (int) (height / 20)));
        label.setEffect(new DropShadow(width / 200, 0, 0, Color.web("#000000", 0.20)));
        label.setAlignment(Pos.CENTER);
        label.setText(text);
        return label;
    }

    public ImageView getSimpleIconImgView(String path) {
        ImageView iconImgView = new ImageView(Resource.getTexture(path));
        iconImgView.setFitHeight(this.width / 40);
        iconImgView.setPreserveRatio(true);
        iconImgView.setEffect(new DropShadow(width / 200, 0, 0, Color.web("#000000", 0.20)));
        return iconImgView;
    }

    public HBox getIconBox(ImageView imageView, int face) {
        HBox iconSymbolBox = new HBox(imageView);
        iconSymbolBox.setMouseTransparent(true);

        switch (face) {
            case 0 -> {
                iconSymbolBox.setAlignment(Pos.CENTER_LEFT);
                iconSymbolBox.setPadding(new Insets(0, 0, 0, width / 150)); // Abstand vom linken Rand
            }
            case 1 -> {
                iconSymbolBox.setAlignment(Pos.TOP_CENTER);
                iconSymbolBox.setPadding(new Insets(width / 150, 0, 0, 0));
            }
            case 2 -> {
                iconSymbolBox.setAlignment(Pos.CENTER_RIGHT);
                iconSymbolBox.setPadding(new Insets(0, width / 150, 0, 0));
            }
            case 3 -> {
                iconSymbolBox.setAlignment(Pos.BOTTOM_CENTER);
                iconSymbolBox.setPadding(new Insets(0, 0, width / 150, 0));
            }
            default -> {
                iconSymbolBox.setAlignment(Pos.CENTER);
                iconSymbolBox.setPadding(new Insets(width / 150));
            }
        }
        return iconSymbolBox;
    }

    public StackPane getDropdown(double insets, Node... nodes) {
        StackPane pane = new StackPane();
        ParallelTransition openTransition = new ParallelTransition();
        ParallelTransition closeTransition = new ParallelTransition();

        double transY = 1 + insets;

        pane.setMaxSize(Region.USE_PREF_SIZE, nodes[0].getLayoutBounds().getHeight() * nodes.length * transY);
        pane.setPickOnBounds(false);
        pane.getChildren().addAll(nodes);

        Platform.runLater(() -> {
            double nodeHeight = nodes[0].getLayoutBounds().getHeight();

            pane.setMaxSize(Region.USE_PREF_SIZE, nodeHeight * nodes.length * transY);

            for (int i = 1; i < nodes.length; i++) {
                nodes[i].setOpacity(0);
                nodes[i].setPickOnBounds(false);
                FadeTransition fadeInTransition = new FadeTransition(Duration.millis(300), nodes[i]);
                TranslateTransition translateInTransition = new TranslateTransition(Duration.millis(300), nodes[i]);
                translateInTransition.setToY(nodeHeight * i * transY);
                fadeInTransition.setToValue(1);
                openTransition.getChildren().addAll(translateInTransition, fadeInTransition);

                FadeTransition fadeOutTransition = new FadeTransition(Duration.millis(300), nodes[i]);
                TranslateTransition translateOutTransition = new TranslateTransition(Duration.millis(300), nodes[i]);
                translateOutTransition.setToY(0);
                fadeOutTransition.setToValue(0);
                closeTransition.getChildren().addAll(translateOutTransition, fadeOutTransition);
            }
        });
        nodes[0].setPickOnBounds(false);
        nodes[0].toFront();
        nodes[0].setOnMouseClicked(event -> {
            if (nodes[nodes.length - 1].getTranslateY() > nodes[0].getTranslateY()){
                closeTransition.play();
            } else openTransition.play();
        });

        return pane;
    }

    public HBox getIconBox(String path, int face) {
        return getIconBox(getSimpleIconImgView(path), face);
    }

    public static Stop[] getStops(Stop... stops) {
        return stops;
    }

    public String getWeather(String city) {
        try {
            return Objects.requireNonNull(getWeatherJson(city)).getJSONArray("weather").getJSONObject(0).getString("main");

        } catch (Exception e) {
            log.error("e: ", e);
            System.out.println("Funkt nicht");
            return "--°C";
        }
    }

    public String getTemperature(String city) {
        try {
            return Math.round(Objects.requireNonNull(getWeatherJson(city)).getJSONObject("main").getDouble("temp")) + "°C";
        } catch (Exception e) {
            log.error("e: ", e);
            return "--°C";
        }
    }

    private JSONObject getWeatherJson(String city) {
        return getWeatherJsonByURL("https://api.openweathermap.org/data/2.5/weather?q=" + city + ",de&units=metric&appid=" + API_KEY);
    }

    private JSONObject getWeatherJson() {
        return getWeatherJsonByURL("https://api.openweathermap.org/data/2.5/weather?lat=" + latitude +
                "&lon=" + longitude +
                "&units=metric&appid=" + API_KEY);
    }

    private JSONObject getWeatherJsonByURL(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );
            StringBuilder result = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();

            return new JSONObject(result.toString());
        } catch (Exception e) {
            log.error("e: ", e);
            return null;
        }
    }

    public double getCurrentSunProgress() {
        JSONObject weather = getWeatherJson("Berlin");
        if (weather == null) {
            return -1;
        }
        long sunrise = weather.getJSONObject("sys").getLong("sunrise");
        long sunset = weather.getJSONObject("sys").getLong("sunset");
        long currentTime = ZonedDateTime.now(ZoneId.systemDefault()).toEpochSecond();
        if (currentTime < sunrise || currentTime > sunset ) {
            return -1;
        }
        return (double)(currentTime - sunrise) / (sunset - sunrise);
    }

    public double getCurrentSunPos() {
        double padding = height / 5;

        // Zeitfortschritt von 0 (Sonnenaufgang) bis 1 (Sonnenuntergang)
        double progress = getCurrentSunProgress();

        if (progress == -1) {
            return height;
        }

        // Winkel von 0 (Sonnenaufgang) bis PI (Sonnenuntergang)
        double angle = progress * Math.PI;

        // Max Höhe: Padding (oben), Min Höhe: height - padding (unten)
        double amplitude = (height - padding);
        // y-Koordinate in JavaFX

        return height - (Math.sin(angle) * amplitude);
    }

    public static double[] getCoordinatesFromIP() {
        try {
            URL url = new URL("http://ip-api.com/json/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(result.toString());
            return new double[] {
                    json.getDouble("lat"),
                    json.getDouble("lon")
            };

        } catch (Exception e) {
            e.printStackTrace();
            return new double[] {
                    0,
                    0
            };
        }
    }
}