package demoapp.dom.domain.properties.Property.hidden;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.MemberSupport;

import lombok.RequiredArgsConstructor;

import demoapp.dom._infra.values.ValueHolderRepository;

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
