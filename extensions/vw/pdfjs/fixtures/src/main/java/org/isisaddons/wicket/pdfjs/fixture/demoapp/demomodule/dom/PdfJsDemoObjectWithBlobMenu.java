package org.isisaddons.wicket.pdfjs.fixture.demoapp.demomodule.dom;

import java.util.List;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.repository.RepositoryService;

@DomainService(
        nature = NatureOfService.VIEW_MENU_ONLY,
        objectType = "wktPdfJsFixture.PdfJsDemoObjectWithBlobMenu"
)
@DomainServiceLayout(
        named = "Demo",
        menuOrder = "10.4"
)
public class PdfJsDemoObjectWithBlobMenu {


    //region > listAll (action)

    @Action(
            semantics = SemanticsOf.SAFE
    )
    @ActionLayout(
            bookmarking = BookmarkPolicy.AS_ROOT
    )
    @MemberOrder(sequence = "1")
    public List<PdfJsDemoObjectWithBlob> listAllDemoObjectsWithBlob() {
        return repositoryService.allInstances(PdfJsDemoObjectWithBlob.class);
    }

    //endregion

    //region > create (action)

    @MemberOrder(sequence = "2")
    public PdfJsDemoObjectWithBlob createDemoObjectWithBlob(
            @ParameterLayout(named = "Name")
            final String name) {
        final PdfJsDemoObjectWithBlob obj = repositoryService.instantiate(PdfJsDemoObjectWithBlob.class);
        obj.setName(name);
        repositoryService.persist(obj);
        return obj;
    }

    //endregion

    @javax.inject.Inject
    RepositoryService repositoryService;

}
