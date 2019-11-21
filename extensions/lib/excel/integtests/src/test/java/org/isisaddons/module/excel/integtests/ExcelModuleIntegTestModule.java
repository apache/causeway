package org.isisaddons.module.excel.integtests;

import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Sets;

import org.apache.isis.applib.Module;
import org.apache.isis.applib.ModuleAbstract;

import org.isisaddons.module.excel.ExcelModule;
import org.isisaddons.module.excel.fixture.ExcelFixturesModule;
import org.isisaddons.module.fakedata.FakeDataModule;

@XmlRootElement(name = "module")
public class ExcelModuleIntegTestModule extends ModuleAbstract {

    @Override
    public Set<Module> getDependencies() {
        return Sets.newHashSet(
                new ExcelModule(),
                new ExcelFixturesModule(),
                new FakeDataModule()
        );
    }
}
