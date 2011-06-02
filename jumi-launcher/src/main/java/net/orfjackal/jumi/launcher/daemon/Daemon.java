package net.orfjackal.jumi.launcher.daemon;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Properties;

public class Daemon {

    private static final String daemonJarName;

    static {
        InputStream in = Daemon.class.getResourceAsStream("daemon.properties");
        try {
            Properties p = new Properties();
            p.load(in);
            daemonJarName = p.getProperty("daemonJarName");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public static String getDaemonJarName() {
        return daemonJarName;
    }

    public static InputStream getDaemonJarAsStream() {
        return Daemon.class.getResourceAsStream(daemonJarName);
    }
}
