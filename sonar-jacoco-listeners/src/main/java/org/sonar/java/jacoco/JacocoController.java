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

class JacocoController {

  private static final String ERROR = "Unable to access JaCoCo Agent - make sure that you use JaCoCo and version not lower than 0.6.2.";

  private final IAgent agent;

  public JacocoController() {
    try {
      this.agent = RT.getAgent();
    } catch (NoClassDefFoundError e) {
      throw new IllegalStateException(ERROR, e);
    } catch (Exception e) {
      throw new IllegalStateException(ERROR, e);
    }
  }

  JacocoController(IAgent agent) {
    this.agent = agent;
  }

  public void onTestStart(String name) {
    System.out.println("Test " + name + " started");

    // TODO naming convention for sessions
    agent.setSessionId(name);
    agent.reset();
  }

  public void onTestFinish(String name) {
    System.out.println("Test " + name + " finished");
    try {
      agent.dump(true);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}