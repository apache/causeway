package org.apache.isis.subdomains.excel.fixtures;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.subdomains.excel.applib.IsisModuleSubdomainsExcelApplib;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.fixturescripts.ExcelDemoToDoItem_tearDown;
import org.apache.isis.testing.fixtures.applib.IsisModuleTestingFixturesApplib;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.modules.ModuleWithFixtures;

@Configuration
@Import({
        IsisModuleSubdomainsExcelApplib.class,
        IsisModuleTestingFixturesApplib.class
})
@ComponentScan
public class IsisModuleSubdomainsExcelFixtures implements ModuleWithFixtures {

    @Override public FixtureScript getTeardownFixture() {
        return new ExcelDemoToDoItem_tearDown();
    }
}
