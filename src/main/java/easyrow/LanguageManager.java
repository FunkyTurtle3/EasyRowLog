package easyrow;

import easyrow.cofig.ConfigManager;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {

    private ResourceBundle bundle;

    public LanguageManager(String baseName) {
        bundle = ResourceBundle.getBundle(baseName, new Locale(ConfigManager.readConfig("user.language")));
    }

    public String get(String key, Object... params) {
        String raw = bundle.getString(key);
        return java.text.MessageFormat.format(raw, params);
    }

    public void changeLang(String baseName) {
        bundle = ResourceBundle.getBundle(baseName, new Locale(ConfigManager.readConfig("user.language")));
    }
}
