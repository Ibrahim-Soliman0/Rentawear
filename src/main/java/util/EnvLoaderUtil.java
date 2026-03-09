package util;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvLoaderUtil {

    public static void load() {
        Dotenv dotenv = Dotenv.configure().load();

        System.setProperty("DB_USER", dotenv.get("DB_USER"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
        System.setProperty("DB_URL", dotenv.get("DB_URL"));
    }

    public static void unload() {
        System.clearProperty("DB_USER");
        System.clearProperty("DB_PASSWORD");
        System.clearProperty("DB_URL");
    }
}