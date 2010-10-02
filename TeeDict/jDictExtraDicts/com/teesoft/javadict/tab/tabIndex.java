/*
 * tabIndex.java
 * 
 * Created on Aug 5, 2007, 5:34:34 PM
 * 
Copyright (C) 2007  Yong Li. All rights reserved.

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

package com.teesoft.javadict.tab;

import com.teesoft.javadict.ByteArrayString;
import com.teesoft.javadict.DictItem;
import com.teesoft.javadict.ItemList;
import com.teesoft.javadict.bucketBase;
import com.teesoft.javadict.bucketLet;
import java.util.Vector;

/**
 *
 * @author wind
 */
public class tabIndex extends bucketBase{

    Vector /*<tabIndexItem>*/ indexes = new Vector();
    public tabIndex(tabDict dict,int count) {
        super(dict);
        this.dict = dict;
        indexes.ensureCapacity(count);

    }
    public void addIndexItem(tabIndexItem item)
    {
        indexes.addElement(item);
    }
    public tabIndexItem getIndexItem(int index)
    {
        return (tabIndexItem) indexes.elementAt(index);
    }
    public int size()
    {
        return indexes.size();
    }

    protected  bucketLet get(int pos) {
        if (pos<0 || pos>=indexes.size())
            return null;
       return (bucketLet) indexes.elementAt(pos);
    }
    protected  Vector getIndexes() {
        return indexes;
    }
    private tabDict dict;




}
