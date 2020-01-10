package org.apache.isis.subdomains.excel.fixtures;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.subdomains.excel.applib.IsisModuleSubExcelApplib;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.fixturescripts.ExcelDemoToDoItem_tearDown;
import org.apache.isis.testing.fixtures.applib.IsisModuleTstFixturesApplib;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.modules.ModuleWithFixtures;

@Configuration
@Import({
        IsisModuleSubExcelApplib.class,
        IsisModuleTstFixturesApplib.class
})
@ComponentScan
public class IsisModuleSubExcelFixtures implements ModuleWithFixtures {

    @Override public FixtureScript getTeardownFixture() {
        return new ExcelDemoToDoItem_tearDown();
    }
}
