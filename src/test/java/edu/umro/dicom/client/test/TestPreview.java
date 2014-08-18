package edu.umro.dicom.client.test;

/*
 * Copyright 2013 Regents of the University of Michigan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import org.junit.Test;

import edu.umro.dicom.client.DicomClient;
import edu.umro.dicom.client.Series;
import static org.junit.Assert.assertTrue;

/**
 * Automatic test for command line functionality.
 * 
 * @author irrer
 * 
 */
public class TestPreview {
    private static final File CT_FILE = new File("src/test/resources/dicom/99999999/99999999_CT_2_0001.DCM");

    private static final String UID = "1.3.6.1.4.1.22361.48658618118952.752771718.1386779753570.172";

    volatile boolean done = false;

    private void waitForReady() {

        long start = System.currentTimeMillis();
        class WaitForIt implements Runnable {
            private volatile Semaphore semaphore = new Semaphore(1);

            private void acquire() {
                try {
                    semaphore.acquire();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            private WaitForIt() {
                acquire();
            }

            public void run() {
                semaphore.release();
            }

            public void complete() {
                acquire();
            }
        }
        ;

        WaitForIt waitForIt = new WaitForIt();

        javax.swing.SwingUtilities.invokeLater(waitForIt);
        waitForIt.complete();

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("elapsed: " + elapsed);
    }

    @Test
    public void basicText() {
        try {
            String[] args = { CT_FILE.getAbsolutePath() };
            DicomClient.main(args);

            waitForReady();

            DicomClient dicomClient = DicomClient.getInstance();
            ArrayList<File> fileList = new ArrayList<File>();
            fileList.add(CT_FILE);

            waitForReady();

            Series series = dicomClient.findSeries(null, fileList);
            series.doClickPreviewButtonTest();

            waitForReady();
            
            dicomClient.getPreview().doClickTextPreview();
            waitForReady();
            
            String text = dicomClient.getPreview().getTextTest();
            assertTrue("text preview", text.indexOf(UID) != 1);
            //System.out.println("text:\n" + text);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        {
        }
    }

}
