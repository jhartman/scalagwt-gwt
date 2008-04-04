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

/**
 * Regroups the COL and COLGROUP elements.
 * 
 * @see http://www.w3.org/TR/1999/REC-html401-19991224/struct/tables.html#edef-COL
 */
public class TableColElement extends Element {

  /**
   * Assert that the given {@link Element} is compatible with this class and
   * automatically typecast it.
   */
  public static TableColElement as(Element elem) {
    assert elem.getTagName().equalsIgnoreCase("col")
        || elem.getTagName().equalsIgnoreCase("colgroup");
    return (TableColElement) elem;
  }

  protected TableColElement() {
  }

  /**
   * Horizontal alignment of cell data in column.
   * 
   * @see http://www.w3.org/TR/1999/REC-html401-19991224/struct/tables.html#adef-align-TD
   */
  public final native String getAlign() /*-{
    return this.align;
  }-*/;

  /**
   * Alignment character for cells in a column.
   * 
   * @see http://www.w3.org/TR/1999/REC-html401-19991224/struct/tables.html#adef-char
   */
  public final native String getCh() /*-{
     return this.ch;
   }-*/;

  /**
   * Offset of alignment character.
   * 
   * @see http://www.w3.org/TR/1999/REC-html401-19991224/struct/tables.html#adef-charoff
   */
  public final native String getChOff() /*-{
     return this.chOff;
   }-*/;

  /**
   * Indicates the number of columns in a group or affected by a grouping.
   * 
   * @see http://www.w3.org/TR/1999/REC-html401-19991224/struct/tables.html#adef-span-COL
   */
  public final native int getSpan() /*-{
    return this.span;
  }-*/;

  /**
   * Vertical alignment of cell data in column.
   * 
   * @see http://www.w3.org/TR/1999/REC-html401-19991224/struct/tables.html#adef-valign
   */
  public final native String getVAlign() /*-{
    return this.vAlign;
  }-*/;

  /**
   * Default column width.
   * 
   * @see http://www.w3.org/TR/1999/REC-html401-19991224/struct/tables.html#adef-width-COL
   */
  public final native String getWidth() /*-{
    return this.width;
  }-*/;

  /**
   * Horizontal alignment of cell data in column.
   * 
   * @see http://www.w3.org/TR/1999/REC-html401-19991224/struct/tables.html#adef-align-TD
   */
  public final native void setAlign(String align) /*-{
    this.align = align;
  }-*/;

  /**
   * Alignment character for cells in a column.
   * 
   * @see http://www.w3.org/TR/1999/REC-html401-19991224/struct/tables.html#adef-char
   */
  public final native void setCh(String ch) /*-{
     this.ch = ch;
   }-*/;

  /**
   * Offset of alignment character.
   * 
   * @see http://www.w3.org/TR/1999/REC-html401-19991224/struct/tables.html#adef-charoff
   */
  public final native void setChOff(String chOff) /*-{
     this.chOff = chOff;
   }-*/;

  /**
   * Indicates the number of columns in a group or affected by a grouping.
   * 
   * @see http://www.w3.org/TR/1999/REC-html401-19991224/struct/tables.html#adef-span-COL
   */
  public final native void setSpan(int span) /*-{
    this.span = span;
  }-*/;

  /**
   * Vertical alignment of cell data in column.
   * 
   * @see http://www.w3.org/TR/1999/REC-html401-19991224/struct/tables.html#adef-valign
   */
  public final native void setVAlign(String vAlign) /*-{
    this.vAlign = vAlign;
  }-*/;

  /**
   * Default column width.
   * 
   * @see http://www.w3.org/TR/1999/REC-html401-19991224/struct/tables.html#adef-width-COL
   */
  public final native void setWidth(String width) /*-{
    this.width = width;
  }-*/;
}