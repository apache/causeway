package org.apache.isis.testdomain.jdo;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.repository.RepositoryService;

@DomainService(
		nature = NatureOfService.VIEW_REST_ONLY,
		objectType = "testdomain.InventoryRepository")
public class InventoryRepository {

	@Action
	public List<Product> listProducts() {
		return repository.allInstances(Product.class);
	}
	
	@Action
	public List<Book> listBooks() {
		return repository.allInstances(Book.class);
	}
	
	@Action
	public Book recommendedBookOfTheWeek() {
		return Book.of("Book of the week", "An awesome Book", 12, "Author", "ISBN", "Publisher");
	}
	
    // -- DEPENDENCIES
    
    @Inject RepositoryService repository;
	
}
