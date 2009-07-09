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
package com.google.gwt.user.server;

/**
 * A utility to decode and encode byte arrays as Strings, using only "safe" characters.
 */
public class Base64Utils {

  /**
   * An array mapping size but values to the characters that will be used to represent them.
   */
  private static final char[] base64Chars = new char[] {
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
    'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B',
    'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
    'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3',
    '4', '5', '6', '7', '8', '9', '$', '_'
  };

  /**
   * An array mapping legal base 64 characters [a-zA-Z0-9$_] to their associated 6-bit values.
   */
  private static final byte[] base64Values = new byte[123];

  /**
   * Initialize the base 64 encoder values.
   */
  static {
    for (int i = 'a'; i <= 'z'; i++) {
      base64Values[i] = (byte) (i - 'a');
    }
    for (int i = 'A'; i <= 'Z'; i++) {
      base64Values[i] = (byte) (i - 'A' + 26);
    }
    for (int i = '0'; i <= '9'; i++) {
      base64Values[i] = (byte) (i - '0' + 52);
    }
    base64Values['$'] = 62;
    base64Values['_'] = 63;
  }

  /**
   * Decode a base64 string into a byte array.
   * 
   * @param data the encoded data.
   * @return a byte array.
   * @see #fromBase64(String)
   */
  public static byte[] fromBase64(String data) {
    if (data == null) {
      return null;
    }
    
    int len = data.length();
    assert (len % 4) == 0;
    
    if (len == 0) {
      return new byte[0];
    }
    
    char[] chars = new char[len];
    data.getChars(0, len, chars, 0);
    
    int olen = 3 * (len / 4);
    if (chars[len - 2] == '=') {
      --olen;
    }
    if (chars[len - 1] == '=') {
      --olen;
    }
    
    byte[] bytes = new byte[olen];
    
    int iidx = 0;
    int oidx = 0;
    while (iidx < len) {
      int c0 = base64Values[chars[iidx++] & 0xff];
      int c1 = base64Values[chars[iidx++] & 0xff];
      int c2 = base64Values[chars[iidx++] & 0xff];
      int c3 = base64Values[chars[iidx++] & 0xff];
      int c24 = (c0 << 18) | (c1 << 12) | (c2 << 6) | c3;
      
      bytes[oidx++] = (byte) (c24 >> 16);
      if (oidx == olen) {
        break;
      }
      bytes[oidx++] = (byte) (c24 >>  8);
      if (oidx == olen) {
        break;
      }
      bytes[oidx++] = (byte)  c24;
    }
    
    return bytes;
  }

  /**
   * Converts a byte array into a base 64 encoded string. Null is encoded as
   * null, and an empty array is encoded as an empty string. Otherwise, the byte
   * data is read 3 bytes at a time, with bytes off the end of the array padded
   * with zeros. Each 24-bit chunk is encoded as 4 characters from the sequence
   * [a-zA-Z0-9$_]. If one of the size-bit source positions consists entirely of
   * padding zeros, an '=' character is used instead.
   * 
   * @param data a byte array, which may be null or empty
   * @return a String
   */
  public static String toBase64(byte[] data) {
    if (data == null) {
      return null;
    }

    int len = data.length;
    if (len == 0) {
      return "";
    }
    
    int olen = 4 * ((len + 2) / 3);
    char[] chars = new char[olen];

    int iidx = 0;
    int oidx = 0;
    int charsLeft = len;
    while (charsLeft > 0) {
      int b0 = data[iidx++] & 0xff;
      int b1 = (charsLeft > 1) ? data[iidx++] & 0xff : 0;
      int b2 = (charsLeft > 2) ? data[iidx++] & 0xff : 0;
      int b24 = (b0 << 16) | (b1 << 8) | b2;

      int c0 = (b24 >> 18) & 0x3f;
      int c1 = (b24 >> 12) & 0x3f;
      int c2 = (b24 >> 6) & 0x3f;
      int c3 = b24 & 0x3f;

      chars[oidx++] = base64Chars[c0];
      chars[oidx++] = base64Chars[c1];
      chars[oidx++] = (charsLeft > 1) ? base64Chars[c2] : '=';
      chars[oidx++] = (charsLeft > 2) ? base64Chars[c3] : '=';

      charsLeft -= 3;
    }

    return new String(chars);
  }
}