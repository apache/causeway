package demoapp.dom.domain.objects.DomainObject.autoComplete;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.objects.DomainObject.aliased.DomainObjectAliased;
import lombok.RequiredArgsConstructor;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.*;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;

@Collection()
@CollectionLayout()
@RequiredArgsConstructor
public class DomainObjectAutoCompleteVm_objects {

    @SuppressWarnings("unused")
    private final DomainObjectAutoCompleteVm mixee;

    @MemberSupport
    public List<? extends DomainObjectAutoComplete> coll() {
        return objectRepository.all();
    }

    @Inject
    ValueHolderRepository<String, ? extends DomainObjectAutoComplete> objectRepository;

}
