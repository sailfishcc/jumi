// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.launcher.daemon;

import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DirBasedStewardTest {

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    private static final String expectedName = "daemon-1.2.3.jar";
    private final byte[] expectedContent = new byte[]{1, 2, 3};
    private final StubDaemonJar stubDaemonJar = new StubDaemonJar(expectedName, expectedContent);

    @Test
    public void copies_the_embedded_daemon_JAR_to_the_settings_dir() throws IOException {
        DirBasedSteward steward = new DirBasedSteward(stubDaemonJar, tempDir.getRoot().toPath());

        Path daemonJar = steward.getDaemonJar();

        assertThat(daemonJar.getFileName().toString(), is(expectedName));
        assertThat(FileUtils.readFileToByteArray(daemonJar.toFile()), is(expectedContent));
    }

    @Test
    public void does_not_copy_the_daemon_JAR_if_it_has_already_been_copied() throws IOException {
        DirBasedSteward steward = new DirBasedSteward(stubDaemonJar, tempDir.getRoot().toPath());

        FileTime lastModified1 = Files.getLastModifiedTime(steward.getDaemonJar());
        FileTime lastModified2 = Files.getLastModifiedTime(steward.getDaemonJar());

        assertThat(lastModified2, is(lastModified1));
    }

    @Test
    public void overwrites_an_existing_daemon_JAR_that_has_difference_file_size() throws IOException {
        DirBasedSteward steward = new DirBasedSteward(stubDaemonJar, tempDir.getRoot().toPath());
        overwriteWithFileOfSize(expectedContent.length + 1, steward.getDaemonJar());

        Path daemonJar = steward.getDaemonJar();

        assertThat(FileUtils.readFileToByteArray(daemonJar.toFile()), is(expectedContent));
    }

    private static void overwriteWithFileOfSize(int fileSize, Path path) throws IOException {
        Files.write(path, new byte[fileSize]);
    }


    private static class StubDaemonJar implements DaemonJar {
        private final String name;
        private final byte[] content;

        private StubDaemonJar(String name, byte[] content) {
            this.name = name;
            this.content = content;
        }

        @Override
        public String getDaemonJarName() {
            return name;
        }

        @Override
        public InputStream getDaemonJarAsStream() {
            return new ByteArrayInputStream(content);
        }
    }
}