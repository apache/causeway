package org.nakedobjects.example.library;

import org.nakedobjects.example.exploration.DefaultExploration;
import org.nakedobjects.object.defaults.AbstractUserContext;
import org.nakedobjects.object.exploration.AbstractExplorationFixture;


public class LibraryExploration extends DefaultExploration {

    protected void setUpFixtures() {
        addFixture(new AbstractExplorationFixture() {
            public void install() {
                registerClass(Member.class);
                registerClass(Book.class);
                registerClass(Loan.class);
            }
        });
    }

    public static void main(String[] args) {
        new LibraryExploration();
    }

    protected AbstractUserContext applicationContext() {
        return null;
    }
}