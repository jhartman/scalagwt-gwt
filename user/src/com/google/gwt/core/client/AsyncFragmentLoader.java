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

import java.util.LinkedList;
import java.util.Queue;

/**
 * <p>
 * Low-level support to download an extra fragment of code. This should not be
 * invoked directly by user code.
 * </p>
 * 
 * <p>
 * The fragments are numbered as follows, assuming there are <em>m</em> split
 * points:
 * 
 * <ul>
 * <li> 0 -- the <em>base</em> fragment, which is initially downloaded
 * <li> 1-m -- the <em>exclusively live</em> fragments, holding the code
 * needed exclusively for each split point
 * <li> m -- the <em>secondary base</em> fragment for entry point 1. It holds
 * precisely that code needed if entry point 1 is the first one reached after
 * the application downloads.
 * <li> m+1 -- the <em>leftovers fragment</em> for entry point 1. It holds all
 * code not in fragments 0-(m-1) nor in fragment m.
 * <li> (m+2)..(3m) -- secondary bases and leftovers for entry points 2..m
 * </ul>
 * 
 * <p>
 * Different linkers have different requirements about how the code is
 * downloaded and installed. Thus, when it is time to actually download the
 * code, this class defers to a JavaScript function named
 * <code>__gwtStartLoadingFragment</code>. Linkers must arrange that a
 * suitable <code>__gwtStartLoadingFragment</code> function is in scope.
 */
public class AsyncFragmentLoader {
  /**
   * The first entry point reached after the program started.
   */
  private static int base = -1;

  /**
   * Whether the secondary base fragment is currently loading.
   */
  private static boolean baseLoading = false;

  /**
   * Whether the leftovers fragment has loaded yet.
   */
  private static boolean leftoversLoaded = false;

  /**
   * Whether the leftovers fragment is currently loading.
   */
  private static boolean leftoversLoading = false;

  /**
   * The total number of split points in the program, counting the initial entry
   * as a split point. This is changed to the correct value by
   * {@link com.google.gwt.dev.jjs.impl.ReplaceRunAsyncs}.
   */
  private static int numEntries = 1;

  /**
   * Split points that have been reached, but that cannot be downloaded until
   * the leftovers fragment finishes downloading.
   */
  private static Queue<Integer> waitingForLeftovers = new LinkedList<Integer>();

  /**
   * Inform the loader that the code for an entry point has now finished
   * loading.
   * 
   * @param entry The entry whose code fragment is now loaded.
   */
  public static void fragmentHasLoaded(int entry) {
    if (base < 0) {
      // The base fragment has loaded
      base = entry;
      baseLoading = false;

      // Go ahead and download the appropriate leftovers fragment
      leftoversLoading = true;
      startLoadingFragment(numEntries + 2 * (entry - 1) + 1);
    }
  }

  /**
   * Loads the specified split point.
   * 
   * @param splitPoint the fragment to load
   */
  public static void inject(int splitPoint) {
    if (leftoversLoaded) {
      /*
       * A base and a leftovers fragment have loaded. Load an exclusively live
       * fragment.
       */
      startLoadingFragment(splitPoint);
      return;
    }

    if (baseLoading || leftoversLoading) {
      /*
       * Wait until the leftovers fragment has loaded before loading this one.
       */
      waitingForLeftovers.add(splitPoint);
      return;
    }

    // Nothing has loaded or started to load. Treat this fragment as the base.
    baseLoading = true;
    startLoadingFragment(numEntries + 2 * (splitPoint - 1));
  }

  /**
   * Inform the loader that the "leftovers" fragment has loaded.
   */
  public static void leftoversFragmentHasLoaded() {
    leftoversLoaded = true;
    leftoversLoading = false;
    while (!waitingForLeftovers.isEmpty()) {
      inject(waitingForLeftovers.remove());
    }
  }

  /**
   * Logs an event with the GWT lightweight metrics framework.
   */
  public static void logEventProgress(String eventGroup, String type) {
    @SuppressWarnings("unused")
    boolean toss = isStatsAvailable()
        && stats(createStatsEvent(eventGroup, type));
  }

  /**
   * Create an event object suitable for submitting to the lightweight metrics
   * framework.
   */
  private static native JavaScriptObject createStatsEvent(String eventGroup,
      String type) /*-{
    return {
      moduleName: @com.google.gwt.core.client.GWT::getModuleName()(), 
      subSystem: 'runAsync',
      evtGroup: eventGroup,
      millis: (new Date()).getTime(),
      type: type
    };
  }-*/;

  private static native void gwtStartLoadingFragment(int fragment) /*-{
    __gwtStartLoadingFragment(fragment);
  }-*/;

  private static native boolean isStatsAvailable() /*-{
    return !!$stats;
  }-*/;

  private static void startLoadingFragment(int fragment) {
    logEventProgress("download" + fragment, "begin");
    gwtStartLoadingFragment(fragment);
  }

  /**
   * Always use this as {@link isStatsAvailable} &amp;&amp;
   * {@link #stats(JavaScriptObject)}.
   */
  private static native boolean stats(JavaScriptObject data) /*-{
    return $stats(data);
  }-*/;
}