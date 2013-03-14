package org.apache.isis.tool.mavenplugin.util;

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
