/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.teesoft.javadict.stardict.sound;

import com.teesoft.javadict.ByteArrayString;
import com.teesoft.javadict.DictFactory;
import com.teesoft.javadict.ItemList;
import com.teesoft.javadict.SoundFactory;
import com.teesoft.javadict.SoundRepository;
import com.teesoft.javadict.stardict.starBucketItem;
import com.teesoft.javadict.stardict.starDict;
import com.teesoft.javadict.stardict.startDictFactory;
import com.teesoft.jfile.FileAccessBase;
import com.teesoft.jfile.FileFactory;
import com.teesoft.jfile.WrapperFile;
import java.io.IOException;
import java.util.Vector;

/**
 *
 * @author wind
 */
public class StardictSound extends SoundRepository{
    
    static{
            try {
            Vector roots = FileFactory.listRoots();
            startDictFactory factory =startDictFactory.getInstance();
            for (int i = 0; i < roots.size(); i++) {
                FileAccessBase file = (FileAccessBase) roots.elementAt(i);
                FileAccessBase sound = file.child("dict/sound/");
                if (sound !=null && sound.exists() && sound.isDirectory())
                {
                    Vector files = sound.listFiles();
                    if (files!=null)
                    for(int k=0;k<files.size();k++)
                    {
                        FileAccessBase dir = (FileAccessBase) files.elementAt(k);
                        if (dir.exists() && dir.isDirectory() && dir.getName().startsWith("stardict"))
                        {
                            starDict stardict = (starDict) factory.AcceptDict(dir, null);
                            if (stardict!=null)
                            {
                                try{
                                    stardict.open();
                                    SoundFactory.addSoundRepository(new StardictSound(stardict));
                                }catch(Exception ex){
                                    
                                }
                            }
                        }                            
                    }
                }
            }
            } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    starDict dict;

    private StardictSound(starDict stardict)
    {
        dict = stardict;
    }
    public String getFormat()
    {
        return "stardict";
    }

    public FileAccessBase getSound(String word)
    {
        word =word.toLowerCase();
        ItemList  item = dict.search(word, 2);
        if (item==null || item.size()==0)
            return null;
        for(int i=0;i<item.size();i++)
        {
            {
                WrapperFile soundFle = null;
                try
                {
                    starBucketItem dictittem = (starBucketItem) item.getItem(i);
                        
                    if (word.equals(dictittem.getString()) ||  ByteArrayString.startsWith(dictittem.getBytes(), (word + ".").getBytes("utf-8")))
                    {
                        soundFle = new WrapperFile(dict.getDictStream(), dictittem.getString(), dictittem.getStart(), dictittem.getLength());
                        return soundFle;
                    }
                } catch (IOException ex)
                {
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
