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

package com.google.gwt.soyc;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

public class MakeTopLevelHtmlForPerm {
  
  public static void makeHTMLShell(HashMap<String, CodeCollection> nameToCodeColl, TreeMap<String, LiteralsCollection> nameToLitColl) throws IOException{
    //this will contain the place holder iframes where the actual information is going to go.

    String fileName = "SoycDashboard-index.html";
    
    final PrintWriter outFile = new PrintWriter(fileName);
    outFile.println("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML//EN\">");
    outFile.println("<html>");
    outFile.println("<head>");
    outFile.println("<title>Story of Your Compile - Top Level Dashboard for Permutation</title>");
    
    outFile.println("<style type=\"text/css\">");
    outFile.println("body {background-color: #728FCE}");
    outFile.println("h2 {background-color: transparent}");
    outFile.println("p {background-color: fuchsia}");
    outFile.println("</style>");
    outFile.println("</head>");
    
    
    outFile.println("<body>");
    outFile.println("<center>");
    outFile.println("<h3>Story of Your Compile Dashboard</h3>");
    outFile.println("<hr>");
    if (GlobalInformation.cumSizeInitialFragment != 0){
      outFile.println("<b>Full code size: <span style=\"color:maroon\">" + GlobalInformation.cumSizeAllCode + "</span>, Size of Initial Download:  <span style=\"color:maroon\">" + GlobalInformation.cumSizeInitialFragment + "</span></b>");
    }
    else{
      outFile.println("<b>Full code size: <span style=\"color:maroon\">" + GlobalInformation.cumSizeAllCode + "</span></b>");
    }
    outFile.println("</center>");
    outFile.println("  <div style=\"width:50%;  float:left; padding-top: 10px;\">");
    outFile.println("<b>Package breakdown</b>");
    outFile.println("    </div>");
    outFile.println("  <div style=\"width:48%;  float:right; padding-top: 10px; \">");
    outFile.println("<b>Code type breakdown</b>");
    outFile.println("    </div>");
    

    outFile.println("  <div style=\"width:50%;  float:left; padding-top: 10px;\">");
    outFile.println("<div style=\"width: 110px; float: left; font-size:16px;\">Size</div>");
    outFile.println("<div style=\"width: 200px; float: left; text-align:left; font-size:16px; \">Package Name</div>");
    outFile.println("    </div>");
    
    
    outFile.println("  <div style=\"width:48%;  float:right; padding-top: 10px;\">");
    outFile.println("<div style=\"width: 110px; float: left; font-size:16px;\">Size</div>");
    outFile.println("<div style=\"width: 200px; float: left; text-align:left; font-size:16px; \">Code Type</div>");
    outFile.println("    </div>");
    
    
    
    outFile.println("<div style=\"height:35%; width:48%; margin:0 auto; background-color:white; float:left;\">");
    outFile.println("<iframe src=\"packageBreakdown.html\" width=100% height=100% scrolling=auto></iframe>");
    outFile.println("  </div>");
    makePackageHtml("packageBreakdown.html");
    
    outFile.println("<div style=\"height:35%; width:48%; margin:0 auto; background-color:white; float:right;\">");
    outFile.println("<iframe src=\"codeTypeBreakdown.html\" width=100% height=100% scrolling=auto></iframe>");
    outFile.println("  </div>");
    makeCodeTypeHtml("codeTypeBreakdown.html", nameToCodeColl);
    
    outFile.println("  <div style=\"width:50%;  float:left; padding-top: 10px;\">");
    outFile.println("<b>Literals breakdown</b>");
    outFile.println("    </div>");
    outFile.println("  <div style=\"width:48%;  float:right; padding-top: 10px;  \">");
    outFile.println("<b>String literals breakdown</b>");
    outFile.println("    </div>");
    

    outFile.println("  <div style=\"width:50%;  float:left; padding-top: 10px;\">");
    outFile.println("<div style=\"width: 110px; float: left; font-size:16px;\">Size</div>");
    outFile.println("<div style=\"width: 200px; float: left; text-align:left; font-size:16px; \">Literal Type</div>");
    outFile.println("    </div>");
    
    
    outFile.println("  <div style=\"width:48%;  float:right; padding-top: 10px; \">");
    outFile.println("<div style=\"width: 110px; float: left; font-size:16px;\">Size</div>");
    outFile.println("<div style=\"width: 200px; float: left; text-align:left; font-size:16px; \">String Literal Type</div>");
    outFile.println("    </div>");
    
    
    outFile.println("<div style=\"height:35%; width:48%; margin:0 auto; background-color:white; float:left;\">");
    outFile.println("<iframe src=\"literalsBreakdown.html\" width=100% height=100% scrolling=auto></iframe>");
    outFile.println("</div>");
    makeLiteralsHtml("literalsBreakdown.html", nameToLitColl);

    outFile.println("<div style=\"height:35%; width:48%; margin:0 auto; background-color:white; float:right;\">");
    outFile.println("<iframe src=\"stringLiteralsBreakdown.html\" width=100% height=100% scrolling=auto></iframe>");
    outFile.println("  </div>");
    makeStringLiteralsHtml("stringLiteralsBreakdown.html", nameToLitColl);



    if (GlobalInformation.fragmentToStories.size() == 1){
      outFile.println("  <div style=\"width:100%;  float:left; padding-top: 10px;\">");
      outFile.println("<b>Fragments breakdown</b>");
      outFile.println("    </div>");
  
      outFile.println("  <div style=\"width:100%;  float:left; padding-top: 10px;\">");
      outFile.println("<div style=\"width: 110px; float: left; font-size:16px;\">Size</div>");
      outFile.println("<div style=\"width: 200px; float: left; text-align:left; font-size:16px; \">Fragment Name</div>");
      outFile.println("    </div>");
      
      outFile.println("<div style=\"height:35%; width:50%; margin:0 auto; background-color:white; float:left;\">");
      outFile.println("<iframe src=\"fragmentsBreakdown.html\" width=100% height=100% scrolling=auto></iframe>");
      outFile.println("</div>");
      makeFragmentsHtml("fragmentsBreakdown.html");
    }

    outFile.println("  </body>");
    outFile.println("</html>");
    outFile.close();
    
  }

  
  private static void makePackageHtml(String outFileName) throws IOException{
    
    
    TreeMap<Float, String> sortedPackages = new TreeMap<Float, String>(Collections.reverseOrder());
    float maxSize = 0f;
    float sumSize = 0f;
    for (String packageName : GlobalInformation.packageToPartialSize.keySet()){
      sortedPackages.put(GlobalInformation.packageToPartialSize.get(packageName), packageName);
      sumSize += GlobalInformation.packageToPartialSize.get(packageName);
      if (GlobalInformation.packageToPartialSize.get(packageName) > maxSize){
        maxSize = GlobalInformation.packageToPartialSize.get(packageName);
      }
    }
    
    final PrintWriter outFile = new PrintWriter(outFileName);
    
    outFile.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"");
    outFile.println("\"http://www.w3.org/TR/html4/strict.dtd\">");
    outFile.println("<html>");
    outFile.println("<head>");
    outFile.println("<meta http-equiv=\"content-type\" content=\"text/html;charset=ISO-8859-1\">");
    outFile.println("<link rel=\"stylesheet\" href=\"roundedCorners.css\" media=\"screen\">");
    outFile.println("</head>");
    outFile.println("<body>");

