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

package com.google.gwt.user.client.ui;

import com.google.gwt.junit.DoNotRunWith;
import com.google.gwt.junit.Platform;
import com.google.gwt.junit.client.GWTTestCase;

/**
 * Test for {@link DeckPanel}.
 */
public class DeckPanelTest extends GWTTestCase {
  @Override
  public String getModuleName() {
    return "com.google.gwt.user.User";
  }

  /**
   * Test that the {@link DeckPanel} calls widget.setVisible(true) on the
   * visible widget, but does NOT call widget.setVisible(false) when a widget is
   * hidden.
   */
  public void testSetWidgetVisible() {
    // Show a widget with animations disabled
    {
      DeckPanel deck = new DeckPanel();
      deck.setAnimationEnabled(false);
      Label[] labels = new Label[3];
      for (int i = 0; i < labels.length; i++) {
        labels[i] = new Label("content" + i);
        deck.add(labels[i]);
      }

      // Show widget at index 1, make sure it becomes visible
      deck.showWidget(1);
      assertFalse(labels[0].isVisible());
      assertTrue(labels[1].isVisible());
      assertFalse(labels[2].isVisible());

      // Show widget at index 0, make sure widget 1 is still visible
      deck.showWidget(0);
      assertTrue(labels[0].isVisible());
      assertFalse(labels[1].isVisible());
      assertFalse(labels[2].isVisible());
    }

    // Show a widget with animations enabled
    {
      DeckPanel deck = new DeckPanel();
      deck.setAnimationEnabled(true);
      Label[] labels = new Label[3];
      for (int i = 0; i < labels.length; i++) {
        labels[i] = new Label("content" + i);
        deck.add(labels[i]);
      }

      // Show widget at index 1, make sure it becomes visible
      deck.showWidget(1);
      assertFalse(labels[0].isVisible());
      assertTrue(labels[1].isVisible());
      assertFalse(labels[2].isVisible());

      // Show widget at index 0, make sure widget 1 is still visible
      deck.showWidget(0);
      assertTrue(labels[0].isVisible());
      assertFalse(labels[1].isVisible());
      assertFalse(labels[2].isVisible());
    }
  }

  /**
   * Test that the offsetHeight/Width of a widget are defined when the widget is
   * added to the DeckPanel.
   */
  @DoNotRunWith(Platform.Htmlunit)
  public void testWidgetOffsetDimensionsOnload() {
    DeckPanel deck = new DeckPanel();
    RootPanel.get().add(deck);

    // Add a widget to the DeckPanel
    Label content = new Label("detached") {
      @Override
      public void onLoad() {
        // Verify that the offsetWidth/Height are greater than zero
        assertTrue(this.getOffsetHeight() > 0);
        assertTrue("Expect positive offsetWidth. "
            + "This will fail in WebKit if run headless",
            this.getOffsetWidth() > 0);
        setText("attached");
      }
    };
    deck.add(content);

    // Verify content.onLoad was actually called
    assertEquals("attached", content.getText());
  }
}
