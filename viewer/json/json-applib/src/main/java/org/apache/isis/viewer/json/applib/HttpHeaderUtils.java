package org.apache.isis.viewer.json.applib;


final class HttpHeaderUtils {
    
    private HttpHeaderUtils() {
    }
    
    static String enumToHttpHeader(String name) {
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

    static String enumToHttpValue(String name) {
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

