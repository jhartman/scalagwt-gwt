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
package com.google.gwt.user.client.ui;

import com.google.gwt.dom.client.Document;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.layout.client.Layout.Layer;

/**
 * A panel that lays its children out in arbitrary {@link Layout.Layer layers}
 * using the {@link Layout} class.
 * 
 * <p>
 * NOTE: This widget will <em>only</em> work in standards mode, which requires
 * that the HTML page in which it is run have an explicit &lt;!DOCTYPE&gt;
 * declaration.
 * </p>
 * 
 * <p>
 * NOTE: This class is still very new, and its interface may change without
 * warning. Use at your own risk.
 * </p>
 * 
 * <p>
 * <h3>Example</h3>
 * {@example com.google.gwt.examples.LayoutPanelExample}
 * </p>
 */
public class LayoutPanel extends ComplexPanel implements RequiresLayout,
    ProvidesLayout {

  private final Layout layout;

  /**
   * Creates an empty layout panel.
   */
  public LayoutPanel() {
    setElement(Document.get().createDivElement());
    layout = new Layout(getElement());
  }

  /**
   * Adds a widget to this panel.
   * 
   * <p>
   * By default, each child will fill the panel. To build more interesting
   * layouts, use {@link #getLayer(Widget)} to get the {@link Layout.Layer}
   * associated with each child, and set its layout constraints as desired.
   * </p>
   * 
   * @param widget the widget to be added
   */
  public void add(Widget widget) {
    // Detach new child.
    widget.removeFromParent();

    // Logical attach.
    getChildren().add(widget);

    // Physical attach.
    Layer layer = layout.attachChild(widget.getElement(), widget);
    widget.setLayoutData(layer);

    // Adopt.
    adopt(widget);
  }

  /**
   * Gets the {@link Layer} associated with the given widget. This layer may be
   * used to manipulate the child widget's layout constraints.
   * 
   * <p>
   * After you have made changes to any of the child widgets' constraints, you
   * must call one of the {@link HasAnimatedLayout} methods for those changes to
   * be reflected visually.
   * </p>
   * 
   * @param child the child widget whose layer is to be retrieved
   * @return the associated layer
   */
  public Layout.Layer getLayer(Widget child) {
    assert child.getParent() == this : "The requested widget is not a child of this panel";
    return (Layout.Layer) child.getLayoutData();
  }

  /**
   * This method, or one of its overloads, must be called whenever any of the
   * {@link Layout.Layer layers} associated with its children is modified.
   * 
   * @see #layout(int)
   * @see #layout(int, com.google.gwt.layout.client.Layout.AnimationCallback)
   */
  public void layout() {
    layout.layout();
  }

  /**
   * This method, or one of its overloads, must be called whenever any of the
   * {@link Layout.Layer layers} associated with its children is modified.
   * 
   * <p>
   * This overload will cause the layout to be updated by animating over a
   * specified period of time.
   * </p>
   * 
   * @param duration the animation duration, in milliseconds
   * 
   * @see #layout()
   * @see #layout(int, com.google.gwt.layout.client.Layout.AnimationCallback)
   */
  public void layout(int duration) {
    layout.layout(duration);
  }

  /**
   * This method, or one of its overloads, must be called whenever any of the
   * {@link Layout.Layer layers} associated with its children is modified.
   * 
   * <p>
   * This overload will cause the layout to be updated by animating over a
   * specified period of time. In addition, it provides a callback that will be
   * informed of updates to the layers. This can be used to create more complex
   * animation effects.
   * </p>
   * 
   * @param duration the animation duration, in milliseconds
   * @param callback the animation callback
   * 
   * @see #layout()
   * @see #layout(int, com.google.gwt.layout.client.Layout.AnimationCallback)
   */
  public void layout(int duration, final Layout.AnimationCallback callback) {
    layout.layout(duration, new Layout.AnimationCallback() {
      public void onAnimationComplete() {
        // Chain to the passed callback.
        if (callback != null) {
          callback.onAnimationComplete();
        }
      }

      public void onLayout(Layer layer, double progress) {
        // Inform the child associated with this layer that its size may have
        // changed.
        Widget child = (Widget) layer.getUserObject();
        if (child instanceof RequiresLayout) {
          ((RequiresLayout) child).onLayout();
        }

        // Chain to the passed callback.
        if (callback != null) {
          callback.onLayout(layer, progress);
        }
      }
    });
  }

  public void onLayout() {
    for (Widget child : getChildren()) {
      if (child instanceof RequiresLayout) {
        ((RequiresLayout) child).onLayout();
      }
    }
  }

  @Override
  public boolean remove(Widget w) {
    boolean removed = super.remove(w);
    if (removed) {
      layout.removeChild((Layer) w.getLayoutData());
    }
    return removed;
  }

  /**
   * Gets the {@link Layout} instance associated with this widget.
   * 
   * @return this widget's layout instance
   */
  protected Layout getLayout() {
    return layout;
  }

  @Override
  protected void onLoad() {
    layout.onAttach();
  }

  @Override
  protected void onUnload() {
    layout.onDetach();
  }
}