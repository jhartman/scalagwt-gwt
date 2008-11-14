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
package com.google.gwt.dev.util.arg;

import com.google.gwt.util.tools.ArgHandlerExtra;

/**
 * Argument handler for module name, which has no option profix.
 */
public final class ArgHandlerModuleName extends ArgHandlerExtra {

  private final OptionModuleName option;

  public ArgHandlerModuleName(OptionModuleName option) {
    this.option = option;
  }

  @Override
  public boolean addExtraArg(String arg) {
    option.setModuleName(arg);
    return true;
  }

  @Override
  public String getPurpose() {
    return "Specifies the name of the module to compile";
  }

  @Override
  public String[] getTagArgs() {
    return new String[] {"module"};
  }

  @Override
  public boolean isRequired() {
    return true;
  }
}