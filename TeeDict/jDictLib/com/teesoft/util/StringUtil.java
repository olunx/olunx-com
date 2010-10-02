/*
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

import java.util.Vector;

public final class StringUtil
{

    public StringUtil()
    {
    }

    public static final String replace(String s, String s1, String s2)
    {
        int i = s1.length();
        if (i==0)
            return s;
        String s3;
        for (int j = s.indexOf(s1); j != -1; j = (s = s3 + s2 + s.substring(j + i)).indexOf(s1))
            s3 = new String(s.substring(0, j));

        return s;
    }

    public static final String[] split(String s, String s1)
    {
        String s2 = s = trim(s);
        Vector vector = new Vector();
        do
        {
            if (s2.indexOf(s1) == -1)
            {
                if (vector.isEmpty())
                    vector.addElement(((Object) (s2)));
                else
                if (s2.length() > 0)
                    vector.addElement(((Object) (s2)));
                break;
            }
            if (s2.indexOf(s1) != 0 && s2.substring(0, s2.indexOf(s1)) != null)
                vector.addElement(((Object) (s2.substring(0, s2.indexOf(s1)))));
            if (s2.indexOf(s1) + s1.length() >= s2.length())
                break;
            s2 = s2.substring(s2.indexOf(s1) + s1.length());
        } while (true);
        int i;
        String as[] = new String[i = vector.size()];
        for (int j = 0; j < i; j++)
            as[j] = (String)vector.elementAt(j);

        return as;
    }

    public static final String trim(String s)
    {
        int i = 0;
        int j = s.length();
        for (; i < s.length(); i++)
            if (s.charAt(i) != ' ')
                break;

        for (; j >= 0; j--)
            if (s.charAt(j - 1) != ' ')
                break;

        return s.substring(i, j);
    }

}