    int yOffset = 0;
    for (Float size : sortedPackages.keySet()){
      
      String packageName = sortedPackages.get(size);
      String drillDownFileName = packageName + "Classes.html";
      
      float ratio = (size / maxSize) * 79;
      
      if (ratio < 3){
        ratio = 3;
      }

      float perc = (size / sumSize) * 100;
      
      outFile.println("<div id=\"box\" style=\"width:" + ratio + "%; top: " + yOffset + "px; left: 110px;\">");
      outFile.println("<div id=\"lb\">");
      outFile.println("<div id=\"rb\">");
      outFile.println("<div id=\"bb\"><div id=\"blc\"><div id=\"brc\">");
      outFile.println("<div id=\"tb\"><div id=\"tlc\"><div id=\"trc\">");
      outFile.println("<div id=\"content\">");
      outFile.println("</div>");
      outFile.println("</div></div></div></div>");
      outFile.println("</div></div></div></div>");
      outFile.println("</div>");
      
      int yOffsetText = yOffset+8;
      outFile.printf("<div class=\"barlabel\" style=\"top:" + yOffsetText + "px; left:5px;\">%.1f</div>\n", size);
      outFile.printf("<div class=\"barlabel\" style=\"top:" + yOffsetText + "px; left:70px;\">%.1f", perc);
      outFile.println("%</div>\n");
      outFile.println("<div class=\"barlabel\" style=\"top:" + yOffsetText + "px; left:110px;\"><a href=\"" + drillDownFileName + "\" target=\"_top\">"+packageName+"</a></div>");
      
      yOffset = yOffset + 25;

    }
    outFile.println("</body>");
    outFile.println("</html>");
    outFile.close();
    
  }
  
  
  private static void makeFragmentsHtml(String outFileName) throws IOException{
    
    TreeMap<Float, Integer> sortedFragments = new TreeMap<Float, Integer>(Collections.reverseOrder());
    float maxSize = 0f;
    float sumSize = 0f;
    for (Integer fragmentName : GlobalInformation.fragmentToPartialSize.keySet()){
      sortedFragments.put(GlobalInformation.fragmentToPartialSize.get(fragmentName), fragmentName);
      sumSize += GlobalInformation.fragmentToPartialSize.get(fragmentName);
      if (GlobalInformation.fragmentToPartialSize.get(fragmentName) > maxSize){
        maxSize = GlobalInformation.fragmentToPartialSize.get(fragmentName);
      }
    }
    
    final PrintWriter outFile = new PrintWriter(outFileName);
    
    outFile.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"");
    outFile.println("\"http://www.w3.org/TR/html4/strict.dtd\">");
    outFile.println("<html>");
    outFile.println("<head>");
    outFile.println("<meta http-equiv=\"content-type\" content=\"text/html;charset=ISO-8859-1\">");
    outFile.println("<link rel=\"stylesheet\" href=\"roundedCorners.css\" media=\"screen\">");
    outFile.println("</head>");
    outFile.println("<body>");

    int yOffset = 0;
    for (Float size : sortedFragments.keySet()){
      
      Integer fragmentName = sortedFragments.get(size);
      String drillDownFileName = "fragment" + Integer.toString(fragmentName) + "Classes.html";
      
      float ratio = (size / maxSize) * 79;
      
      if (ratio < 3){
        ratio = 3;
      }

      float perc = (size / sumSize) * 100;
      
      outFile.println("<div id=\"box\" style=\"width:" + ratio + "%; top: " + yOffset + "px; left: 110px;\">");
      outFile.println("<div id=\"lb\">");
      outFile.println("<div id=\"rb\">");
      outFile.println("<div id=\"bb\"><div id=\"blc\"><div id=\"brc\">");
      outFile.println("<div id=\"tb\"><div id=\"tlc\"><div id=\"trc\">");
      outFile.println("<div id=\"content\">");
      outFile.println("</div>");
      outFile.println("</div></div></div></div>");
      outFile.println("</div></div></div></div>");
      outFile.println("</div>");
      
      int yOffsetText = yOffset+8;
      outFile.printf("<div class=\"barlabel\" style=\"top:" + yOffsetText + "px; left:5px;\">%.1f</div>\n", size);
      outFile.printf("<div class=\"barlabel\" style=\"top:" + yOffsetText + "px; left:80px;\">%.1f", perc);
      outFile.println("%</div>\n");
      outFile.println("<div class=\"barlabel\" style=\"top:" + yOffsetText + "px; left:120px;\"><a href=\"" + drillDownFileName + "\" target=\"_top\">fragment"+ Integer.toString(fragmentName) +"</a></div>");
      
      yOffset = yOffset + 25;

    }
    outFile.println("</body>");
    outFile.println("</html>");
    outFile.close();
    
  }
  

  
  
  private static void makeCodeTypeHtml(String outFileName, HashMap<String, CodeCollection> nameToCodeColl) throws IOException{
    
    
    float maxSize = 0f;
    float sumSize = 0f;
    TreeMap<Float, String> sortedCodeTypes = new TreeMap<Float, String>(Collections.reverseOrder());
    
    //TODO(kprobst): turn this into a multimap? com.google.common.collect.TreeMultimap
    for (String codeType : nameToCodeColl.keySet()){
      float curSize = nameToCodeColl.get(codeType).getCumPartialSize();
      sumSize += curSize;
      
      if (curSize != 0f){
        sortedCodeTypes.put(curSize, codeType);
        if (curSize > maxSize){
          maxSize = curSize;
        }
      }
    }
    
    final PrintWriter outFile = new PrintWriter(outFileName);
    
    outFile.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"");
    outFile.println("\"http://www.w3.org/TR/html4/strict.dtd\">");
    outFile.println("<html>");
    outFile.println("<head>");
    outFile.println("<meta http-equiv=\"content-type\" content=\"text/html;charset=ISO-8859-1\">");
    outFile.println("<link rel=\"stylesheet\" href=\"roundedCorners.css\" media=\"screen\">");
    outFile.println("</head>");
    outFile.println("<body>");

    int yOffset = 0;
    for (Float size : sortedCodeTypes.keySet()){
      
      String codeType = sortedCodeTypes.get(size);
      String drillDownFileName = codeType + "Classes.html";
      
      float ratio = (size / maxSize) * 79;    
      float perc = (size / sumSize) * 100;
      
      if (ratio < 3){
        ratio = 3;
      }

      outFile.println("<div id=\"box\" style=\"width:" + ratio + "%; top: " + yOffset + "px; left: 110px;\">");
      outFile.println("<div id=\"lb\">");
      outFile.println("<div id=\"rb\">");
      outFile.println("<div id=\"bb\"><div id=\"blc\"><div id=\"brc\">");
      outFile.println("<div id=\"tb\"><div id=\"tlc\"><div id=\"trc\">");
      outFile.println("<div id=\"content\">");
      outFile.println("</div>");
      outFile.println("</div></div></div></div>");
      outFile.println("</div></div></div></div>");
      outFile.println("</div>");
      
      int yOffsetText = yOffset+8;
      outFile.printf("<div class=\"barlabel\" style=\"top:" + yOffsetText + "px; left:5px;\">%.1f</div>\n", size);
      outFile.printf("<div class=\"barlabel\" style=\"top:" + yOffsetText + "px; left:70px;\">%.1f", perc);
      outFile.println("%</div>\n");
      outFile.println("<div class=\"barlabel\" style=\"top:" + yOffsetText + "px; left:110px;\"><a href=\"" + drillDownFileName + "\" target=\"_top\">"+codeType+"</a></div>");
      
      yOffset = yOffset + 25;

    }
    outFile.println("</body>");
    outFile.println("</html>");
    outFile.close();
    
    
  }
  


  private static void makeLiteralsHtml(String outFileName, TreeMap<String, LiteralsCollection> nameToLitColl) throws IOException{
    

    float maxSize = 0f;
    float sumSize = 0f;
    TreeMap<Float, String> sortedLitTypes = new TreeMap<Float, String>(Collections.reverseOrder());
 
    for (String literal : nameToLitColl.keySet()){
      float curSize = nameToLitColl.get(literal).cumSize;
      sumSize += curSize;
      
      if (curSize != 0f){
        sortedLitTypes.put(curSize, literal);
      
        if (curSize > maxSize){
          maxSize = curSize;
        }
      }
    }


    final PrintWriter outFile = new PrintWriter(outFileName);
    
    outFile.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"");
    outFile.println("\"http://www.w3.org/TR/html4/strict.dtd\">");
    outFile.println("<html>");
    outFile.println("<head>");
    outFile.println("<meta http-equiv=\"content-type\" content=\"text/html;charset=ISO-8859-1\">");
    outFile.println("<link rel=\"stylesheet\" href=\"roundedCorners.css\" media=\"screen\">");
    outFile.println("</head>");
    outFile.println("<body>");

    int yOffset = 0;
    for (Float size : sortedLitTypes.keySet()){
      
      String literal = sortedLitTypes.get(size);
      String drillDownFileName = literal + "Lits.html";
      
      float ratio = (size / maxSize) * 79;   
      float perc = (size / sumSize) * 100;
      
      if (ratio < 3){
        ratio = 3;
      }
      
      outFile.println("<div id=\"box\" style=\"width:" + ratio + "%; top: " + yOffset + "px; left: 110px;\">");
      outFile.println("<div id=\"lb\">");
      outFile.println("<div id=\"rb\">");
      outFile.println("<div id=\"bb\"><div id=\"blc\"><div id=\"brc\">");
      outFile.println("<div id=\"tb\"><div id=\"tlc\"><div id=\"trc\">");
      outFile.println("<div id=\"content\">");
      outFile.println("</div>");
      outFile.println("</div></div></div></div>");
      outFile.println("</div></div></div></div>");
      outFile.println("</div>");
      
      int yOffsetText = yOffset+8;
      outFile.printf("<div class=\"barlabel\" style=\"top:" + yOffsetText + "px; left:5px;\">%.1f</div>\n", size);
      outFile.printf("<div class=\"barlabel\" style=\"top:" + yOffsetText + "px; left:70px;\">%.1f", perc);
      outFile.println("%</div>\n");
      outFile.println("<div class=\"barlabel\" style=\"top:" + yOffsetText + "px; left:110px;\"><a href=\"" + drillDownFileName + "\" target=\"_top\">"+literal+"</a></div>");
      
      yOffset = yOffset + 25;

    }
    outFile.println("</body>");
    outFile.println("</html>");
    outFile.close();
    
  }
  

  private static void makeStringLiteralsHtml(String outFileName, TreeMap<String, LiteralsCollection> nameToLitColl) throws IOException{

    final PrintWriter outFile = new PrintWriter(outFileName);
    
    outFile.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"");
    outFile.println("\"http://www.w3.org/TR/html4/strict.dtd\">");
    outFile.println("<html>");
    outFile.println("<head>");
    outFile.println("<meta http-equiv=\"content-type\" content=\"text/html;charset=ISO-8859-1\">");
    outFile.println("<link rel=\"stylesheet\" href=\"roundedCorners.css\" media=\"screen\">");
    outFile.println("</head>");
    outFile.println("<body>");
    
    
    if (nameToLitColl.get("string").stringTypeToSize.size() > 0){
      
      float maxSize = 0f;
      float sumSize = 0f;
      TreeMap<Float, String> sortedStLitTypes = new TreeMap<Float, String>(Collections.reverseOrder());
      
      for (String stringLiteral : nameToLitColl.get("string").stringTypeToSize.keySet()){
        float curSize = nameToLitColl.get("string").stringTypeToSize.get(stringLiteral);
        sumSize += curSize;
        
        if (curSize != 0f){
          sortedStLitTypes.put(curSize, stringLiteral);
          
          if (curSize > maxSize){
            maxSize = curSize;
          }
        }
      }

  
      int yOffset = 0;
      for (Float size : sortedStLitTypes.keySet()){
        
        String stringLiteral = sortedStLitTypes.get(size);
        String drillDownFileName = stringLiteral + "Strings.html";
        
        float ratio = (size / maxSize) * 79;
        float perc = (size / sumSize) * 100;
        
        if (ratio < 3){
          ratio = 3;
        }
  
        outFile.println("<div id=\"box\" style=\"width:" + ratio + "%; top: " + yOffset + "px; left: 110px;\">");
        outFile.println("<div id=\"lb\">");
        outFile.println("<div id=\"rb\">");
        outFile.println("<div id=\"bb\"><div id=\"blc\"><div id=\"brc\">");
        outFile.println("<div id=\"tb\"><div id=\"tlc\"><div id=\"trc\">");
        outFile.println("<div id=\"content\">");
        outFile.println("</div>");
        outFile.println("</div></div></div></div>");
        outFile.println("</div></div></div></div>");
        outFile.println("</div>");
        
        int yOffsetText = yOffset+8;
        outFile.printf("<div class=\"barlabel\" style=\"top:" + yOffsetText + "px; left:5px;\">%.1f</div>\n", size);
        outFile.printf("<div class=\"barlabel\" style=\"top:" + yOffsetText + "px; left:70px;\">%.1f", perc);
        outFile.println("%</div>\n");
        outFile.println("<div class=\"barlabel\" style=\"top:" + yOffsetText + "px; left:110px;\"><a href=\"" + drillDownFileName + "\" target=\"_top\">"+stringLiteral+"</a></div>");
        
        yOffset = yOffset + 25;
  
      }
    
    }
    
    else{
      outFile.println("No string literals found for this application.");
      
    }

    outFile.println("</body>");
    outFile.println("</html>");
    outFile.close();
  }
  
  

  public static void makePackageClassesHtmls() throws IOException{
        
    for (String packageName : GlobalInformation.packageToClasses.keySet()){
      
      String outFileName = packageName + "Classes.html";
      TreeMap<Float, String> sortedClasses = new TreeMap<Float, String>(Collections.reverseOrder());
      float maxSize = 0f;
      
      //TODO(kprobst): not currently used, but will be for dependencies
      int maxDepCount = 1;

      for (String className : GlobalInformation.packageToClasses.get(packageName)){
        
        float curSize = 0f;
        if (! GlobalInformation.classToPartialSize.containsKey(className)){
          System.err.println("*** NO PARTIAL SIZE FOUND FOR CLASS " + className + " *****");
        }
        else{
          curSize = GlobalInformation.classToPartialSize.get(className);
        }
          
        //TODO(kprobst): not currently used, but will be for dependencies
        int depCount = 0;
        if (GlobalInformation.classToWhatItDependsOn.containsKey(className)){
          depCount = GlobalInformation.classToWhatItDependsOn.get(className).size();
        }
        
        if (curSize != 0f){
          
          sortedClasses.put(curSize, className);
          if (curSize > maxSize){
            maxSize = curSize;
          }
          //TODO(kprobst): not currently used, but will be for dependencies
          if (depCount > maxDepCount){
            maxDepCount = depCount;
          }
        }
      }
      
        
      final PrintWriter outFile = new PrintWriter(outFileName);
      
      outFile.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"");
      outFile.println("\"http://www.w3.org/TR/html4/strict.dtd\">");
      outFile.println("<html>");
      outFile.println("<head>");
      outFile.println("<meta http-equiv=\"content-type\" content=\"text/html;charset=ISO-8859-1\">");
      outFile.println("<link rel=\"stylesheet\" href=\"classLevel.css\" media=\"screen\">");
      outFile.println("<title>Classes in package \"" + packageName + "\"</title>");
      outFile.println("</head>");
      outFile.println("<body>");
      

      outFile.println("<center>");
      outFile.println("<h2>Classes in package \"" + packageName + "\"</h2>");
      outFile.println("</center>");
      outFile.println("<hr>");

      outFile.println("<div style=\"width:90%; height:80%; overflow-y:auto; overflow-x:auto; top: 90px; left:70px; position:absolute; background-color:white\"");

  
      int yOffset = 0;
      for (Float size : sortedClasses.keySet()){
        
        String className = sortedClasses.get(size);
        
        //TODO(kprobst): switch out the commented/uncommented lines below when showing dependencies
        float ratio = (size / maxSize) * 45;
        //float ratio = (size / maxSize) * 85;
         
        if (ratio < 3){
          ratio = 3;
        }
        
        //TODO(kprobst): not currently used, but will be for dependencies
       // get the dependency count
        int depCount = 0;
        if (GlobalInformation.classToWhatItDependsOn.containsKey(className)){
          depCount = GlobalInformation.classToWhatItDependsOn.get(className).size();
        }
        float depRatio = ((float)depCount / (float)maxDepCount) * 45f;
        if (depRatio < 3.0){
          depRatio = 3;
        }
  
        outFile.println("<div class=\"box\" style=\"width:" + ratio + "%; top: " + yOffset + "px; left: 60px;\">");
        outFile.println("<div id=\"lb\">");
        outFile.println("<div id=\"rb\">");
        outFile.println("<div id=\"bb\"><div id=\"blc\"><div id=\"brc\">");
        outFile.println("<div id=\"tb\"><div id=\"tlc\"><div id=\"trc\">");
        outFile.println("<div id=\"content\">");
        outFile.println("</div>");
        outFile.println("</div></div></div></div>");
        outFile.println("</div></div></div></div>");
        outFile.println("</div>");
        
        
        //TODO(kprobst): not currently used, but will be for dependencies
        // place holder for mock-up of dependency display  
        outFile.println("<div class=\"box-right\" style=\"width:" + depRatio + "%; top: " + yOffset + "px; left: 50%\">");
        outFile.println("<div id=\"lb\">");
        outFile.println("<div id=\"rb\">");
        outFile.println("<div id=\"bb\"><div id=\"blc\"><div id=\"brc\">");
        outFile.println("<div id=\"tb\"><div id=\"tlc\"><div id=\"trc\">");
        outFile.println("<div id=\"content\">");
        outFile.println("</div>");
        outFile.println("</div></div></div></div>");
        outFile.println("</div></div></div></div>");
        outFile.println("</div>");
              
        int yOffsetText = yOffset+8;
        outFile.printf("<div class=\"barlabel\" style=\"top:" + yOffsetText + "px; left:5px;\">%.1f</div>\n", size);
        outFile.println("<div class=\"barlabel\" style=\"top:" + yOffsetText + "px; left:70px;\">"+className+"</div>");
        //TODO(kprobst) make this a link
        String drillDownFileName = className + "Deps.html";
        outFile.println("<div class=\"barlabel\" style=\"top:" + yOffsetText + "px; left:50%;\"><a href=\"" + drillDownFileName + "\" target=\"_top\">Dependencies: " + depCount + "</a></div>");
        
        yOffset = yOffset + 25;
  
      }
      
      outFile.println("</div>");
      outFile.println("</body>");
      outFile.println("</html>");
      outFile.close();
    }
  }
  

  public static void makeCodeTypeClassesHtmls(HashMap<String, CodeCollection> nameToCodeColl) throws IOException{
    
    for (String codeType : nameToCodeColl.keySet()){
      
      //construct file name
      String outFileName = codeType + "Classes.html";
      
  
      float maxSize = 0f;
      TreeMap<Float, String> sortedClasses = new TreeMap<Float, String>(Collections.reverseOrder());
      for (String className : nameToCodeColl.get(codeType).classes){
        if (GlobalInformation.classToPartialSize.containsKey(className)){

          float curSize = 0f;
          if (! GlobalInformation.classToPartialSize.containsKey(className)){
            System.err.println("*** NO PARTIAL SIZE FOUND FOR CLASS " + className + " *****");
          }
          else{
            curSize = GlobalInformation.classToPartialSize.get(className);
          }
          
          if (curSize != 0f){
            sortedClasses.put(curSize, className);
            if (curSize > maxSize){
              maxSize = curSize;
            }
          }
        }
      }
  
      final PrintWriter outFile = new PrintWriter(outFileName);
      
      outFile.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"");
      outFile.println("\"http://www.w3.org/TR/html4/strict.dtd\">");
      outFile.println("<html>");
      outFile.println("<head>");

      outFile.println("<style type=\"text/css\">");
      outFile.println("body {background-color: #728FCE}");
      outFile.println("h2 {background-color: transparent}");
      outFile.println("p {background-color: fuchsia}");
      outFile.println("</style>");
      
      outFile.println("<meta http-equiv=\"content-type\" content=\"text/html;charset=ISO-8859-1\">");
      outFile.println("<link rel=\"stylesheet\" href=\"classLevel.css\" media=\"screen\">");
      outFile.println("<title>Classes of type \"" + codeType + "\"</title>");
      outFile.println("</head>");
      outFile.println("<body>");
      


      outFile.println("<center>");
      outFile.println("<h2>Classes of type \"" + codeType + "\"</h2>");
      outFile.println("</center>");
      outFile.println("<hr>");

      outFile.println("<div style=\"width:90%; height:80%; overflow-y:auto; overflow-x:auto; top: 90px; left:70px; position:absolute; background-color:white\"");

  
      int yOffset = 0;
      for (Float size : sortedClasses.keySet()){
        
        String className = sortedClasses.get(size);
        
        float ratio = (size / maxSize) * 85;
        
        if (ratio < 3){
          ratio = 3;
        }
  
        outFile.println("<div class=\"box\" style=\"width:" + ratio + "%; top: " + yOffset + "px; left: 60px;\">");
        outFile.println("<div id=\"lb\">");
        outFile.println("<div id=\"rb\">");
        outFile.println("<div id=\"bb\"><div id=\"blc\"><div id=\"brc\">");
        outFile.println("<div id=\"tb\"><div id=\"tlc\"><div id=\"trc\">");
        outFile.println("<div id=\"content\">");
        outFile.println("</div>");
        outFile.println("</div></div></div></div>");
        outFile.println("</div></div></div></div>");
        outFile.println("</div>");
        
        int yOffsetText = yOffset+8;
         outFile.printf("<div class=\"barlabel\" style=\"top:" + yOffsetText + "px; left:5px;\">%.1f</div>\n", size);
         outFile.println("<div class=\"barlabel\" style=\"top:" + yOffsetText + "px; left:70px;\">"+className+"</div>");
        
        yOffset = yOffset + 25;
  
      }
      
      outFile.println("</div>");
      outFile.println("</body>");
      outFile.println("</html>");
      outFile.close();
    }    
  }


  public static void makeLiteralsClassesTableHtmls(TreeMap<String, LiteralsCollection> nameToLitColl) throws IOException{

    for (String literalType : nameToLitColl.keySet()){
      
      String outFileName = literalType + "Lits.html";
      final PrintWriter outFile = new PrintWriter(outFileName);
      
      outFile.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"");
      outFile.println("\"http://www.w3.org/TR/html4/strict.dtd\">");
      outFile.println("<html>");
      outFile.println("<head>");
      outFile.println("<meta http-equiv=\"content-type\" content=\"text/html;charset=ISO-8859-1\">");
      outFile.println("<title>Literals of type \"" + literalType + "\"</title>");
      outFile.println("</head>");

      outFile.println("<style type=\"text/css\">");
      outFile.println("body {background-color: #728FCE}");
      outFile.println("h2 {background-color: transparent}");
      outFile.println("p {background-color: fuchsia}");
      outFile.println("</style>");
      
      outFile.println("<body>");
      outFile.println("<center>");
      outFile.println("<h2>Literals of type \"" + literalType + "\"</h2>");
      outFile.println("</center>");
      outFile.println("<hr>");
      
      outFile.println("<center>");
      outFile.println("<table border=\"1\" width=\"80%\" style=\"font-size: 11pt;\" bgcolor=\"white\">");
   
      for (String literal : nameToLitColl.get(literalType).literalToLocations.keySet()){
  
        if (literal.trim().compareTo("") == 0){
          literal = "[whitespace only string]";
        }
        

        String newLiteral = "";
        if(literal.length() > 80){
          int i;
          for (i = 80; i < literal.length(); i=i+80){
            String part1 = literal.substring(i-80, i);
            newLiteral = newLiteral + part1 + " "; 
          }
          if (i-80 > 0){
            newLiteral = newLiteral + literal.substring(i-80);
          }
        }
        else{
          newLiteral = literal;
        }
        
        String escliteral = escapeXml(newLiteral);

        outFile.println("<tr>");
        outFile.println("<td width=\"40%\">" + escliteral + "</td>");
        

        int ct = 0;
        for (String location : nameToLitColl.get(literalType).literalToLocations.get(literal)){
          
          if (ct > 0){
            outFile.println("<tr>");  
            outFile.println("<td width=\"40%\"> </td>");          
          }
          
          String newLocation = "";
          if(location.length() > 80){
            int i;
            for (i = 80; i < location.length(); i=i+80){
              String part1 = location.substring(i-80, i);
              newLocation = newLocation + part1 + " "; 
            }
            if (i-80 > 0){
              newLocation = newLocation + location.substring(i-80);
            }
          }
          else{
            newLocation = location;
          }
          
          
          outFile.println("<td width=\"40%\">" + newLocation + "</td>");

          if (ct > 0){
            outFile.println("</tr>");
          }
          ct++;
          
        }

        
        outFile.println("</tr>");
        
      }

      outFile.println("</table>");
      outFile.println("<center>");

      
      outFile.println("</div>");
      outFile.println("</body>");
      outFile.println("</html>");
      outFile.close();
     }
  }

  public static void makeLiteralsClassesHtmls(TreeMap<String, LiteralsCollection> nameToLitColl) throws IOException{
    
    
    for (String literalType : nameToLitColl.keySet()){
      
      String outFileName = literalType + "Lits.html";
      final PrintWriter outFile = new PrintWriter(outFileName);
      
      outFile.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"");
      outFile.println("\"http://www.w3.org/TR/html4/strict.dtd\">");
      outFile.println("<html>");
      outFile.println("<head>");
      outFile.println("<meta http-equiv=\"content-type\" content=\"text/html;charset=ISO-8859-1\">");
      outFile.println("<title>Literals of type \"" + literalType + "\"</title>");
      outFile.println("</head>");

      outFile.println("<style type=\"text/css\">");
      outFile.println("body {background-color: #728FCE}");
      outFile.println("h2 {background-color: transparent}");
      outFile.println("p {background-color: fuchsia}");
      outFile.println(".tablediv {");
      outFile.println("display:  table;");
      outFile.println("width:100%;");
      outFile.println("background-color:#eee;");
      outFile.println("border:1px solid  #666666;");
      outFile.println("border-spacing:5px;/*cellspacing:poor IE support for  this*/");
      outFile.println("border-collapse:separate;");
      outFile.println("}");
      outFile.println(".celldiv {");
      outFile.println("float:left;/*fix for  buggy browsers*/");
      outFile.println("display:  table-cell;");
      outFile.println("width:50%;");
      outFile.println("font-size: 14px;");
      outFile.println("background-color:white;");
      outFile.println("}");
      outFile.println(".rowdiv  {");
      outFile.println("display:  table-row;");
      //outFile.println("width:90%;");
      outFile.println("}");
      outFile.println("</style>");
      
      outFile.println("<body>");
      outFile.println("<center>");
      outFile.println("<h2>Literals of type \"" + literalType + "\"</h2>");
      outFile.println("</center>");
      outFile.println("<hr>");
      
      outFile.println("<div style=\"width:90%; height:80%; overflow-y:auto; overflow-x:auto; top: 30px; left:60px; position:relative; background-color:white\"");  
      outFile.println("<div class=\"tablediv\">");      
      for (String literal : nameToLitColl.get(literalType).literalToLocations.keySet()){
  
        if (literal.trim().compareTo("") == 0){
          literal = "[whitespace only string]";
        }
        

        String newLiteral = "";
        if(literal.length() > 100){
          int i;
          for (i = 100; i < literal.length(); i=i+100){
            String part1 = literal.substring(i-100, i);
            newLiteral = newLiteral + part1 + " "; 
          }
          if (i-100 > 0){
            newLiteral = newLiteral + literal.substring(i-100);
          }
        }
        else{
          newLiteral = literal;
        }
        
        String escliteral = escapeXml(newLiteral);
        outFile.println("<div class=\"rowdiv\">");   
        outFile.println("<div class=\"celldiv\">" + escliteral + "</div>");

        for (String location : nameToLitColl.get(literalType).literalToLocations.get(literal)){
          
          String newLocation = "";
          if(location.length() > 100){
            int i;
            for (i = 100; i < location.length(); i=i+100){
              String part1 = location.substring(i-100, i);
              newLocation = newLocation + part1 + " "; 
            }
            if (i-100 > 0){
              newLocation = newLocation + location.substring(i-100);
            }
          }
          else{
            newLocation = location;
          }
          
          outFile.println("<div class=\"celldiv\">" + newLocation + "</div>");
        }
        outFile.println("</div>");
      }
      outFile.println("</div>");
      outFile.println("</body>");
      outFile.println("</html>");
      outFile.close();
     }
  }

  /**
   * Makes html file for fragment classes. 
   * TODO(kprobst): update this once we have SOYC updated to supply enough information
   * @throws IOException
   */
  public static void makeFragmentClassesHtmls() throws IOException{
    
    
    for (Integer fragmentName : GlobalInformation.fragmentToStories.keySet()){
      HashSet<String> alreadyPrintedClasses = new HashSet<String>();
      
      String outFileName = "fragment" + Integer.toString(fragmentName) + "Classes.html";
      
      final PrintWriter outFile = new PrintWriter(outFileName);
      
      outFile.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"");
      outFile.println("\"http://www.w3.org/TR/html4/strict.dtd\">");
      outFile.println("<html>");
      outFile.println("<head>");
      outFile.println("<meta http-equiv=\"content-type\" content=\"text/html;charset=ISO-8859-1\">");
      outFile.println("<title>Classes correlated with fragment " + Integer.toString(fragmentName) + " </title>");
      outFile.println("</head>");

      outFile.println("<style type=\"text/css\">");
      outFile.println("body {background-color: #728FCE}");
      outFile.println("h2 {background-color: transparent}");
      outFile.println("p {background-color: fuchsia}");
      outFile.println(".tablediv {");
      outFile.println("display:  table;");
      outFile.println("width:100%;");
      outFile.println("background-color:#eee;");
      outFile.println("border:1px solid  #666666;");
      outFile.println("border-spacing:5px;/*cellspacing:poor IE support for  this*/");
      outFile.println("border-collapse:separate;");
      outFile.println("}");
      outFile.println(".celldiv {");
      outFile.println("float:left;/*fix for  buggy browsers*/");
      outFile.println("display:  table-cell;");
      outFile.println("width:49.5%;");
      outFile.println("font-size: 14px;");
      outFile.println("background-color:white;");
      outFile.println("}");
      outFile.println(".rowdiv  {");
      outFile.println("display:  table-row;");
      outFile.println("width:100%;");
      outFile.println("}");
      outFile.println("</style>");
      
      outFile.println("<body>");
      outFile.println("<center>");
      outFile.println("<h2>Classes correlated with fragment " + Integer.toString(fragmentName) + "</h2>");
      outFile.println("</center>");
      outFile.println("<hr>");
      
      outFile.println("<div style=\"width:90%; height:80%; overflow-y:auto; overflow-x:auto; top: 30px; left:60px; position:relative; background-color:white\"");  
      outFile.println("<div  class=\"tablediv\">");      
      
      for (String storyName : GlobalInformation.fragmentToStories.get(fragmentName)){
        if (GlobalInformation.storiesToCorrClasses.containsKey(storyName)){
          for (String className : GlobalInformation.storiesToCorrClasses.get(storyName)){
  
            if (! alreadyPrintedClasses.contains(className)){
              //outFile.println("<div class=\"rowdiv\">");   
              outFile.println("<div  class=\"rowdiv\">" + className + "</div>");
              //outFile.println("</div>");
              alreadyPrintedClasses.add(className);
            }
          }
        }
      }
      outFile.println("</div>");
      outFile.println("</body>");
      outFile.println("</html>");
      outFile.close();
     }
  }

  
  public static void makeStringLiteralsClassesHtmls(TreeMap<String, LiteralsCollection> nameToLitColl) throws IOException{
    
    
    for (String literalType : nameToLitColl.get("string").stringTypeToSize.keySet()){
      
      String outFileName = literalType + "Strings.html";
      
      final PrintWriter outFile = new PrintWriter(outFileName);
      
      
      outFile.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"");
      outFile.println("\"http://www.w3.org/TR/html4/strict.dtd\">");
      outFile.println("<html>");
      outFile.println("<head>");
      outFile.println("<meta http-equiv=\"content-type\" content=\"text/html;charset=ISO-8859-1\">");
      outFile.println("<title>Literals of type \"" + literalType + "\"</title>");
      outFile.println("</head>");

      outFile.println("<style type=\"text/css\">");
      outFile.println("body {background-color: #728FCE}");
      outFile.println("h2 {background-color: transparent}");
      outFile.println("p {background-color: fuchsia}");
      outFile.println(".tablediv {");
      outFile.println("display:  table;");
      outFile.println("width:100%;");
      outFile.println("background-color:#eee;");
      outFile.println("border:1px solid  #666666;");
      outFile.println("border-spacing:5px;/*cellspacing:poor IE support for  this*/");
      outFile.println("border-collapse:separate;");
      outFile.println("}");
      outFile.println(".celldiv {");
      outFile.println("float:left;/*fix for  buggy browsers*/");
      outFile.println("display:  table-cell;");
      outFile.println("width:49.5%;");
      outFile.println("font-size: 14px;");
      outFile.println("background-color:white;");
      outFile.println("}");
      outFile.println(".rowdiv  {");
      outFile.println("display:  table-row;");
      outFile.println("width:100%;");
      outFile.println("}");
      outFile.println("</style>");
      
      outFile.println("<body>");
      outFile.println("<center>");
      outFile.println("<h2>Literals of type \"" + literalType + "\"</h2>");
      outFile.println("</center>");
      outFile.println("<hr>");
      
      outFile.println("<div style=\"width:90%; height:80%; overflow-y:auto; overflow-x:auto; top: 30px; left:60px; position:relative; background-color:white\"");  
      outFile.println("<div  class=\"tablediv\">");      

      
      for (String literal : nameToLitColl.get("string").stringLiteralToType.keySet()){
        
        if (nameToLitColl.get("string").stringLiteralToType.get(literal).compareTo(literalType) == 0){
       

          if (literal.trim().compareTo("") == 0){
            literal = "[whitespace only string]";
          }
          
          String newLiteral = "";
          if(literal.length() > 100){
            int i;
            for (i = 100; i < literal.length(); i=i+100){
              String part1 = literal.substring(i-100, i);
              newLiteral = newLiteral + part1 + " "; 
            }
            if (i-100 > 0){
              newLiteral = newLiteral + literal.substring(i-100);
            }
          }
          else{
            newLiteral = literal;
          }
          
          String escliteral = escapeXml(newLiteral);
          outFile.println("<div class=\"rowdiv\">");   
          outFile.println("<div  class=\"celldiv\">" + escliteral + "</div>");

          for (String location : nameToLitColl.get("string").literalToLocations.get(literal)){
            
            String newLocation = "";
            if(location.length() > 100){
              int i;
              for (i = 100; i < location.length(); i=i+100){
                String part1 = location.substring(i-100, i);
                newLocation = newLocation + part1 + " "; 
              }
              if (i-100 > 0){
                newLocation = newLocation + location.substring(i-100);
              }
            }
            else{
              newLocation = location;
            }
            
            outFile.println("<div  class=\"celldiv\">" + newLocation + "</div>");
          }
          outFile.println("</div>");
        }
      }
      outFile.println("</div>");
      outFile.println("</body>");
      outFile.println("</html>");
      outFile.close();
     }
         
          

  }
  
  
  
  
  

  public static void makeStringLiteralsClassesTableHtmls(TreeMap<String, LiteralsCollection> nameToLitColl) throws IOException{

    
    for (String literalType : nameToLitColl.get("string").stringTypeToSize.keySet()){
 
      
      String outFileName = literalType + "Strings.html";
      final PrintWriter outFile = new PrintWriter(outFileName);
      
      outFile.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"");
      outFile.println("\"http://www.w3.org/TR/html4/strict.dtd\">");
      outFile.println("<html>");
      outFile.println("<head>");
      outFile.println("<meta http-equiv=\"content-type\" content=\"text/html;charset=ISO-8859-1\">");
      outFile.println("<title>Literals of type \"" + literalType + "\"</title>");
      outFile.println("</head>");

      outFile.println("<style type=\"text/css\">");
      outFile.println("body {background-color: #728FCE}");
      outFile.println("h2 {background-color: transparent}");
      outFile.println("p {background-color: fuchsia}");
      outFile.println("</style>");
      
      outFile.println("<body>");
      outFile.println("<center>");
      outFile.println("<h2>Literals of type \"" + literalType + "\"</h2>");
      outFile.println("</center>");
      outFile.println("<hr>");
      
      outFile.println("<center>");      
      outFile.println("<table border=\"1\" width=\"80%\" style=\"font-size: 11pt;\" bgcolor=\"white\">");
      
   
      
      for (String literal : nameToLitColl.get("string").stringLiteralToType.keySet()){
        
        if (nameToLitColl.get("string").stringLiteralToType.get(literal).compareTo(literalType) == 0){
 
            if (literal.trim().compareTo("") == 0){
            literal = "[whitespace only string]";
          }
          
  
          String newLiteral = "";
          if(literal.length() > 80){
            int i;
            for (i = 80; i < literal.length(); i=i+80){
              String part1 = literal.substring(i-80, i);
              newLiteral = newLiteral + part1 + " "; 
            }
            if (i-80 > 0){
              newLiteral = newLiteral + literal.substring(i-80);
            }
          }
          else{
            newLiteral = literal;
          }
          
          String escliteral = escapeXml(newLiteral);
  
          outFile.println("<tr>");
          outFile.println("<td width=\"40%\">" + escliteral + "</td>");
          
  
          int ct = 0;

          for (String location : nameToLitColl.get("string").literalToLocations.get(literal)){
            
            if (ct > 0){
              outFile.println("<tr>");  
              outFile.println("<td width=\"40%\"> </td>");          
            }
            
            String newLocation = "";
            if(location.length() > 80){
              int i;
              for (i = 80; i < location.length(); i=i+80){
                String part1 = location.substring(i-80, i);
                newLocation = newLocation + part1 + " "; 
              }
              if (i-80 > 0){
                newLocation = newLocation + location.substring(i-80);
              }
            }
            else{
              newLocation = location;
            }
            
            
            outFile.println("<td width=\"40%\">" + newLocation + "</td>");
  
            if (ct > 0){
              outFile.println("</tr>");
            }
            ct++;
            
          }
  
          
          outFile.println("</tr>");
          
        }
  
        outFile.println("</table>");
        outFile.println("<center>");
  
        
        outFile.println("</div>");
        outFile.println("</body>");
        outFile.println("</html>");
        outFile.close();
       }
    }
  }

  


  public static void makeDependenciesTableHtmls() throws IOException{

    for (String className : GlobalInformation.classToWhatItDependsOn.keySet()){
          
      String outFileName = className + "Deps.html";
      final PrintWriter outFile = new PrintWriter(outFileName);
      
      outFile.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"");
      outFile.println("\"http://www.w3.org/TR/html4/strict.dtd\">");
      outFile.println("<html>");
      outFile.println("<head>");
      outFile.println("<meta http-equiv=\"content-type\" content=\"text/html;charset=ISO-8859-1\">");
      outFile.println("<title>Classes that \"" + className + "\" depends on</title>");
      outFile.println("</head>");

      outFile.println("<style type=\"text/css\">");
      outFile.println("body {background-color: #728FCE}");
      outFile.println("h2 {background-color: transparent}");
      outFile.println("p {background-color: fuchsia}");
      outFile.println("</style>");
      
      outFile.println("<body>");
      outFile.println("<center>");
      outFile.println("<h2>Classes that \"" + className + "\" depends on</h2>");
      outFile.println("</center>");
      outFile.println("<hr>");
      
      outFile.println("<center>");
      outFile.println("<table border=\"1\" width=\"80%\" style=\"font-size: 11pt;\" bgcolor=\"white\">");
      
      for (String depClassName : GlobalInformation.classToWhatItDependsOn.get(className)){
        
          outFile.println("<tr>");
          outFile.println("<td width=\"80%\">" + depClassName + "</td>");
          outFile.println("</tr>");
          
      }
  
      outFile.println("</table>");
      outFile.println("<center>");
      
      outFile.println("</div>");
      outFile.println("</body>");
      outFile.println("</html>");
      outFile.close();
    }
  }

  
  

  public static String escapeXml(String unescaped) {
    String escaped = unescaped.replaceAll("\\&", "&amp;");
    escaped = escaped.replaceAll("\\<", "&lt;");
    escaped = escaped.replaceAll("\\>", "&gt;");
    escaped = escaped.replaceAll("\\\"", "&quot;");
    escaped = escaped.replaceAll("\\'", "&apos;");
    return escaped;
  }

}