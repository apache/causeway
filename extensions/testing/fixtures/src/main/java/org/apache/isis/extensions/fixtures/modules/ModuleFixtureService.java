package org.apache.isis.extensions.fixtures.modules;

import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript;

@DomainService(nature = NatureOfService.DOMAIN)
public class ModuleFixtureService {

    private final ModuleService moduleService;

    public ModuleFixtureService(final ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    public FixtureScript getRefDataSetupFixture() {
        return new FixtureScript() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                final List<ModuleService.ModuleDescriptor> modules = moduleService.modules();
                executionContext.executeChildren(this,
                        modules.stream()
                        .map(ModuleService.ModuleDescriptor::getModule)
                        .map(Module::getRefDataSetupFixture));
            }
        };
    }

    public FixtureScript getTeardownFixture() {
        return new FixtureScript() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                final List<ModuleService.ModuleDescriptor> modules = moduleService.modules();
                Collections.reverse(modules);
                executionContext.executeChildren(this,
                        modules.stream()
                            .map(ModuleService.ModuleDescriptor::getModule)
                            .map(Module::getTeardownFixture));
            }

        };
    }

}
