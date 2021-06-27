package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import lombok.Getter;

@Getter
public class
BooleanWrapperHolder extends HolderAbstract<BooleanWrapperHolder> {
    private Boolean someBooleanWrapper;

    public void setSomeBooleanWrapper(Boolean someBooleanWrapper) {
        bump();
        this.someBooleanWrapper = broken ? false : someBooleanWrapper;
    }
}
