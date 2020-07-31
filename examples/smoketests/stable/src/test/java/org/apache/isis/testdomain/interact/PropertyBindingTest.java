package org.apache.isis.testdomain.interact;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.model.interaction.Configuration_usingInteractionDomain;
import org.apache.isis.testdomain.model.interaction.InteractionDemo;

import lombok.val;

@Smoketest
@SpringBootTest(
        classes = { 
                Configuration_headless.class,
                Configuration_usingInteractionDomain.class
        }, 
        properties = {
                "isis.core.meta-model.introspector.mode=FULL",
                "isis.applib.annotation.domain-object.editing=TRUE",
                "isis.core.meta-model.validator.explicit-object-type=FALSE", // does not override any of the imports
        })
@TestPropertySource({
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel
})
class PropertyBindingTest extends InteractionTestAbstract {

    SimulatedUiComponent uiPropA;
    
    @BeforeEach
    void setUpSimulatedUi() {

        val propertyInteraction = startPropertyInteractionOn(InteractionDemo.class, "stringMultiline", Where.OBJECT_FORMS);  

        assertTrue(propertyInteraction.getManagedProperty().isPresent(), "prop is expected to be editable");
        
        val managedProperty = propertyInteraction.getManagedProperty().get();
        
        // setting up and binding all the simulated UI components
        
        uiPropA = new SimulatedUiComponent();
        
        uiPropA.bind(managedProperty);
        
        //TODO verify that initial defaults are as expected
        
//        assertEquals(1, uiParamA.getValue().getPojo());
        
        //TODO verify that initial choices are as expected
        
//        assertComponentWiseUnwrappedEquals(NumberRange.POSITITVE.numbers(), uiParamA.getChoices());
//        assertComponentWiseUnwrappedEquals(NumberRange.NEGATIVE.numbers(), uiParamB.getChoices());
//        assertEmpty(uiParamC.getChoices()); // empty because the search argument is also empty
        
        //TODO verify that initial validation messages are all empty, 
        // because we don't validate anything until a user initiated submit attempt occurs 
        
//        assertEmpty(uiParamRangeA.getValidationMessage());
//        assertEmpty(uiParamRangeB.getValidationMessage());
//        assertEmpty(uiParamRangeC.getValidationMessage());
//        
//        assertEmpty(uiParamA.getValidationMessage());
//        assertEmpty(uiParamB.getValidationMessage());
//        assertEmpty(uiParamC.getValidationMessage());
//        
//        assertEmpty(uiSubmit.getValidationMessage());
        
        //TODO verify that validation feedback is not active
        
//        assertFalse(pendingArgs.getObservableValidationFeedbackActive().getValue());
        
    }

    
}
