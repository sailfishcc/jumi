// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.files;

import fi.jumi.actors.ActorRef;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.List;

@NotThreadSafe
public class EnumeratedTestClassFinder implements TestClassFinder {

    private final ClassLoader classLoader;
    private final List<String> testClassNames;

    public EnumeratedTestClassFinder(ClassLoader classLoader, List<String> testClassNames) {
        this.classLoader = classLoader;
        this.testClassNames = testClassNames;
    }

    @Override
    public void findTestClasses(ActorRef<TestClassFinderListener> listener) {
        for (String testClassName : testClassNames) {
            try {
                Class<?> testClass = classLoader.loadClass(testClassName);
                listener.tell().onTestClassFound(testClass);

            } catch (ClassNotFoundException e) {
                // fail gracefully; try to still find other classes
                logTestClassNotFound(testClassName, e);
            }
        }
    }

    protected void logTestClassNotFound(String testClassName, ClassNotFoundException e) {
        // TODO: better logging
        System.err.println("Could not find class \"" + testClassName + "\"");
        e.printStackTrace();
    }
}