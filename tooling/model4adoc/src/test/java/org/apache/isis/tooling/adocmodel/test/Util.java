package org.apache.isis.tooling.adocmodel.test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import javax.annotation.Nullable;

final class Util {
    
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
