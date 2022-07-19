package org.apache.isis.extensions.executionoutbox.applib.integtest.model;

import javax.inject.Named;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Publishing;

@Named("executionoutbox.test.Counter")
@DomainObject(nature = Nature.ENTITY)
public abstract class Counter implements Comparable<Counter> {

    @Property(
            editing = Editing.ENABLED,
            executionPublishing = Publishing.ENABLED
    )
    public abstract Long getNum();
    public abstract void setNum(Long num);

    @Property(
            editing = Editing.ENABLED,
            executionPublishing = Publishing.DISABLED)
    public abstract Long getNum2();
    public abstract void setNum2(Long num2);

    public abstract String getName();
    public abstract void setName(String name);

    @Action(executionPublishing = Publishing.ENABLED)
    public Counter bumpUsingDeclaredAction() {
        return doBump();
    }

    @Action(executionPublishing = Publishing.DISABLED)
    public Counter bumpUsingDeclaredActionWithExecutionPublishingDisabled() {
        return doBump();
    }

    Counter doBump() {
        if (getNum() == null) {
            setNum(1L);
        } else {
            setNum(getNum() + 1);
        }
        return this;
    }

    @Override
    public int compareTo(final Counter o) {
        return this.getName().compareTo(o.getName());
    }
}
