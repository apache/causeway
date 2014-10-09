package org.apache.isis.applib.services.command;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.applib.services.eventbus.ActionInteractionEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public abstract class Command2ContractTestAbstract {

    Command2 command;

    ActionInteractionEvent<?> ev1;
    ActionInteractionEvent<?> ev2;

    @Before
    public void setUp() throws Exception {

        final Object source = new Object();
        ev1 = new ActionInteractionEvent.Default(source, null);
        ev2 = new ActionInteractionEvent.Default(source, null);
        command = newCommand();
    }

    protected abstract Command2 newCommand();

    @Test
    public void givenEmpty() throws Exception {
        // then
        assertNull(command.peekActionInteractionEvent());
        assertNull(command.popActionInteractionEvent());
    }

    @Test
    public void givenOne() throws Exception {
        // given
        command.pushActionInteractionEvent(ev1);

        // then
        assertEquals(ev1, command.peekActionInteractionEvent());

        // and when
        assertEquals(ev1, command.popActionInteractionEvent());

        // then
        assertNull(command.peekActionInteractionEvent());
    }

    @Test
    public void givenTwo() throws Exception {
        // given
        command.pushActionInteractionEvent(ev1);
        command.pushActionInteractionEvent(ev2);

        // then
        assertEquals(ev2, command.peekActionInteractionEvent());

        // and when
        assertEquals(ev2, command.popActionInteractionEvent());

        // then
        assertEquals(ev1, command.peekActionInteractionEvent());

        // and when
        assertEquals(ev1, command.popActionInteractionEvent());

        // then
        assertNull(command.peekActionInteractionEvent());
    }

    @Test
    public void pushSame() throws Exception {

        // given
        command.pushActionInteractionEvent(ev1);
        command.pushActionInteractionEvent(ev1);

        // then
        assertEquals(ev1, command.peekActionInteractionEvent());

        // and when
        assertEquals(ev1, command.popActionInteractionEvent());

        // then
        assertNull(command.peekActionInteractionEvent());
    }


    @Test
    public void clear() throws Exception {

        // given
        command.pushActionInteractionEvent(ev1);
        command.pushActionInteractionEvent(ev2);

        // then
        assertEquals(ev2, command.peekActionInteractionEvent());

        // and when
        final List<ActionInteractionEvent<?>> events = command.flushActionInteractionEvents();

        // then
        assertEquals(ev1, events.get(0));
        assertEquals(ev2, events.get(1));
        assertNull(command.peekActionInteractionEvent());

    }



}