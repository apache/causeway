package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus;

import static org.hamcrest.CoreMatchers.*;
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

public class Persistence_persist {

    private PrimitiveValuedEntityRepository repo = new PrimitiveValuedEntityRepository();
    
    @Rule
    public IsisSystemWithFixtures iswf = Utils.systemBuilder()
        .with(Utils.listenerToDeleteFrom("PRIMITIVEVALUEDENTITY"))
        .withServices(repo)
        .build();

    @Test
    public void persistTwo() throws Exception {
        iswf.beginTran();
        repo.newEntity().setId(1);
        repo.newEntity().setId(2);
        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        List<PrimitiveValuedEntity> list = repo.list();
        assertThat(list.size(), is(2));
        iswf.commitTran();
    }

    @Test
    public void persistAllValues() throws Exception {
        iswf.beginTran();
        PrimitiveValuedEntity entity = repo.newEntity();
        entity.setId(1);
        entity.setBooleanProperty(true);
        entity.setByteProperty((byte)123);
        entity.setDoubleProperty(9876543210987.0);
        entity.setFloatProperty(123456.0f);
        entity.setIntProperty(456);
        entity.setLongProperty(12345678901L);
        entity.setShortProperty((short)4567);
        entity.setCharProperty('X');
        
        iswf.commitTran();

        iswf.bounceSystem();
        
        iswf.beginTran();
        PrimitiveValuedEntity entityRetrieved = repo.list().get(0);
        assertThat(entityRetrieved.getBooleanProperty(), is(true));
        assertThat(entityRetrieved.getByteProperty(), is((byte)123));
        assertThat(entityRetrieved.getDoubleProperty(), is(9876543210987.0));
        assertThat(entityRetrieved.getFloatProperty(), is(123456.0f));
        assertThat(entityRetrieved.getIntProperty(), is(456));
        assertThat(entityRetrieved.getLongProperty(), is(12345678901L));
        assertThat(entityRetrieved.getShortProperty(), is((short)4567));
        assertThat(entityRetrieved.getCharProperty(), is('X'));
        
        iswf.commitTran();
    }

    @Test
    public void adapterResolveState_isResolved() throws Exception {
        
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
        assertThat(adapter.getResolveState(), is(ResolveState.RESOLVED));
        assertThat(adapter.isTransient(), is(false));
        assertThat(adapter.getOid().enString(), is("PRMV:1"));

        iswf.commitTran();
    }

    

}
