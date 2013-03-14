package org.apache.isis.tool.mavenplugin.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.classworlds.ClassRealm;

public final class ClassRealms {
    
    private ClassRealms(){}

    public static void addFileToRealm(ClassRealm isisRealm, final File file, Log log) throws IOException, MalformedURLException {
        log.info(file.getCanonicalPath());
    
        final URL url = file.toURI().toURL();
        isisRealm.addConstituent(url);
    }


}
