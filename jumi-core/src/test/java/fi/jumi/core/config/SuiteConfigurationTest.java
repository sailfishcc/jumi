// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import org.junit.*;

import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SuiteConfigurationTest {

    private SuiteConfigurationBuilder builder = new SuiteConfigurationBuilder();

    @Before
    public void setup() {
        // make sure that melting makes all fields back mutable
        builder = builder.freeze().melt();
    }


    // classPath

    @Test
    public void class_path_can_be_changed() {
        builder.addToClassPath(Paths.get("foo.jar"));

        assertThat(configuration().classPath(), contains(Paths.get("foo.jar").toUri()));
    }

    @Test
    public void class_path_defaults_to_empty() {
        assertThat(configuration().classPath(), is(empty()));
    }


    // jvmOptions

    @Test
    public void jvm_options_can_be_changed() {
        builder.addJvmOptions("-option");

        assertThat(configuration().jvmOptions(), contains("-option"));
    }

    @Test
    public void jvm_options_defaults_to_empty() {
        assertThat(configuration().jvmOptions(), is(empty()));
    }


    // testClasses

    @Test
    public void test_classes_can_be_changed() {
        builder.testClasses("TheClass", "AnotherClass");

        assertThat(configuration().testClasses(), contains("TheClass", "AnotherClass"));
    }

    @Test
    public void test_classes_defaults_to_empty() {
        assertThat(configuration().testClasses(), is(empty()));
    }


    // helpers

    private SuiteConfiguration configuration() {
        return builder.freeze();
    }
}