/*
 * zdicSpeech.java
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

package com.teesoft.javadict.zdicspeech;

import com.teesoft.javadict.SoundRepository;
import com.teesoft.jfile.CharsetEncodingFactory;
import com.teesoft.jfile.FileAccessBase;
import com.teesoft.jfile.FileFactory;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 *
 * @author wind
 */
public class zdicSpeech extends SoundRepository{
    Vector  baseDirs = new Vector();
    public static String dir="dict/sound/ZDicSpeech.pdb";
    public static String dir2="dict/sound/ZDicSpeech2.pdb";
    private FileAccessBase lastSound=null;
    public zdicSpeech()
    {
        try {
            Vector roots = FileFactory.listRoots();
            for (int i = 0; i < roots.size(); i++) {
                FileAccessBase sound = null;
                try {
                    FileAccessBase file = (FileAccessBase) roots.elementAt(i);

                    sound = file.child(dir);
                    if (sound!=null && sound.exists())
                        baseDirs.addElement( new zdicSpeechPDB (sound ) );
                    else
                    {
                        sound = file.child(dir2);
                        if (sound!=null && sound.exists())
                            baseDirs.addElement(new zdicSpeechPDB (sound));
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
        return "zdic";
    }

    public FileAccessBase getSound(String word) {
            word = word.toLowerCase();

        try {
            if (lastSound != null && lastSound.getName().toLowerCase().startsWith(word + ".")) {
                lastSound.absolute(0);
                return lastSound;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
            byte[] b;
            try {
                b = CharsetEncodingFactory.getBytes(word, "utf-8");
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
                b = word.getBytes();
            }

            for (int i = 0; i < baseDirs.size(); ++i) {
                FileAccessBase sound = null;
                try {
                    zdicSpeechPDB pdb = (zdicSpeechPDB) baseDirs.elementAt(i);
                    sound = pdb.getSound(b);
                    
                    if (sound != null) {
                        if (lastSound!=null)
                            lastSound.close();
                        lastSound = sound;
                        return sound;
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    try {
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
        return null;
    }

}
