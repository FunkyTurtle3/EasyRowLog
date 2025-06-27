package easyrow;

import javafx.scene.image.Image;
import javafx.scene.text.Font;

import java.io.IOException;
import java.io.InputStream;

public class Resource {

    public static InputStream getResource(String path) {
        try {
            return Resource.class.getResourceAsStream(path);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static Image getTexture(String path) {
        return new Image(getResource("/assets/texture" + path));
    }

    public static Font getFont(String path, int size) {
        return Font.loadFont(getResource("/assets/font" + path), size);
    }
}
