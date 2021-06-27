package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import lombok.Getter;

@Getter
public class LongWrapperHolder extends HolderAbstract<LongWrapperHolder> {
    private Long someLongWrapper;

    public void setSomeLongWrapper(Long someLongWrapper) {
        bump();
        this.someLongWrapper = broken ? 0L : someLongWrapper;
    }
}
