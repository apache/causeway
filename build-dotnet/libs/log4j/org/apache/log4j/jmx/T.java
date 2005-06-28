

import org.apache.log4j.jmx.Agent;
import org.apache.log4j.Category;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.*;

public class T {


  public static void main(String[] args) {
    Category cat = Category.getInstance(T.class);
    Layout layout = new PatternLayout("%r %p [%t] %c - %m%n");
    ConsoleAppender consoleAppender = new ConsoleAppender(layout);
							  
    consoleAppender.setName("console");
    BasicConfigurator.configure(consoleAppender);
    Agent agent = new Agent();
    agent.start();
  }
  
}
