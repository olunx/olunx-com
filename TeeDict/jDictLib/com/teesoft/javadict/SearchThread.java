/*
teedict , to be the best dictionary application for java me enabled devices.
Copyright (C) 2006,2007  Yong Li. All rights reserved.
 
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>. 
*/
package com.teesoft.javadict;

public class SearchThread implements Runnable {

    String word = null;
    long lastTime = 0;
    private boolean force = false;
    SearchListener searhListener;
    private boolean bSearching=false;
    DictManager dictManager=null;
    private String nextSearch;

    private ItemList lastIndex=null;
    private boolean running=true;
    private Thread searchThread=null;

    public boolean isBSearching() {
        return bSearching;
    }

    public void setBSearching(boolean bSearching) {
        this.bSearching = bSearching;
    }

    public String getNextSearch() {
        return nextSearch;
    }

    public void setNextSearch(String nextSearch) {
        this.nextSearch = nextSearch;
        newThread();
    }
    
    
    
    
    public SearchThread(DictManager dictManager, SearchListener searhListener) {
        super();
        this.dictManager = dictManager;
        this.searhListener = searhListener;
    }


    public synchronized void setWord(String word) {
        lastTime = System.currentTimeMillis();
        this.word = word;
        nextSearch=null;
        newThread();
    }

    int step = 128;
    public void run() {
        while (running) {
            if (word == null || (searhListener.getDelay() != 0 && System.currentTimeMillis() - lastTime < searhListener.getDelay())) {
                try {
                    while (searhListener.getDelay() != 0 && !force && System.currentTimeMillis() - lastTime < searhListener.getDelay()) {
                            
                        Thread.sleep( step);
                            
                            //this.wait(step);
                    }
                    
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            if (word != null) {
                bSearching = true;
                    
                threadDoSearch();
                searhListener.onSearchResult(word, lastIndex);
                word = null;                
                bSearching = false;
                if (nextSearch!=null)
                {
                    setWord(nextSearch);
                    force = true;
                }                
                else
                    running = false;
                
            }
        }
    }

    private void newThread() {
        if (searchThread == null || !searchThread.isAlive()) {
            running = true;
            searchThread = null;
            searchThread = new Thread(this);
            searchThread.start();
        }
    }



    private synchronized void threadDoSearch() {
        try {
            try {
                searhListener.onStartSearch(word);
                lastIndex = dictManager.search(word, searhListener.getSearchDictName());
            } catch (Throwable oe) {
                searhListener.onSearchError(oe);
                System.gc();
            }
            while (nextSearch != null) {
                setWord(nextSearch);
                //setForce(forceSearch);
                //nextSearch = null;
                threadDoSearch();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setForce(boolean force) {
        this.force = force;
    }
    public static interface SearchListener 
    {
        boolean onStartSearch(String word);
        void onSearchResult(String word,ItemList searchResult);
        void onSearchError(Throwable ex);
        String getSearchDictName();
        int getDelay();
    }
}
