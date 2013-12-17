package edu.umro.dicom.client.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import edu.umro.dicom.client.Util;
import edu.umro.util.Log;
import edu.umro.util.RunCommand;
import edu.umro.util.Utility;
import static org.junit.Assert.assertTrue;
import org.junit.rules.ExternalResource;

public class TestCommandLine {

    /** Source directory for data files. */
    private final File SRC_DIR = new File("src/test/resources/dicom/99999999");
    
    /** Reference directory containing files of previously successful test runs used to compare newly generated files. */ 
    private final File REFERENCE_DIR = new File("src/test/resources/dicom/output");

    /** Destination directory for test results.  All files in this directory and the directory itself are temporary. */
    private final File DEST_DIR = new File("target/testOutput");
    
    private String srcPath(String fileName) {
        return (new File(SRC_DIR, fileName)).getAbsolutePath();
    }
    
    private String refPath(String fileName) {
        return (new File(REFERENCE_DIR, fileName)).getAbsolutePath();
    }
    
    private String destPath(String fileName) {
        return (new File(DEST_DIR, fileName)).getAbsolutePath();
    }
    
    private boolean compareFiles(String fileNameA, String fileNameB) {
        try {
            return Utility.compareFiles(new File(fileNameA), new File(fileNameB));
        }
        catch (Exception e) {
            System.out.println("Unexpected exception during comparison of files: " + Log.fmtEx(e));
            return false;
        }
    }
    
    private boolean compareFiles(String fileName) {
        try {
            return Utility.compareFiles(new File(destPath(fileName)), new File(refPath(fileName)));
        }
        catch (Exception e) {
            System.out.println("Unexpected exception during comparison of files: " + Log.fmtEx(e));
            return false;
        }
    }
    
    private boolean compareAllFilesWithSuffixes(String fileName) {
        if (fileName.endsWith(Util.DICOM_SUFFIX)) fileName = fileName.substring(0, fileName.length() - Util.DICOM_SUFFIX.length());
        boolean ok = true;
        for (String suf : new String[] {}) {
            File destFile = new File(destPath(fileName + suf));
            if (destFile.exists()) {
                ok = ok && compareFiles(fileName + suf);
            }
        }
        return ok;
    }
    
    
    
    /**
     * Run the main program as a command line.
     * 
     * @param args Command line arguments.
     * 
     * @return
     */
    private int runMain(String... args) {
        String[] baseArgs = { "java", "-Xmx256m", "-cp", "target/dicomclient-1.0.28-jar-with-dependencies.jar",
                "-D" + Util.TESTING_PROPERTY + "=" + Util.TESTING_PROPERTY,
                // "-Djava.util.logging.config.file=src\\test\\resources\\test\\logging.propertiesWindows",
                "edu.umro.dicom.client.DicomClient", "-c" };

        ArrayList<String> argList = new ArrayList<String>();
        for (String a : baseArgs)
            argList.add(a);
        if (args != null) for (String a : args)
            argList.add(a);
        String[] allArgs = new String[argList.size()];
        int i = 0;
        for (String a : argList)
            allArgs[i++] = a;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            int exitCode = RunCommand.runArgs(allArgs, baos, baos);
            return exitCode;
        }
        catch (IOException e) {
            assertTrue("Unexpected IOException: " + Log.fmtEx(e), false);
        }
        catch (InterruptedException e) {
            assertTrue("Unexpected InterruptedException: " + Log.fmtEx(e), false);
        }
        return Integer.MIN_VALUE;
    }

    @BeforeClass
    public static void setUpClass() {
        System.out.println("set up class");
        //Utility.deleteFileTree(new File(DEST_DIR));
    }

    @After  // runs after each method
    public void tearDownMethod() {
       // Utility.deleteFileTree(new File(DEST_DIR));
    }
    
    @Test
    public void commandLineModeNoOptions() {
        int code = runMain();
        assertTrue("command line mode with no options", code == 0);
    }

    @Test
    public void invalidOption() {
        int code = runMain("-9");
        assertTrue("bad command line option should fail", code != 0);
    }

    @Test
    public void commandLineRTPlanAnonymization() {
        Utility.deleteFileTree(DEST_DIR);
        String inFile = srcPath("99999999_RTPLAN.DCM");
        String outFile = "1234_RTPLAN" + Util.DICOM_SUFFIX;
        int code = runMain("-P", "1234", "-o", destPath(outFile), "-z", inFile);
        assertTrue("command line mode RTPLAN anonymization", code == 0);
        assertTrue("Files are equal", compareAllFilesWithSuffixes(outFile));
        Utility.deleteFileTree(DEST_DIR);
    }

    @Test
    public void commandLineMultipleFilesWithMinusO() {
        Utility.deleteFileTree(DEST_DIR);
        String inFile1 = srcPath("99999999_RTIMAGE_0001.DCM");
        String inFile2 = srcPath("99999999_RTIMAGE_0002.DCM");
        int code = runMain("-P", "1234", "-o", DEST_DIR.getAbsolutePath(), "-z", inFile1, inFile2);
        assertTrue("command line mode with -o and multiple files", code != 0);
        assertTrue("no files generated", !DEST_DIR.exists());
        Utility.deleteFileTree(DEST_DIR);
    }

    @Test
    public void commandLineMultipleFilesWithMinusD() {
        Utility.deleteFileTree(DEST_DIR);
        String inFile1 = srcPath("99999999_RTIMAGE_0001.DCM");
        String inFile2 = srcPath("99999999_RTIMAGE_0002.DCM");
        String outFile1 = "1234_RTIMAGE_0001.DCM";
        String outFile2 = "1234_RTIMAGE_0002.DCM";
        int code = runMain("-P", "1234", "-d", DEST_DIR.getAbsolutePath(), "-z", inFile1, inFile2);
        assertTrue("command line mode with -d and multiple files", code == 0);
        assertTrue("Files are equal", compareAllFilesWithSuffixes(outFile1));
        assertTrue("Files are equal", compareAllFilesWithSuffixes(outFile2));
        Utility.deleteFileTree(DEST_DIR);
    }

    /*
    @Test
    public void xcommandLineMultipleFilesWithMinusO() {
        int code = runMain("-P", "1234", "-o", DEST_DIR, "-z", "src/test/resources/dicom/99999999/99999999_RTIMAGE_0001.DCM", "src/test/resources/dicom/99999999/99999999_RTIMAGE_0002.DCM");
        assertTrue("command line mode with -o and multiple files fails", code != 0);
    }

    @Test
    public void commandLineRTImageAnonymization() {
        int code = runMain("-P", "1234", "-d", DEST_DIR, "-z", "src/test/resources/dicom/99999999/99999999_RTIMAGE_0001.DCM", "src/test/resources/dicom/99999999/99999999_RTIMAGE_0002.DCM");
        assertTrue("command line mode with multiple RTIMAGE files", code == 0);
    }
    */

    /**
     * @param args
     */
    public static void main(String[] args) {
        {
        }
    }

}
