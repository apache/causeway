package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import lombok.Getter;

@Getter
public class HolderAbstract<H> {
    public int counter = 0;

    public void bump() {
        counter++;
    }

    boolean broken = false;

    public H butBroken() {
        broken = true;
        return (H) this;
    }

}
