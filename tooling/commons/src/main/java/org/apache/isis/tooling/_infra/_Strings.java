package org.apache.isis.tooling._infra;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import javax.annotation.Nullable;

public final class _Strings {
    
    /**
     * @return whether {@code x} is of zero length or null.
     */
    public static boolean isNullOrEmpty(final @Nullable CharSequence x){
        return x==null || x.length()==0;
    }
    
    /**
     * @param input
     * @return null if the {@code input} is null or empty, the {@code input} otherwise 
     */
    public static @Nullable String emptyToNull(final @Nullable String input) {
        if(isNullOrEmpty(input)) {
            return null;
        }
        return input;
    }

    /**
     * @param input
     * @return the empty string if the {@code input} is null, the {@code input} otherwise 
     */
    public static String nullToEmpty(final @Nullable String input) {
        if(input==null) {
            return "";
        }
        return input;
    }
    
    // -- RESOURCE LOADING

    public static String read(final @Nullable InputStream input) {
        if(input==null) {
            return "";
        }
        // see https://stackoverflow.com/questions/309424/how-to-read-convert-an-inputstream-into-a-string-in-java
        try(Scanner scanner = new Scanner(input, StandardCharsets.UTF_8.name())){
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next().replace("\r", "") : "";
        }
    }
    
    public static String readResource(Class<?> location, String name) {
        return read(location.getResourceAsStream(name));  
    }
    
    public static String readResource(Object location, String name) {
        return readResource(location.getClass(), name);  
    }
    

    
}
