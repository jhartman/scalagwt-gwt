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
package com.google.gwt.i18n.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.constants.TimeZoneConstants;
import com.google.gwt.junit.client.GWTTestCase;

import java.util.Date;

/**
 * Tests formatting functionality in {@link DateTimeFormat} for the German
 * language.
 */
public class DateTimeFormat_en_Test extends GWTTestCase {

  public String getModuleName() {
    return "com.google.gwt.i18n.I18NTest";
  }

  public void test_EEEEMMMddyy() {
    Date date = new Date(2006 - 1900, 6, 27, 13, 10, 10);
    assertEquals("Thursday,July 27, 2006", DateTimeFormat.getFormat(
        "EEEE,MMMM dd, yyyy").format(date));
  }

  public void test_EEEMMMddyy() {
    Date date = new Date(2006 - 1900, 6, 27, 13, 10, 10);
    assertEquals("Thu, Jul 27, 06",
        DateTimeFormat.getFormat("EEE, MMM d, yy").format(date));
  }

  public void test_HHmmss() {
    Date date = new Date(2006 - 1900, 6, 27, 13, 10, 10);
    assertEquals("13:10:10", DateTimeFormat.getFormat("HH:mm:ss").format(date));
  }

  public void test_hhmmssa() {
    Date date = new Date(2006 - 1900, 6, 27, 13, 10, 10);
    assertEquals("1:10:10 PM", DateTimeFormat.getFormat("h:mm:ss a").format(
        date));
  }

  public void test_predefinedFormat() {
    Date date = new Date(2006 - 1900, 7, 4, 13, 49, 24);

    TimeZoneConstants timeZoneData = GWT.create(TimeZoneConstants.class);
    String str = timeZoneData.americaLosAngeles();
    TimeZone usPacific = TimeZone.createTimeZone(TimeZoneInfo.buildTimeZoneData(str));

    String fullDateFormat = DateTimeFormat.getFullDateFormat().format(date);
    assertEquals("Friday, August 4, 2006", fullDateFormat);

    String longDateFormat = DateTimeFormat.getLongDateFormat().format(date);
    assertEquals("August 4, 2006", longDateFormat);

    String medDateFormat = DateTimeFormat.getMediumDateFormat().format(date);
    assertEquals("Aug 4, 2006", medDateFormat);

    String shortDateFormat = DateTimeFormat.getShortDateFormat().format(date);
    assertEquals("8/4/06", shortDateFormat);

    // When dealing with time zone, better use UTC time.
    // And when UTC time is used, time zone must be given in "format".
    date.setTime(Date.UTC(2006 - 1900, 7, 4, 20, 49, 24));
    String fullTimeFormat = DateTimeFormat.getFullTimeFormat().format(date,
        usPacific);
    assertEquals("1:49:24 PM America/Los_Angeles", fullTimeFormat);

    String longTimeFormat = DateTimeFormat.getLongTimeFormat().format(date,
        usPacific);
    assertEquals("1:49:24 PM PDT", longTimeFormat);

    String medTimeFormat = DateTimeFormat.getMediumTimeFormat().format(date,
        usPacific);
    assertEquals("1:49:24 PM", medTimeFormat);

    String shortTimeFormat = DateTimeFormat.getShortTimeFormat().format(date,
        usPacific);
    assertEquals("1:49 PM", shortTimeFormat);

    String medFormat = DateTimeFormat.getMediumDateTimeFormat().format(date,
        usPacific);
    assertEquals("Aug 4, 2006 1:49:24 PM", medFormat);

    String shortFormat = DateTimeFormat.getShortDateTimeFormat().format(date,
        usPacific);
    assertEquals("8/4/06 1:49 PM", shortFormat);
  }

