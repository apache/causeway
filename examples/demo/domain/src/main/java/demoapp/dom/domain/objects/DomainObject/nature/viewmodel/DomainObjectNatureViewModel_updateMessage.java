package demoapp.dom.domain.objects.DomainObject.nature.viewmodel;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.objects.DomainObject.nature.DomainObjectNaturePage;
import demoapp.dom.domain.objects.DomainObject.nature.entity.DomainObjectNatureEntity;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.DomainObject;

//tag::class[]
@Action()
@RequiredArgsConstructor
public class DomainObjectNatureViewModel_updateMessage {
    // ...
//end::class[]
    @SuppressWarnings("unused")
    private final DomainObjectNatureViewModel mixee;

//tag::class[]
    public DomainObjectNatureViewModel act(@Nullable String newMessage) {   // <.>
        mixee.setMessage(newMessage);
        return mixee;
    }
    public String default0Act() {
        return mixee.getMessage();
    }
}
//end::class[]
