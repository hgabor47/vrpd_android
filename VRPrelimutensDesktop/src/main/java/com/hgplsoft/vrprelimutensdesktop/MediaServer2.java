package com.hgplsoft.vrprelimutensdesktop;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

/**
 * Created by horvath3ga on 2017.10.06..
 */

class MediaServer2 {
    public static int SIBUFFER_SIZE = 100;
    ArrayList<MediaBuffer> SIBuffer;

    public MediaServer2(){

        SIBuffer = new ArrayList<MediaBuffer>(SIBUFFER_SIZE);
        for(int i=0;i<SIBUFFER_SIZE;i++) {
            SIBuffer.add(null);
        }
    }

    public MediaBuffer get(byte sii) {
        byte cnt = (byte) SIBuffer.size();
        if (cnt>sii)
        {
            MediaBuffer buf = null;
            buf = SIBuffer.get(sii);
            return buf; // ha null akkor nem talalta meg.. ez gaz
        }
        return null; // ha null akkor nem talalta meg.. ez gaz
    }

    public MediaBuffer store(byte index, ByteArrayInputStream mem) {
        byte cnt = (byte) SIBuffer.size();
        /*if (index>=cnt) {
            SIBuffer.ensureCapacity(index);
        }*/
        MediaBuffer buf = null;
        buf = new MediaBuffer(mem);
        SIBuffer.set(index,buf);
        //test();
        return buf; // ha null akkor fatal error mert tullepte a keretret
    }
    public MediaBuffer store(byte index, byte[] mem) {
        byte cnt = (byte) SIBuffer.size();
        /*if (index>=cnt) {
            SIBuffer.ensureCapacity(index);
        }*/
        MediaBuffer buf = null;
        buf = new MediaBuffer(mem);
        SIBuffer.set(index,buf);
        //test();
        return buf; // ha null akkor fatal error mert tullepte a keretret
    }

    /*
    public MediaBuffer store(AppInfo app) {
        return store(app.SII,app.mem);
    }
    */

}
