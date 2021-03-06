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
package org.sonar.java.bytecode;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sonar.graph.DirectedGraph;
import org.sonar.java.JavaConfiguration;
import org.sonar.java.JavaSquid;
import org.sonar.java.bytecode.asm.AsmResource;
import org.sonar.squid.api.SourceCode;
import org.sonar.squid.api.SourceCodeEdge;
import org.sonar.squid.api.SourceCodeEdgeUsage;
import org.sonar.squid.indexer.SquidIndex;
import org.sonar.squid.measures.Metric;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;

public class BytecodeVisitorsTest {

  private static SquidIndex index;
  private static DirectedGraph<SourceCode, SourceCodeEdge> graph;

  static SourceCode todo;
  static SourceCode fixme;
  static SourceCode file;
  static SourceCode tag;
  static SourceCode tagFile;
  static SourceCode line;
  static SourceCode sourceFile;
  static SourceCode language;
  static SourceCode tagName;
  static SourceCode tagException;
  static SourceCode pacTag;
  static SourceCode pacImpl;

  @BeforeClass
  public static void setup() {
    JavaConfiguration conf = new JavaConfiguration(Charset.forName("UTF-8"));
    JavaSquid squid = new JavaSquid(conf);
    squid.scanDirectories(
        Collections.singleton(new File("src/test/files/bytecode/src")),
        Collections.singleton(new File("src/test/files/bytecode/bin")));
    index = squid.getIndex();
    graph = squid.getGraph();

    tag = index.search("tags/Tag");
    tagFile = index.search("tags/Tag.java");
    file = index.search("tags/File");
    line = index.search("tags/Line");
    tagName = index.search("tags/TagName");
    tagException = index.search("tags/TagException");
    language = index.search("tags/Language");
    sourceFile = index.search("tags/SourceFile");
    todo = index.search("tags/impl/Todo");
    fixme = index.search("tags/impl/FixMe");
    pacTag = index.search("tags");
    pacImpl = index.search("tags/impl");
  }

  @Test
  public void rfc() {
    assertThat(index.search("tags/impl/Todo").getInt(Metric.RFC)).isEqualTo(8);
    assertThat(index.search("tags/impl/Todo.java").getInt(Metric.RFC)).isEqualTo(8);
    assertThat(index.search("tags/SourceFile").getInt(Metric.RFC)).isEqualTo(5);
  }

  @Test
  public void dit() {
    assertThat(index.search("tags/SourceFile").getInt(Metric.DIT)).isEqualTo(3);
    assertThat(index.search("tags/File").getInt(Metric.DIT)).isEqualTo(2);
    assertThat(index.search("tags/Content").getInt(Metric.DIT)).isEqualTo(1);
    assertThat(index.search("tags/TagException").getInt(Metric.DIT)).isEqualTo(3);
    assertThat(index.search("tags/Comment").getInt(Metric.DIT)).isEqualTo(0);
    assertThat(index.search("tags/Tag").getInt(Metric.DIT)).isEqualTo(1);
  }

  @Test
  public void noc() {
    assertThat(index.search("tags/File").getInt(Metric.NOC)).isEqualTo(1);
    assertThat(index.search("tags/Tag").getInt(Metric.NOC)).isEqualTo(2); // Tag is an interface
    assertThat(index.search("tags/SourceFile").getInt(Metric.NOC)).isEqualTo(0);
    assertThat(index.search("tags/Content").getInt(Metric.NOC)).isEqualTo(2); // Content has only one direct child
  }

  @Test
  public void lcom4() {
    assertThat(index.search("tags/impl/Todo").getInt(Metric.LCOM4)).isEqualTo(1);
    assertThat(index.search("tags/impl/Todo.java").getInt(Metric.LCOM4)).isEqualTo(1);

    List<Set<AsmResource>> lcom4Blocks = (List<Set<AsmResource>>) index.search("tags/impl/Todo.java").getData(Metric.LCOM4_BLOCKS);
    assertThat(lcom4Blocks.size()).isEqualTo(0);

    assertThat(index.search("tags/Tag").getInt(Metric.LCOM4)).isEqualTo(1);
    assertThat(index.search("tags/TagName").getInt(Metric.LCOM4)).isEqualTo(1);
  }

