package org.apache.isis.examples.embeddedinspring.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import org.apache.isis.examples.embeddedinspring.model.Person;

@Repository("personRepo")
public class PersonDao {

    @PersistenceContext
    private EntityManager entityManager;

    public Person find(final Long id) {
        return entityManager.find(Person.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<Person> listPeople() {
        return entityManager.createQuery("select p from Person p").getResultList();
    }

    @Transactional
    public Person save(final Person person) {
        if (person.getId() == null) {
            entityManager.persist(person);
            return person;
        } else {
            return entityManager.merge(person);
        }
    }

}
