import org.nakedobjects.example.exploration.JavaExploration;


public class Exploration {
    public static void main(String[] args) {
       JavaExploration e =  new JavaExploration();
 
       e.registerClass(Room.class);
       e.registerClass(Suspect.class);
       e.registerClass(Weapon.class);
       e.registerClass(Assertion.class);
       
       Suspect s = (Suspect) e.createInstance(Suspect.class);
       s.getName().setValue("Mrs Peacock");
       
       Weapon w = (Weapon) e.createInstance(Weapon.class);
       w.getName().setValue("Revolver");

       e.display();
    }
}

