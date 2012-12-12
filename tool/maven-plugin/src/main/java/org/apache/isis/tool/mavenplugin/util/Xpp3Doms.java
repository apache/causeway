package org.apache.isis.tool.mavenplugin.util;

import org.codehaus.plexus.util.xml.Xpp3Dom;

import com.google.common.base.Function;

public final class Xpp3Doms {
    
    private Xpp3Doms(){}

    public static Function<Xpp3Dom, String> GET_VALUE = new Function<Xpp3Dom, String>(){

        public String apply(Xpp3Dom el) {
            return el.getValue();
        }};


}
