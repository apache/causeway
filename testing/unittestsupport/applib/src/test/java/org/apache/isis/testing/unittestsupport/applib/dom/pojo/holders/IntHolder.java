package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import lombok.Getter;

@Getter
public class IntHolder extends HolderAbstract<IntHolder> {
    private int someInt;

    public void setSomeInt(int someInt) {
        bump();
        this.someInt = broken ? 0 : someInt;
    }
}
