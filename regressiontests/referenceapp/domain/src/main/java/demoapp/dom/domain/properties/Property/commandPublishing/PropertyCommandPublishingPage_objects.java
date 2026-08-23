package demoapp.dom.domain.properties.Property.commandPublishing;

import java.util.List;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.MemberSupport;

import demoapp.dom._infra.values.ValueHolderRepository;
import lombok.RequiredArgsConstructor;

//tag::class[]
@Collection()
@RequiredArgsConstructor
public class PropertyCommandPublishingPage_objects {

    @SuppressWarnings("unused")
    private final PropertyCommandPublishingPage page;

    @MemberSupport public List<? extends PropertyCommandPublishingEntity> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends PropertyCommandPublishingEntity> objectRepository;
}
//end::class[]
