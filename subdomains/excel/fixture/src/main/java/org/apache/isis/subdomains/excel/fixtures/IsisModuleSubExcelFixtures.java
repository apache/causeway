package org.apache.isis.subdomains.excel.fixtures;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.extensions.excel.ExcelModule;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.fixturescripts.ExcelDemoToDoItem_tearDown;
import org.apache.isis.extensions.fixtures.IsisExtFixturesModule;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript;
import org.apache.isis.extensions.fixtures.modules.Module;

@Configuration
@Import({
        ExcelModule.class,
        IsisExtFixturesModule.class
})
@ComponentScan
public class IsisModuleSubExcelFixtures implements Module {

    @Override public FixtureScript getTeardownFixture() {
        return new ExcelDemoToDoItem_tearDown();
    }
}
