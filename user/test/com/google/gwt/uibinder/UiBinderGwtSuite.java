/*
 * Copyright 2009 Google Inc.
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
package com.google.gwt.uibinder;

import com.google.gwt.junit.tools.GWTTestSuite;
import com.google.gwt.uibinder.client.UiBinderUtilTest;
import com.google.gwt.uibinder.test.client.TestParameterizedWidgets;
import com.google.gwt.uibinder.test.client.UiBinderTest;

import junit.framework.Test;

/**
 * Test suite for UiBinder GWTTestCases.
 */
public class UiBinderGwtSuite {
  public static Test suite() {
    GWTTestSuite suite = new GWTTestSuite(
        "Test suite for UiBinder GWTTestCases");

    suite.addTestSuite(UiBinderTest.class);
    suite.addTestSuite(UiBinderUtilTest.class);
    suite.addTestSuite(TestParameterizedWidgets.class);

    return suite;
  }

  private UiBinderGwtSuite() {
  }
}
