package org.nakedobjects.example.musicagent;

import org.nakedobjects.example.exploration.DefaultExploration;
import org.nakedobjects.object.defaults.AbstractUserContext;


public class MusicAgentExploration extends DefaultExploration {
    public static void main(String[] args) {
        new MusicAgentExploration();
    }

    protected AbstractUserContext applicationContext() {
        return new Context();
        
        // THE FOLLOWING FAILS
/*        return new AbstractUserContext() {
            public void created() {
                addClass(Performance.class);
       //         addClass(Musician.class);
        //        addClass(Part.class);
        //        addClass(Instrument.class);
            }
        };
    */}

    protected void setUpFixtures() {
        addFixture(new Fixture());    
    }
}

