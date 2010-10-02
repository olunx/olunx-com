/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.teesoft.javadict;

import com.teesoft.javadict.stardict.starDict;
import com.teesoft.javadict.stardict.starIndex;
import com.teesoft.javadict.stardict.startDictFactory;
import com.teesoft.jfile.FileAccessBase;
import com.teesoft.jfile.FileFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wind
 */
public class exportSound {
    public static void exportAllSound(final File target,final String targetFormat)
    {
        if (target.exists() && target.isFile())
        {
            return;
        }

        Thread thread = new Thread(new Runnable() {

            public void run()
            {
                LoadingFrame.showLoading();
                exportSound.doExportAllSound(target, targetFormat);
                LoadingFrame.hideLoading();                
            }
        },"ExportSOund");
        thread.start();
    }
    public static boolean doExportAllSound(File target,String targetFormat)
    {
        Vector reps = SoundFactory.getRepositories();
        if (reps==null)
            return false;
        Vector <FileAccessBase> sounds = new Vector <FileAccessBase>();
        for(int i=0;i<reps.size();i++)
        {
            SoundRepository rep = (SoundRepository) reps.get(i);
            if (rep!=null)
            {
                Vector s = rep.allSounds();
                if (s!=null)
                {
                    for(Object f:s)
                        sounds.add((FileAccessBase)f);
                }
            }
        }
        return exportAllSound(sounds, target, targetFormat);
    }
    public static boolean exportAllSound(Vector sounds,File target,String targetFormat)
    {
            OutputStream dictStream = null;
            OutputStream indexStream = null;
            OutputStream ifoStream = null;
            boolean success = false;
            Vector<Sound> source = new Vector<Sound>();
            for (Object s : sounds)
            {
                Sound sound = new Sound((FileAccessBase) s);
                if (sound.isValid())
                {
                    source.add(sound);
                }
            }
            Collections.sort(source, new Comparator<Sound>()
            {

                public int compare(Sound s1, Sound s2)
                {
                    return s1.name.compareTo(s2.name);
                }
            });
            String name = target.getName();
            String dictname = "stardict-" + name + "-3.0.0";
            target = new File(target.getParentFile().getAbsolutePath() + File.separator + dictname);
            target.delete();
            new File(target.getAbsolutePath() + File.separator + name + ".idx.idx").delete();
            target.mkdirs();
            try
            {
                File dict = new File(target.getAbsolutePath() + File.separator + name + ".dict");
                File indexFile = new File(target.getAbsolutePath() + File.separator + name + ".idx");
                dictStream = new FileOutputStream(dict);
                indexStream = new FileOutputStream(indexFile);
                int index = 0;
                int indexSize = 0;
                for (int i = 0; i < source.size(); i++)
                {
                    Sound s = source.get(i);
                    //System.out.println(s);
                    byte[] nameByte = null;
                    try
                    {
                        nameByte = (s.fullname + "\0").getBytes("utf-8");
                    } catch (UnsupportedEncodingException ex)
                    {
                        nameByte = (s.fullname + "\0").getBytes();
                    }
                    dictStream.write(nameByte);
                    byte []b = new byte[(int) s.file.fileSize()];
                    int len = s.file.read(b);
                    s.file.close();
                    dictStream.write(b, 0, len);
                    b = null;
                    indexStream.write(nameByte);
                    indexSize += nameByte.length + 8;
                    index += nameByte.length;
                    byte[] aInt = new byte[4];
                    starIndex.putIntintoByte(index, 0, aInt);
                    indexStream.write(aInt);
                    starIndex.putIntintoByte(len, 0, aInt);
                    indexStream.write(aInt);
                    index += len;
                }
                File infoFile = new File(target.getAbsolutePath() + File.separator + name + ".ifo");
                ifoStream = new FileOutputStream(infoFile);
                String info = "jstardict tts voice dict ifo file\n" + "version=3.0.0\n" + "wordcount=" + source.size() + "\n" + "idxfilesize=" + indexSize + "\n" + "bookname=jstardict tts voice dict " + name + "\n" + "date=" + getDate() + "\n" + "description=jstardict tts voice dict created by jStarDict\n" + "sametypesequence=x\n";
                byte[] b = null;
                try
                {
                    b = info.getBytes("utf-8");
                } catch (UnsupportedEncodingException ex)
                {
                    b = info.getBytes();
                }
                ifoStream.write(b);
            } catch (IOException ex)
            {
                Logger.getLogger(exportSound.class.getName()).log(Level.SEVERE, null, ex);
            } finally
            {
                try
                {
                    if (dictStream != null)
                    {
                        dictStream.close();
                    }
                } catch (IOException ex)
                {
                }
                try
                {
                    if (indexStream != null)
                    {
                        indexStream.close();
                    }
                } catch (IOException ex)
                {
                }
                try
                {
                    if (ifoStream != null)
                    {
                        ifoStream.close();
                    }
                } catch (IOException ex)
                {
                }
            }
        FileAccessBase dictFolder = null;
        try
        {
            dictFolder = FileFactory.openFileAccess(target.getAbsolutePath(), true);
            starDict stardict = (starDict) startDictFactory.getInstance().AcceptDict(dictFolder, null);
            if (stardict != null)
            {
                stardict.open();
                stardict.close();
            }

            try{
                java.awt.Desktop.getDesktop().open(new File(target.getAbsolutePath()));
            }catch (Throwable ex) {
            
            }  
        } catch (IOException ex)
        {
        } finally
        {
            try
            {
                if (dictFolder!=null)
                dictFolder.close();
            } catch (IOException ex)
            {
            }
        }
        return success;
    }

    private static String getDate()
    {
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
        return df.format(new Date());
    }

    public static class Sound
    {

        public String name;
        public String ext = "";
        public FileAccessBase file;
        private String fullname;

        Sound(FileAccessBase file)
        {
            try
            {
                this.file = file;
                fullname = file.getName().toLowerCase();
                name = fullname;
                if (name.lastIndexOf(".") != -1)
                {
                    ext = name.substring(name.lastIndexOf(".") + 1);
                    name = name.substring(0, name.lastIndexOf("."));
                }
            } catch (IOException ex)
            {
                Logger.getLogger(exportSound.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        private boolean isValid()
        {
            return ext.length() > 0;
        }

        @Override
        public String toString()
        {
            try
            {
                return name + " @" + file.getAbsolutePath();
            } catch (IOException ex)
            {
                return name;
            }
        }
    }
}
