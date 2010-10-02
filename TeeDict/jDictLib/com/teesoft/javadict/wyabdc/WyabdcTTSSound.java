/*
 * WyabdcTTSSound.java
 * 
 * Created on 2007-9-22, 12:09:26
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

package com.teesoft.javadict.wyabdc;

import com.teesoft.javadict.SoundRepository;
import com.teesoft.jfile.FileAccessBase;
import com.teesoft.jfile.FileFactory;
import com.teesoft.util.StringUtil;
import java.io.IOException;
import java.util.Vector;

/**
 *
 * @author wind
 */
public class WyabdcTTSSound extends SoundRepository{
    Vector  baseDirs = new Vector();
    public static String [] dirs={"dict/sound/WyabdcRealPeopleTTS/WyabdcRealPeopleTTS"
            ,"dict/sound/WyabdcRealPeopleTTS"
            ,"dict/sound/EnglishSounds.ti!"
            ,"dict/sound/EnglishSounds.ti"
            ,"dict/sound/EnglishSounds"
            ,"dict/sound/Sounds"};
    public static String [] exts = {".wav",".aac", ".mp3",".amr"};
    public WyabdcTTSSound()
    {
        try {
            Vector roots = FileFactory.listRoots();
            for (int i = 0; i < roots.size(); i++) {
                FileAccessBase sound = null;
                try {
                    FileAccessBase file = (FileAccessBase) roots.elementAt(i);

                    for(int f = 0;f<dirs.length;++f)
                    {
                        sound = file.child(dirs[f]);
                        if (sound!=null && sound.exists())
                        {
                            baseDirs.addElement(sound);
                            break;
                        }
                        
                    }                   
                    
                } catch (IOException ex) {
                    ex.printStackTrace();
                }finally {
                try {
                    if (sound!=null)
                        sound.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }

    public String getFormat() {
        return "wyabdc";
    }

    public FileAccessBase getSound(String word) {
        word =word.toLowerCase();
        String words[] = {word};
        if (word.indexOf(" ")!=-1)
        {
            words = new String[]{word,StringUtil.replace(word, " ", "_")};
        }
        for(int i=0;i<baseDirs.size();++i)
        {
            FileAccessBase sound = null;
            try {
                FileAccessBase baseDir = (FileAccessBase) baseDirs.elementAt(i);
                for(int k=0;k<exts.length;++k)
                {
                    for(int w = 0;w<words.length;w++)
                    {
                        sound = baseDir.child(words[w].charAt(0) + baseDir.getSeparator() + words[w] + exts[k]);
                        if (sound.exists()) {
                            return sound;
                        }
                        sound.close();
                        sound = baseDir.child( words[w] + exts[k]);
                        if (sound.exists()) {
                            return sound;
                        }
                        sound.close();                        
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (sound!=null)
                        sound.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            
        }
        return null;
    }

    public Vector allSounds()
    {
        FileAccessBase [] files = null;
        Vector v = new Vector();
        
        for(int i=0;i<baseDirs.size();i++)
        {
            enumerate((FileAccessBase)baseDirs.elementAt(i),v);
        }
        return v;
    }

    private void enumerate(FileAccessBase file, Vector v)
    {
        try
        {
            Vector files = file.listFiles();
            for (int i = 0; i < files.size(); i++)
            {
                FileAccessBase f = (FileAccessBase) files.elementAt(i);
                if (f.isFile())
                    v.addElement(f);
                else
                    enumerate(f,v);
            }
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

}
