// Copyright © 2011, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.runners;

import fi.jumi.actors.OnDemandActors;
import fi.jumi.api.drivers.*;
import fi.jumi.core.*;
import fi.jumi.core.drivers.DriverFinder;
import fi.jumi.core.files.*;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.concurrent.Executor;

@NotThreadSafe
public class SuiteRunner implements Startable, TestClassFinderListener {

    private final SuiteListener suiteListener;
    private final TestClassFinder testClassFinder;
    private final DriverFinder driverFinder;
    private final OnDemandActors actors;
    private final Executor executor;
    private int workers = 0;

    public SuiteRunner(SuiteListener suiteListener,
                       TestClassFinder testClassFinder,
                       DriverFinder driverFinder,
                       OnDemandActors actors,
                       Executor executor) {
        this.suiteListener = suiteListener;
        this.testClassFinder = testClassFinder;
        this.driverFinder = driverFinder;
        this.actors = actors;
        this.executor = executor;
    }

    public void start() {
        // XXX: this call might not be needed (it could even be harmful because of asynchrony); the caller of SuiteRunner knows when the suite is started
        suiteListener.onSuiteStarted();

        final TestClassFinderListener finderListener = actors.createSecondaryActor(TestClassFinderListener.class, this);
        startUnattendedWorker(new Runnable() {
            public void run() {
                testClassFinder.findTestClasses(finderListener);
            }
        });
    }

    private void startUnattendedWorker(Runnable worker) {
        fireWorkerStarted();
        actors.startUnattendedWorker(worker, new Runnable() {
            public void run() {
                fireWorkerFinished();
            }
        });
    }

    private void fireWorkerStarted() {
        workers++;
    }

    private void fireWorkerFinished() {
        workers--;
        assert workers >= 0;
        if (workers == 0) {
            suiteListener.onSuiteFinished();
        }
    }

    public void onTestClassFound(final Class<?> testClass) {
        Class<? extends Driver> driverClass = driverFinder.findTestClassDriver(testClass);

        fireWorkerStarted();
        TestClassRunnerListener listener = new TestClassRunnerListener() {
            public void onTestFound(TestId id, String name) {
                suiteListener.onTestFound(testClass.getName(), id, name);
            }

            public void onTestStarted(TestId id) {
                suiteListener.onTestStarted(testClass.getName(), id);
            }

            public void onFailure(TestId id, Throwable cause) {
                suiteListener.onFailure(testClass.getName(), id, cause);
            }

            public void onTestFinished(TestId id) {
                suiteListener.onTestFinished(testClass.getName(), id);
            }

            public void onTestClassFinished() {
                fireWorkerFinished();
            }
        };
        new TestClassRunner(testClass, driverClass, listener, actors, executor).start();
    }
}
