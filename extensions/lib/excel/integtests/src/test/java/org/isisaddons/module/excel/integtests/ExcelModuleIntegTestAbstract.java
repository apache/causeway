package org.isisaddons.module.excel.integtests;

import org.apache.isis.applib.ModuleAbstract;
import org.apache.isis.core.integtestsupport.IntegrationTestAbstract3;

public abstract class ExcelModuleIntegTestAbstract extends IntegrationTestAbstract3 {

    public static ModuleAbstract module() {
        return new ExcelModuleIntegTestModule();
    }

    protected ExcelModuleIntegTestAbstract() {
        super(module());
    }

}
