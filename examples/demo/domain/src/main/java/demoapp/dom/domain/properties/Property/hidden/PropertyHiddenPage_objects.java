package demoapp.dom.domain.properties.Property.hidden;

import demoapp.dom._infra.values.ValueHolderRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.MemberSupport;

//tag::class[]
@Collection()
@RequiredArgsConstructor
public class PropertyHiddenPage_objects {

    @SuppressWarnings("unused")
    private final PropertyHiddenPage page;

    @MemberSupport public List<? extends PropertyHidden> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends PropertyHidden> objectRepository;
}
//end::class[]
