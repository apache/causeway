package org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;
import org.apache.isis.tck.dom.scalars.PrimitiveValuedEntity;
import org.apache.isis.tck.dom.scalars.PrimitiveValuedEntityRepository;

public class OpenJpaPersistenceMechanismInstallerTest_persist {

    private PrimitiveValuedEntityRepository repo = new PrimitiveValuedEntityRepository();
    
    @Rule
    public IsisSystemWithFixtures iswf = Utils.systemBuilder()
        .with(Utils.listenerToDeleteFrom("PRIMITIVEVALUEDENTITY"))
        .withServices(repo)
        .build();

    @Test
    public void emptyList() {
        iswf.beginTran();
        final List<PrimitiveValuedEntity> list = repo.list();
        assertThat(list.size(), is(0));
        iswf.commitTran();
    }

    @Test
    public void retrieveWithoutBouncing() throws Exception {
        iswf.beginTran();
        PrimitiveValuedEntity entity = repo.newEntity();
        entity.setId(1);
        entity = repo.newEntity();
        entity.setId(2);
        iswf.commitTran();

        // don't bounce
        iswf.beginTran();
        List<PrimitiveValuedEntity> list = repo.list();
        assertThat(list.size(), is(2));
        iswf.commitTran();
    }

    @Test
    public void retrieveAfterBouncingSystem() throws Exception {
        iswf.beginTran();
        PrimitiveValuedEntity entity = repo.newEntity();
        entity.setId(1);
        entity = repo.newEntity();
        entity.setId(2);
        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        List<PrimitiveValuedEntity> list = repo.list();
        assertThat(list.size(), is(2));
        iswf.commitTran();
    }

    @Test
    public void adapterResolveStateChanges() throws Exception {
        iswf.beginTran();
        PrimitiveValuedEntity entity = repo.newEntity();
        ObjectAdapter adapter = iswf.adapterFor(entity);
        
        assertThat(adapter.isTransient(), is(true));
        assertThat(adapter.getResolveState(), is(ResolveState.TRANSIENT));
        assertThat(adapter.getOid().isTransient(), is(true));
        
        entity.setId(1);
        iswf.commitTran();
        
        iswf.bounceSystem();
        
        iswf.beginTran();
        final List<PrimitiveValuedEntity> list = repo.list();
        assertThat(list.size(), is(1));
        
        adapter = iswf.adapterFor(list.get(0));
        assertThat(adapter.getResolveState(), is(ResolveState.GHOST));
        assertThat(adapter.isTransient(), is(false));
        assertThat(adapter.getOid().enString(), is("PRMV:1"));

        iswf.commitTran();
    }

}
