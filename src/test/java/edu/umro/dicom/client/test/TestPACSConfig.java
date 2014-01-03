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

import java.util.ArrayList;

import org.junit.Test;

import edu.umro.dicom.client.PACS;
import edu.umro.dicom.client.PACSConfig;
import static org.junit.Assert.assertTrue;

/**
 * Automatic test for command line functionality.
 * 
 * @author irrer
 *
 */
public class TestPACSConfig {

    @Test
    public void getIdent() {
        PACSConfig pacsConfig = PACSConfig.getInstance();
        PACS identity = pacsConfig.getIdentity();
        System.out.println("identity: " + identity);
        assertTrue("identity", identity.aeTitle.length() > 1);
        assertTrue("identity port", identity.port > 10);
    }

    @Test
    public void getList() {
        PACSConfig pacsConfig = PACSConfig.getInstance();
        ArrayList<PACS> list = pacsConfig.getPacsList();
        System.out.println("PACS list:");
        for (PACS p : list) 
            System.out.println("    " + p);
        
        assertTrue("list size", list.size() > 0);
        assertTrue("list port", list.get(0).port > 0);
        assertTrue("list host", list.get(0).host.length() > 0);
    }


    /**
     * @param args
     */
    public static void main(String[] args) {
        {
        }
    }

}
