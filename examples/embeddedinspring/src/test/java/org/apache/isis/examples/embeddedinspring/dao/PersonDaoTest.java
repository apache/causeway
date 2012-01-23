package org.apache.isis.examples.embeddedinspring.dao;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import org.apache.isis.examples.embeddedinspring.controller.DataInitializer;
import org.apache.isis.examples.embeddedinspring.model.Person;

@ContextConfiguration({ "/test-context.xml", "/test-isis.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class PersonDaoTest {

    @Autowired
    private PersonDao personDao;

    @Autowired
    private DataInitializer dataInitializer;

    @Before
    public void prepareData() {
        dataInitializer.initData();
    }

    @Test
    public void shouldSaveAPerson() {
        final Person p = new Person();
        p.setFirstName("Andy");
        p.setLastName("Gibson");
        personDao.save(p);
        final Long id = p.getId();
        Assert.assertNotNull(id);
    }

    @Test
    public void shouldLoadAPerson() {
        final Long template = dataInitializer.people.get(0);
        final Person p = personDao.find(template);

        Assert.assertNotNull("Person not found!", p);
        Assert.assertEquals(template, p.getId());
    }

    public void shouldListPeople() {
        final List<Person> people = personDao.listPeople();
        Assert.assertEquals(DataInitializer.PERSON_COUNT, people.size());

    }

}
