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
package com.google.gwt.dom.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * All HTML element interfaces derive from this class.
 */
public class Element extends Node {

  /**
   * Assert that the given {@link Node} is of type {@link Node#ELEMENT_NODE} and
   * automatically typecast it.
   */
  public static Element as(Node node) {
    assert node.getNodeType() == Node.ELEMENT_NODE;
    return (Element) node;
  }

  protected Element() {
  }

  /**
   * Gets an element's absolute left coordinate in the document's coordinate
   * system.
   */
  public final int getAbsoluteLeft() {
    return DOMImpl.impl.getAbsoluteLeft(this);
  }

  /**
   * Gets an element's absolute top coordinate in the document's coordinate
   * system.
   */
  public final int getAbsoluteTop() {
    return DOMImpl.impl.getAbsoluteTop(this);
  }

  /**
   * Retrieves an attribute value by name.
   * 
   * @param name The name of the attribute to retrieve
   * @return The Attr value as a string, or the empty string if that attribute
   *         does not have a specified or default value
   */
  public final native String getAttribute(String name) /*-{
    return this.getAttribute(name);
  }-*/;

  /**
   * The class attribute of the element. This attribute has been renamed due to
   * conflicts with the "class" keyword exposed by many languages.
   * 
   * @see http://www.w3.org/TR/1999/REC-html401-19991224/struct/global.html#adef-class
   */
  public final native String getClassName() /*-{
    return this.className;
  }-*/;

  /**
   * Specifies the base direction of directionally neutral text and the
   * directionality of tables.
   */
  public final native String getDir() /*-{
    return this.dir;
  }-*/;

  /**
   * Returns a NodeList of all descendant Elements with a given tag name, in the
   * order in which they are encountered in a preorder traversal of this Element
   * tree.
   * 
   * @param name The name of the tag to match on. The special value "*" matches
   *          all tags
   * @return A list of matching Element nodes
   */
  public final native NodeList<Element> getElementsByTagName(String name) /*-{
    return this.getElementsByTagName(name);
  }-*/;

  /**
   * The first child of element this element. If there is no such element, this
   * returns null.
   */
  public final Element getFirstChildElement() {
    return DOMImpl.impl.getFirstChildElement(this);
  }

  /**
   * The element's identifier.
   * 
   * @see http://www.w3.org/TR/1999/REC-html401-19991224/struct/global.html#adef-id
   */
  public final native String getId() /*-{
    return this.id;
  }-*/;

  /**
   * All of the markup and content within a given element.
   */
  public final String getInnerHTML() {
    return DOMImpl.impl.getInnerHTML(this);
  }

  /**
   * The text between the start and end tags of the object.
   */
  public final String getInnerText() {
    return DOMImpl.impl.getInnerText(this);
  }

  /**
   * Language code defined in RFC 1766.
   */
  public final native String getLang() /*-{
    return this.lang;
  }-*/;

  /**
   * The element immediately following this element. If there is no such
   * element, this returns null.
   */
  public final Element getNextSiblingElement() {
    return DOMImpl.impl.getNextSiblingElement(this);
  }

  /**
   * The height of an element relative to the layout.
   */
  public final int getOffsetHeight() {
    return getPropertyInt("offsetHeight");
  }

  /**
   * The number of pixels that the upper left corner of the current element is
   * offset to the left within the offsetParent node.
   */
  public final int getOffsetLeft() {
    return getPropertyInt("offsetLeft");
  }

  /**
   * Returns a reference to the object which is the closest (nearest in the
   * containment hierarchy) positioned containing element.
   */
  public final native Element getOffsetParent() /*-{
    return this.offsetParent;
  }-*/;

  /**
   * The number of pixels that the upper top corner of the current element is
   * offset to the top within the offsetParent node.
   */
  public final int getOffsetTop() {
    return getPropertyInt("offsetTop");
  }

  /**
   * The width of an element relative to the layout.
   */
  public final int getOffsetWidth() {
    return getPropertyInt("offsetWidth");
  }

  /**
   * The parent element of this element.
   */
  public final Element getParentElement() {
    return DOMImpl.impl.getParentElement(this);
  }

  /**
   * Gets a boolean property from this element.
   * 
   * @param name the name of the property to be retrieved
   * @return the property value
   */
  public final native boolean getPropertyBoolean(String name) /*-{
    return !!this[name];
  }-*/;

  /**
   * Gets a double property from this element.
   * 
   * @param name the name of the property to be retrieved
   * @return the property value
   */
  public final native double getPropertyDouble(String name) /*-{
    return parseFloat(this[name]) || 0.0;
  }-*/;

  /**
   * Gets an integer property from this element.
   * 
   * @param name the name of the property to be retrieved
   * @return the property value
   */
  public final native int getPropertyInt(String name) /*-{
    return parseInt(this[name]) || 0;
  }-*/;

  /**
   * Gets a property from this element.
   * 
   * @param name the name of the property to be retrieved
   * @return the property value
   */
  public final native String getPropertyString(String name) /*-{
    var ret = this[name];
    return (ret == null) ? null : String(ret);
  }-*/;

