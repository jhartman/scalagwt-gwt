/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.core.linker.soyc;

import com.google.gwt.core.ext.LinkerContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.linker.AbstractLinker;
import com.google.gwt.core.ext.linker.ArtifactSet;
import com.google.gwt.core.ext.linker.CompilationAnalysis;
import com.google.gwt.core.ext.linker.CompilationResult;
import com.google.gwt.core.ext.linker.LinkerOrder;
import com.google.gwt.core.ext.linker.SelectionProperty;
import com.google.gwt.core.ext.linker.SyntheticArtifact;
import com.google.gwt.core.ext.linker.CompilationAnalysis.Snippet;
import com.google.gwt.core.ext.linker.LinkerOrder.Order;
import com.google.gwt.core.ext.soyc.ClassMember;
import com.google.gwt.core.ext.soyc.FieldMember;
import com.google.gwt.core.ext.soyc.FunctionMember;
import com.google.gwt.core.ext.soyc.Member;
import com.google.gwt.core.ext.soyc.MethodMember;
import com.google.gwt.core.ext.soyc.Range;
import com.google.gwt.core.ext.soyc.Story;
import com.google.gwt.core.ext.soyc.Story.Origin;
import com.google.gwt.dev.util.HtmlTextOutput;
import com.google.gwt.util.tools.Utility;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.GZIPOutputStream;

/**
 * Generates the XML report containing the Story of Your Compile.
 */
@LinkerOrder(Order.PRE)
public class SoycReportLinker extends AbstractLinker {

  int curNumIndents = 0;

  public String escapeXml(String unescaped) {
    String escaped = unescaped.replaceAll("\\&", "&amp;");
    escaped = escaped.replaceAll("\\<", "&lt;");
    escaped = escaped.replaceAll("\\>", "&gt;");
    escaped = escaped.replaceAll("\\\"", "&quot;");
    // escaped = escaped.replaceAll("\\'", "&apos;");
    return escaped;
  }

  @Override
  public String getDescription() {
    return "Story of your compile report";
  }

