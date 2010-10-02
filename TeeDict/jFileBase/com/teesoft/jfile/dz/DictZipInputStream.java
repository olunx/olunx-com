/*
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
package com.teesoft.jfile.dz;



import com.jcraft.jzlib.ZInputStream;


import com.teesoft.jfile.FileAccessBase;
import com.teesoft.jfile.FileAccessBase;

import com.teesoft.jfile.dz.CRC32;

import java.io.*;



public class DictZipInputStream extends ZInputStream
{
    
    FileAccessBase in=null;
    
    DictZipHeader h=null;
    
    long offset = 0;
    
    
    
    /**
     *
     * CRC-32 for uncompressed data.
     *
     */
    
    protected CRC32 crc = new CRC32();
    
    
    
    /**
     *
     * Indicates end of input stream.
     *
     */
    
    protected boolean eos;
    
    
    
    /**
     *
     * Creates a new input stream with a default buffer size.
     *
     * @param in the input stream
     *
     * @exception IOException if an I/O error has occurred
     *
     */
    
    protected  DictZipInputStream(InputStream in) throws IOException
    {
        
        this(in, 512);
        
    }
    
    /**
     *
     * Creates a new input stream with the specified buffer size.
     *
     * @param in the input stream
     *
     * @param size the input buffer size
     *
     * @exception IOException if an I/O error has occurred
     *
     */
    
    protected DictZipInputStream(InputStream in, int size) throws IOException
    {
        
        super(in,size);
        
    }
    
    public DictZipInputStream(FileAccessBase in) throws IOException
    {
        
        this(in,  512);

    }
    
    public DictZipInputStream(FileAccessBase in, int size) throws IOException
    {
        
        super(in,  true);
        
        this.in = in;
        
        reset();
        
    }
    
    public synchronized void reset() throws IOException
    {
        
        in.reset();
        
        h = readHeader();
        
        //crc.reset();
        
        offset = 0;
        
    }
    
    public long skip(long n) throws IOException
    {
        
        if (n==0)
            
            return 0;
        
        int targetChunck = h.getChunckByDstOffset(offset + n);
        
        if (targetChunck >= h.chunkCount)
            
            return -1;
        
        else if (targetChunck < 0)
            
            return -1;
        
        
        
        FileAccessBase fa = in;
        
        long srcOffset =fa.getOffset();
        
        int srcChunck = h.getChunckByDstOffset(offset);
        
        //System.out.println("offset:" + offset);
        
        long curroffset = offset+ n;
        
        long ret=n;
        
        //System.out.println("n:" + n + " srcOffset:" + srcOffset + " srcChunck:" + srcChunck);
        
        //System.out.println("offset:" + offset + " targetOffset:" + h.getOffset(targetChunck ) + " targetChunck:" + targetChunck);
        
        //System.out.println(" srcChunck Offset:" + h.getOffset(srcChunck ) + " h.chunkLength: " + h.chunkLength  );
        
        if (n>0 && targetChunck == srcChunck)
            
        {
            
            ret = superForceSkip(n);
            
        }
        
        else
            
        {
            
            //super.reset();
            
            fa.absolute(  (int)(h.getOffset(targetChunck ) ));
            
            
            resetZStream();
            
            offset = h.chunkLength * targetChunck;
            
            superForceSkip(curroffset - offset);
            
        }
        
        
        
        return ret;
        
    }
    
    /**
     *
     * Closes the input stream.
     *
     * @exception IOException if an I/O error has occurred
     *
     */
    
    public void close() throws IOException
    {
        
        super.close();
        
        FileAccessBase fa = in;
        
        fa.close();
        
        eos = true;
        
        offset = 0;
        
    }
    
    /**
     *
     * Reads uncompressed data into an array of bytes. Blocks until enough
     *
     * input is available for decompression.
     *
     * @param buf the buffer into which the data is read
     *
     * @param off the start offset of the data
     *
     * @param len the maximum number of bytes read
     *
     * @return	the actual number of bytes read, or -1 if the end of the
     *
     *		compressed input stream is reached
     *
     * @exception IOException if an I/O error has occurred or the compressed
     *
     *			      input data is corrupt
     *
     */
    
    public int read(byte b[], int off, int len) throws IOException
    {
        
        int n = 0;
        
        while (n < len)
        {
            
            int count = readPart(b, off + n, len - n);
            
            if (count < 0)
                
                return n;
            
            n += count;
            
        }
        
        
        return n;
        
    }
    
    public int readPart(byte[] buf, int off, int len) throws IOException
    {
        
        if (eos)
        {
            
            return -1;
            
        }
        
        int allLen =0;
        
        int ret=0;
        
        if(!eos && len-allLen > 0)
            
        {
            
            ret = super.read(buf, off + allLen, len-allLen);
            
            if (ret >= 0 )
                
            {
                
                allLen += ret;
                
                //crc.update(buf, off, len);
                
            }
            
            else
                
                eos = true;
            
        }
        
        if (allLen > 0)
            
            offset += allLen;
        
        //System.out.println("offset:" + offset);
        
        //System.out.println("buf:" + CharsetEncodingFactory.newString(buf,"utf-8"));
        
        return allLen;
        
    }
    
    public final void readFully(byte b[]) throws IOException
    {
        
        read(b, 0, b.length);
        
    }
    
    public DictZipHeader readHeader() throws IOException
    {
        
        DictZipHeader h = new DictZipHeader();
        
        h.readHeader(h, in, crc);
        
        return h;
        
    }
    
        /*
         
         * Reads GZIP member trailer.
         
         */
    
    /*
     
    private void readTrailer() throws IOException {
     
        InputStream in = this.in;
     
        int n = inf.getRemaining();
     
        if (n > 0) {
     
            in = new SequenceInputStream(
     
                    new ByteArrayInputStream(buf, len - n, n), in);
     
        }
     
        long v = crc.getValue();
     
        long crcVal = readUInt(in);
     
        if (crcVal != v) {
     
            throw new IOException("Incorrect CRC");
     
        }
     
        long total = inf.getTotalOut();
     
        long trailerTotal = readUInt(in);
     
        //System.out.println("Computed CRC = "+v+" / From input "+crcVal);
     
        //System.out.println("Computed size = "+total+" / From input "+trailerTotal);
     
        if (trailerTotal != total) {
     
            throw new IOException("False number of uncompressed bytes");
     
        }
     
    }
     
     */
    
    private long readUInt(InputStream in) throws IOException
    {
        
        return DictZipHeader.readUInt(in);
        
    }
    
    
    
    private long superForceSkip(long l) throws IOException
    {
        
        long ret = super.skip(l);
        
        int alllen = 0;
        
        if (ret < 0)
            
            return -1;
        
        while(ret > 0 && l - ret >= 0)
            
        {
            
            alllen += ret;
            
            l -= ret;
            
            ret = super.skip(l);
            
        }
        
        return alllen;
        
    }
    
}

