/*
 * bucketBase.java
 *
 * Created on Aug 5, 2007, 9:05:18 PM
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

import java.util.Vector;

/**
 *
 * @author wind
 */
public abstract class bucketBase {

    static Vector/*<bucketLet>*/ bucketLets = new Vector/*<bucketLet>*/();
    static void clearCache(int count)
    {
        while(bucketLets.size()>count)
        {
            bucketLet let = (bucketLet) bucketLets.elementAt(0);
            //System.out.println("clearCache let:" + let.getClass());
            let.clearBucket();
            bucketLets.removeElementAt(0);            
        }
    }
    private synchronized void addCache(bucketLet let,int count) {
        if (bucketLets.contains(let))
            return;
        if (Runtime.getRuntime().freeMemory()/1024< 64)
            clearCache(count);
        bucketLets.addElement(let);
    }

    public bucketBase(Dict dict) {
        this.dict = dict;
        lastPos = 0;
    }
    

    public ItemList search(byte[] word,int maxCount) {
        synchronized (this) {
            ItemList list = new ItemList();
            lastPos = ByteArrayString.search(getIndexes(), word, lastPos, true);
            if (lastPos<0 && getIndexes().size()>0)
                lastPos = 0;
            if (lastPos >= 0) {
                bucketLet bucket = getBucketLet(lastPos,maxCount);
                //System.out.println("Start load bucket");
                //System.gc();
                Vector bucketVec = bucket.getBucket();
                //System.out.println("End load bucket");
                int bucketIndex = ByteArrayString.search(bucketVec, word, 0, false);
                //System.out.println("End search bucket");
                if (bucketIndex<0 && bucketVec.size()>0)
                    bucketIndex = 0;
                if (bucketIndex >= 0) {
                    int max = maxCount;
                    int minIndex = bucketIndex - max / 2;
                    int inserted = 0;
                    if (minIndex < 0) {
                        if (lastPos > 0) {
                            //load previous bucket
                            Vector preBucketVec = getBucketLet(lastPos - 1,maxCount).getBucket();
                            int preMin = preBucketVec.size() - 1 + minIndex;
                            if (preMin < 0) {
                                preMin = 0;
                            }
                            for (int i = preMin; i < preBucketVec.size() - 0; ++i) {
                                list.appendItem((DictItem) preBucketVec.elementAt(i));
                                inserted++;
                            }
                        }
                        minIndex = 0;
                    }
                    max -= inserted;
                    int maxIndex = minIndex + max;
                    if (maxIndex >= bucketVec.size()) {
                        maxIndex = bucketVec.size() - 1;
                    }
                    for (int i = minIndex; i <= maxIndex && list.size()<maxCount; ++i) {
                        list.appendItem((DictItem) bucketVec.elementAt(i));
                    }


                    if (minIndex + max >= bucketVec.size() && list.size()<maxCount && lastPos < getIndexes().size() - 1) {
                        Vector nextBucketVec = getBucketLet(lastPos + 1,maxCount).getBucket();
                        int nextMax = (minIndex + max) - bucketVec.size() + 1;
                        if (nextMax > nextBucketVec.size() - 1) {
                            nextMax = nextBucketVec.size() - 1;
                        }
                        for (int i = 0; i < nextMax; ++i) {
                            list.appendItem((DictItem) nextBucketVec.elementAt(i));
                        }
                    }
                }
            }
            return list;
        }
    }

    private int lastPos;
    private Dict dict;

    protected abstract bucketLet get(int lastPos);

    protected abstract Vector getIndexes();


    private bucketLet getBucketLet(int lastPos, int maxCount) {
        bucketLet let = get(lastPos);
        addCache(let,maxCount);
        return let;
    }
}
