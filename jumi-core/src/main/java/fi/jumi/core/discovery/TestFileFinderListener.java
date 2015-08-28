// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.discovery;

import fi.jumi.actors.generator.GenerateEventizer;
import fi.jumi.core.api.TestFile;

@GenerateEventizer(targetPackage = "fi.jumi.core.events")
public interface TestFileFinderListener {

    void onTestFileFound(TestFile testFile);

    void onAllTestFilesFound();
}
