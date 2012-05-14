package org.apache.isis.core.testsupport.files;

import java.io.File;
import java.io.IOException;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.testsupport.files.Files.Deleter;
import org.apache.isis.core.testsupport.files.Files.Recursion;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;

public class FilesTest_deleteFiles {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private Deleter deleter;
    
    @Test
    public void test() throws IOException {
        final File cusIdxFile = new File("xml/objects/CUS.xml");
        final File cus1File = new File("xml/objects/CUS/1.xml");
        final File cus2File = new File("xml/objects/CUS/2.xml");
        context.checking(new Expectations() {
            {
                one(deleter).deleteFile(with(equalsFile(cusIdxFile)));
                one(deleter).deleteFile(with(equalsFile(cus1File)));
                one(deleter).deleteFile(with(equalsFile(cus2File)));
            }
        });
        
        Files.deleteFiles(new File("xml/objects"), Files.filterFileNameExtension(".xml"), Recursion.DO_RECURSE, deleter);
    }
    

    private static Matcher<File> equalsFile(final File file) throws IOException {
        final String canonicalPath = file.getCanonicalPath();
        return new TypeSafeMatcher<File>() {

            @Override
            public void describeTo(Description arg0) {
                arg0.appendText("file '" + canonicalPath + "'");
            }

            @Override
            public boolean matchesSafely(File arg0) {
                try {
                    return arg0.getCanonicalPath().equals(canonicalPath);
                } catch (IOException e) {
                    return false;
                }
            }
        };
    }

    
}
