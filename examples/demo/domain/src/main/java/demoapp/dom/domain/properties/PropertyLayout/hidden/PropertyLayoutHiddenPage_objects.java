package demoapp.dom.domain.properties.PropertyLayout.hidden;

import demoapp.dom._infra.values.ValueHolderRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.MemberSupport;

//tag::class[]
@Collection()
@RequiredArgsConstructor
public class PropertyLayoutHiddenPage_objects {

    @SuppressWarnings("unused")
    private final PropertyLayoutHiddenPage page;

    @MemberSupport public List<? extends PropertyLayoutHidden> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends PropertyLayoutHidden> objectRepository;
}
//end::class[]
