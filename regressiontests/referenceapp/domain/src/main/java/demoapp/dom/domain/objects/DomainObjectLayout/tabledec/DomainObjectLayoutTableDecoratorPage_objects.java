package demoapp.dom.domain.objects.DomainObjectLayout.tabledec;

import java.util.List;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.MemberSupport;

import demoapp.dom._infra.values.ValueHolderRepository;
import lombok.RequiredArgsConstructor;

@Collection()
@RequiredArgsConstructor
public class DomainObjectLayoutTableDecoratorPage_objects {

    @SuppressWarnings("unused")
    private final DomainObjectLayoutTableDecoratorPage page;

    @MemberSupport
    public List<? extends DomainObjectLayoutTableDecoratorEntity> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends DomainObjectLayoutTableDecoratorEntity> objectRepository;

}
