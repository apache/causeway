package org.isisaddons.module.fakedata.integtests;

import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Sets;

import org.apache.isis.applib.Module;
import org.apache.isis.applib.ModuleAbstract;

import org.isisaddons.module.fakedata.FakeDataModule;
import org.isisaddons.module.fakedata.fixture.FakeDataFixturesModule;

@XmlRootElement(name = "module")
public class FakeDataModuleIntegTestModule extends ModuleAbstract {

    @Override
    public Set<Module> getDependencies() {
        return Sets.newHashSet(
                new FakeDataModule(),
                new FakeDataFixturesModule()
        );
    }
}
