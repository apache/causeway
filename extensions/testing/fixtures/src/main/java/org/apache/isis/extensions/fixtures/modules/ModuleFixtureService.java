package org.apache.isis.extensions.fixtures.modules;

import lombok.extern.log4j.Log4j2;

import java.util.Collections;
import java.util.List;

import javax.inject.Named;

import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript;
import org.springframework.stereotype.Service;

@Service
@Named("isisExtFixtures.moduleFixtureService")
@Log4j2
public class ModuleFixtureService {

    private final ModuleService moduleService;

    public ModuleFixtureService(final ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    public FixtureScript getRefDataSetupFixture() {
        return new FixtureScript() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                final List<ModuleService.ModuleWithFixturesDescriptor> descriptors = moduleService.modules();
                executionContext.executeChildren(this,
                        descriptors.stream()
                        .map(ModuleService.ModuleWithFixturesDescriptor::getModule)
                        .map(ModuleWithFixtures::getRefDataSetupFixture));
            }
        };
    }

    public FixtureScript getTeardownFixture() {
        return new FixtureScript() {
            @Override
            protected void execute(final ExecutionContext executionContext) {
                final List<ModuleService.ModuleWithFixturesDescriptor> descriptors = moduleService.modules();
                Collections.reverse(descriptors);
                executionContext.executeChildren(this,
                        descriptors.stream()
                            .map(ModuleService.ModuleWithFixturesDescriptor::getModule)
                            .map(ModuleWithFixtures::getTeardownFixture));
            }

        };
    }

}
