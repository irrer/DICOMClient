package edu.umro.dicom.client;


/**
 * Provide a simple profiler to determine processing bottlenecks.
 * 
 * @author irrer
 *
 */
public class Profile {

    /** Time in milliseconds when last profile call was made. */
    private volatile static long previousTime = 0;

    /** Description of previous location of profile call. */
    private volatile static String previousLocation = "";

    /** Elapsed minimum time in milliseconds to make it worthwhile to report
     * a profiling event.
     */
    private volatile static long threshhold = 20;

    /** Determines whether profiling is done or not. */
    private volatile static boolean enabled = false;

    
    /**
     * Enable profiling.
     * 
     * @param en True to turn it on, false if off.
     */
    public static void setEnabled(boolean en) {
        enabled = en;
    }
    
    
    /**
     * Get the enabled status (whether profiling is enabled).
     * 
     * @return The enabled status.
     */
    public static boolean getEnabled() {
        return enabled;
    }
    
    
    /**
     * Resets the values so that the next call to profile will
     * not emit an entry.  This is useful to call after waiting
     * for user (or other) input.
     */
    public static void reset() {
        previousLocation = "";
        previousTime = 0;
    }
    
    /**
     * Get just the class name of the given class path.
     * 
     * @param classPath
     * 
     * @return Class name.
     */
    private static String classOfPath(String classPath) {
        int last = classPath.lastIndexOf(".");
        if (last == -1) {
            return classPath;
        }
        return classPath.substring(last+1);
    }

    
    /**
     * Determine how long since last called and print
     * the source code location if enough time has elapsed.
     */
    public synchronized static void profile() {
        if (enabled) {
            try {
                throw new RuntimeException();
            }
            catch (Exception e) {
                StackTraceElement stack = e.getStackTrace()[1];
                long now = System.currentTimeMillis();

                long elapsed = (previousTime == 0) ? 0 : (now - previousTime);
                String location = Thread.currentThread().getName() + "--" + classOfPath(stack.getClassName()) + "." + classOfPath(stack.getMethodName()) + ":" + stack.getLineNumber();
                if (elapsed > threshhold) {
                    String msg =  previousLocation + " :: " + location + "   " + elapsed;
                    System.out.println(msg);
                }
                previousLocation = location;
                previousTime = now;
            }
        }
    }

    
    /**
     * For self testing.
     * 
     * @throws InterruptedException
     */
    private static void methd() throws InterruptedException {
        profile();
        profile();
        Thread.sleep(200);
        profile();
        profile();
    }

    /**
     * For self testing.
     * 
     * @param args
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws InterruptedException {
        setEnabled(true);
        profile();
        profile();
        methd();
        profile();
        profile();
    }

}
