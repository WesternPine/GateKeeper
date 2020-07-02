package dev.westernpine.common.strings;

import java.net.URL;

public class Strings {
    
    /*
     * Returns if a string resembles null.
     */
    public static boolean resemblesNull(String string) {
        if (string == null || string.equals("") || string.equals(" ")) {
            return true;
        }
        return false;
    }

    /*
     * Capitalizes first letter of a string.
     */
    public static String capitalizeFirst(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }
    
    /*
     * Returns if a string is only letters.
     */
    public static boolean isAlpha(String string) {
        return string.matches("[a-zA-Z]+");
    }
    
    /*
     * Returns if a string is only numbers.
     */
    public static boolean isNumeric(String string) {
        return string.matches("[0-9]+");
    }
    
    /*
     * Returns if a string is only letters or numbers.
     */
    public static boolean isAlphaNumeric(String string) {
        return string.matches("[a-zA-Z0-9]+");
    }
    
    /*
     * Compiles a string with the specified separator between each Object array.
     */
    public static String compile(Object[] strings, String separator) {
        if(separator == null)
            separator = "";
        String string = "";
        boolean first = true;
        for(Object o : strings) {
            String s = o.toString();
            if(first)
                string = s;
            else
                string = separator + s;
        }
        return string;
    }
    
    /*
     * Checks if a string is a URL.
     */
    public static boolean isURL(String string) {
        try {
            new URL(string);
            return true;
        } catch(Exception e) {
            return false;
        }
    }

}