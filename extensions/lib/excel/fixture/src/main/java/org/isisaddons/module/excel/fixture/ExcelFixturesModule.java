package org.isisaddons.module.excel.fixture;

import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Sets;

import org.apache.isis.applib.Module;
import org.apache.isis.applib.ModuleAbstract;
import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.isisaddons.module.excel.ExcelModule;
import org.isisaddons.module.excel.fixture.demoapp.todomodule.fixturescripts.ExcelDemoToDoItem_tearDown;

@XmlRootElement(name = "module")
public class ExcelFixturesModule extends ModuleAbstract {

    @Override
    public Set<Module> getDependencies() {
        return Sets.newHashSet(
                new ExcelModule()
        );
    }

    @Override public FixtureScript getTeardownFixture() {
        return new ExcelDemoToDoItem_tearDown();
    }
}
