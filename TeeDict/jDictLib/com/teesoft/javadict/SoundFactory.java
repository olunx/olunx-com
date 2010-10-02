/*
 * SoundFactory.java
 * 
 * Created on 2007-9-22, 11:55:58
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

import com.teesoft.javadict.stardict.sound.StardictSound;
import com.teesoft.jfile.FileAccessBase;
import java.io.InputStream;
import java.util.Vector;

/**
 *
 * @author wind
 */
public abstract class SoundFactory {
    static Vector repositories = new Vector();
    
    static{
        try{
             Class.forName("com.teesoft.javadict.stardict.sound.StardictSound");
        }catch(Exception e)
        {            
        }

        try{
            SoundRepository rep=(SoundRepository) Class.forName("com.teesoft.javadict.wyabdc.WyabdcTTSSound").newInstance();
            if (rep != null)
                addSoundRepository(rep);
        }catch(Exception e)
        {            
        }
        try{
            SoundRepository rep=(SoundRepository) Class.forName("com.teesoft.javadict.zdicspeech.zdicSpeech").newInstance();
            if (rep != null)
                addSoundRepository(rep);
        }catch(Exception e)
        {            
        }
                  
    }
    
    public static FileAccessBase getSound(String word)
    {
        for(int i=0;i<repositories.size();++i)
        {
            try{
                SoundRepository repository = (SoundRepository) repositories.elementAt(i);
                FileAccessBase stream = repository.getSound(word);
                if (stream != null)
                    return stream;
            }catch(Exception ex)
            {
            }
        }
        return null;
    }
    public static boolean hasWord(String word)
    {
        for(int i=0;i<repositories.size();++i)
        {
            try{
                SoundRepository repository = (SoundRepository) repositories.elementAt(i);
                boolean has = repository.hasWord(word);
                if (has)
                    return true;
            }catch(Exception ex)
            {
            }
        }
        return false;
    }

    public static void addSoundRepository(SoundRepository newInstance) {
        repositories.addElement(newInstance);
    }

    public static Vector getRepositories()
    {
        return repositories;
    }

}
