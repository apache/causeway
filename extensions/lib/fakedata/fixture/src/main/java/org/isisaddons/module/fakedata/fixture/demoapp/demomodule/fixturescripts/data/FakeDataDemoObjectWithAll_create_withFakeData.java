package org.isisaddons.module.fakedata.fixture.demoapp.demomodule.fixturescripts.data;

import org.apache.isis.applib.annotation.Programmatic;

import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript;
import org.isisaddons.module.fakedata.dom.FakeDataService;
import org.isisaddons.module.fakedata.fixture.demoapp.demomodule.dom.FakeDataDemoObjectWithAll;
import org.isisaddons.module.fakedata.fixture.demoapp.demomodule.dom.FakeDataDemoObjectWithAllMenu;

import lombok.Getter;
import lombok.Setter;

@lombok.experimental.Accessors(chain = true)
public class FakeDataDemoObjectWithAll_create_withFakeData extends FixtureScript {

    @Getter(onMethod = @__( @Programmatic )) @Setter
    private Boolean withFakeData;

    @Getter(onMethod = @__( @Programmatic )) @Setter
    private String name;

    @Getter(onMethod = @__( @Programmatic )) @Setter
    private Boolean someBoolean;

    @Getter(onMethod = @__( @Programmatic )) @Setter
    private Character someChar;

    @Getter(onMethod = @__( @Programmatic )) @Setter
    private Byte someByte;

    @Getter(onMethod = @__( @Programmatic )) @Setter
    private Short someShort;

    @Getter(onMethod = @__( @Programmatic )) @Setter
    private Integer someInt;

    @Getter(onMethod = @__( @Programmatic )) @Setter
    private Long someLong;

    @Getter(onMethod = @__( @Programmatic )) @Setter
    private Float someFloat;

    @Getter(onMethod = @__( @Programmatic )) @Setter
    private Double someDouble;

    @Getter
    private FakeDataDemoObjectWithAll fakeDataDemoObject;

    @Override
    protected void execute(final ExecutionContext executionContext) {

        // defaults
        this.defaultParam("withFakeData", executionContext, true);

        this.defaultParam("name", executionContext, fakeDataService.name().firstName());

        this.defaultParam("someBoolean", executionContext, withFakeData ? fakeDataService.booleans().any() : false);
        this.defaultParam("someChar", executionContext, withFakeData ? fakeDataService.chars().any() : (char)0);
        this.defaultParam("someByte", executionContext,   withFakeData ? fakeDataService.bytes().any(): (byte)0);
        this.defaultParam("someShort", executionContext,  withFakeData ? fakeDataService.shorts().any(): (short)0);
        this.defaultParam("someInt", executionContext,    withFakeData ? fakeDataService.ints().any(): 0);
        this.defaultParam("someLong", executionContext,   withFakeData ? fakeDataService.longs().any(): 0L);
        this.defaultParam("someFloat", executionContext,  withFakeData ? fakeDataService.floats().any(): 0.0f);
        this.defaultParam("someDouble", executionContext, withFakeData ? fakeDataService.doubles().any(): 0.0);

        // create
        this.fakeDataDemoObject =
                wrap(demoObjectWithAllMenu).createDemoObjectWithAll(getName(), getSomeBoolean(), getSomeChar(), getSomeByte(), getSomeShort(), getSomeInt(), getSomeLong(), getSomeFloat(), getSomeDouble());

        executionContext.addResult(this, fakeDataDemoObject);
    }

    @javax.inject.Inject
    FakeDataDemoObjectWithAllMenu demoObjectWithAllMenu;

    @javax.inject.Inject
    FakeDataService fakeDataService;
}
