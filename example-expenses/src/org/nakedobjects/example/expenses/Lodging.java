package org.nakedobjects.example.expenses;

import org.nakedobjects.application.valueholder.Money;
import org.nakedobjects.application.valueholder.WholeNumber;


public class Lodging extends Expense {
   private Location hotel;
   private final WholeNumber nights;
   private final Money room;
   private final Money food;
   private final Money telephone;
   private final Money other;

    public static String fieldOrder() {
        return "date, hotel, description, room, food, telephone, other, total, project, receipt, claim, status";
    }	

  public Lodging() {
      super();
      nights = new WholeNumber();
      room = new Money();
      food = new Money();
      telephone = new Money();
      other = new Money();
   }

   public void created() {
   	super.created();
      nights.setValue(1);
   }

   public Money deriveTotal() {
      Money total = new Money();
      total.add(room);
      total.add(food);
      total.add(telephone);
      total.add(other);

      return total;
   }

   public void copyDetails(Expense copy) {
      ((Lodging) copy).hotel = hotel;
   }

   public Money getFood() {
      return food;
   }

   public Location getHotel() {
   	resolve(hotel);
      return hotel;
   }

   public WholeNumber getNights() {
      return nights;
   }

   public Money getOther() {
      return other;
   }

   public Money getRoom() {
      return room;
   }

   public Money getTelephone() {
      return telephone;
   }

   public void setHotel(Location hotel) {
      this.hotel = hotel;
      objectChanged();
   }
}