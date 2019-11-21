package org.isisaddons.module.fakedata.fixture.demoapp.demomodule.dom;

import java.util.List;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;

@DomainService(
        nature = NatureOfService.VIEW_MENU_ONLY,
        objectType = "libFakeDataFixture.FakeDataDemoObjectWithAllMenu"
)
@DomainServiceLayout(
        named = "Demo",
        menuOrder = "10.2"
)
public class FakeDataDemoObjectWithAllMenu {


    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(bookmarking = BookmarkPolicy.AS_ROOT)
    @MemberOrder(sequence = "1")
    public List<FakeDataDemoObjectWithAll> listAllDemoObjectsWithAll() {
        return container.allInstances(FakeDataDemoObjectWithAll.class);
    }


    @MemberOrder(sequence = "2")
    public FakeDataDemoObjectWithAll createDemoObjectWithAll(
            final String name,
            final boolean someBoolean,
            final char someChar,
            final byte someByte,
            final short someShort,
            final int someInt,
            final long someLong,
            final float someFloat,
            final double someDouble) {
        final FakeDataDemoObjectWithAll obj = container.newTransientInstance(FakeDataDemoObjectWithAll.class);
        obj.setName(name);
        obj.setSomeBoolean(someBoolean);
        obj.setSomeChar(someChar);
        obj.setSomeByte(someByte);
        obj.setSomeShort(someShort);
        obj.setSomeInt(someInt);
        obj.setSomeLong(someLong);
        obj.setSomeFloat(someFloat);
        obj.setSomeDouble(someDouble);
        container.persistIfNotAlready(obj);
        return obj;
    }


    @javax.inject.Inject 
    DomainObjectContainer container;

}