  @Test
  public void afferent_coupling() {
    assertThat(index.search("tags/Tag").getInt(Metric.CA)).isEqualTo(2);
    assertThat(index.search("tags/Tag.java").getInt(Metric.CA)).isEqualTo(2);
    assertThat(index.search("tags").getInt(Metric.CA)).isEqualTo(2);
    assertThat(index.search("tags/impl").getInt(Metric.CA)).isEqualTo(0);
  }

  @Test
  public void efferent_coupling() {
    assertThat(index.search("tags/Tag").getInt(Metric.CE)).isEqualTo(3);
    assertThat(index.search("tags/Tag.java").getInt(Metric.CE)).isEqualTo(3);
    assertThat(index.search("tags").getInt(Metric.CE)).isEqualTo(0);
  }

  @Test
  public void testExtendsRelationShips() {
    assertThat(graph.getEdge(sourceFile, file).getUsage()).isEqualTo(SourceCodeEdgeUsage.EXTENDS);
  }

  @Test
  public void testClassDefinitionWithGenerics() {
    assertThat(graph.getEdge(todo, language).getUsage()).isEqualTo(SourceCodeEdgeUsage.USES);
  }

  @Test
  public void testImplementsRelationShips() {
    assertThat(graph.getEdge(todo, tag).getUsage()).isEqualTo(SourceCodeEdgeUsage.IMPLEMENTS);
  }

  @Test
  public void testLdcRelationShips() {
    assertThat(graph.getEdge(tagName, tagException).getUsage()).isEqualTo(SourceCodeEdgeUsage.USES);
  }

  @Test
  public void testFieldRelationShip() {
    assertThat(graph.getEdge(todo, file).getUsage()).isEqualTo(SourceCodeEdgeUsage.USES);
  }

  @Test
  public void testFieldRelationShipWithGenerics() {
    assertThat(graph.getEdge(todo, line).getUsage()).isEqualTo(SourceCodeEdgeUsage.USES);
  }

  @Test
  public void testMethodReturnType() {
    assertThat(graph.getEdge(todo, tagName).getUsage()).isEqualTo(SourceCodeEdgeUsage.USES);
  }

  @Test
  public void testMethodArgs() {
    assertThat(graph.getEdge(todo, sourceFile).getUsage()).isEqualTo(SourceCodeEdgeUsage.USES);
  }

  @Test
  public void testMethodException() {
    assertThat(graph.getEdge(todo, tagException).getUsage()).isEqualTo(SourceCodeEdgeUsage.USES);
  }

  @Test
  public void testAccessFieldOfAnObject() {
    assertThat(graph.getEdge(fixme, sourceFile).getUsage()).isEqualTo(SourceCodeEdgeUsage.USES);
  }

  @Test
  public void testTypeInsn() {
    assertThat(graph.getEdge(fixme, file).getUsage()).isEqualTo(SourceCodeEdgeUsage.USES);
  }

  @Test
  public void testAccessMethodOfAnObject() {
    assertThat(graph.getEdge(fixme, tagException).getUsage()).isEqualTo(SourceCodeEdgeUsage.USES);
  }

  @Test
  public void testTryCatchBlock() {
    assertThat(graph.getEdge(sourceFile, tagException).getUsage()).isEqualTo(SourceCodeEdgeUsage.USES);
  }

  @Test
  public void testPackageDependencies() {
    assertThat(graph.getEdge(pacImpl, pacTag).getUsage()).isEqualTo(SourceCodeEdgeUsage.USES);
    assertThat(graph.getEdge(pacImpl, pacTag).getWeight()).isEqualTo(13);
  }

  @Test
  public void noDependencyFromOneSquidUnitToItself() {
    assertThat(graph.getEdge(pacTag, pacTag)).isNull();
    assertThat(graph.getEdge(fixme, fixme)).isNull();
  }

  @Test
  public void testFileDependencies() {
    assertThat(graph.getEdge(sourceFile.getParent(), tagException.getParent()).getUsage()).isEqualTo(SourceCodeEdgeUsage.USES);
  }

}
