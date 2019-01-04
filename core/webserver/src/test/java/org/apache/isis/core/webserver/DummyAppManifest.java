package org.apache.isis.core.webserver;

import org.apache.isis.applib.AppManifestAbstract2;
import org.apache.isis.applib.ModuleAbstract;

public class DummyAppManifest extends AppManifestAbstract2 {
    private static final Builder builder = AppManifestAbstract2.Builder.forModule(new ModuleAbstract() {
    });

    public DummyAppManifest() {
        super(builder);
    }
}