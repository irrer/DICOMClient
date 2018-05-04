package edu.umro.dicom.client;

import java.io.File;
import java.util.Properties;

public class ShowProperties {
    /** Determine if this the system on which software development is done. */
    public static void main(String[] args) {
        Properties propList = System.getProperties();
        for (Object o : propList.keySet()) {
            String key = o.toString();
            System.out.println("    " + key + " : " + propList.getProperty(key));
        }

        System.out.println("\n\n");
        System.out.println("java.class.path: " + propList.getProperty("java.class.path"));

        String[] classPathList = propList.getProperty("java.class.path").split(";");

        for (String cp : classPathList) {
            File f = new File(cp);
            File dir = (f.isDirectory()) ? f : f.getParentFile();
            File cfgFile = new File(dir, "DicomClientConfig.xml");
            System.out.println("    " + cfgFile.getAbsolutePath() + "   can read: " + cfgFile.canRead());
        }

    }
}
