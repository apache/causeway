package demoapp.dom.domain.properties.Property.snapshot;

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
public class PropertySnapshotPage_friends {

    @SuppressWarnings("unused")
    private final PropertySnapshotPage page;

    @MemberSupport
    public List<? extends PropertySnapshotEntity> coll() {
        return objectRepository.all();
    }

    @Inject ValueHolderRepository<String, ? extends PropertySnapshotEntity> objectRepository;

}
