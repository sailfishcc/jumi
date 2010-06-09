// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.test.util;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class TestEnvironment {

    private static final File sandboxDir;
    private static final File deploymentDir;
    private static final AtomicInteger tempDirCounter = new AtomicInteger(0);

    static {
        Properties p = testEnvironmentProperties();
        sandboxDir = canonicalFile(p.getProperty("test.sandbox"));
        deploymentDir = canonicalFile(p.getProperty("test.deployment"));
    }

    private static Properties testEnvironmentProperties() {
        InputStream in = TestEnvironment.class.getResourceAsStream("TestEnvironment.properties");
        if (in == null) {
            throw new RuntimeException("Properties not found");
        }
        try {
            Properties p = new Properties();
            p.load(in);
            return p;
        } catch (IOException e) {
            throw new RuntimeException("Reading properties failed", e);
        }
    }

    private static File canonicalFile(String path) {
        try {
            return new File(path).getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File getSandboxDir() {
        return sandboxDir;
    }

    public static File getDeploymentDir() {
        return deploymentDir;
    }

    public static File createTempDir() {
        File dir = new File(sandboxDir, "temp-" + tempDirCounter.incrementAndGet());
        if (!dir.mkdir()) {
            throw new IllegalStateException("Directory with the same name already exists: " + dir);
        }
        return dir;
    }

    public static void deleteTempDir(File dir) {
        if (!dir.getParentFile().equals(sandboxDir)) {
            throw new IllegalArgumentException("I did not create that file, deleting it would be dangerous: " + dir);
        }
        deleteRecursively(dir);
    }

    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                deleteRecursively(f);
            }
        }
        if (!file.delete()) {
            throw new RuntimeException("Unable to delete file: " + file);
        }
    }
}