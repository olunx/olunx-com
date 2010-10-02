/*
 * jFilterInputStream.java
 *
 * Created on 2006-9-28, 11:06 AM
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



package com.teesoft.jfile;



import java.io.IOException;

import java.io.InputStream;



/**

 *

 * @author ly

 */

public       class jFilterInputStream extends InputStream {

    protected volatile InputStream in;

    long offset =0;

    public jFilterInputStream(InputStream in) {

        this.setIn(in);

    }

    

    public int read() throws IOException {

        int ret=getIn().read();

        if (ret >0)

            offset+= ret;

        return ret;

    }

    

    public int read(byte b[]) throws IOException {

        return read(b, 0, b.length);

    }

    

    public int read(byte b[], int off, int len) throws IOException {

        int ret=getIn().read(b, off, len);

        if (ret >0)

            offset+= ret;

        return ret;

    }

    public long skip(long n) throws IOException {

        long ret=getIn().skip(n);

        if (ret >0)

            offset+= ret;

        return ret;

    }

    public int available() throws IOException {

        return getIn().available();

    }

    

    public void close() throws IOException {

        getIn().close();

        offset = 0;

    }

    

    public synchronized void mark(int readlimit) {

        getIn().mark(readlimit);

    }

    

    public synchronized void reset() throws IOException {

        getIn().reset();

        offset = 0;

    }

    

    public boolean markSupported() {

        return getIn().markSupported();

    }

    public long getOffset()

    {

        return offset;

    }

    public InputStream getIn() {
        return in;
    }

    public void setIn(InputStream in) {
        this.in = in;
    }

}