  public void test_QQQQyy() {
    Date date;

    date = new Date(2006 - 1900, 0, 27, 13, 10, 10);
    assertEquals("1st quarter 06", DateTimeFormat.getFormat("QQQQ yy").format(
        date));

    date = new Date(2006 - 1900, 1, 27, 13, 10, 10);
    assertEquals("1st quarter 06", DateTimeFormat.getFormat("QQQQ yy").format(
        date));

    date = new Date(2006 - 1900, 2, 27, 13, 10, 10);
    assertEquals("1st quarter 06", DateTimeFormat.getFormat("QQQQ yy").format(
        date));

    date = new Date(2006 - 1900, 3, 27, 13, 10, 10);
    assertEquals("2nd quarter 06", DateTimeFormat.getFormat("QQQQ yy").format(
        date));

    date = new Date(2006 - 1900, 4, 27, 13, 10, 10);
    assertEquals("2nd quarter 06", DateTimeFormat.getFormat("QQQQ yy").format(
        date));

    date = new Date(2006 - 1900, 5, 27, 13, 10, 10);
    assertEquals("2nd quarter 06", DateTimeFormat.getFormat("QQQQ yy").format(
        date));

    date = new Date(2006 - 1900, 6, 27, 13, 10, 10);
    assertEquals("3rd quarter 06", DateTimeFormat.getFormat("QQQQ yy").format(
        date));

    date = new Date(2006 - 1900, 7, 27, 13, 10, 10);
    assertEquals("3rd quarter 06", DateTimeFormat.getFormat("QQQQ yy").format(
        date));

    date = new Date(2006 - 1900, 8, 27, 13, 10, 10);
    assertEquals("3rd quarter 06", DateTimeFormat.getFormat("QQQQ yy").format(
        date));

    date = new Date(2006 - 1900, 9, 27, 13, 10, 10);
    assertEquals("4th quarter 06", DateTimeFormat.getFormat("QQQQ yy").format(
        date));

    date = new Date(2006 - 1900, 10, 27, 13, 10, 10);
    assertEquals("4th quarter 06", DateTimeFormat.getFormat("QQQQ yy").format(
        date));

    date = new Date(2006 - 1900, 11, 27, 13, 10, 10);
    assertEquals("4th quarter 06", DateTimeFormat.getFormat("QQQQ yy").format(
        date));
  }

  public void test_QQyyyy() {
    Date date = new Date(2006 - 1900, 0, 27);
    date.setHours(13);
    date.setMinutes(10);
    date.setSeconds(10);
    assertEquals("Q1 2006", DateTimeFormat.getFormat("QQ yyyy").format(date));
    date = new Date(2006 - 1900, 1, 27);
    date.setHours(13);
    date.setMinutes(10);
    date.setSeconds(10);
    assertEquals("Q1 2006", DateTimeFormat.getFormat("QQ yyyy").format(date));
    date = new Date(2006 - 1900, 2, 27);
    date.setHours(13);
    date.setMinutes(10);
    date.setSeconds(10);
    assertEquals("Q1 2006", DateTimeFormat.getFormat("QQ yyyy").format(date));
    date = new Date(2006 - 1900, 3, 27);
    date.setHours(13);
    date.setMinutes(10);
    date.setSeconds(10);
    assertEquals("Q2 2006", DateTimeFormat.getFormat("QQ yyyy").format(date));
    date = new Date(2006 - 1900, 4, 27);
    date.setHours(13);
    date.setMinutes(10);
    date.setSeconds(10);
    assertEquals("Q2 2006", DateTimeFormat.getFormat("QQ yyyy").format(date));
    date = new Date(2006 - 1900, 5, 27);
    date.setHours(13);
    date.setMinutes(10);
    date.setSeconds(10);
    assertEquals("Q2 2006", DateTimeFormat.getFormat("QQ yyyy").format(date));
    date = new Date(2006 - 1900, 6, 27);
    date.setHours(13);
    date.setMinutes(10);
    date.setSeconds(10);
    assertEquals("Q3 2006", DateTimeFormat.getFormat("QQ yyyy").format(date));
    date = new Date(2006 - 1900, 7, 27);
    date.setHours(13);
    date.setMinutes(10);
    date.setSeconds(10);
    assertEquals("Q3 2006", DateTimeFormat.getFormat("QQ yyyy").format(date));
    date = new Date(2006 - 1900, 8, 27);
    date.setHours(13);
    date.setMinutes(10);
    date.setSeconds(10);
    assertEquals("Q3 2006", DateTimeFormat.getFormat("QQ yyyy").format(date));
    date = new Date(2006 - 1900, 9, 27);
    date.setHours(13);
    date.setMinutes(10);
    date.setSeconds(10);
    assertEquals("Q4 2006", DateTimeFormat.getFormat("QQ yyyy").format(date));
    date = new Date(2006 - 1900, 10, 27);
    date.setHours(13);
    date.setMinutes(10);
    date.setSeconds(10);
    assertEquals("Q4 2006", DateTimeFormat.getFormat("QQ yyyy").format(date));
    date = new Date(2006 - 1900, 11, 27);
    date.setHours(13);
    date.setMinutes(10);
    date.setSeconds(10);
    assertEquals("Q4 2006", DateTimeFormat.getFormat("QQ yyyy").format(date));
  }

