/*
 * ItemList.java
 *
 * Created on Aug 5, 2007, 2:32:46 PM
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

import com.teesoft.jfile.CharsetEncodingFactory;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 *
 * @author wind
 */
public class ItemList {

    Vector list = new /*<DictItem>*/ Vector /*<DictItem>*/ ();
    int lastPos = 0;
    int selected = 0;

    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    public int select(String word) {
        try {
            return select(CharsetEncodingFactory.getBytes(word,"utf-8"));
        } catch (UnsupportedEncodingException ex) {
            return select(word.getBytes());
        }
    }
    public int select(byte[] word) {
        selected = ByteArrayString.search(list, word, lastPos, false);
        if (selected == -1 && list.size()>0)
            selected = 0;
        return selected;
    }

    public ItemList() {
    }

    public int addItem(DictItem item) {
        DictItem newItem = item;
        if (!(newItem instanceof WrapperItem) &&
            !newItem.getDict().getEncoding().equals("utf-8"))
        {
            newItem = new WrapperItem(newItem);
        }
        if (list.size() == 0) {
            list.addElement(newItem);
            lastPos = 0;
            return 1;
        }
        if (lastPos >= list.size()) {
            lastPos = list.size() - 1;
        }
        int comp = ByteArrayString.compareToIgnoreCase(getItem(lastPos), newItem);
        if (comp == 0) {
            addSubItem(lastPos, newItem);
        } else {
            int min = comp > 0 ? 0 : lastPos + 1;
            int max = comp > 0 ? lastPos : list.size() - 1;
            int i = min;

            while (i <= max && ByteArrayString.compareToIgnoreCase(getItem(i), newItem) < 0) {
                ++i;
            }
            if (i > max || ByteArrayString.compareToIgnoreCase(getItem(i), newItem) != 0) {
                list.insertElementAt(newItem, i);
            } else {
                addSubItem(i, newItem);
            }
        }
        return list.size();
    }

    private void addSubItem(int lastPos, DictItem item) {
        DictItem oldItem = getItem(lastPos);
        if (oldItem instanceof DuplicateItem) {
            DuplicateItem di = (DuplicateItem) oldItem;
            if (!di.contains(item))
                di.addItem(item);
        } else {
            DuplicateItem newItem = new DuplicateItem(oldItem);
            newItem.addItem(item);
            list.setElementAt(newItem, lastPos);
        }
    }

    public int appendItem(DictItem item) {
        int c =  this.addItem(item);
        return c;
    }

    public int addItems(ItemList addlist) {
        for (int i = 0; addlist !=null && i < addlist.size(); ++i) {
            addItem(addlist.getItem(i));
        }
        return list.size();
    }

    public void clear() {
        while (list.size() > 0) {
            list.removeAllElements();
        }
    }

    public int size() {
        return list.size();
    }

    public DictItem getItem(int index) {
        return (DictItem) list.elementAt(index);
    }

    public static class WrapperItem extends DictItem {
        DictItem item;
        
        byte buf[];
        
        WrapperItem(DictItem item)
        {
            this.item = item;
            buf = null;
            
        }
        public byteArray getExplains() {
            return item.getExplains();
        }

        public byte[] getBytes() {
            if (buf == null)
            {
                if (!(item instanceof WrapperItem) && !item.getDict().getEncoding().equals("utf-8"))
                {
                    try {
                        buf = CharsetEncodingFactory.getBytes(item.getString(),"utf-8");
                    } catch (UnsupportedEncodingException ex) {
                        buf = item.getBytes();
                    }
                }
                else
                {
                    buf = item.getBytes();
                }
            }
            return buf;
        }

        public String getString() {
            try {
                return CharsetEncodingFactory.newString(getBytes(),"utf-8");
            } catch (UnsupportedEncodingException ex) {
                return new String(getBytes());
            }
        }
        public String toString()
        {
            return getString();
        }

        public void setDict(Dict dict) {
            item.setDict(dict);
        }

        public DictItem getSubItem(int index) {
            return  item.getSubItem(index);
        }

        public int getSubItemCount() {
            return  item.getSubItemCount();
        }

        public Dict getDict() {
            return item.getDict();
        }

        public DictItem getNext() {
            return item.getNext();
        }

        public DictItem getPrevious() {
            return item.getPrevious();
        }
    }
    public static class DuplicateItem extends DictItem {

        Vector items = new Vector();

        DuplicateItem(DictItem item) {
            items.addElement(item);
        }

        public Dict getDict() {
            return getSubItem(0).getDict();
        }

        public int addItem(DictItem item) {
            items.addElement(item);
            return items.size();
        }

        public DictItem getSubItem(int index) {
            return (DictItem) items.elementAt(index);
        }

        public int getSubItemCount() {
            return items.size();
        }

        public byteArray getExplains() {
            return getSubItem(0).getExplains();
        }

        public byteArray getHtmlExplains() {
            return getSubItem(0).getHtmlExplains();
        }

        public byte[] getBytes() {
            return getSubItem(0).getBytes();
        }

        public String getString() {
            return getSubItem(0).getString();
        }

        public String toString() {
            return getString();
        }

        public DictItem getNext() {
            return getSubItem(0).getNext();
        }

        public DictItem getPrevious() {
            return getSubItem(0).getPrevious();
        }

        public boolean contains(DictItem item) {
            return items.contains(item);
        }
    }
}
