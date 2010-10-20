package org.starobjects.restful.testapp.client;

import nu.xom.Document;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.log4j.BasicConfigurator;
import org.starobjects.restful.applib.AbstractRestfulClient;
import org.starobjects.restful.testapp.client.CmdLineUtil.Optionality;

import static org.starobjects.restful.applib.XomUtils.*;

public class FindEmployee extends AbstractRestfulClient {

	private static final String DEFAULT_HOST_URI = "http://localhost:7070";
	private static final String DEFAULT_EMPLOYEE_NAME = "Sam Jones";

	public static void main(String[] args) throws Exception {

		BasicConfigurator.configure();
		
		Options options = new Options();
		CmdLineUtil.addOption(options, 
				"h", "host-uri", true, "host URI, eg " + DEFAULT_HOST_URI, 
				Optionality.OPTIONAL);
		CmdLineUtil.addOption(options, 
				"n", "name", true, 
				"name of employee to search for, eg '" + DEFAULT_EMPLOYEE_NAME + "'", 
				Optionality.OPTIONAL);

		CommandLine parse = CmdLineUtil.parse(FindEmployee.class.getSimpleName(), options, args);
		String hostUri = parse.getOptionValue("h", DEFAULT_HOST_URI);
		String employeeName = parse.getOptionValue("n", DEFAULT_EMPLOYEE_NAME);

		FindEmployee client = new FindEmployee(hostUri);
		client.findEmployee(employeeName);
	}

	public FindEmployee(String hostUri) {
		super(hostUri);
	}

	private void findEmployee(String name) throws Exception {

		// list the services
		Document servicesDoc = get(
				combine(getHostUri(),"/services"));
		prettyPrint(servicesDoc);
		
		// extract the 'Employees' service... 
		String employeesServicePath = getAttributeValue(servicesDoc, 
				"//a[@class='nof-service'][text()='Employees']/@href", 
				"Unable to find 'Employees' service");
		
		// ... and invoke its findEmployees action
		Document employeeFindEmployeesDoc = post(
				combine(getHostUri(), employeesServicePath, 
						"/action/findEmployees(java.lang.String)"), 
				"arg0", name);
		prettyPrint(employeeFindEmployeesDoc);


		// extract the first Employee...
		String employeePath = getAttributeValue(employeeFindEmployeesDoc, 
				"//a[@class='nof-action-result']/@href", 
				"No Employee found");
		
		// ... and get its details
		Document employeeDoc = get(
				combine(getHostUri(), employeePath));
		prettyPrint(employeeDoc);
	}

}
