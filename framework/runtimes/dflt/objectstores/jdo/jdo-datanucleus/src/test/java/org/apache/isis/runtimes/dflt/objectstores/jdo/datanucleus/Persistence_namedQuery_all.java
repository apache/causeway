package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.runtimes.dflt.testsupport.IsisSystemWithFixtures;
import org.apache.isis.tck.dom.scalars.PrimitiveValuedEntity;
import org.apache.isis.tck.dom.scalars.PrimitiveValuedEntityRepository;

public class Persistence_namedQuery_all {

    private PrimitiveValuedEntityRepository repo = new PrimitiveValuedEntityRepository();
    
    @Rule
    public IsisSystemWithFixtures iswf = Utils.systemBuilder()
        .with(Utils.listenerToDeleteFrom("PRIMITIVEVALUEDENTITY"))
        .withServices(repo)
        .build();

    @Before
    public void setUp() throws Exception {

        iswf.beginTran();

        PrimitiveValuedEntity entity = repo.newEntity();
        entity.setId(1);
        entity.setIntProperty(111);

        entity = repo.newEntity();
        entity.setId(2);
        entity.setIntProperty(222);

        entity = repo.newEntity();
        entity.setId(3);
        entity.setIntProperty(333);

        entity = repo.newEntity();
        entity.setId(4);
        entity.setIntProperty(111);

        iswf.commitTran();
    }
    
    @Test
    public void whenOne() throws Exception {
        
        iswf.beginTran();

        List<PrimitiveValuedEntity> entities = repo.findByNamedQueryAll("pve_findByIntProperty", ImmutableMap.of("i", (Object)222));
        assertThat(entities, is(not(nullValue())));
        assertThat(entities.size(), is(1));

        iswf.commitTran();
    }

    @Test
    public void whenTwo() throws Exception {
        
        iswf.beginTran();

        List<PrimitiveValuedEntity> entities = repo.findByNamedQueryAll("pve_findByIntProperty", ImmutableMap.of("i", (Object)111));
        assertThat(entities, is(not(nullValue())));
        assertThat(entities.size(), is(2));

        iswf.commitTran();
    }

    @Test
    public void whenNone() throws Exception {
        
        iswf.beginTran();

        List<PrimitiveValuedEntity> entities = repo.findByNamedQueryAll("pve_findByIntProperty", ImmutableMap.of("i", (Object)999));
        assertThat(entities, is(not(nullValue())));
        assertThat(entities.size(), is(0));

        iswf.commitTran();
    }
}
