package org.nakedobjects.example.library;

import org.nakedobjects.container.exploration.Exploration;
import org.nakedobjects.object.exploration.AbstractExplorationFixture;


public class LibraryExploration extends Exploration {

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
}