  /**
   * The height of the scroll view of an element.
   */
  public final native int getScrollHeight() /*-{
    return this.scrollHeight;
  }-*/;

  /**
   * The number of pixels that an element's content is scrolled to the left.
   */
  public final native int getScrollLeft() /*-{
    return this.scrollLeft;
  }-*/;

  /**
   * The number of pixels that an element's content is scrolled to the top.
   */
  public final native int getScrollTop() /*-{
    return this.scrollTop;
  }-*/;

  /**
   * The height of the scroll view of an element.
   */
  public final native int getScrollWidth() /*-{
    return this.scrollWidth;
  }-*/;

  /**
   * Gets a string representation of this element (as outer HTML).
   * 
   * We do not override {@link #toString()} because it is final in
   * {@link JavaScriptObject}.
   * 
   * @return the string representation of this element
   */
  public final String getString() {
    return DOMImpl.impl.toString(this);
  }

  /**
   * Gets this element's {@link Style} object.
   */
  public final native Style getStyle() /*-{
    return this.style;
  }-*/;

  /**
   * The name of the element.
   */
  public final native String getTagName() /*-{
    return this.tagName;
  }-*/;

  /**
   * The element's advisory title.
   */
  public final native String getTitle() /*-{
    return this.title;
  }-*/;

  /**
   * Determine whether an element is equal to, or the child of, this element.
   * 
   * @param child the potential child element
   * @return <code>true</code> if the relationship holds
   */
  public final boolean isOrHasChild(Element child) {
    return DOMImpl.impl.isOrHasChild(this, child);
  }

  /**
   * Removes an attribute by name.
   */
  public final native void removeAttribute(String name) /*-{
    this.removeAttribute(name);
  }-*/;

  /**
   * Scrolls this element into view.
   * 
   * <p>
   * This method crawls up the DOM hierarchy, adjusting the scrollLeft and
   * scrollTop properties of each scrollable element to ensure that the
   * specified element is completely in view. It adjusts each scroll position by
   * the minimum amount necessary.
   * </p>
   */
  public final void scrollIntoView() {
    DOMImpl.impl.scrollIntoView(this);
  }

  /**
   * Adds a new attribute. If an attribute with that name is already present in
   * the element, its value is changed to be that of the value parameter.
   * 
   * @param name The name of the attribute to create or alter
   * @param value Value to set in string form
   */
  public final native void setAttribute(String name, String value) /*-{
    this.setAttribute(name, value);
  }-*/;

  /**
   * The class attribute of the element. This attribute has been renamed due to
   * conflicts with the "class" keyword exposed by many languages.
   * 
   * @see http://www.w3.org/TR/1999/REC-html401-19991224/struct/global.html#adef-class
   */
  public final native void setClassName(String className) /*-{
    this.className = className;
  }-*/;

  /**
   * Specifies the base direction of directionally neutral text and the
   * directionality of tables.
   */
  public final native void setDir(String dir) /*-{
    this.dir = dir;
  }-*/;

  /**
   * The element's identifier.
   * 
   * @see http://www.w3.org/TR/1999/REC-html401-19991224/struct/global.html#adef-id
   */
  public final native void setId(String id) /*-{
    this.id = id;
  }-*/;

  /**
   * All of the markup and content within a given element.
   */
  public final native void setInnerHTML(String html) /*-{
    this.innerHTML = html || '';
  }-*/;

  /**
   * The text between the start and end tags of the object.
   */
  public final void setInnerText(String text) {
    DOMImpl.impl.setInnerText(this, text);
  }

  /**
   * Language code defined in RFC 1766.
   */
  public final native void setLang(String lang) /*-{
    this.lang = lang;
  }-*/;

  /**
   * Sets a boolean property on this element.
   * 
   * @param name the name of the property to be set
   * @param value the new property value
   */
  public final native void setPropertyBoolean(String name, boolean value) /*-{
    this[name] = value;
  }-*/;

  /**
   * Sets a double property on this element.
   * 
   * @param name the name of the property to be set
   * @param value the new property value
   */
  public final native void setPropertyDouble(String name, double value) /*-{
    this[name] = value;
  }-*/;

  /**
   * Sets an integer property on this element.
   * 
   * @param name the name of the property to be set
   * @param value the new property value
   */
  public final native void setPropertyInt(String name, int value) /*-{
    this[name] = value;
  }-*/;

  /**
   * Sets a property on this element.
   * 
   * @param name the name of the property to be set
   * @param value the new property value
   */
  public final native void setPropertyString(String name, String value) /*-{
    this[name] = value;
  }-*/;

  /**
   * The number of pixels that an element's content is scrolled to the left.
   */
  public final native void setScrollLeft(int scrollLeft) /*-{
    this.scrollLeft = scrollLeft;
  }-*/;

  /**
   * The number of pixels that an element's content is scrolled to the top.
   */
  public final native void setScrollTop(int scrollTop) /*-{
    this.scrollTop = scrollTop;
  }-*/;

  /**
   * The element's advisory title.
   */
  public final native void setTitle(String title) /*-{
    this.title = title || '';
  }-*/;
}