  @Override
  public ArtifactSet link(TreeLogger logger, LinkerContext context,
      ArtifactSet artifacts) throws UnableToCompleteException {
    SortedSet<CompilationAnalysis> reports = artifacts.find(CompilationAnalysis.class);

    // Do nothing if there are no reports to be generated.
    if (reports.isEmpty()) {
      return artifacts;
    }

    logger = logger.branch(TreeLogger.DEBUG, "SOYC report linker");
    initialize(logger);

    if (reports.isEmpty()) {
      logger.log(TreeLogger.DEBUG, "No SOYC report artifacts");
      return artifacts;
    }

    artifacts = new ArtifactSet(artifacts);
    int reportNum = 0;
    SortedMap<CompilationResult, String> partialPathsByResult = new TreeMap<CompilationResult, String>();

    // TODO: This goes much faster in parallel, but what's the policy?
    ExecutorService executor = Executors.newSingleThreadExecutor();
    List<Future<SyntheticArtifact>> futures = new ArrayList<Future<SyntheticArtifact>>(
        reports.size());
    for (final CompilationAnalysis report : reports) {
      final TreeLogger loopLogger = logger.branch(TreeLogger.SPAM,
          "Report for " + report.toString());
      final String reportName = "report" + reportNum++ + ".xml.gz";
      partialPathsByResult.put(report.getCompilationResult(), reportName);
      Future<SyntheticArtifact> future = executor.submit(new Callable<SyntheticArtifact>() {
        public SyntheticArtifact call() throws Exception {
          loopLogger.log(TreeLogger.INFO, "Started");
          SyntheticArtifact reportArtifact = emitReport(loopLogger, report,
              reportName, true);
          return reportArtifact;
        }
      });
      futures.add(future);
    }
    executor.shutdown();

    for (Future<SyntheticArtifact> future : futures) {
      SyntheticArtifact artifact;
      try {
        artifact = future.get();
      } catch (InterruptedException e) {
        logger.log(TreeLogger.ERROR, "Unable to process report", e);
        throw new UnableToCompleteException();
      } catch (ExecutionException e) {
        logger.log(TreeLogger.ERROR, "Unable to process report", e);
        throw new UnableToCompleteException();
      }
      artifact.setPrivate(true);
      artifacts.add(artifact);
    }

    // Emit manifest
    try {
      SyntheticArtifact sa = emitManifest(logger, artifacts,
          partialPathsByResult, false);
      sa.setPrivate(true);
      artifacts.add(sa);
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return artifacts;
  }

  private void emitAliases(HtmlTextOutput htmlOut, Set<String> methodAliases) {
    String curLine;
    if (methodAliases.size() > 0) {
      htmlOut.indentIn();
      htmlOut.indentIn();
      curNumIndents++;

      curLine = "<aliases>";
      htmlOut.printRawOpt(curLine);
      htmlOut.newlineOpt();
      htmlOut.indentIn();
      htmlOut.indentIn();
      curNumIndents++;
    }

    for (String methodAlias : methodAliases) {
      curLine = "<alias jsName=\"" + methodAlias + "\"/>";
      htmlOut.printRawOpt(curLine);
      htmlOut.newlineOpt();
    }
    if (methodAliases.size() > 0) {
      htmlOut.indentOut();
      htmlOut.indentOut();
      curNumIndents--;

      curLine = "</aliases>";
      htmlOut.printRawOpt(curLine);
      htmlOut.newlineOpt();
      htmlOut.indentOut();
      htmlOut.indentOut();
      curNumIndents--;
    }
  }

  private void emitClasses(HtmlTextOutput htmlOut,
      SortedMap<String, Set<ClassMember>> packageToClasses, String packageName) {
    String curLine;
    /**
     * sort the classes alphabetically
     */
    TreeMap<String, ClassMember> sortedClasses = new TreeMap<String, ClassMember>();
    for (ClassMember classMember : packageToClasses.get(packageName)) {
      String className = classMember.getSourceName();
      sortedClasses.put(className, classMember);
    }

    for (String className : sortedClasses.keySet()) {
      ClassMember classMember = sortedClasses.get(className);
      curLine = "<class id=\"" + className + "\" ";
      htmlOut.printRawOpt(curLine);

      String jsName = classMember.getJsName();
      String name = className.substring(className.lastIndexOf('.') + 1);
      curLine = "jsName=\"" + jsName + "\" name=\"" + name + "\">";

      if (jsName == null) {
        curLine = "name=\"" + name + "\">";
      }

      emitOverrides(htmlOut, curLine, classMember);
      emitDependencies(htmlOut, classMember);
      emitMethods(htmlOut, classMember);
      emitFields(htmlOut, classMember);

      curLine = "</class>";
      htmlOut.printRawOpt(curLine);
      htmlOut.newlineOpt();
    }
  }

  private void emitDependencies(HtmlTextOutput htmlOut, ClassMember classMember) {
    String curLine;
    Set<Member> dependencies = classMember.getDependencies();
    if (dependencies.size() > 0) {
      htmlOut.indentIn();
      htmlOut.indentIn();
      curNumIndents++;

      curLine = "<depends>";
      htmlOut.printRawOpt(curLine);
      htmlOut.newlineOpt();
      htmlOut.indentIn();
      htmlOut.indentIn();
      curNumIndents++;
    }
    for (Member dependency : dependencies) {
      curLine = "<on idref=\"" + dependency.getSourceName() + "\"/>";
      htmlOut.printRawOpt(curLine);
      htmlOut.newlineOpt();
    }
    if (dependencies.size() > 0) {
      htmlOut.indentOut();
      htmlOut.indentOut();
      curNumIndents--;

      curLine = "</depends>";
      htmlOut.printRawOpt(curLine);
      htmlOut.newlineOpt();
      htmlOut.indentOut();
      htmlOut.indentOut();
      curNumIndents--;
    }
  }

  private void emitFields(HtmlTextOutput htmlOut, ClassMember classMember) {
    String curLine;
    Set<FieldMember> fields = classMember.getFields();
    if (fields.size() > 0) {
      htmlOut.indentIn();
      htmlOut.indentIn();
      curNumIndents++;
    }
    for (FieldMember field : fields) {
      curLine = "<field id=\"" + field.getSourceName() + "\" jsName=\""
          + field.getJsName() + "\"/>";
      String curJsName = field.getJsName();
      if (curJsName == null) {
        curLine = "<field id=\"" + field.getSourceName() + "\"/>";
      }
      htmlOut.printRawOpt(curLine);
      htmlOut.newlineOpt();
    }
    if (fields.size() > 0) {
      htmlOut.indentOut();
      htmlOut.indentOut();
      curNumIndents--;
    }
  }

  private void emitFunctions(CompilationAnalysis report, HtmlTextOutput htmlOut) {
    String curLine;

    Set<FunctionMember> functions = report.getFunctions();
    for (FunctionMember function : functions) {
      curLine = "<function ";
      htmlOut.printRawOpt(curLine);

      String sourceName = function.getSourceName();
      String jsName = function.getJsName();
      Set<Member> dependencies = function.getDependencies();
      if (dependencies.size() == 0) {
        curLine = "id=\"" + sourceName + "\" jsName=\"" + jsName + "\"/>";
        if (jsName == null) {
          curLine = "id=\"" + sourceName + "\"/>";
        }
        htmlOut.printRawOpt(curLine);
        htmlOut.newline();
      } else {
        curLine = "id=\"" + sourceName + "\" jsName=\"" + jsName + "\">";
        if (jsName == null) {
          curLine = "id=\"" + sourceName + "\">";
        }
        htmlOut.printRawOpt(curLine);
        htmlOut.newline();
        htmlOut.indentIn();
        htmlOut.indentIn();
        curNumIndents++;

        curLine = "<depends>";
        htmlOut.printRawOpt(curLine);
        htmlOut.newline();
        htmlOut.indentIn();
        htmlOut.indentIn();
        curNumIndents++;
      }
      for (Member dependency : dependencies) {
        curLine = "<on idref=\"" + dependency.getSourceName() + "\"/>";
        htmlOut.printRawOpt(curLine);
        htmlOut.newline();
      }
      if (dependencies.size() > 0) {
        htmlOut.indentOut();
        htmlOut.indentOut();
        curNumIndents--;
        curLine = "</depends>";
        htmlOut.printRawOpt(curLine);
        htmlOut.newline();
        htmlOut.indentOut();
        htmlOut.indentOut();
        curNumIndents--;

        curLine = "</function>";
        htmlOut.printRawOpt(curLine);
        htmlOut.newlineOpt();
      }
    }
  }

  private void emitJs(CompilationAnalysis report, HtmlTextOutput htmlOut,
      Map<Story, Integer> storyIds) {

    String curLine;
    int fragment = 0;
    for (String contents : report.getCompilationResult().getJavaScript()) {
      curLine = "<js fragment=\"" + fragment + "\">";
      htmlOut.printRawOpt(curLine);
      htmlOut.newlineOpt();

      htmlOut.indentIn();
      htmlOut.indentIn();
      curNumIndents++;

      for (Snippet snippet : report.getSnippets(fragment)) {
        Range range = snippet.getRange();
        Story story = snippet.getStory();
        assert storyIds.containsKey(story);
        int storyId = storyIds.get(story);

        String jsCode = contents.substring(range.getStart(), range.getEnd());
        jsCode = escapeXml(jsCode);
        if ((jsCode.length() == 0) || (jsCode.compareTo("\n") == 0)) {
          curLine = "<storyref idref=\"story" + Integer.toString(storyId)
              + "\"/>";
        } else {
          curLine = "<storyref idref=\"story" + Integer.toString(storyId)
              + "\">" + jsCode + "</storyref>";
        }
        htmlOut.printRawOpt(curLine);
        htmlOut.newlineOpt();
      }

      htmlOut.indentOut();
      htmlOut.indentOut();
      curNumIndents--;

      curLine = "</js>";
      htmlOut.printRawOpt(curLine);
      htmlOut.newlineOpt();
      fragment++;
    }
  }

  private SyntheticArtifact emitManifest(TreeLogger logger,
      ArtifactSet artifacts,
      SortedMap<CompilationResult, String> partialPathsByResult,
      boolean compress) throws UnableToCompleteException, IOException {

    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    OutputStreamWriter out;
    try {
      out = new OutputStreamWriter(compress ? new GZIPOutputStream(bytes)
          : bytes);
    } catch (IOException e) {
      logger.log(TreeLogger.ERROR, "Unable to set up gzip stream", e);
      throw new UnableToCompleteException();
    }
    PrintWriter pw = new PrintWriter(out);
    HtmlTextOutput htmlOut = new HtmlTextOutput(pw, false);

    String curLine = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    htmlOut.printRawOpt(curLine);
    htmlOut.newline();
    curLine = "<soyc-manifest>";
    htmlOut.printRawOpt(curLine);
    htmlOut.newline();
    htmlOut.indentIn();
    htmlOut.indentIn();
    curNumIndents++;

    for (Map.Entry<CompilationResult, String> entry : partialPathsByResult.entrySet()) {
      curLine = "<report href=\"" + entry.getValue() + "\">";
      htmlOut.printRawOpt(curLine);
      htmlOut.newline();
      htmlOut.indentIn();
      htmlOut.indentIn();
      curNumIndents++;

      for (Map<SelectionProperty, String> map : entry.getKey().getPropertyMap()) {

        if (map.size() > 0) {
          curLine = "<permutation>";
          htmlOut.printRawOpt(curLine);
          htmlOut.newline();
          htmlOut.indentIn();
          htmlOut.indentIn();
          curNumIndents++;

        } else {
          curLine = "<permutation/>";
          htmlOut.printRawOpt(curLine);
          htmlOut.newline();
        }
        for (Map.Entry<SelectionProperty, String> propertyEntry : map.entrySet()) {
          curLine = "<property name=\"" + propertyEntry.getKey().getName()
              + "\" value=\"" + propertyEntry.getValue() + "\"/>";
          htmlOut.printRawOpt(curLine);
          htmlOut.newline();
        }
        if (map.size() > 0) {
          htmlOut.indentOut();
          htmlOut.indentOut();
          curNumIndents--;

          curLine = "</permutation>";
          htmlOut.printRawOpt(curLine);
          htmlOut.newline();
        }
      }
      htmlOut.indentOut();
      htmlOut.indentOut();
      curNumIndents--;

      curLine = "</report>";
      htmlOut.printRawOpt(curLine);
      htmlOut.newline();
    }

    htmlOut.indentOut();
    htmlOut.indentOut();
    curNumIndents--;

    curLine = "</soyc-manifest>";
    htmlOut.printRawOpt(curLine);
    htmlOut.newline();

    pw.close();
    Utility.close(out);
    SyntheticArtifact toReturn = emitBytes(logger, bytes.toByteArray(),
        "manifest.xml");

    return toReturn;
  }

  private void emitMembers(CompilationAnalysis report, HtmlTextOutput htmlOut) {
    String curLine;

    curLine = "<members>";

    htmlOut.printRawOpt(curLine);
    htmlOut.newlineOpt();
    htmlOut.indentIn();
    htmlOut.indentIn();
    curNumIndents++;

    SortedMap<String, Set<ClassMember>> packageToClasses = new TreeMap<String, Set<ClassMember>>();

    emitPackages(report, htmlOut, packageToClasses);
    emitFunctions(report, htmlOut);

    htmlOut.indentOut();
    htmlOut.indentOut();
    curNumIndents--;

    curLine = "</members>";
    htmlOut.printRawOpt(curLine);
    htmlOut.newlineOpt();
  }

  private void emitMethodDependencies(HtmlTextOutput htmlOut,
      Set<Member> methodDependencies) {
    String curLine;
    if (methodDependencies.size() > 0) {
      htmlOut.indentIn();
      htmlOut.indentIn();
      curNumIndents++;

      curLine = "<depends>";
      htmlOut.printRawOpt(curLine);
      htmlOut.newlineOpt();
      htmlOut.indentIn();
      htmlOut.indentIn();
      curNumIndents++;

      for (Member methodDependency : methodDependencies) {
        curLine = "<on idref=\"" + methodDependency.getSourceName() + "\"/>";
        htmlOut.printRawOpt(curLine);
        htmlOut.newlineOpt();
      }

      htmlOut.indentOut();
      htmlOut.indentOut();
      curNumIndents--;

      curLine = "</depends>";
      htmlOut.printRawOpt(curLine);
      htmlOut.newlineOpt();
      htmlOut.indentOut();
      htmlOut.indentOut();
      curNumIndents--;
    }
  }

  private void emitMethodOverrides(HtmlTextOutput htmlOut,
      Set<MethodMember> methodOverrides) {
    String curLine;
    if (methodOverrides.size() > 0) {
      htmlOut.indentIn();
      htmlOut.indentIn();
      curNumIndents++;

      curLine = "<override>";
      htmlOut.printRawOpt(curLine);
      htmlOut.newlineOpt();
      htmlOut.indentIn();
      htmlOut.indentIn();
      curNumIndents++;
    }
    for (MethodMember overrideMethodMember : methodOverrides) {
      curLine = "<of idref=\"" + overrideMethodMember.getSourceName() + "\"/>";
      htmlOut.printRawOpt(curLine);
      htmlOut.newlineOpt();
    }
    if (methodOverrides.size() > 0) {
      htmlOut.indentOut();
      htmlOut.indentOut();
      curNumIndents--;

      curLine = "</override>";
      htmlOut.printRawOpt(curLine);
      htmlOut.newlineOpt();
      htmlOut.indentOut();
      htmlOut.indentOut();
      curNumIndents--;
    }
  }

  private void emitMethods(HtmlTextOutput htmlOut, ClassMember classMember) {
    String curLine;
    Set<MethodMember> methods = classMember.getMethods();
    if (methods.size() > 0) {
      htmlOut.indentIn();
      htmlOut.indentIn();
      curNumIndents++;
    }
    for (MethodMember method : methods) {
      curLine = "<method ";
      htmlOut.printRawOpt(curLine);

      String jsAtt = " jsName=\"" + method.getJsName() + "\"";
      String curJsName = method.getJsName();

      if (curJsName == null) {
        jsAtt = "";
      }

      Set<String> methodAliases = method.getJsAliases();
      Set<MethodMember> methodOverrides = method.getOverrides();
      Set<Member> methodDependencies = method.getDependencies();

      if ((methodOverrides.size() > 0) || (methodDependencies.size() > 0)
          || (methodAliases.size() > 0)) {
        curLine = "id=\"" + method.getSourceName() + "\"" + jsAtt + ">";
        htmlOut.printRawOpt(curLine);
        htmlOut.newlineOpt();
      } else {
        curLine = "id=\"" + method.getSourceName() + "\"" + jsAtt + "/>";
        htmlOut.printRawOpt(curLine);
        htmlOut.newlineOpt();
      }

      emitAliases(htmlOut, methodAliases);
      emitMethodOverrides(htmlOut, methodOverrides);
      emitMethodDependencies(htmlOut, methodDependencies);

      if ((methodOverrides.size() > 0) || (methodDependencies.size() > 0)
          || (methodAliases.size() > 0)) {
        curLine = "</method>";
        htmlOut.printRawOpt(curLine);
        htmlOut.newlineOpt();
      }
    }

    if (methods.size() > 0) {
      htmlOut.indentOut();
      htmlOut.indentOut();
      curNumIndents--;
    }
  }

  private void emitOverrides(HtmlTextOutput htmlOut, String curLine,
      ClassMember classMember) {
    htmlOut.printRawOpt(curLine);
    htmlOut.newlineOpt();
    Set<ClassMember> overrides = classMember.getOverrides();
    if (overrides.size() > 0) {
      htmlOut.indentIn();
      htmlOut.indentIn();
      curNumIndents++;
      curLine = "<override>";
      htmlOut.printRawOpt(curLine);
      htmlOut.newlineOpt();
      htmlOut.indentIn();
      htmlOut.indentIn();
      curNumIndents++;
    }
    for (ClassMember overrideClassMember : overrides) {
      curLine = "<of idref=\"" + overrideClassMember.getSourceName() + "\"/>";
      htmlOut.printRawOpt(curLine);
      htmlOut.newlineOpt();
    }
    if (overrides.size() > 0) {
      htmlOut.indentOut();
      htmlOut.indentOut();
      curNumIndents--;

      curLine = "</override>";
      htmlOut.printRawOpt(curLine);
      htmlOut.newlineOpt();
      htmlOut.indentOut();
      htmlOut.indentOut();
      curNumIndents--;
    }
  }

  private void emitPackages(CompilationAnalysis report, HtmlTextOutput htmlOut,
      SortedMap<String, Set<ClassMember>> packageToClasses) {

    String curLine;
    for (ClassMember classMember : report.getClasses()) {
      String packageName = classMember.getPackage();
      if (packageToClasses.containsKey(packageName)) {
        packageToClasses.get(packageName).add(classMember);
      } else {
        Set<ClassMember> insertSet = new HashSet<ClassMember>();
        insertSet.add(classMember);
        packageToClasses.put(packageName, insertSet);
      }
    }

    for (String packageName : packageToClasses.keySet()) {

      curLine = "<package id=\"" + packageName + "\">";
      htmlOut.printRawOpt(curLine);
      htmlOut.newlineOpt();

      if (packageToClasses.get(packageName).size() > 0) {
        htmlOut.indentIn();
        htmlOut.indentIn();
        curNumIndents++;
      }
      emitClasses(htmlOut, packageToClasses, packageName);
      if (packageToClasses.get(packageName).size() > 0) {
        htmlOut.indentOut();
        htmlOut.indentOut();
        curNumIndents--;
      }

      curLine = "</package>";
      htmlOut.printRawOpt(curLine);
      htmlOut.newlineOpt();
    }
  }

  private SyntheticArtifact emitReport(TreeLogger logger,
      CompilationAnalysis report, String partialPath, boolean compress)
      throws UnableToCompleteException, IOException {

    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    OutputStreamWriter out;
    try {
      out = new OutputStreamWriter(compress ? new GZIPOutputStream(bytes)
          : bytes);
    } catch (IOException e) {
      logger.log(TreeLogger.ERROR, "Unable to set up gzip stream", e);
      throw new UnableToCompleteException();
    }
    PrintWriter pw = new PrintWriter(out);
    HtmlTextOutput htmlOut = new HtmlTextOutput(pw, false);

    String curLine = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    htmlOut.printRawOpt(curLine);
    htmlOut.newlineOpt();
    curLine = "<soyc>";
    htmlOut.printRawOpt(curLine);
    htmlOut.newlineOpt();
    htmlOut.indentIn();
    htmlOut.indentIn();
    curNumIndents++;

    emitMembers(report, htmlOut);
    Map<Story, Integer> storyIds = emitStories(report, htmlOut);
    emitJs(report, htmlOut, storyIds);

    htmlOut.indentOut();
    htmlOut.indentOut();
    curNumIndents--;

    curLine = "</soyc>";
    htmlOut.printRawOpt(curLine);
    htmlOut.newlineOpt();

    pw.close();
    Utility.close(out);
    SyntheticArtifact toReturn = emitBytes(logger, bytes.toByteArray(),
        partialPath);

    return toReturn;
  }

  private Map<Story, Integer> emitStories(CompilationAnalysis report,
      HtmlTextOutput htmlOut) {

    String curLine;
    Map<Story, Integer> storyIds = new HashMap<Story, Integer>();
    Set<Story> stories = report.getStories();
    curLine = "<stories>";
    htmlOut.printRawOpt(curLine);
    htmlOut.newlineOpt();

    if (stories.size() > 0) {
      htmlOut.indentIn();
      htmlOut.indentIn();
      curNumIndents++;
    }
    for (Story story : stories) {

      int storyNum = storyIds.size();
      storyIds.put(story, storyNum);

      curLine = "<story id=\"story" + Integer.toString(storyNum) + "\"";
      if (story.getLiteralTypeName() != null) {
        curLine = curLine + " literal=\"" + story.getLiteralTypeName() + "\"";
      }
      curLine = curLine + ">";
      htmlOut.printRawOpt(curLine);
      htmlOut.newlineOpt();

      Set<Origin> origins = story.getSourceOrigin();
      if (origins.size() > 0) {
        htmlOut.indentIn();
        htmlOut.indentIn();
        curNumIndents++;

        curLine = "<origins>";
        htmlOut.printRawOpt(curLine);
        htmlOut.newlineOpt();
        htmlOut.indentIn();
        htmlOut.indentIn();
        curNumIndents++;
      }
      for (Origin origin : origins) {
        curLine = "<origin lineNumber=\""
            + Integer.toString(origin.getLineNumber()) + "\" location=\""
            + origin.getLocation() + "\"/>";
        htmlOut.printRawOpt(curLine);
        htmlOut.newlineOpt();
      }
      if (origins.size() > 0) {
        htmlOut.indentOut();
        htmlOut.indentOut();
        curNumIndents--;

        curLine = "</origins>";
        htmlOut.printRawOpt(curLine);
        htmlOut.newlineOpt();

        htmlOut.indentOut();
        htmlOut.indentOut();
        curNumIndents--;
      }

      Set<Member> correlations = story.getMembers();
      if (correlations.size() > 0) {
        htmlOut.indentIn();
        htmlOut.indentIn();
        curNumIndents++;

        curLine = "<correlations>";
        htmlOut.printRawOpt(curLine);
        htmlOut.newlineOpt();

        htmlOut.indentIn();
        htmlOut.indentIn();
        curNumIndents++;
      }
      for (Member correlation : correlations) {
        curLine = "<by idref=\"" + correlation.getSourceName() + "\"/>";
        htmlOut.printRawOpt(curLine);
        htmlOut.newlineOpt();
      }
      if (correlations.size() > 0) {
        htmlOut.indentOut();
        htmlOut.indentOut();
        curNumIndents--;

        curLine = "</correlations>";
        htmlOut.printRawOpt(curLine);
        htmlOut.newlineOpt();

        htmlOut.indentOut();
        htmlOut.indentOut();
        curNumIndents--;
      }

      curLine = "</story>";
      htmlOut.printRawOpt(curLine);
      htmlOut.newlineOpt();
    }
    if (stories.size() > 0) {
      htmlOut.indentOut();
      htmlOut.indentOut();
      curNumIndents--;
    }
    curLine = "</stories>";
    htmlOut.printRawOpt(curLine);
    htmlOut.newlineOpt();

    return storyIds;
  }

  private void initialize(TreeLogger logger) throws UnableToCompleteException {
    logger = logger.branch(TreeLogger.SPAM, "Initializing");
  }

}