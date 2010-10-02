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

import com.teesoft.jfile.FileAccessBase;
import com.teesoft.jfile.FileFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class soundPlayer extends Thread {

    private String filename;
    private int curPosition;
    private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb
    private InputStream soundStream;
    private boolean debug = false;
    private SourceDataLine m_line=null;
    private AudioInputStream m_encodedaudioInputStream;
    private int encodedLength;
    private AudioInputStream m_audioInputStream;

    static class Position {

        static int LEFT = 0;
        static int RIGHT = 1;
        static int NORMAL = 2;
        
    }
    
    public soundPlayer(String wavfile) {
        filename = wavfile;
        curPosition = Position.NORMAL;
    }

    public soundPlayer(InputStream wavStream) {
        soundStream = wavStream;
        curPosition = Position.NORMAL;
    }

    public soundPlayer(String wavfile, int p) {
        filename = wavfile;
        curPosition = p;
    }
    public ArrayList getMixers()
    {
        ArrayList arraylist = new ArrayList();
        javax.sound.sampled.Mixer.Info ainfo[] = AudioSystem.getMixerInfo();
        if(ainfo != null)
        {
            for(int i = 0; i < ainfo.length; i++)
            {
                javax.sound.sampled.Line.Info info = new javax.sound.sampled.Line.Info(javax.sound.sampled.SourceDataLine.class);
                Mixer mixer = AudioSystem.getMixer(ainfo[i]);
                if(mixer.isLineSupported(info))
                    arraylist.add(ainfo[i].getName());
            }

        }
        return arraylist;
    }


    public void run() {
        m_audioInputStream = null;

        if (soundStream == null) {
            File soundFile = new File(filename);
            if (!soundFile.exists()) {
                System.err.println("Sound file not found: " + filename);
                return;
            }


            try {
                m_audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            } catch (UnsupportedAudioFileException e1) {
                e1.printStackTrace();
                return;
            } catch (IOException e1) {
                e1.printStackTrace();
                return;
            }
        } else {
            try {
                if (debug) {
                    FileAccessBase file = (FileAccessBase) soundStream;
                    file.absolute(0);

                    FileAccessBase newFile = FileFactory.openFileAccess(getTempDir().getAbsolutePath() , false);
                    
                    file.copyTo(newFile, null);
                    file.absolute(0);
                    
                    newFile.close();
                    newFile = FileFactory.openFileAccess(newFile.getAbsolutePath() + File.separator + file.getName() , false);
                    m_audioInputStream = AudioSystem.getAudioInputStream(newFile);
                    
                }
                else
                    m_audioInputStream = AudioSystem.getAudioInputStream(soundStream);
            } catch (UnsupportedAudioFileException e1) {
                e1.printStackTrace();
                return;
            } catch (IOException e1) {
                e1.printStackTrace();
                return;
            }
        }

        AudioFormat format = m_audioInputStream.getFormat();
        SourceDataLine auline = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        try {
            auline =createLine();
            if (auline==null)
                auline = (SourceDataLine) AudioSystem.getLine(info);
            format = m_audioInputStream.getFormat();
            auline.open(format);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (auline.isControlSupported(FloatControl.Type.PAN)) {
            FloatControl pan = (FloatControl) auline
					.getControl(FloatControl.Type.PAN);
            if (curPosition == Position.RIGHT) {
                pan.setValue(1.0f);
            } else if (curPosition == Position.LEFT) {
                pan.setValue(-1.0f);
            }
        }

        auline.start();
        int nBytesRead = 0;
        byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];

        try {
            while (nBytesRead != -1) {
                nBytesRead = m_audioInputStream.read(abData, 0, abData.length);
                if (nBytesRead >= 0) {
                    auline.write(abData, 0, nBytesRead);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } finally {
            try {
                auline.drain();
                auline.close();
                if (soundStream != null) {
                    soundStream.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    protected SourceDataLine createLine()
        throws LineUnavailableException
    {
        if(m_line == null)
        {
            AudioFormat audioformat = m_audioInputStream.getFormat();
            int i = audioformat.getSampleSizeInBits();
            if(i <= 0)
                i = 16;
            if(audioformat.getEncoding() == javax.sound.sampled.AudioFormat.Encoding.ULAW || audioformat.getEncoding() == javax.sound.sampled.AudioFormat.Encoding.ALAW)
                i = 16;
            if(i != 8)
                i = 16;
            AudioFormat audioformat1 = new AudioFormat(javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED, audioformat.getSampleRate(), i, audioformat.getChannels(), audioformat.getChannels() * (i / 8), audioformat.getSampleRate(), false);
            m_encodedaudioInputStream = m_audioInputStream;
            try
            {
                encodedLength = m_encodedaudioInputStream.available();
            }
            catch(IOException ioexception)
            {
                ioexception.printStackTrace();
            }
            m_audioInputStream = AudioSystem.getAudioInputStream(audioformat1, m_audioInputStream);
            AudioFormat audioformat2 = m_audioInputStream.getFormat();
            javax.sound.sampled.DataLine.Info info = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.SourceDataLine.class, audioformat2, -1);
            
//            javax.sound.sampled.Mixer.Info ainfo[] = AudioSystem.getMixerInfo();
//            for(int k=0;k<ainfo.length;k++)
//            {
//                System.out.println(ainfo[k]);
//                try{
//                    Mixer  mixer = AudioSystem.getMixer(ainfo[k]);
//                    m_line = (SourceDataLine)mixer.getLine(info);
//                    if (m_line!=null)
//                        return m_line;
//                }catch(Exception ex)
//                {
//                    ex.printStackTrace();
//                }
//            }
            
                m_line = (SourceDataLine)AudioSystem.getLine(info);
            
        }
        return m_line;
    }

    

    public static File getTempDir() {
        String tmpFilePath = System.getProperty("java.io.tmpdir");
        File tmp = new File(tmpFilePath);
        File teePath = new File(tmp.getAbsolutePath() + File.separator + "teedict");
        teePath = new File(teePath.getAbsolutePath() + File.separator + String.valueOf(java.lang.Math.random()) + ".tmp");
        teePath.deleteOnExit();
        teePath.mkdirs();
        return teePath;
    }
}