  public void test_quote() {
    Date date = new Date(2006 - 1900, 6, 27);
    date.setHours(13);
    date.setMinutes(10);
    date.setSeconds(10);
    assertEquals("13 o'clock",
        DateTimeFormat.getFormat("HH 'o''clock'").format(date));
    assertEquals("13 oclock", DateTimeFormat.getFormat("HH 'oclock'").format(
        date));
    assertEquals("13 '", DateTimeFormat.getFormat("HH ''").format(date));
  }

  public void test_yyyyyMMMMM() {
    Date date = new Date(2006 - 1900, 6, 27, 13, 10, 10);
    assertEquals("2006.J.27 AD 01:10 PM", DateTimeFormat.getFormat(
        "yyyyy.MMMMM.dd GGG hh:mm aaa").format(date));
  }

  public void test_timezonev() {
    TimeZoneConstants timeZoneData = GWT.create(TimeZoneConstants.class);
    String str = timeZoneData.americaLosAngeles();
    TimeZone usPacific = TimeZone.createTimeZone(str);

    Date date = new Date();
    date.setTime(Date.UTC(2006 - 1900, 6, 27, 13, 10, 10));

    assertEquals("07/27/2006 06:10:10 America/Los_Angeles",
        DateTimeFormat.getFormat("MM/dd/yyyy HH:mm:ss v").format(date,
            usPacific));

    assertEquals("07/27/2006 06:10:10 America/Los_Angeles",
        DateTimeFormat.getFormat("MM/dd/yyyy HH:mm:ss vv").format(date,
            usPacific));

    assertEquals("07/27/2006 06:10:10 America/Los_Angeles",
        DateTimeFormat.getFormat("MM/dd/yyyy HH:mm:ss vvv").format(date,
            usPacific));

    assertEquals("07/27/2006 06:10:10 America/Los_Angeles",
        DateTimeFormat.getFormat("MM/dd/yyyy HH:mm:ss vvvv").format(date,
            usPacific));
  }

