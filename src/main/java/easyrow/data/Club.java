package easyrow.data;

import java.awt.*;

public enum Club {
    RGW(new Color(15, 141, 49)),
    NRCB(new Color(0, 177, 240));

    private final Color color;

    Club(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
