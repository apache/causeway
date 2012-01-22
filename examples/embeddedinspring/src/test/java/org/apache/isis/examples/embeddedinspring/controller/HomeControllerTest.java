package org.apache.isis.examples.embeddedinspring.controller;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

public class HomeControllerTest {

    @Test
    public void testController() {
        final HomeController controller = new HomeController();
        final Model model = new ExtendedModelMap();
        Assert.assertEquals("home", controller.home(model));

        final Object message = model.asMap().get("controllerMessage");
        Assert.assertEquals("This is the message from the controller!", message);
    }
}
