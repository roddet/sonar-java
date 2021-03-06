/*
 * Sonar Java
 * Copyright (C) 2012 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.java.jacoco;

import org.jacoco.agent.rt.IAgent;
import org.jacoco.agent.rt.RT;

import java.io.IOException;

class JacocoController {

  private static final String ERROR = "Unable to access JaCoCo Agent - make sure that you use JaCoCo and version not lower than 0.6.2.";

  private final IAgent agent;

  private boolean testStarted;

  private static JacocoController singleton;

  public static synchronized JacocoController getInstance() {
    if (singleton == null) {
      singleton = new JacocoController();
    }
    return singleton;
  }

  private JacocoController() {
    try {
      this.agent = RT.getAgent();
    } catch (NoClassDefFoundError e) {
      throw new Error(ERROR, e);
    } catch (Exception e) {
      throw new Error(ERROR, e);
    }
  }

  JacocoController(IAgent agent) {
    this.agent = agent;
  }

  public synchronized void onTestStart(String name) {
    if (testStarted) {
      throw new Error("Looks like several tests executed in parallel in the same JVM, thus coverage per test can't be recorded correctly.");
    }
    // Dump coverage between tests
    dump("");
    testStarted = true;
  }

  public synchronized void onTestFinish(String name) {
    // Dump coverage for test
    dump(name);
    testStarted = false;
  }

  private void dump(String sessionId) {
    agent.setSessionId(sessionId);
    try {
      agent.dump(true);
    } catch (IOException e) {
      throw new Error(e);
    }
  }

}
