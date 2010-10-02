/*
 * SoundRepository.java
 * 
 * Created on 2007-9-22, 12:00:54
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

import com.teesoft.jfile.FileAccessBase;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 *
 * @author wind
 */
public abstract class SoundRepository {
    public abstract String getFormat();

    public abstract FileAccessBase  getSound(String word) ;
    boolean hasWord(String word)
    {
        InputStream stream = getSound(word);
        if (stream != null)
        {
            try {
                stream.close();
                return true;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }
    //traversal all sound
    public abstract Vector allSounds();
}
