package org.nakedobjects.example.library;

import org.nakedobjects.system.JavaExploration;


public class LibraryExploration{

    public static void main(String[] args) {
        JavaExploration e = new JavaExploration();
        
        e.registerClass(Member.class);
        e.registerClass(Book.class);
        e.registerClass(Loan.class);
        
        e.display();
    }

}