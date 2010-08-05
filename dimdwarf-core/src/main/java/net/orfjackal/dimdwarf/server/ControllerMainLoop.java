// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.server;

import net.orfjackal.dimdwarf.controller.Controller;
import org.slf4j.*;

public class ControllerMainLoop implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ControllerMainLoop.class);

    private final Controller controller;

    public ControllerMainLoop(Controller controller) {
        this.controller = controller;
    }

    public void run() {
        try {
            while (true) {
                controller.processNextMessage();
            }
        } catch (Throwable t) {
            logger.error("Internal error", t);
        } finally {
            killServer();
        }
    }

    private void killServer() {
        // the server is meant to be crash-only software, so it shall never exit cleanly
        logger.info("Server will halt now");
        Runtime.getRuntime().halt(1);
    }
}