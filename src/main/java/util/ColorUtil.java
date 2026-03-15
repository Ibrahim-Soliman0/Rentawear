package util;

import java.awt.*;

public final class ColorUtil {
    private ColorUtil() {}

    /**
     * Extracts 1st 7 characters from string representing hexadecimal
     * color code
     * ex: "#FFFFFF-White" -> #FFFFFF*/
    public static String hex(String encoded) {
        return encoded.substring(0,7);
    }


    /**
     * Extracts the rest of the characters after '-' from a string representing
     * color name
     * ex: "#FFFFFF-White" -> White*/
    public static String name(String encoded) {
        return encoded.substring(8);
    }

    /** URL/filesystem slug: "#FFFFFF-White" -> "white" */
    public static String slug(String encoded) {
        return name(encoded).toLowerCase()
                .replace(' ', '-')
                .replaceAll("[^a-z0-9\\-]", "");
    }

    /** Build encoded string form parts*/
    public static String encode(String hex, String name){
        return hex+"-"+name;
    }

    /** Validate format — call in upload servlet before persisting */
    public static boolean isValid(String encoded) {
        return encoded != null
                && encoded.length() > 8
                && encoded.charAt(0) == '#'
                && encoded.charAt(7) == '-';
    }

}
