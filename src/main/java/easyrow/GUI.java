package easyrow;

import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
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
    private ImageView weatherImgView;
    private double width;
    private double height;

    public static void main(String[] args) {
        launch(args);
    }

    public GUI() {

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

        weatherImgView = new ImageView(Resource.getTexture("/UI/drizzle_symbol.png"));
        weatherImgView.setFitHeight(width / 40);
        weatherImgView.setPreserveRatio(true);
        weatherImgView.setEffect(new DropShadow(width / 200, 0, 0, Color.web("#FFFFFF", 0.25)));

        BorderPane mainPane = new BorderPane();

        try {
            stage.getIcons().add(Resource.getTexture("/UI/logo_500x500.png"));
        } catch (Exception e) {
            log.error("Icon konnte nicht geladen werden", e);
        }

        // --- Left Panel ---
        VBox leftPanel = new VBox(10);
        leftPanel.setPrefWidth(width / 4);

        // --- Center Panel ---

        RadialGradient clearSkyGradient = new RadialGradient(0, 0, 0.5, 1, 1,  true,
                CycleMethod.NO_CYCLE, getStops(new Stop(0, Color.web("C7FFF4")), new Stop(1, Color.web("0096C7"))));

        RadialGradient sunsetSunriseGradient = new RadialGradient(0, 0, 0.5, 1, 1, true, CycleMethod.NO_CYCLE, getStops(new Stop(0, Color.web("EEAF61")), new Stop(1, Color.web("CE4993"))));

        Pane root = new Pane();

        Rectangle clearSky = new Rectangle(width / 2, height);
        clearSky.setFill(clearSkyGradient);
        root.setPrefWidth(width / 2);

        Rectangle sunsetSunriseSky = new Rectangle(width / 2, height);
        sunsetSunriseSky.setFill(sunsetSunriseGradient);
        sunsetSunriseSky.setOpacity(0.5);
        root.setPrefWidth(width / 2);

        Circle sun = new Circle(width / 20);
        sun.setFill(new RadialGradient(0, 0, 0.5, 0.5, 1,  true,
                CycleMethod.NO_CYCLE, getStops(new Stop(0, Color.web("#FFF0D7")), new Stop(1, Color.web("#FFDB72")))));
        sun.setCenterX(width / 4);
        sun.setCenterY(getCurrentSunPos());
        sun.setEffect(new DropShadow(75, Color.web("#F7B801", 0.75)));
        TranslateTransition sunTranslate = new TranslateTransition(Duration.seconds(60), sun);
        sunTranslate.setToY(getCurrentSunPos() - height / 5);
        sunTranslate.play();


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

        ImageView compassImgView = getSimpleIconImgView("/UI/compass_symbol.png");
        compassImgView.setRotate(Objects.requireNonNull(getWeatherJson("Berlin")).getJSONObject("wind").getInt("deg"));

        HBox symbolPane = new HBox(new StackPane(getInfoCircle(false), weatherSymbol), new StackPane(offCircle, getIconBox("/UI/shutdown_symbol.png", -1)), new StackPane(getInfoCircle(false), getIconBox(compassImgView, -1)), new StackPane(getInfoCircle(false), getIconBox("/UI/compass_symbol.png", -1)));
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
        wavesImageView.setY(height - waveImg.getHeight() / 2);
        wavesImageView.setEffect(new DropShadow(width / 200, Color.WHITE));

        // --- Right Panel ---
        StackPane rightPanel = new StackPane();
        rightPanel.setStyle("-fx-background-color: #ffffff;");
        rightPanel.setPrefWidth(width / 4);

        // Alles hinzufügen
        root.getChildren().addAll(clearSky, sunsetSunriseSky, sun, infobox, wavesImageView);

        // Add Panels to main layout
        mainPane.setLeft(leftPanel);
        mainPane.setCenter(root);
        mainPane.setRight(rightPanel);

        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> timeLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")))));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        Timeline weatherCycle = new Timeline(new KeyFrame(Duration.seconds(60), e -> {
            tempLabel.setText(getTemperature("Berlin"));
            weatherImgView.setImage(Resource.getTexture("/UI/" + getWeather("Berlin").toLowerCase() + "_symbol.png"));
            sunTranslate.setToY(getCurrentSunPos() - height / 5);
            sunTranslate.play();

            RotateTransition compassRotate = new RotateTransition(Duration.seconds(2) , compassImgView);
            compassRotate.setToAngle(Objects.requireNonNull(getWeatherJson("Berlin")).getJSONObject("wind").getInt("deg"));
            compassRotate.play();
        }));
        weatherCycle.setCycleCount(Animation.INDEFINITE);
        weatherCycle.play();

        Scene scene = new Scene(mainPane);
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();
    }

    public Rectangle getInfoRectangle() {
        Rectangle rectangle = new Rectangle(width / 5, height / 15);
        rectangle.prefWidth(width / 5);
        rectangle.prefHeight(height / 15);
        rectangle.setArcWidth(width / 25);
        rectangle.setArcHeight(height / 7.5);
        rectangle.setFill(Color.web("#FFFFFF", 0.2));
        rectangle.setStroke(Color.web("#FFFFFF", 0.5));
        rectangle.setStrokeWidth(2);
        rectangle.setEffect(new DropShadow(width / 200, 2, 2, Color.web("#FFFFFF", 1)));
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
                System.out.println("Funkt");
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

    public static String getWeather(String city) {
        try {
            return Objects.requireNonNull(getWeatherJson(city)).getJSONArray("weather").getJSONObject(0).getString("main");

        } catch (Exception e) {
            log.error("e: ", e);
            System.out.println("Funkt nicht");
            return "--°C";
        }
    }

    public static String getTemperature(String city) {
        try {
            return Math.round(Objects.requireNonNull(getWeatherJson(city)).getJSONObject("main").getDouble("temp")) + "°C";
        } catch (Exception e) {
            log.error("e: ", e);
            return "--°C";
        }
    }

    private static JSONObject getWeatherJson(String city) {
        try {
            String urlStr = "https://api.openweathermap.org/data/2.5/weather?q=" + city +
                    "&units=metric&appid=" + API_KEY;

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
        double amplitude = (height - 2 * padding);
        double y = height - (Math.sin(angle) * amplitude + padding);  // y-Koordinate in JavaFX

        return y;
    }
}