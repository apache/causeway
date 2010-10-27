package org.apache.isis.viewer.restful.viewer.util;

import javax.servlet.http.HttpServletRequest;


public final class PathUtils {

    private PathUtils() {}


    public static String combine(final HttpServletRequest request, final String... pathElements) {
        final StringBuilder buf = new StringBuilder(request.getContextPath());
        for (final String pathElement : pathElements) {
            if (!pathElement.startsWith("/")) {
                buf.append("/");
            }
            buf.append(pathElement);
        }
        return buf.toString();
    }

}
