package demoapp.dom.domain.properties.Property.executionPublishing;

import java.util.List;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.MemberSupport;

import demoapp.dom._infra.values.ValueHolderRepository;
import lombok.RequiredArgsConstructor;

//tag::class[]
@Collection()
@RequiredArgsConstructor
public class PropertyExecutionPublishingPage_objects {

    @SuppressWarnings("unused")
    private final PropertyExecutionPublishingPage page;

    @MemberSupport public List<? extends PropertyExecutionPublishingEntity> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends PropertyExecutionPublishingEntity> objectRepository;
}
//end::class[]
