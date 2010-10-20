package org.apache.isis.extensions.bdd.testapp.claims;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import fitnesse.junit.JUnitHelper;



@RunWith(Parameterized.class)
public class InteractionFixtureTest {

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][]{
				
//				{"ExampleStorySuite.TestAddToAndRemoveFromCollection"},
//				{"ExampleStorySuite.TestAllEmployees"},
				
//				{"ExampleStorySuite.TestCheckAction"},
//				{"ExampleStorySuite.TestCheckAddToAndRemoveFromCollection"},
//				{"ExampleStorySuite.TestCheckAndSaveObject1"},
//				{"ExampleStorySuite.TestCheckAndSaveObject2"},
				
//				{"ExampleStorySuite.TestCheckClearProperty"},
//				{"ExampleStorySuite.TestCheckCollection"},
//				{"ExampleStorySuite.TestCheckProperty"},
//				{"ExampleStorySuite.TestCheckSetProperty"},
//				{"ExampleStorySuite.TestClearProperty"},
				
//				{"ExampleStorySuite.TestGetActionParameterDefault"},
//				{"ExampleStorySuite.TestGetActionParameterChoices"},
//				{"ExampleStorySuite.TestGetCollection"},
//				{"ExampleStorySuite.TestGetProperty"},
//				{"ExampleStorySuite.TestGetPropertyDefault"},
//				{"ExampleStorySuite.TestGetPropertyChoices"},
				
//				{"ExampleStorySuite.TestInvokeAction"},
//				{"ExampleStorySuite.TestNewClaim"},

//				{"ExampleStorySuite.SetProperty"},

//				{"ExampleStorySuite.TestRunViewer"},
				
//				{"ExampleStorySuite.SetUp"},
//				{"ExampleStorySuite.DebuggingAndDiagnostics"},
				{"ClaimsAppSuite.ZzzDebuggingAndDiagnostics"},
//				{"BootstrapNakedObjects"},
		});
	}
	
	private final String testPage;
	private JUnitHelper helper;
	
	public InteractionFixtureTest(String testPage) {
		this.testPage = testPage;
	}

	@Before
	public void initHelper() throws Exception {
		helper = new JUnitHelper(
				"src/main/resources", 
				new File(System.getProperty("java.io.tmpdir"),"fitnesse").getAbsolutePath());
	}

	@Test
	public void runFitnesseTest() throws Exception {
		helper.assertTestPasses(testPage);
	}


}