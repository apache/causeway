package org.apache.isis.viewer.json.applib;


final class HeaderNameUtils {
    
    private HeaderNameUtils() {
    }
    
    static String convert(String name) {
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

}

