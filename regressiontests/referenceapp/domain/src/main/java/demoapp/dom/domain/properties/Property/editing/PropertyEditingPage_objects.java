package demoapp.dom.domain.properties.Property.editing;

import java.util.List;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;

import demoapp.dom._infra.values.ValueHolderRepository;
import lombok.RequiredArgsConstructor;

@Collection()
@CollectionLayout()
@RequiredArgsConstructor
public class PropertyEditingPage_objects {

    @SuppressWarnings("unused")
    private final PropertyEditingPage page;

    @MemberSupport
    public List<? extends PropertyEditingEntity> coll() {
        return objectRepository.all();
    }

    @Inject
    ValueHolderRepository<String, ? extends PropertyEditingEntity> objectRepository;

}
