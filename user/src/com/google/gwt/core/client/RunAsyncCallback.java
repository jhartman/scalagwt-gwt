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
package com.google.gwt.core.client;

/**
 * A callback meant to be used by
 * {@link com.google.gwt.core.client.GWT#runAsync(RunAsyncCallback) }.
 */
public interface RunAsyncCallback {
  /**
   * Called when, for some reason, the necessary code cannot be loaded. For
   * example, the user might no longer be on the network.
   */
  void onFailure(Throwable caught);

  /**
   * Called once the necessary code for it has been loaded.
   */
  void onSuccess();
}