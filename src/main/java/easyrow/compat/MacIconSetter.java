package easyrow.compat;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

public class MacIconSetter {
    public static void setDockIcon() {
        if (Taskbar.isTaskbarSupported()) {
            Taskbar taskbar = Taskbar.getTaskbar();
            try {
                URL imageURL = MacIconSetter.class.getResource("/assets/texture/UI/logo_500x500.png");
                if (imageURL != null) {
                    Image image = ImageIO.read(imageURL);
                    taskbar.setIconImage(image);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}