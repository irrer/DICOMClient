package edu.umro.dicom.client;

public class Profile {

    private volatile static long previousTime = 0;

    private volatile static String previousLocation = "";

    private volatile static long threshhold = 20;

    private volatile static boolean enabled = System.getProperty("PROFILE") != null;

    private static String justName(String classPath) {
        int last = classPath.lastIndexOf(".");
        if (last == -1) {
            return classPath;
        }
        return classPath.substring(last+1);
    }

    public synchronized static void profile() {
        if (enabled) {
            try {
                throw new RuntimeException();
            }
            catch (Exception e) {
                StackTraceElement stack = e.getStackTrace()[1];
                long now = System.currentTimeMillis();

                long elapsed = (previousTime == 0) ? 0 : (now - previousTime);
                String location = Thread.currentThread().getName() + "--" + justName(stack.getClassName()) + "." + justName(stack.getMethodName()) + ":" + stack.getLineNumber();
                if (elapsed > threshhold) {
                    String msg =  previousLocation + " :: " + location + "   " + elapsed;
                    System.out.println(msg);
                }
                previousLocation = location;
                previousTime = now;
            }
        }
    }

    private static void methd() throws InterruptedException {
        profile();
        profile();
        Thread.sleep(200);
        profile();
        profile();
    }

    /**
     * @param args
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws InterruptedException {
        profile();
        profile();
        methd();
        profile();
        profile();
    }

}
