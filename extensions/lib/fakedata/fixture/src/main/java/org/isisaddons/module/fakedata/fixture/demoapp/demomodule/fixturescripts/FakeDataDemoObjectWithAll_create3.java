package org.isisaddons.module.fakedata.fixture.demoapp.demomodule.fixturescripts;

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.fixturescripts.FixtureScript;

import org.isisaddons.module.fakedata.dom.FakeDataService;
import org.isisaddons.module.fakedata.fixture.demoapp.demomodule.dom.FakeDataDemoObjectWithAll;
import org.isisaddons.module.fakedata.fixture.demoapp.demomodule.fixturescripts.data.FakeDataDemoObjectWithAll_create_withFakeData;

import lombok.Getter;
import lombok.Setter;

@lombok.experimental.Accessors(chain = true)
public class FakeDataDemoObjectWithAll_create3 extends FixtureScript {

    @Getter(onMethod = @__( @Programmatic )) @Setter
    private Integer numberToCreate;
    @Getter(onMethod = @__( @Programmatic )) @Setter
    private Boolean withFakeData;

    @Getter(onMethod = @__( @Programmatic ))
    private List<FakeDataDemoObjectWithAll> demoObjects = Lists.newArrayList();

    @Override
    protected void execute(final ExecutionContext executionContext) {

        this.defaultParam("numberToCreate", executionContext, 3);
        this.defaultParam("withFakeData", executionContext, true);

        for (int i = 0; i < getNumberToCreate(); i++) {
            final FakeDataDemoObjectWithAll_create_withFakeData fs = new FakeDataDemoObjectWithAll_create_withFakeData().setWithFakeData(withFakeData);
            executionContext.executeChildT(this, fs);
            demoObjects.add(fs.getFakeDataDemoObject());
        }

    }

    @javax.inject.Inject
    FakeDataService fakeDataService;
}
