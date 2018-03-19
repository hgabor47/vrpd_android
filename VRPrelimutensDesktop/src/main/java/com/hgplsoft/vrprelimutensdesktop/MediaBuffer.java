package com.hgplsoft.vrprelimutensdesktop;

import android.graphics.Bitmap;

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by horvath3ga on 2017.10.06..
 */


public class MediaBuffer
{
    //public String hash; //a bufferben levő adat HASH-je Android alat nemm kell használni mert a server mondja meg melyik képet kell kirakni
    public byte[] buffer; //public ByteArrayInputStream buffer; //InputStream
    public Date created;
    public Bitmap bmp;

    public MediaBuffer(ByteArrayInputStream mem)
    {
        readbytes(mem);//TODO 3 buffer=mem;
        //hash = Hash;
        created = Calendar.getInstance().getTime();
    }
    public MediaBuffer(byte[] mem)
    {
        buffer = mem;
        created = Calendar.getInstance().getTime();
    }

    public void Modify(MediaBuffer buf, ByteArrayInputStream mem)
    {
        try {
            buf.buffer = new byte[mem.available()];
            mem.read(buf.buffer);
            created = Calendar.getInstance().getTime();
        }catch (Exception e){}
    }

    public void readbytes(ByteArrayInputStream bais) {
        try {
            buffer = new byte[bais.available()];
            bais.read(buffer);
        }catch (Exception e){}
    }
}