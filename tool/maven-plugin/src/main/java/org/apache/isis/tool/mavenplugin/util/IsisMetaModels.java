package org.apache.isis.tool.mavenplugin.util;

import org.apache.isis.core.progmodel.app.IsisMetaModel;

public final class IsisMetaModels {
    
    private IsisMetaModels(){}

    public static void disposeSafely(IsisMetaModel isisMetaModel) {
        if (isisMetaModel == null) {
            return;
        }
        isisMetaModel.shutdown();
    }



}
