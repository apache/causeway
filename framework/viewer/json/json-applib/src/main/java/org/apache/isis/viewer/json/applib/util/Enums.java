package org.apache.isis.viewer.json.applib.util;

public final class Enums {
    
    private Enums() {}

    public static String enumToHttpHeader(Enum<?> anEnum) {
        return enumNameToHttpHeader(anEnum.name());
    }

    public static String enumNameToHttpHeader(String name) {
        StringBuilder builder = new StringBuilder();
        boolean nextUpper = true;
        for(char c: name.toCharArray()) {
            if(c == '_') {
                nextUpper = true;
                builder.append("-");
            } else {
                builder.append(nextUpper?c:Character.toLowerCase(c));
                nextUpper = false;
            }
        }
        return builder.toString();
    }

    public static String enumToCamelCase(Enum<?> anEnum) {
        return enumNameToCamelCase(anEnum.name());
    }

    private static String enumNameToCamelCase(String name) {
        StringBuilder builder = new StringBuilder();
        boolean nextUpper = false;
        for(char c: name.toCharArray()) {
            if(c == '_') {
                nextUpper = true;
            } else {
                builder.append(nextUpper?c:Character.toLowerCase(c));
                nextUpper = false;
            }
        }
        return builder.toString();
    }


}
