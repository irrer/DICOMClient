package edu.umro.dicom.common;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Get the MAC address from the current host.
 * 
 * @author irrer
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