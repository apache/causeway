package org.isisaddons.module.fakedata.integtests;


import org.apache.isis.extensions.fixtures.IsisIntegrationTestAbstractWithFixtures;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        classes = SimpleModuleManifestForTesting.class,
        properties = {
                "isis.objects.editing=false"
        })
public abstract class FakeDataModuleIntegTestAbstract extends IsisIntegrationTestAbstractWithFixtures {

    public static ModuleAbstract module() {
        return new FakeDataModuleIntegTestModule();
    }

    protected FakeDataModuleIntegTestAbstract() {
        super(module());
    }

}
