package org.apache.isis.tck.dom.poly;


public class EmptyEntityWithOwnProperty implements Empty {

    // {{ Special: string
    private String special;

    public String getSpecial() {
        return special;
    }

    public void setSpecial(final String special) {
        this.special = special;
    }
    // }}
    
}