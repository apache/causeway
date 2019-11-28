package org.isisaddons.module.excel.fixture;

import java.util.Set;

import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript;
import org.apache.isis.extensions.fixtures.modules.Module;
import org.isisaddons.module.excel.ExcelModule;
import org.isisaddons.module.excel.fixture.demoapp.todomodule.fixturescripts.ExcelDemoToDoItem_tearDown;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.google.common.collect.Sets;
import com.sun.org.apache.xpath.internal.operations.Mod;

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