  public void test_simepleTimezonev() {
    TimeZone simpleTimeZone = TimeZone.createTimeZone(480);

    Date date = new Date();
    date.setTime(Date.UTC(2006 - 1900, 6, 27, 14, 10, 10));

    assertEquals("07/27/2006 06:10:10 Etc/GMT+8", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss v").format(date, simpleTimeZone));

    assertEquals("07/27/2006 06:10:10 Etc/GMT+8", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss vv").format(date, simpleTimeZone));

    assertEquals("07/27/2006 06:10:10 Etc/GMT+8", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss vvv").format(date, simpleTimeZone));

    assertEquals("07/27/2006 06:10:10 Etc/GMT+8", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss vvvv").format(date, simpleTimeZone));
  }

  public void test_timezoneZ() {
    TimeZoneConstants timeZoneData = GWT.create(TimeZoneConstants.class);
    String str = timeZoneData.americaLosAngeles();
    TimeZone usPacific = TimeZone.createTimeZone(str);

    Date date = new Date();
    date.setTime(Date.UTC(2006 - 1900, 6, 27, 13, 10, 10));

    assertEquals("07/27/2006 06:10:10 -0700", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss Z").format(date, usPacific));

    assertEquals("07/27/2006 06:10:10 -0700", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss ZZ").format(date, usPacific));

    assertEquals("07/27/2006 06:10:10 -0700", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss ZZZ").format(date, usPacific));

    assertEquals("07/27/2006 06:10:10 GMT-07:00", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss ZZZZ").format(date, usPacific));

    date.setTime(Date.UTC(2006 - 1900, 1, 27, 13, 10, 10));
    assertEquals("02/27/2006 05:10:10 -0800", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss Z").format(date, usPacific));

    assertEquals("02/27/2006 05:10:10 -0800", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss ZZ").format(date, usPacific));

    assertEquals("02/27/2006 05:10:10 -0800", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss ZZZ").format(date, usPacific));

    assertEquals("02/27/2006 05:10:10 GMT-08:00", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss ZZZZ").format(date, usPacific));
  }

  public void test_simpleTimezoneZ() {
    TimeZone simpleTimeZone = TimeZone.createTimeZone(420);
    Date date = new Date();
    date.setTime(Date.UTC(2006 - 1900, 6, 27, 13, 10, 10));

    assertEquals("07/27/2006 06:10:10 -0700", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss Z").format(date, simpleTimeZone));

    assertEquals("07/27/2006 06:10:10 -0700", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss ZZ").format(date, simpleTimeZone));

    assertEquals("07/27/2006 06:10:10 -0700", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss ZZZ").format(date, simpleTimeZone));

    assertEquals("07/27/2006 06:10:10 GMT-07:00", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss ZZZZ").format(date, simpleTimeZone));
  }

  public void test_timezonez() {
    TimeZoneConstants timeZoneData = GWT.create(TimeZoneConstants.class);
    String str = timeZoneData.americaLosAngeles();
    TimeZone usPacific = TimeZone.createTimeZone(str);

    Date date = new Date();
    date.setTime(Date.UTC(2006 - 1900, 6, 27, 13, 10, 10));

    assertEquals("07/27/2006 06:10:10 PDT", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss z").format(date, usPacific));

    assertEquals("07/27/2006 06:10:10 PDT", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss zz").format(date, usPacific));

    assertEquals("07/27/2006 06:10:10 PDT", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss zzz").format(date, usPacific));

    assertEquals("07/27/2006 06:10:10 Pacific Daylight Time",
        DateTimeFormat.getFormat("MM/dd/yyyy HH:mm:ss zzzz").format(date,
            usPacific));

    date.setTime(Date.UTC(2006 - 1900, 1, 27, 13, 10, 10));
    assertEquals("02/27/2006 05:10:10 PST", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss z").format(date, usPacific));

    assertEquals("02/27/2006 05:10:10 PST", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss zz").format(date, usPacific));

    assertEquals("02/27/2006 05:10:10 PST", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss zzz").format(date, usPacific));

    assertEquals("02/27/2006 05:10:10 Pacific Standard Time",
        DateTimeFormat.getFormat("MM/dd/yyyy HH:mm:ss zzzz").format(date,
            usPacific));
  }

  public void test_simpleTimezonez() {
    TimeZone simpleTimeZone = TimeZone.createTimeZone(420);
    Date date = new Date();
    date.setTime(Date.UTC(2006 - 1900, 6, 27, 13, 10, 10));

    assertEquals("07/27/2006 06:10:10 UTC-7", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss z").format(date, simpleTimeZone));

    assertEquals("07/27/2006 06:10:10 UTC-7", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss zz").format(date, simpleTimeZone));

    assertEquals("07/27/2006 06:10:10 UTC-7", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss zzz").format(date, simpleTimeZone));

    assertEquals("07/27/2006 06:10:10 UTC-7", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss zzzz").format(date, simpleTimeZone));
  }

  public void test_daylightTimeTransition() {
    // US PST transitioned to PDT on 2006/4/2 2:00am, jump to 2006/4/2 3:00am.
    // That's UTC time 2006/4/2 10:00am

    TimeZoneConstants timeZoneData = GWT.create(TimeZoneConstants.class);
    String str = timeZoneData.americaLosAngeles();
    TimeZone usPacific = TimeZone.createTimeZone(str);

    Date date = new Date();
    date.setTime(Date.UTC(2006 - 1900, 3, 2, 9, 59, 0));
    assertEquals("04/02/2006 01:59:00 PST", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss z").format(date, usPacific));
    date.setTime(Date.UTC(2006 - 1900, 3, 2, 10, 01, 0));
    assertEquals("04/02/2006 03:01:00 PDT", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss z").format(date, usPacific));
    date.setTime(Date.UTC(2006 - 1900, 3, 2, 10, 0, 0));
    assertEquals("04/02/2006 03:00:00 PDT", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss z").format(date, usPacific));

    // US PDT transition to PST on 2006/10/29 2:00am, jump back to PDT
    // 2006/4/2 1:00am
    date.setTime(Date.UTC(2006 - 1900, 10 - 1, 29, 8, 59, 0));
    assertEquals("10/29/2006 01:59:00 PDT", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss z").format(date, usPacific));
    date.setTime(Date.UTC(2006 - 1900, 10 - 1, 29, 9, 01, 0));
    assertEquals("10/29/2006 01:01:00 PST", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss z").format(date, usPacific));
    date.setTime(Date.UTC(2006 - 1900, 10 - 1, 29, 9, 0, 0));
    assertEquals("10/29/2006 01:00:00 PST", DateTimeFormat.getFormat(
        "MM/dd/yyyy HH:mm:ss z").format(date, usPacific));
  }
}