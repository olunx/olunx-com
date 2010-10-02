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
package com.teesoft.javadict;

import com.teesoft.jfile.FileAccessBase;
import com.teesoft.jfile.FileFactory;
import com.teesoft.jfile.IFileFactory;
import com.teesoft.jfile.sparse.SparseFileAccess;
import java.io.IOException;
import java.io.OutputStream;


public class SplitDictHttpAccess extends SparseFileAccess {

    protected String dict;
    protected String file;
    static final long SIZE = 64 * 1024;
    static String preifx = "splitdict://";

    public SplitDictHttpAccess(String s) throws IOException {

        super(s);
    }

    protected boolean loadInfo() {
        String s = getLocation();
        s = s.substring(preifx.length());
        String[] tmp = com.teesoft.jfile.resource.resourceFileAccess.split(s, '/');
        dict = tmp[0];
        file = tmp[1];
        sparseSize = (int) SIZE;
        try {
            String url = OnlineInfo.dictsizeUrl + dict + "/" + file;
            FileAccessBase con = FileFactory.openFileAccess(url, false);
            byte [] buf = new byte[32];
            int len = con.read(buf);
            
            filesize = Long.parseLong(new String(buf,0,len));//
            //con.fileSize();
            setConnection(con);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    protected String getFileName(int pos) {
        int part = (int) (getOffset() / SIZE);
        long start = part * SIZE;
        long end = (part + 1) * SIZE;
        if (end > filesize) {
            end = filesize;
        }
        String url =OnlineInfo.splitfileUrl + dict + "/" + file + "/" + start + "/" + end;
        //System.out.println(url);
        return url;
    }

    public IFileFactory getFileFactory() {
        return SplitDictFileFactory.getInstance();
    }

    public boolean canWrite() throws IOException {
        return false;
    }

    public OutputStream getOutputStream() throws IOException {
        return null;
    }
}
