/*
 * SparseFileAccess.java
 *
 * Created on September 28, 2006, 7:37 PM
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
package com.teesoft.jfile.sparse;

import com.teesoft.jfile.CharsetEncodingFactory;
import com.teesoft.jfile.FileAccessBase;
import com.teesoft.jfile.FileFactory;
import com.teesoft.jfile.IFileFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

/**
 *
 * @author wind
 */
public class SparseFileAccess extends FileAccessBase {

    protected FileAccessBase connection = null;
    protected InputStream input = null;
    protected OutputStream output = null;
    protected String basename;
    protected int sparseSize;
    protected long newOffset;
    protected long filesize;
    protected int digit;
    protected String digitString;
    protected boolean exists;

    public SparseFileAccess(String s) throws IOException {
        super(s);
        exists = loadInfo();
    }

    protected String getFileName(int pos) {
        return getBaseName() + "." + getDigit(pos) + ".split";
    }

    protected boolean loadInfo() {
        try {
            if (getConnection().exists()) {
                byte[] buf = new byte[(int) getConnection().fileSize()];
                getConnection().read(buf);
                String str = CharsetEncodingFactory.newString(buf, "utf-8");
                String magic = str.substring(0, 8);
                String version = str.substring(8, 10).trim();
                String len = str.substring(10, 20).trim();
                filesize = Long.parseLong(len);
                String sparse = str.substring(20, 28).trim();
                sparseSize = Integer.parseInt(sparse);

                digit = Integer.parseInt(str.substring(28, 29));
                digitString = str.substring(29, str.length()).trim();
                getConnection().close();
                connection = null;
                return true;
            } else {
                filesize = 0;
                sparseSize = 128 * 1024;
                digit = 5;
                digitString = "0123456789";

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public InputStream getInputStream() throws IOException {
        return this;
    }

    public InputStream getIntenelInputStream() throws IOException {
        open();
        int newBlock = (int) (newOffset / this.sparseSize);
        int currBlock = (int) (this.getOffset() / this.sparseSize);
        if (newOffset >= this.filesize) {
            return null;
        }
        if (input == null || this.getOffset() > newOffset || currBlock != newBlock || (this.getOffset() != 0 && this.getOffset() % this.sparseSize == 0)) {
            if (input != null) {
                input.close();
                input = null;
            }
            input = openFile(newBlock);
            long skipLen = input.skip(newOffset - newBlock * sparseSize);
        } else if (newOffset - this.getOffset() != 0) {
            input.skip(newOffset - this.getOffset());
        }
        setOffset(newOffset);


        return input;
    }

    public boolean canRead() throws IOException {
        return getConnection().canRead();
    }

    public boolean canWrite() throws IOException {
        return !getConnection().exists();
    }

    public boolean exists() throws IOException {
        return exists;
    }

    public long fileSize() {
        return filesize;
    }

    public OutputStream getOutputStream() throws IOException {
        if (output == null) {
            output = new SparseOutputStream();
        }
        return output;
    }

    public boolean isDirectory() throws IOException {
        return false;
    }

    public boolean isFile() throws IOException {
        return true;
    }

    public boolean isHidden() throws IOException {
        return getConnection().isHidden();
    }

    public Vector listFiles() throws IOException {
        return null;
    }

    public Vector listFiles(String name, boolean includeHidden) throws IOException {
        return null;
    }

    public void open() throws IOException {
        if (connection == null) {
            setConnection((FileAccessBase) FileFactory.openFileAccess(getIndexFileURI(getLocation()), true));
        }
    }

    protected String getIndexFileURI(String s) {
        return getRealFileURI(s) + ".mul";
    }

    protected String getRealFileURI(String s) {
        if (s.startsWith("sparse://")) {
            return s.substring("sparse://".length());
        }
        return s;
    }

    public void setInputStream(InputStream input) {
        this.input = input;
    }

    public void setOutputStream(OutputStream output) {
    }

    public FileAccessBase child(String dir) throws IOException {
        return null;
    }

    public String getName() throws IOException {
        String name = getConnection().getName();
        return name.substring(0, name.length() - ".mul".length());
    }

    public String getAbsolutePath() throws IOException {
        String path = getConnection().getAbsolutePath();
        return path.substring(0, path.length() - ".mul".length());
    }

    public void create() throws IOException {
    }

    public IFileFactory getFileFactory() {
        return SparseFactory.getInstance();
    }

    public FileAccessBase getConnection() {
        if (connection == null) {
            try {
                open();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return connection;
    }

    public void setConnection(FileAccessBase connection) {
        this.connection = connection;
    }

    public long skip(long len) throws IOException {
        long oldOffset = this.getOffset();

        if (len < 0) {
            close();
            open();
        }
        absolute(oldOffset + len);

        return this.getOffset() - oldOffset;
    }

    public void absolute(long offset) throws IOException {
        if (offset <= filesize) {
            newOffset = offset;
        } else {
            newOffset = filesize;
        }
        this.getIntenelInputStream();
    }

    public String getBaseName() {
        if (basename == null) {
            try {
                basename = getConnection().getAbsolutePath();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            basename = basename.substring(0, basename.length() - ".mul".length());

        }
        return basename;
    }

    protected String getDigit(int num) {
        int digitLen = this.digitString.length();
        int tmp = num;
        String result = "";
        for (int i = 0; i < this.digit; ++i) {
            result = digitString.charAt(tmp % digitLen) + result;
            tmp = tmp / digitLen;
        }
        return result;
    }

    protected FileAccessBase openFile(int pos) throws IOException {
        String file = getFileName(pos);
        return FileFactory.openFileAccess(file, true);
    }

    public int read() throws IOException {

        int ret = this.getIntenelInputStream().read();
        setOffset(getOffset() + 1);
        return ret;
    }

    public int read(byte[] b, int start, int len) throws IOException {
        int resultRet = 0;
        int ret = readPart(b, start, len);
        while (ret > 0 && len - ret > 0) {
            resultRet += ret;
            len -= ret;
            start += ret;
            ret = readPart(b, start, len);
        }
        if (ret > 0) {
            resultRet += ret;
        } else if (resultRet == 0) {
            return ret;
        }
        return resultRet;
    }

    public int readPart(byte[] b, int start, int len) throws IOException {
        if (isDirectory()) {
            return -1;
        }
        if (getIntenelInputStream() == null) {
            return -1;
        }
        if (len - start > b.length) {
            len = b.length - start;
        }
        if (len == 0) {
            return 0;
        }
        int ret = getIntenelInputStream().read(b, start, len);
        if (ret > 0) {
            setOffset(getOffset() + ret);
        }
        return ret;
    }

    protected void setOffset(long offset) {
        this.newOffset = offset;
        super.setOffset(offset);
    }

    public void close() throws IOException {
        if (input != null) {
            input.close();
            input = null;
        }
        if (output != null) {
            output.close();
            output=null;
        }
        if (connection != null) {
            connection.close();
            connection = null;
        }
        setInputStream(null);
        setConnection(null);
        setOffset(0);
    }

    class SparseOutputStream extends OutputStream {

        OutputStream output = null;
        FileAccessBase currentFile = null;
        int pos = -1;

        OutputStream getOutputStream() {
            int newpos = (int) ((filesize + 1) / SparseFileAccess.this.sparseSize);

            if (newpos != pos) {
                FileAccessBase file = null;
                try {
                    pos = newpos;
                    String fileName = getBaseName() + "." + getDigit(pos) + ".split";
                    if (output != null) {
                        try {
                            output.flush();
                            //output.close();
                            output = null;
                            currentFile.close();
                        } catch (IOException ex) {
                            //ex.printStackTrace();
                        }
                    }
                    file = FileFactory.newFileAccess(fileName);
                    if (!file.exists()) {
                        file.create();
                    }
                    currentFile = file;
                    output = file.getOutputStream();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
            return output;
        }

        public void write(int arg0) throws IOException {
            OutputStream out = getOutputStream();
            out.write(arg0);
        }

        public void close() throws IOException {
            if (output != null) {
                output.flush();
                //output.close();
                output = null;
                if (currentFile!=null)
                    currentFile.close();
            }
            OutputStream indexStream = getConnection().getOutputStream();
            String desc = "sparseft" + "01" + getString(filesize, 10) + getString(sparseSize, 8) + getString(digit, 1) + digitString;
            indexStream.write(desc.getBytes("utf-8"));
            indexStream.close();
        }

        private String getString(long len, int i) {
            String str = String.valueOf(len);
            while (str.length() < i) {
                str = " " + str;
            }
            return str.substring(str.length() - i);
        }

        public void flush() throws IOException {
            if (output != null) {
                output.flush();
            }
        }

        public void write(byte[] bytes) throws IOException {
            if (bytes != null) {
                write(bytes, 0, bytes.length);
            }
        }

        public void write(byte[] bytes, int start, int len) throws IOException {
            while (len > 0) {
                OutputStream out = getOutputStream();
                int writeLen = (int) ((pos + 1) * sparseSize - filesize);
                writeLen = java.lang.Math.min(len, writeLen);
                out.write(bytes, start, writeLen);
                start += writeLen;
                len -= writeLen;
                filesize += writeLen;
            }
        }
    }

    public void delete() throws IOException {
        try {
            getConnection().delete();
        } catch (IOException ex) {
        }

        int count = (int) (filesize / sparseSize);
        if (count * sparseSize < filesize) {
            count++;
        }
        for (int i = 0; i < count; ++i) {
            FileAccessBase file = openFile(i);
            try {
                file.delete();
            } catch (IOException ex) {
            }
        }
    }
    public boolean isRawFile() {
        return false;
    }
}
