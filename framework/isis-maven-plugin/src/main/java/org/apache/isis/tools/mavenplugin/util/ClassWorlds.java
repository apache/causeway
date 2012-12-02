package org.apache.isis.tools.mavenplugin.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.classworlds.NoSuchRealmException;

public final class ClassWorlds {
    
    private ClassWorlds(){}

    public static void disposeSafely(ClassWorld classWorld, String realmId) {
        if (classWorld == null) {
            return;
        }
        try {
            classWorld.disposeRealm(realmId);
        } catch (NoSuchRealmException e) {
            // ignore
        }
    }


}
