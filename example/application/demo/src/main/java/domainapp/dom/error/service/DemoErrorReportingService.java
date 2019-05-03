package domainapp.dom.error.service;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.error.ErrorDetails;
import org.apache.isis.applib.services.error.ErrorReportingService;
import org.apache.isis.applib.services.error.Ticket;
import org.apache.isis.applib.services.error.Ticket.StackTracePolicy;
import org.apache.isis.core.runtime.services.error.EmailTicket;
import org.apache.isis.core.runtime.services.error.EmailTicket.MailTo;

import lombok.val;

@DomainService(nature = NatureOfService.DOMAIN)
public class DemoErrorReportingService implements ErrorReportingService {
	
	@Override
	public Ticket reportError(ErrorDetails errorDetails) {

		String reference = "#0";
		String userMessage = errorDetails.getMainMessage();
		String details = "Apologies!";
		
		val mailTo = MailTo.builder()
		.receiver("support@hello.world")
		.subject("[Simple-Module] Unexpected Error ("+reference+")")
		.body(MailTo.mailBodyOf(errorDetails))
		.build();
		
		StackTracePolicy policy = StackTracePolicy.SHOW;
		val ticket = new EmailTicket(mailTo, reference, userMessage, details, 
               policy,
               "http://www.randomkittengenerator.com/cats/rotator.php");
		
		return ticket;
	}
	
	
}
