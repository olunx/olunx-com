/*
 * HtmlConvertor.java
 * 
 * Created on 2007-9-15, 22:24:51
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
public class HtmlConvertor {
    char[]      text;
    int         textLength;
    static final int INDENTATION = 2;

    static final String[] breakTags = {
        "li", "br", "tr", "p", "h1", "h2", "h3", "hr", "/p", "/h1", "/h2", "/h3"
    };

    static final String[] skipTags = {
        "form", "script", "style"
    };
    public HtmlConvertor(char [] text)
    {
        this.text = text;
        this.textLength = text.length;
        this.convertText();
    }

    void convertText() { 
        char[] t = text;
        int length = textLength;
        int src, dst;
        int skip = 0;
        int preserve = 0;

        for (src = 0, dst = 0; src < length; src++) { 
            char b = t[src];
            if (b == '<') { 
                if (src+3 < length && t[src+1] == '!' && t[src+2] == '-' && t[src+3] == '-')
                {
                    src += 3;
                    do { 
                        src += 1;
                    } while (src+3 < length && !(t[src] == '-' &&  t[src+1] == '-' &&  t[src+2] == '>'));
                    src += 2;
                    continue;
                }
                int start = src+1;
                while (++src < length && t[src] != '>') { 
                    if (t[src] == '<') { 
                        start = src+1;
                    }
                }
                String element = new String(t, start, src-start);
                String tagName = element.toLowerCase();
                for (int j = 0; j < skipTags.length; j++) { 
                    String st = skipTags[j];
                    if (tagName.equals(st) 
                        || (tagName.startsWith(st) && tagName.charAt(st.length()) == ' ')) 
                    {
                        skip += 1;
                        break;
                    } 
                }
                if (skip > 0 && tagName.startsWith("/")) { 
                    String suf = tagName.substring(1);
                    for (int j = 0; j < skipTags.length; j++) { 
                        if (suf.equals(skipTags[j])) { 
                            skip -= 1;
                            break;
                        } 
                    }
                }
                if (skip > 0) { 
                    continue;
                }
                if (tagName.startsWith("pre")) { 
                    preserve += 1;
                } else if (tagName.startsWith("/pre")) { 
                    preserve -= 1;
                } else { 
                    for (int j = 0; j < breakTags.length; j++) { 
                        String bt = breakTags[j];
                        if (tagName.equals(bt) 
                            || (tagName.startsWith(bt) && tagName.charAt(bt.length()) == ' ')) 
                        { 
                            if (bt.equals("p")) { 
                                for (int i = 0; i < INDENTATION; i++) { 
                                    t[dst++] = ' ';
                                }
                            } else if (dst == 0 || t[dst-1] == '\n') { 
                                continue;
                            }
                            t[dst++] = '\n';                                
                            break;
                        }
                    }
                }
            } else if (skip == 0) { 
                if (b == '&') { 
                    int j, n = src + 8 < length ? src + 8 : length;
                    for (j = src+1; j < n && t[j] != ';'; j++);
                    if (j < n && t[j] == ';') { 
                        String symStr = new String(t, src+1, j-src-1).toLowerCase();
                        src = j;
                        char ch;
                        if (symStr.equals("amp")) { 
                            ch = '&';
                        } else if (symStr.equals("quot")) { 
                            ch = '"';
                        } else if (symStr.equals("lt")) { 
                            ch = '<';
                        } else if (symStr.equals("gt")) { 
                            ch = '>';
                        } else if (symStr.equals("nbsp")) { 
                            ch = ' ';
                        } else if (symStr.startsWith("#")) { 
                            ch = (char)Integer.parseInt(symStr.substring(1), 16);
                        } else { 
                            continue;
                        }
                        t[dst++] = ch;                        
                    } else { 
                        t[dst++] = '&';
                    }                    
                } else if (b == '\n' || b == ' ' || b == '\t') { 
                    if (preserve != 0) { 
                        t[dst++] = ((b == '\n') ? '\r' : ' ');
                    } else { 
                        if (dst != 0  && t[dst-1] != ' ' && t[dst-1] != '\n') { 
                            t[dst++] = ' ';
                        }
                    }
                } else if (b != '\r') { 
                    t[dst++] = b;
                }
            }
        }
        textLength = dst;
    }
    public String getString()
    {
        return new String(text,0,this.textLength);
    }
    public static String ConvertHtmlToText(String html)
    {
        HtmlConvertor con = new HtmlConvertor( ("<body>" +html + "</body>").toCharArray() );
        return con.getString();
    }
}
