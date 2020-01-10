package org.apache.isis.testing.fakedata.fixtures.demoapp.demomodule.dom;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.repository.RepositoryService;

import lombok.val;

@DomainService(
        nature = NatureOfService.VIEW,
        objectType = "libFakeDataFixture.FakeDataDemoObjectWithAllMenu"
)
@DomainServiceLayout(named = "Demo")
public class FakeDataDemoObjectWithAllMenu {


    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(bookmarking = BookmarkPolicy.AS_ROOT)
    @MemberOrder(sequence = "1")
    public List<FakeDataDemoObjectWithAll> listAllDemoObjectsWithAll() {
        return repositoryService.allInstances(FakeDataDemoObjectWithAll.class);
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
        val obj = new FakeDataDemoObjectWithAll(name);
        obj.setSomeBoolean(someBoolean);
        obj.setSomeChar(someChar);
        obj.setSomeByte(someByte);
        obj.setSomeShort(someShort);
        obj.setSomeInt(someInt);
        obj.setSomeLong(someLong);
        obj.setSomeFloat(someFloat);
        obj.setSomeDouble(someDouble);
        repositoryService.persist(obj);
        return obj;
    }

    @Inject RepositoryService repositoryService;

}
