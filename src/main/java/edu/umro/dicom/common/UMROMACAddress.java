package edu.umro.dicom.common;

/*
 * Copyright 2012 Regents of the University of Michigan
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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Get the MAC address from the current host.
 * 
 * @author Jim Irrer  irrer@umich.edu 
 *
 */

public class UMROMACAddress {

    /** Save MAC address here for efficiency. */
    private static long macAddress = 0;


    /**
     * Get the MAC address of this host.
     * 
     * @return The MAC address.
     * 
     * @throws UnknownHostException 
     * @throws SocketException 
     */
    public static long getMACAddress() throws UnknownHostException, SocketException {
        if (macAddress == 0) {
            InetAddress localMachine = InetAddress.getLocalHost();
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localMachine);
            byte[] hwAddr = networkInterface.getHardwareAddress();
            for (byte b : hwAddr) {
                macAddress = (macAddress << 8) + ((int)b);
            }
        }
        return macAddress;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            System.out.println("MacAddr: " + Long.toHexString(UMROMACAddress.getMACAddress()));
        }
        catch (Exception ex) {
            System.err.println("ex: " + ex);
            ex.printStackTrace();
        }
    }
}