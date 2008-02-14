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
package com.google.gwt.dev.linker;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

import java.io.InputStream;

/**
 * Provides access to resources used by the module.
 */
public interface ModuleResource {
  /**
   * Provides access to the contents of the resource if it is statically
   * available.
   * 
   * @return An InputStream accessing the contents of the resource or
   *         <code>null</code> if the resource is not available at link time.
   */
  InputStream tryGetResourceAsStream(TreeLogger logger)
      throws UnableToCompleteException;
}