package org.apache.isis.extensions.excel.fixtures;

import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript;
import org.apache.isis.extensions.fixtures.modules.Module;
import org.apache.isis.extensions.excel.ExcelModule;
import org.apache.isis.extensions.excel.fixtures.demoapp.todomodule.fixturescripts.ExcelDemoToDoItem_tearDown;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        ExcelModule.class
})
@ComponentScan
public class ExcelFixturesModule implements Module {

    @Override public FixtureScript getTeardownFixture() {
        return new ExcelDemoToDoItem_tearDown();
    }
}
