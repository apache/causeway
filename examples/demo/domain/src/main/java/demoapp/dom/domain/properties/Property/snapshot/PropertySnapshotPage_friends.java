package demoapp.dom.domain.properties.Property.snapshot;

import java.util.List;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;

import lombok.RequiredArgsConstructor;

import demoapp.dom._infra.values.ValueHolderRepository;

@Collection()
@CollectionLayout()
@RequiredArgsConstructor
public class PropertySnapshotPage_friends {

    @SuppressWarnings("unused")
    private final PropertySnapshotPage page;

    @MemberSupport
    public List<? extends PropertySnapshot> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends PropertySnapshot> objectRepository;

}
