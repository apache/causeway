package org.nakedobjects.example.musicagent;

import org.nakedobjects.system.JavaExploration;



public class MusicAgentExploration {
    public static void main(String[] args) {
       JavaExploration e =  new JavaExploration();
 
       e.registerClass(Performance.class);
       e.registerClass(Musician.class);
       e.registerClass(Part.class);
       e.registerClass(Instrument.class);
       
       Instrument violin = (Instrument) e.createInstance(Instrument.class);
       violin.getName().setValue("Violin");
       
       Musician m = (Musician) e.createInstance(Musician.class);
       m.getName().setValue("Andre Snitch");
       m.addToInstruments(violin);
       
       e.createInstance(Performance.class);

       e.display();
    }
}

