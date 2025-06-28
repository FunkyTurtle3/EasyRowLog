package easyrow;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
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
    private EasyRowLog easyRowLog;
    private static final String API_KEY = "2c4efc9c04e72b05668e99873bae5cdb";
    private double latitude;
    private double longitude;
    private ImageView weatherImgView;
    private double width;
    private double height;

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
        weatherImgView.setEffect(new DropShadow(width / 200, 0, 0, Color.web("#FFFFFF", 0.25)));

        BorderPane mainPane = new BorderPane();

        stage.getIcons().add(Resource.getTexture("/UI/logo_500x500.png"));

        // --- Left Panel ---
        VBox leftPanel = new VBox(10);
        leftPanel.setPrefWidth(width / 4);

        // --- Center Panel ---
        Pane centerPane = new Pane();
        centerPane.setPrefWidth(width / 2);

        RadialGradient clearSkyGradient = new RadialGradient(0, 0, 0.5, 1, 1,  true, CycleMethod.NO_CYCLE, getStops(new Stop(0, Color.web("C7FFF4")), new Stop(1, Color.web("0096C7"))));
        RadialGradient sunsetSunriseGradient = new RadialGradient(0, 0, 0.5, 1, 1, true, CycleMethod.NO_CYCLE, getStops(new Stop(0, Color.web("FCBF49")), new Stop(1, Color.web("D62828"))));
        RadialGradient nightSkyGradient = new RadialGradient(0, 0, 0.5, 1, 1, true, CycleMethod.NO_CYCLE, getStops(new Stop(0, Color.web("03045E")), new Stop(1, Color.web("0D1B2A"))));

        Rectangle clearSky = new Rectangle(width / 2, height);
        clearSky.setFill(clearSkyGradient);

        Rectangle sunsetSunriseSky = new Rectangle(width / 2, height);
        sunsetSunriseSky.setFill(sunsetSunriseGradient);
        sunsetSunriseSky.setOpacity(0);

        if(getCurrentSunProgress() > 0.8 ) {
            sunsetSunriseSky.setOpacity((getCurrentSunProgress() - 0.8) * 5);
        }

        Rectangle nightSky = new Rectangle(width / 2, height);
        nightSky.setFill(nightSkyGradient);
        nightSky.setOpacity(0);
        if (getCurrentSunProgress() == -1 && nightSky.getOpacity() == 0) {
            FadeTransition nightAnimation = new FadeTransition(Duration.minutes(4), nightSky);
            nightAnimation.setToValue(1);
            nightAnimation.play();
        }

        Circle sunDisplay = new Circle(width / 20);
        sunDisplay.setFill(new RadialGradient(0, 0, 0.5, 0.5, 1,  true,
                CycleMethod.NO_CYCLE, getStops(new Stop(0, Color.web("#FFF0D7")), new Stop(1, Color.web("#FFDB72")))));
        sunDisplay.setCenterX(width / 4);
        sunDisplay.setTranslateY(getCurrentSunPos() + sunDisplay.getRadius());
        sunDisplay.setEffect(new DropShadow(75, Color.web("#F7B801", 0.75)));
        TranslateTransition sunTranslate = new TranslateTransition(Duration.seconds(60), sunDisplay);


        Label timeLabel = getInfoLabel(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

        StackPane timePane = new StackPane(getInfoRectangle(), getIconBox("/UI/time_symbol.png", 0), timeLabel);
        timePane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        Label tempLabel = getInfoLabel(getTemperature("Berlin"));

        HBox weatherSymbol = new HBox(weatherImgView);
        weatherSymbol.setPadding(new Insets(0, 0, 0, 0));
        weatherSymbol.setAlignment(Pos.CENTER);

        Circle offCircle = getInfoCircle(true);
        offCircle.setOnMouseClicked(MouseEvent -> System.exit(0));

        StackPane tempPane = new StackPane(getInfoRectangle(), getIconBox("/UI/temp_symbol.png", 0), tempLabel);
        tempPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        tempPane.setAlignment(Pos.CENTER);

        ImageView windDirectionImgView = getSimpleIconImgView("/UI/compass_symbol.png");
        windDirectionImgView.setRotate(Objects.requireNonNull(getWeatherJson("Berlin")).getJSONObject("wind").getInt("deg") + 180);


        Label windSpeedLabel = new Label(((int) getWeatherJson().getJSONObject("wind").getDouble("speed")) + "");
        windSpeedLabel.setFont(Resource.getFont("/anta_regular.otf", (int) (height / 30)));
        windSpeedLabel.setTextFill(Color.WHITE);
        windSpeedLabel.setTooltip(new Tooltip("Windgeschwindigkeit: " + windSpeedLabel.getText() + "km/h"));
        windSpeedLabel.setAlignment(Pos.CENTER);

        HBox symbolPane = new HBox(new StackPane(getInfoCircle(false), weatherSymbol), new StackPane(getInfoCircle(false), windSpeedLabel), new StackPane(getInfoCircle(false), windDirectionImgView), new StackPane(offCircle, getIconBox("/UI/shutdown_symbol.png", -1)));
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

        Image leftSweep = Resource.getTexture("/oars/sweep_left.png");
        ImageView sweepLeftImgView = new ImageView(leftSweep);
        sweepLeftImgView.setPreserveRatio(true);
        sweepLeftImgView.setFitHeight(height * 1.03);
        sweepLeftImgView.setX(-1 * height / 18);
        sweepLeftImgView.setY(height * -0.03);

        Image rightSweep = Resource.getTexture("/oars/sweep_left.png");
        ImageView sweepRightImgView = new ImageView(rightSweep);
        sweepRightImgView.setScaleX(-1);
        sweepRightImgView.setPreserveRatio(true);
        sweepRightImgView.setFitHeight(height * 1.03);
        sweepRightImgView.setTranslateX(width / 2 - (rightSweep.getWidth() - height / 18) / 2 + 1);
        sweepRightImgView.setY(height * -0.03);

        // --- Right Panel ---
        StackPane rightPanel = new StackPane();
        rightPanel.setStyle("-fx-background-color: #ffffff;");
        rightPanel.setPrefWidth(width / 4);

        // Alles hinzufügen
        centerPane.getChildren().addAll(clearSky, sunsetSunriseSky, nightSky, sunDisplay, infobox, wavesImageView, sweepLeftImgView, sweepRightImgView);

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


            if (getCurrentSunProgress() == -1 && nightSky.getOpacity() == 0) {
                FadeTransition nightAnimation = new FadeTransition(Duration.minutes(4), nightSky);
                nightAnimation.setToValue(1);
                nightAnimation.play();
            }

            RotateTransition compassRotate = new RotateTransition(Duration.seconds(2) , windDirectionImgView);
            compassRotate.setToAngle(Objects.requireNonNull(getWeatherJson("Berlin")).getJSONObject("wind").getInt("deg") + 180);
            compassRotate.play();
        }));
        weatherCycle.setCycleCount(Animation.INDEFINITE);
        weatherCycle.play();

        Scene scene = new Scene(mainPane);
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();
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
        rectangle.prefWidth(this.width / 5);
        rectangle.prefHeight(this.height / 15);
        rectangle.setArcWidth(this.width / 25);
        rectangle.setArcHeight(this.height / 7.5);
        rectangle.setFill(Color.web("#FFFFFF", 0.2));
        rectangle.setStroke(Color.web("#FFFFFF", 0.5));
        rectangle.setStrokeWidth(2);
        rectangle.setEffect(new DropShadow(this.width / 200, 2, 2, Color.web("#FFFFFF", 1)));
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
        label.setFont(Resource.getFont("/anta_regular.otf", (int) (height / 20))); // beliebig anpassen
        label.setEffect(new DropShadow(width / 200, 0, 0, Color.web("#FFFFFF", 0.25)));
        label.setAlignment(Pos.CENTER);
        label.setText(text);
        return label;
    }

    public ImageView getSimpleIconImgView(String path) {
        ImageView iconImgView = new ImageView(Resource.getTexture(path));
        iconImgView.setFitHeight(this.width / 40);
        iconImgView.setPreserveRatio(true);
        iconImgView.setEffect(new DropShadow(width / 200, 0, 0, Color.web("#FFFFFF", 0.25)));
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

    public HBox getIconBox(String path, int face) {
        return getIconBox(getSimpleIconImgView(path), face);
    }

    public Stop[] getStops(Stop... stops) {
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