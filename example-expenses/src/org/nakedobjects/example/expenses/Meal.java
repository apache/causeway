package org.nakedobjects.example.expenses;

import org.nakedobjects.application.value.Money;
import org.nakedobjects.application.value.TextString;

import java.util.Vector;


public class Meal extends Expense {
   private Location restaurant;
   private final Vector colleagues;
   private final TextString clients;
   private final Money food;
   private final Money tip;

    public static String fieldOrder() {
        return "date, restaurant, description, food, tip, total, colleagues, clients, project, receipt, claim, status";
    }

   public Meal() {
      super();
      colleagues = new Vector();
      clients = new TextString();
      food = new Money();
      tip = new Money();
   }

   public Money deriveTotal() {
      Money total = new Money();
      total.add(food);
      total.add(tip);

      return total;
   }

   public void copyDetails(Expense copy) {
      ((Meal) copy).restaurant = restaurant;
   }

   public TextString getClients() {
      return clients;
   }

   public void addToColleagues(Employee colleague) {
       colleagues.addElement(colleague);
   }

   public void removeFromColleagues(Employee colleague) {
       colleagues.removeElement(colleague);
   }
   
   public Vector getColleagues() {
      return colleagues;
   }

   public Money getFood() {
      return food;
   }

   public Location getRestaurant() {
   	resolve(restaurant);
      return restaurant;
   }

   public Money getTip() {
      return tip;
   }

   public void setRestaurant(Location restaurant) {
      this.restaurant = restaurant;
      objectChanged();
   }
}