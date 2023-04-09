package demoapp.dom.domain.properties.Property.commandPublishing;

import demoapp.dom._infra.values.ValueHolderRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.MemberSupport;

//tag::class[]
@Collection()
@RequiredArgsConstructor
public class PropertyCommandPublishingPage_objects {

    @SuppressWarnings("unused")
    private final PropertyCommandPublishingPage page;

    @MemberSupport public List<? extends PropertyCommandPublishing> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends PropertyCommandPublishing> objectRepository;
}
//end::class[]
