package org.apache.isis.examples.embeddedinspring.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import org.apache.isis.examples.embeddedinspring.dao.PersonDao;
import org.apache.isis.examples.embeddedinspring.model.Person;
import org.apache.isis.runtimes.embedded.IsisMetaModel;

@Controller
@RequestMapping("/person/")
public class PersonController {

    private static final Logger logger = LoggerFactory.getLogger(PersonController.class);

    @Autowired
    private PersonDao personDao;

    @Autowired
    private IsisMetaModel isisMetaModel;

    @RequestMapping(method = RequestMethod.GET, value = "edit")
    public ModelAndView editPerson(@RequestParam(value = "id", required = false) final Long id) {

        logger.debug("Received request to edit person id : " + id);
        final ModelAndView mav = new ModelAndView();
        mav.setViewName("edit");
        Person person = null;
        if (id == null) {
            person = new Person();
        } else {
            person = personDao.find(id);
        }

        mav.addObject("person", person);
        return mav;

    }

    @RequestMapping(method = RequestMethod.POST, value = "edit")
    public String savePerson(@ModelAttribute final Person person) {
        logger.debug("Received postback on person " + person);
        personDao.save(person);
        return "redirect:list";

    }

    @RequestMapping(method = RequestMethod.GET, value = "list")
    public ModelAndView listPeople() {
        logger.debug("Received request to list persons");
        final ModelAndView mav = new ModelAndView();
        final List<Person> people = personDao.listPeople();
        logger.debug("Person Listing count = " + people.size());
        mav.addObject("people", people);
        mav.setViewName("list");
        return mav;

    }

}
