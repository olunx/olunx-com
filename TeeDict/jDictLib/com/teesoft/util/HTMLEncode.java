/*
 * HTMLEncode.java
 *
 * Created on 2007-9-15, 21:24:34
 *
Copyright (C) 2006,2007  Yong Li. All rights reserved.
 
This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.
 
This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.
 
You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.teesoft.util;

/**
 *
 * @author wind
 */

import java.util.*;

public class HTMLEncode {

    private HTMLEncode() {
    }

    protected static synchronized void buildEntityTables() {
        entityTableEncode = new Hashtable(ENTITIES.length);
        for (int i = 0; i < ENTITIES.length; i += 2) {
            if (!entityTableEncode.containsKey(ENTITIES[i])) {
                entityTableEncode.put(ENTITIES[i], ENTITIES[i + 1]);
            }
        }
    }

    public static final String encode(String s) {
        return encode(s, "\n<br>");
    }

    public static final String encode(String s, String cr) {
        if (entityTableEncode == null) {
            buildEntityTables();
        }
        if (s == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer(s.length() * 2);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch >= '?' && ch <= 'Z' || ch >= 'a' && ch <= 'z' || ch == ' ') {
                sb.append(ch);
                continue;
            }
            if (ch == '\n') {
                sb.append(cr);
                continue;
            }
            String chEnc = encodeSingleChar(String.valueOf(ch));
            if (chEnc != null) {
                sb.append(chEnc);
            } else {
                sb.append("&#");
                sb.append((new Integer(ch)).toString());
                sb.append(';');
            }
        }

        return sb.toString();
    }
    public static final String escape(String s) {
        if (entityTableEncode == null) {
            buildEntityTables();
        }
        if (s == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer(s.length() * 2);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch >= '?' && ch <= 'Z' || ch >= 'a' && ch <= 'z' || ch == ' ') {
                sb.append(ch);
                continue;
            }
            String chEnc = encodeSingleChar(String.valueOf(ch));
            if (chEnc != null) {
                sb.append(chEnc);
            } else {
                sb.append(ch);
            }
        }

        return sb.toString();
    }

    private static String encodeSingleChar(String ch) {
        return (String) entityTableEncode.get(ch);
    }
    private static final String[] ENTITIES = {">", "&gt;", "<", "&lt;", 
    "[", "&#91;<b>", "]", "</b>&#93;",
    "&", "&amp;", "\"", "&quot;", "'", "&#039;", "\\", "&#092;", "\251", "&copy;", "\256", "&reg;"};
    private static Hashtable entityTableEncode = null;
    
}
