/*
 * DictItem.java
 * 
 * Created on Aug 5, 2007, 2:28:09 PM
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

package com.teesoft.javadict;

/**
 *
 * @author wind
 */
public abstract class DictItem implements byteArray{
    private Dict dict;

    public Dict getDict() {
        return dict;
    }
    public void setDict(Dict dict) {
        this.dict = dict;
    }
    
    protected int index;
    
    public abstract byteArray getExplains();
    public  byteArray getHtmlExplains()
    {
        if (this.getDict().isHtml())
            return getExplains();
        else
        {
            java.lang.String ret = com.teesoft.util.HTMLEncode.encode(getExplains().getString());
            return new com.teesoft.javadict.ByteArrayString(ret);
        }
    }
    public int getSubItemCount(){
        return 0;
    }
    public DictItem getSubItem(int index)
    {
        if (index==0)
            return this;
        return null;
    }

    public String getString() {
        return toString();
    }

    public abstract byte[] getBytes() ;
    
    public abstract DictItem getNext();
    public abstract DictItem getPrevious();
    
}
