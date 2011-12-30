package org.apache.isis.examples.embeddedinspring.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.isis.examples.embeddedinspring.model.Person;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

@ContextConfiguration("/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class PersonControllerTest {
	
	@Autowired
	private DataInitializer dataInitializer;
	
	@Autowired
	private PersonController personController;
		
	@Before
	public void before() {
		dataInitializer.initData();
	}
	
	@Test
	public void shouldReturnPersonListView() {
		ModelAndView mav = personController.listPeople();
		assertEquals("list",mav.getViewName());
		
		@SuppressWarnings("unchecked")
		List<Person> people = (List<Person>) mav.getModelMap().get("people");
		assertNotNull(people);		
		assertEquals(DataInitializer.PERSON_COUNT,people.size());		
	}
	
	

	public void shouldReturnNewPersonWithEditMav() {
		ModelAndView mav = personController.editPerson(null);
		assertNotNull(mav);
		assertEquals("edit", mav.getViewName());
		Object object = mav.getModel().get("person");
		assertTrue(Person.class.isAssignableFrom(object.getClass()));
		Person person = (Person) object;
		assertNull(person.getId());
		assertNull(person.getFirstName());
		assertNull(person.getLastName());		
	}
	
	@Test
	public void shouldReturnSecondPersonWithEditMav() {
		Long template = dataInitializer.people.get(1);
		ModelAndView mav = personController.editPerson(template);
		assertNotNull(mav);
		assertEquals("edit", mav.getViewName());
		Object object = mav.getModel().get("person");
		assertTrue(Person.class.isAssignableFrom(object.getClass()));
		Person person = (Person) object;
		assertEquals(template,person.getId());
	}
	
}
