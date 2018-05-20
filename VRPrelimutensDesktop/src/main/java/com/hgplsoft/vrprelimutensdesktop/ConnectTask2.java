package com.hgplsoft.vrprelimutensdesktop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;

import com.google.vr.sdk.base.sensors.internal.Vector3d;
import com.hgplsoft.babylonms.BMSEventSessionParameter;
import com.hgplsoft.babylonms.BMSField;
import com.hgplsoft.babylonms.BMSPack;
import com.hgplsoft.babylonms.BabylonMS;
import com.hgplsoft.babylonms.Util;

import org.rajawali3d.Object3D;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static com.google.vr.cardboard.ThreadUtils.runOnUiThread;
import static com.hgplsoft.babylonms.BabylonMS.CONST_FT_INT64;
import static com.hgplsoft.babylonms.BabylonMS.CONST_FT_INT8;
import static com.hgplsoft.babylonms.BabylonMS.CONST_FT_UUID;
import static com.hgplsoft.vrprelimutensdesktop.MyRenderer.APPID;


/**
 * Created by horvath3ga on 2017.10.06..
 */

public class ConnectTask2 {

    static final String ID = "START";
    static String UUIDTestServer = "cf70c42b-93b7-49a9-b8ab-c5afb8d7dd4d";
    static String UUIDAndroid = "cf70c42b-93b7-49a9-b8ab-c5afb8d7dd4d";
    static BabylonMS bms;
    //static String IP = "172.24.21.203";
    //static String IP = "192.168.42.100";
    //static String IP = "192.168.42.100";
    static int PORT = 9000;
    static int once = 0;
    static double mouseDistanceFromWindow = 0.1f;

    MyRenderer Mainview;
    TcpClient mTcpClient;

    int size = -1;
    int sizeOriginal = -1;
    ByteArrayOutputStream str;
    //ArrayList<AppInfo> unzippedFiles;


    public MediaServer2 mediaserver;
    BMSEventSessionParameter androidsession;
    Semaphore senderlock = new Semaphore(1);

    int cnt = 0;
    boolean firstPackArrived = false;

    public ConnectTask2(MyRenderer renderer) {
        Mainview = renderer;
        mediaserver = new MediaServer2();

        bms = BabylonMS.LaunchMiniShip(Mainview.ip, PORT, null, UUIDAndroid, UUIDTestServer, renderer.getContext());
        bms.setNewInputFrameEventHandler(new BabylonMS.BMSEventHandler() {
            @Override
            public void Event(BMSEventSessionParameter session) {

                try {
                    byte cmd = (byte) session.inputPack.GetField(0).getValue((byte) 0);
                    BMSField pckcnt = session.inputPack.GetFieldByName("PCKCNT");
                    if (pckcnt != null) {
                        byte acnt = (byte) pckcnt.getValue((byte) 0);
                        if (acnt != cnt + 1) {
                            Log.e(ID, "PACKET NUMBER ERROR: last:" + String.valueOf(acnt) + " act:" + String.valueOf(cnt));
                        }
                        cnt = acnt;
                    }

                    switch (cmd) {
                        case VRCEShared.CONST_ANDROIDCOMMAND_RETRIEVE_ALL:
                            firstPackArrived = true;
                            processing_all(session.inputPack);
                            break;
                        case VRCEShared.CONST_ANDROIDCOMMAND_CHANGE_HWND:
                            //if (firstPackArrived )
                        {
                            processing_change(session.inputPack); // a kapcsolat utön azonnal jönnek a frissítések de az első FULL pack után szabad engedni csak.
                        }
                        break;
                        case VRCEShared.CONST_ANDROIDCOMMAND_IC_EVENT:
                            //Log.e(ID,"MOUSE Event");
                            processing_controll(session.inputPack);
                            break;
                        case VRCEShared.CONST_ANDROIDCOMMAND_LOST_WINDOW:
                            processing_delete(session.inputPack);
                            Log.e(ID, "Close window");
                    }
                } catch (Exception e2) {
                    Log.d(APPID, e2.getMessage());
                }
                ;
            }
        });
        bms.setServerReadyEventHandler(new BabylonMS.BMSEventHandler() {
            @Override
            public void Event(BMSEventSessionParameter session) {
                androidsession = session;
                try { //TODO The sleep not a final solution !! write UUID the handler after need an empty line maybe
                    Thread.sleep(3800);

                    BMSPack pack = new BMSPack();
                    pack.AddField("CMD", CONST_FT_INT8).Value(VRCEShared.CONST_ANDROIDCOMMAND_RETRIEVE_ALL);
                    TransferpacketWithlock(pack);

                    Log.e(ID, "ServerReady");
                } catch (Exception e) {
                }

            }
        });
        bms.setServerConnectedEventHandler(new BabylonMS.EventHandler() {
            @Override
            public void Event() {
                Log.e(ID, "Connected1");

            }
        });
        bms.PrepareGate();//no blocking
    }

    void TransferpacketWithlock(BMSPack pack) {
        try {
            senderlock.acquire();
            androidsession.outputPack = pack;
            androidsession.TransferPacket(true);
            senderlock.release();
        } catch (Exception e) {
        }
    }

    double startYaw = -1;
    double startXRollObj = -1;
    double startYRollObj = -1;
    double startRoll = -1;
    boolean startvirtualmode = true;
    Boolean virtualmouse = false;
    PDWindow selectedobj;  //Virtual után LEFT
    final int MODEVIRTLEFT=1;
    final int MODEVIRTRIGHT=2;
    final int MODEVIRTMIDDLE=3;
    final int MODEVIRTNONE=0;
    int virtualmouse_type = MODEVIRTNONE; //default = 0, Left mouse =1 , right = 2

    Integer x, y;
    Integer dxx=0,dyy=0; // LEFT MB can shift the original X,Y

    private void processing_controll(BMSPack pack) {


        BMSField fbutton = pack.GetFieldByName("BUTTON");
        BMSField fx = pack.GetFieldByName("X");
        BMSField fy = pack.GetFieldByName("Y");
        BMSField l = pack.GetFieldByName("LEFT");
        BMSField t = pack.GetFieldByName("TOP");
        int left = 0;
        int top = 0;
        if (l!=null){
            left = (int) l.getValue((byte)0);
        }
        if (l!=null){
            top = (int) t.getValue((byte)0);
        }


        int arraycnt = fbutton.Length();

        for (int i = 0; i < arraycnt; i++) {
            int button = (int) fbutton.getValue((byte) i);
            int x = (int) fx.getValue((byte) i) ;
            int y = (int) fy.getValue((byte) i);
            x-=left;
            y-=top;

            boolean state = (button & VRCEShared.CONST_MOUSEBUTTON_DOWN) != 0;
            virtualmouse = (button & VRCEShared.CONST_MOUSEBUTTON_VIRTUAL) != 0;

            if (virtualmouse) { //xbutton1
                if (startvirtualmode) {
                    startRoll = dxx-((Math.toDegrees(Mainview.origo.getRotY())) * 50);
                    virtualmouse_type=MODEVIRTNONE;
                    Log.e(APPID, "VirtualMouse:(X,roll)" + String.valueOf(x) + " - " + String.valueOf(Mainview.origo.getRotY()));
                    Log.e(APPID, "MouButt:"+String.valueOf(button));
                } else {
//ROLLL SCENE
                    if (virtualmouse_type == MODEVIRTNONE) {
                        double rollpos = (x - startRoll) / 50;
                        Mainview.origo.setRotation(0, rollpos, 0);
                        Log.e(APPID, "VirtualMouse:(X,roll)" + String.valueOf(x) + " - " + String.valueOf(rollpos));
                    }
                }
                startvirtualmode = false;

                if (((button & VRCEShared.CONST_MOUSEBUTTON_LEFT )!=0)
                    &&
                   (virtualmouse_type != MODEVIRTLEFT))
                    {
                        selectedobj = Mainview.lookedobj;
                        //startXRollObj = -(Math.toDegrees(selectedobj.dummyz.getRotY()) );  OK
                        startXRollObj = (x / 20)-(Math.toDegrees(selectedobj.dummyy.getRotY()) );  //Az aktuális elfordulás szöge * 50
                        startYRollObj = (y / 20)-(Math.toDegrees(selectedobj.dummyx.getRotX()) );  //orientationZ
                        virtualmouse_type = MODEVIRTLEFT;
                        Log.e(APPID, "MOU VIRT+LEFT"+selectedobj.getRotY());
                    }
                 else {
                    if ((virtualmouse_type == MODEVIRTLEFT)){
                        if (state==false) {
                            Log.e(APPID, "MOU VIRT+LEFT UP"+selectedobj.getRotY());
                        }
                        if (state==true) {  //click again LEFT
                            Log.e(APPID, "MOU VIRT+LEFT DOWN"+selectedobj.getRotY());
                            startvirtualmode = true;
                            dxx = x;
                            dyy = y;
                        } else {
                            if (selectedobj != null) {
                                double dX = (x / 20 - startXRollObj);
                                double dY = (y / 20 - startYRollObj);
                                Log.e(APPID, "MOU VIRT+LEFT:(Y,roll)" + String.valueOf(y / 20) + " - " + String.valueOf(dY) + " || " + (startYRollObj));
                                //selectedobj.dummyz.setRotY(dX); OK
                                selectedobj.dummyy.setRotY(dX); //OK
                                selectedobj.dummyx.setRotZ(dY);
                                //selectedobj.RotY(dX);
                                //selectedobj.RotZ(dY);
                            }
                        }
                    }

                }

                if ((button & VRCEShared.CONST_MOUSEBUTTON_RIGHT )!=0) {
                    if (virtualmouse_type != MODEVIRTRIGHT)
                    {
                        selectedobj = Mainview.lookedobj;
                        startXRollObj = -(Math.toDegrees(selectedobj.getRotY()) * 50);
                        startYRollObj = -(Math.toDegrees(selectedobj.getRotZ()) * 50);
                        virtualmouse_type = MODEVIRTRIGHT;
                        Log.e(APPID, "MOU VIRT+RIGHT");
                    }
                }
                if ((button & VRCEShared.CONST_MOUSEBUTTON_MIDDLE )!=0) {
                    if (virtualmouse_type != MODEVIRTMIDDLE)
                    {
                        arrange3DObjects();
                        virtualmouse_type = MODEVIRTMIDDLE;
                        Log.e(APPID, "MOU VIRT+MIDDLE");
                    }
                }


                if (virtualmouse_type == MODEVIRTRIGHT){

                }

            } else { //
                startvirtualmode = true;
                //startRoll = Mainview.origo.getRotY();
                if ((button & VRCEShared.CONST_MOUSEBUTTON_XBUTTON2) != 0) { //select object
                    //if (button.compareTo(XButton2) == 0) { //select object
                    Mainview.selectedobj = Mainview.lookedobj;
                    CMD_FOCUS(Mainview.selectedobj.Code);
                    //Mainview.mouse.mouseTcpClient.sendMessage("<?xml version=\"1.0\" encoding=\"utf-8\"?><head APPNUM=\"0\"><bringtofront handle=\"" + Mainview.selectedobj.Code.toString() + "\"/>  </head>");
                } else {
                    if ((button & VRCEShared.CONST_MOUSEBUTTON_MASK) == 0) { //NONEBUTTON //kurzor mozgatas
                        ///*oldschool

                        if (Mainview.selectedobj != null) {
                            try {
                                Vector3d v = calculateXMouseCenter(Mainview.selectedobj);
                                Mainview.arrow.RotY(v.x);
                                Mainview.arrow.RotZ(v.y);
                                v = calculateXMouseShift(Mainview.selectedobj, x, y);
                                Mainview.arrow.setX(v.x);
                                Mainview.arrow.setY(-v.y);
                                Mainview.arrow.setZ(v.z + mouseDistanceFromWindow);
                            } catch (Exception e4) {
                            }
                        }



                    }
                }
            }
        }

    }

    // az eredmény felhasználásával a NYILAT az objektum közepére lehet irányítani (ROT...) X,Y használható
    // Z érték az objektum távolsága, mely tolajdonképpen mindig egyforma.
    public static Vector3d calculateXMouseCenter(PDWindow obj) {
        //Vector3d v = new Vector3d(Math.toDegrees(obj.getRotY()+(obj.pixangleX*(obj.bmpwidth/2))-(obj.pixangleX*(obj.bmpwidth/2))),Math.toDegrees(obj.getRotX()+(obj.pixangleY*(obj.bmpheight/2))-(obj.pixangleY*(obj.bmpheight/2))),obj.getZ());
        Vector3d v = new Vector3d(
                /*Math.toDegrees*/(obj.getRotY()/*+(obj.pixangleX*(obj.bmpwidth/2))-(obj.pixangleX*(obj.bmpwidth/2))*/),
                /*Math.toDegrees*/(obj.getRotZ()/*+(obj.pixangleY*(obj.bmpheight/2))-(obj.pixangleY*(obj.bmpheight/2))*/),
                obj.getX());
        return v;
    }

    // Az objektum közepére helyezett nyilat lehet eltolni a síkban X,Y pixel pontra az eredménnyel
    public static Vector3d calculateXMouseShift(PDWindow obj, int x, int y) {
        double x0 = obj.getScaleX() / obj.bmpwidth;
        double y0 = obj.getScaleY() / obj.bmpheight;
        Vector3d v = new Vector3d((x0 * x) - (obj.getScaleX() / 2), (y0 * y) - (obj.getScaleY() / 2), obj.getZ());
        return v;
    }


    private void processing_delete(BMSPack pack) {
        try {
            final long hwnd = pack.GetFieldByName("HWND").getValue((byte) 0);
            final String group = pack.GetFieldByName("GROUP").getUUIDValue((byte) 0);
            String code = getCodeFromHwndGroup(hwnd, group);
            Mainview.deleteBuffered3DObject(code);
        } catch (Exception e) {
            Log.d(APPID, "Delete problem!");
        }
    }


    /**
     * All window retrieved
     *
     * @param pack
     */
    private void processing_all(BMSPack pack) {
        int c = pack.GetFieldIndexByName("IDX");  //C is the first pack in pack pointer
        for (int i = c; i < pack.FieldsCount(); i += 4) //becaue IDX,HWND, GROUP,IMAGE = 4
        {
            byte idx = (byte) pack.GetField(i).getValue((byte) 0);
            final long hwnd = pack.GetField(i + 1).getValue((byte) 0);
            final String group = pack.GetField(i + 2).getUUIDValue((byte) 0);
            byte[] image = Util.unzipper_unzip(pack.GetField(i + 3).getValue());
            final MediaBuffer buf = mediaserver.store(idx, image);

            //MediaBuffer buf = mediaserver.search(idx); //bufferbe letárolási hely alapján keresem
            if (buf != null) {
                PDWindow obj = get3DObject(getCodeFromHwndGroup(hwnd, group));
                if (obj != null) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(buf.buffer, 0, buf.buffer.length);  //todo 3.decodeStream(buf.buffer);
                    setTextureAndAddObject(obj, bmp);
                    arrange3DObjects();
                }
            }

        }
    }

    private void processing_change(BMSPack pack) {
        byte idx;
        final long hwnd;
        final String group;
        byte[] image = null;

        BMSField fi = pack.GetFieldByName("IDX");
        if (fi == null) {
            return;
        }
        idx = (byte) fi.getValue((byte) 0);

        fi = pack.GetFieldByName("HWND");
        if (fi == null) {
            return;
        }
        hwnd = fi.getValue((byte) 0);

        fi = pack.GetFieldByName("GROUP");
        if (fi == null) {
            return;
        }
        group = fi.getUUIDValue((byte) 0);

        fi = pack.GetFieldByName("IMAGE");
        if (fi == null) {
            image = null;
        } else {
            image = Util.unzipper_unzip(fi.getValue());
        }

        final MediaBuffer buf = mediaserver.get(idx);
        if ((buf == null) && (image == null)) {
            //TODO le kell kérni mert nincs új adat és  nincs régi sem
            // RETRIEVE HWND
            return;
        }

        if (image != null) {
            final MediaBuffer buf2 = mediaserver.store(idx, image); //felülírás az új adattal
            PDWindow obj = get3DObject(getCodeFromHwndGroup(hwnd, group));
            if (obj != null) {
                Bitmap bmp;
                bmp = decodeByteArray(buf2);
                //bmp = BitmapFactory.decodeByteArray(buf2.buffer, 0, buf2.buffer.length);  //todo 3.decodeStream(buf.buffer);
                //Bitmap bmp = BitmapFactory.decodeResource(MyRenderer.res, R.raw.arrow);
                if (bmp != null) {
                    setTextureAndAddObject(obj, bmp);
                    //MyRenderer.win.changeTexture(bmp);
                    //180226 arrange3DObjects();
                } else {
                    Log.d(APPID, "ERROR DECODE ARRAY IMAGE"); //TODO
                }
            }
        } else {
            Log.d(APPID, "No Image"); //TODO
            PDWindow obj = get3DObject(getCodeFromHwndGroup(hwnd, group));
            if (obj != null) {
                Bitmap bmp = BitmapFactory.decodeByteArray(buf.buffer, 0, buf.buffer.length);  //todo 3.decodeStream(buf.buffer);
                setTextureAndAddObject(obj, bmp);
                //180226  arrange3DObjects();
            }
        }

    }

    public static Bitmap decodeByteArray(MediaBuffer buf) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        for (options.inSampleSize = 1; options.inSampleSize <= 32; options.inSampleSize++) {
            try {
                bitmap = BitmapFactory.decodeByteArray(buf.buffer, 0, buf.buffer.length, options);  //todo 3.decodeStream(buf.buffer);
                break;
            } catch (OutOfMemoryError outOfMemoryError) {
                // If an OutOfMemoryError occurred, we continue with for loop and next inSampleSize value
                Log.e(APPID, "outOfMemoryError while reading file for sampleSize " + options.inSampleSize
                        + " retrying with higher value");
            }
        }
        return bitmap;
    }

    public void CMD_RETRIEVE(BabylonMS bms, byte index) {
        BMSPack pack = new BMSPack();
        pack.AddField("CMD", CONST_FT_INT8).Value(VRCEShared.CONST_ANDROIDCOMMAND_RETRIEVE_HWND);
        pack.AddField("INDEX", CONST_FT_INT8).Value(index);
    }

    public void CMD_FOCUS(String code) {
        try {
            String group = getGroupFromCode(code);
            long hwnd = getHwndFromCode(code);
            BMSPack pack = new BMSPack();
            pack.AddField("CMD", CONST_FT_INT8).Value(VRCEShared.CONST_ANDROIDCOMMAND_FOCUS_WINDOW);
            pack.AddField("HWND", CONST_FT_INT64).Value(hwnd);
            pack.AddField("GROUP", CONST_FT_UUID).ValueAsUUID(group);
            TransferpacketWithlock(pack);
        } catch (Exception e) {
        }
    }

    private void initForXMLBuffered3DObject() {
        for (PDWindow obj : Mainview.buffered3DObjects) {
            obj._found = false;
        }
    }

    private int searchBuffered3DObject(String code) {
        int i = 0;
        for (PDWindow obj : Mainview.buffered3DObjects) {
            if (obj.Code.compareTo(code) == 0) {
                obj._found = true;
                return i;
            }
            i++;
        }
        return -1;
    }

    public void setTextureAndAddObject(PDWindow obj, Bitmap bmp) {
        obj.changeTexture(bmp);
        obj.reSize(Mainview.AppSize, Mainview.panelDepthScale);
    }

    //arányosan elrendezi körben az alkalmazásokat
    //egymáshoz illesztve
    double gapw = 1.7f;
    double gaph = 2.5f;

    public void arrange3DObjects3() {
    }

    public void arrange3DObjects() {

        int cnt = Mainview.buffered3DObjects.size();
        Log.e(APPID,"ARRANGE");
        if (cnt > 0) { //van objektum
            int col = cnt / Mainview.AppRow;
            if ((cnt % Mainview.AppRow) > 0) {
                col++;
            }
            double[] ww = new double[col]; //először szélesség majd tárolva a szögérték
            double[] hh = new double[col];
            double www = 0; // teljes szélesség szögben
            double hhh = 0; //max magasság
            double dist = Mainview.panelDistance+(15-Mainview.AppSize);

            PDWindow obj;
            int objidx = 0;
            for (int c = 0; c < col; c++) {
                ww[c] = 0;
                hh[c] = 0;
                for (int r = 0; r < Mainview.AppRow; r++) {
                    obj = Mainview.buffered3DObjects.get(objidx);
                    ww[c] = Math.max(obj.getScaleX(), ww[c]);
                    hh[c] += obj.getScaleY();
                    objidx++;
                    if (objidx >= cnt) {
                        break;
                    }
                }
                ww[c] = Math.toDegrees(Math.atan((ww[c] / 2) / dist));  //m'r nem sz;less;g hanem szög
                ww[c] *= gapw;
                www += ww[c];
                hhh = Math.max(hh[c], hhh);
            }
            hhh *= gaph;
            double angleY = Math.toDegrees(Math.atan(hhh / 2.0f / dist)); // a teljes sorhoz tartozó nyílás szög fele
            double akth = 0;

            double angY;
            double angX = -www; //degree
            objidx = 0;
            for (int c = 0; c < col; c++) {
                akth = (hhh - hh[c]) / 2.0f; //a teljes szélességehez viszonítás miatt ennyivel kell az alapvonalhoz emelni.
                angX += ww[c] + 1; //első féllépés
                for (int r = 0; r < Mainview.AppRow; r++) {
                    obj = Mainview.buffered3DObjects.get(objidx);
                    akth += (obj.getScaleY() * gaph) / 2.0f; //aktuális alakzat közepe
                    angY = -angleY + ((angleY * 2) / hhh) * akth;
                    obj.RotY(angX);
                    obj.RotZ(angY);
                    if ((Mainview.zoomWhenLook) && (obj == Mainview.lookedobj)) {
                        obj.goTo(-obj.NearZ, Mainview.AnimTime);
                    } else {
                        obj.goTo(-Mainview.panelDistance, Mainview.AnimTime);
                    }
                    akth += (obj.getScaleY() * gaph) / 2.0f; //következő alakzat alja
                    objidx++;
                    if (objidx >= cnt) {
                        break;
                    }
                }
                angX += ww[c] + 1; //első féllépés
            }
        }


    }

    private String getCodeFromHwndGroup(long hwnd, String group) {
        return String.valueOf(hwnd) + "_" + group;
    }

    private String getCodeFromHwndGroup(String hwnd, String group) {
        return hwnd + "|" + group;
    }

    private String getGroupFromCode(String code) {
        String[] group = code.split("_");
        return group[1];
    }

    private long getHwndFromCode(String code) {
        String[] group = code.split("_");
        long hwnd = Long.parseLong(group[0]);
        return hwnd;
    }


    private PDWindow get3DObject(String code) {
        if (Mainview.isDeletedObject(code)) {
            return null;
        }
        //String code = getCodeFromHwndGroup(hwnd,group);
        int found = searchBuffered3DObject(code);
        PDWindow obj;
        if (found == -1) //not found
        {
            obj = new PDWindow(PDObjectType.Window, Mainview.origo, 1, code, Mainview.panelDepthScale, Mainview.panelDistanceNear);
            Mainview.buffered3DObjects.add(obj);
            obj.reSize(Mainview.AppSize, Mainview.panelDepthScale);
        } else {
            obj = Mainview.buffered3DObjects.get(found);
        }
        return obj;
    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        if (isExternalStorageReadable() && isExternalStorageWritable()) {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "");  //getExternalStoragePublicDirector
            if (!file.mkdirs()) {
                Log.e(APPID, "Directory not created");
            }
            return file;
        }
        return null;
    }

    public File getAlbumStorageDir(Context context, String albumName) {
        // Get the directory for the app's private pictures directory.
        File file = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e(APPID, "Directory not created");
        }
        return file;
    }

    public void SaveInputStream2(InputStream in, String targetFile) {
        try {
            OutputStream out = new FileOutputStream(targetFile);
// Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (Exception e) {

        }
    }

    public void SaveInputStream(ByteArrayInputStream initialStream, File targetFile)
            throws IOException {
        int cnt = initialStream.available();
        byte[] buffer = new byte[cnt];
        initialStream.read(buffer);
        OutputStream outStream = new FileOutputStream(targetFile);

        outStream.write(buffer);
        outStream.flush();
        outStream.close();
    }
}




/*
    public AppInfo searchByCode(String xmlcode,ArrayList<AppInfo> list){
        for (AppInfo app:list)
        {
            if (app.Code.compareTo(xmlcode)==0){
                return app;
            }
        }
        return null;
    }
*/
    //TODO: Alapvető APPINFO beállítások melyeket az XML adataival kell kiegészíteni később
/*
    private ArrayList<AppInfo> unpackZip(InputStream is)
    {
        ArrayList<AppInfo> ret = new ArrayList<>();
        ZipInputStream zis;
        zis = new ZipInputStream(new BufferedInputStream(is));

        try
        {
            if (is.available()>300){
                //Log.e(APPID,"Content with more than one file.");
            }


            String code;//filename;
            ZipEntry ze;
            byte[] buffer = new byte[4096];
            int count, i =0;
            ByteArrayOutputStream fout;
            while ((ze = zis.getNextEntry()) != null)
            {
                code = ze.getName();
                //Log.e(APPID,"Unzip File start "+i+code);
                fout = new ByteArrayOutputStream();
                long sz = ze.getSize();
                while ((count = zis.read(buffer,0,4096)) != -1)
                {
                    fout.write(buffer, 0, count);
                }

                try {
                    ret.add(new AppInfo(code, new ByteArrayInputStream(fout.toByteArray())));
                } catch (Exception e){
                    Log.e(APPID,"Unzip File Process Error");
                }
                fout.close();
                zis.closeEntry();
                //Log.e(APPID,"Unzip File end "+i);
                i++;
            }
        }
        catch(EOFException e){
        }
        catch(IOException e)
        {
            Log.e(APPID,"UnzipErr:1");
            e.printStackTrace();
        }
        try{
            zis.close();
        }catch (IOException e){
            Log.e(APPID,"UnzipErr:2 Close");
        };
        return ret;
    }
    */
    /*
    public void refresh3DObject(AppInfo app){
        try {
            if (app!=null) {

                MediaBuffer buf = mediaserver.store(app); //bufferbe letárolás
                if (buf != null) {
                    PDWindow obj = prepare3DObject(app); //vagy korabbi vagy most keszult obj
                    //Innen a bufferrel dolgozunk ami mostani vagy korabbi APP bol szarmazik
                    Bitmap bmp = BitmapFactory.decodeByteArray(buf.buffer,0,buf.buffer.length);  //todo 3.decodeStream(buf.buffer);
                    //bmp = Bitmap.createScaledBitmap(bmp, 120, 120,false);
                    setTextureAndAddObject(obj,bmp);
                    arrange3DObjects();
                }
            } else {
                Log.d(APPID, "Fatal error?");
            }
        } catch (Exception e){};
    }
*/




/*
    private void processingA(BMSPack pack) {
        //MAIN!!!!!  MAIN!!!!

        int c = pack.GetFieldIndexByName("IDX");
        for(int i=c; i<pack.FieldsCount(); i+=3) //becaue IDX,HWND, IMAGE = 3
        {
            byte idx = (byte)pack.GetField(i).getValue((byte)0);
            long hwnd = pack.GetField(i+1).getValue((byte)0);
            byte[] image = pack.GetField(i+2).getValue();
            MediaBuffer buf = mediaserver.store(idx,image);

            //MediaBuffer buf = mediaserver.search(idx); //bufferbe letárolási hely alapján keresem
            if (buf != null) {
                PDWindow obj;
                obj = new PDWindow(PDObjectType.Window,Mainview.origo,1,String.valueOf(hwnd),Mainview.panelDepthScale,Mainview.panelDistanceNear);
                Mainview.buffered3DObjects.add(obj);
                obj.reSize(Mainview.AppSize,Mainview.panelDepthScale);
                //Innen a bufferrel dolgozunk ami mostani vagy korabbi APP bol szarmazik
                Bitmap bmp = BitmapFactory.decodeByteArray(buf.buffer,0,buf.buffer.length);  //todo 3.decodeStream(buf.buffer);
                //bmp = Bitmap.createScaledBitmap(bmp, 120, 120,false);
                setTextureAndAddObject(obj,bmp);
                arrange3DObjects();
            }

        }


        //Log.e(APPID,"New zipped pack");
        // mindent beolvastunk egy pack bol
        //unzippedFiles = unpackZip(new ByteArrayInputStream(str.toByteArray())); //minimum setup.xls kell benne. A f'jlok csak akkor vannak elk-ldve, ha v'ltozott a tartalom. Egy;bk;nt meg kell lennie helyben
/*
        AppInfo setup = searchByCode("setup.xml",unzippedFiles);
        AppInfo app;
        if (setup!=null) {
            try {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                org.w3c.dom.Document document = documentBuilder.parse(setup.mem);
                Node n = document.getFirstChild(); //head
                NodeList l = n.getChildNodes();
                Element e;
                String xmlcode;
                String appname;
                byte xmlsii;

                String no = n.getAttributes().getNamedItem("COUNT").getNodeValue();
                //Log.e(APPID, "DEB1: "+no+", "+l.getLength()+" files");

                initForXMLBuffered3DObject(); //törlöm a találati listából, a searchBuffered3DObject fogja beírni truera
                int cnt = l.getLength();
                for (int i=0;i<cnt;i++) {
                    e = (Element) l.item(i);
                    xmlcode = e.getAttribute("CODE"); //Ha a kódok között nem szerepel egy korábbi, akkor azt törölni kell.
                    appname = e.getAttribute("APPNAME");
                    xmlsii = (byte)Integer.parseInt(e.getAttribute("SII"));

                    // Az XML-ben küldött fájl bejegyzés (XMLCODE) alapján keresem a ZIPben a fájlnév alapján (HWND)
                    // Ha nem találom a ZIP fájlok között akkor nem küldték el, tehát nem változott. Illetve meg kell lennie az index alapján a pufferben.
                    app= searchByCode(xmlcode,unzippedFiles);  //Az APP  vagy null

                    //TODO SII ertekkel stb
                    if (app == null ){ //az XML beli HANDLE fájl nincs meg a ZIP ben
                        //nincs mit tárolni itt kell lennie a pufferban
                        try {
                            MediaBuffer buf = mediaserver.search(xmlsii); //bufferbe letárolási hely alapján keresem
                            if (buf != null) {
                                //megtaláltam a letárolási helyet
                                //Innen a bufferrel dolgozunk ami mostani vagy korabbi APP bol szarmazik
                                Bitmap bmp = null;
                                bmp = BitmapFactory.decodeByteArray(buf.buffer,0,buf.buffer.length); //todo 3 .decodeStream(buf.buffer); //buf.bmp
                                //bmp = Bitmap.createScaledBitmap(bmp, 120, 120,false);

                                if (bmp!=null) {
                                    //Log.e(APPID, "DEB7:main1:");
                                    int found = searchBuffered3DObject(xmlcode);
                                    if (found>-1){
                                        //Log.e(APPID, "DEB7:main2:"+String.valueOf(found));
                                        PDWindow obj  = Mainview.buffered3DObjects.get(found);
                                        setTextureAndAddObject(obj,bmp);
                                    }
                                } else {
                                    Log.e(APPID, "DEB1: JPEG hiba: "+appname);
                                    //Miert nem sikerult dekodolni? SAVE ZIP
                                    //SaveInputStream(buf.buffer,new File(getAlbumStorageDir("VRContentExporter"),  "err.jpg"));
                                    //SaveInputStream2(buf.buffer,getAlbumStorageDir("VRContentExporter")+"/err.jpg");
                                    //Log.e(APPID, "imgconvert: OK7" );
                                }
                                arrange3DObjects();
                                //Log.e(APPID, "imgconvert: OK4" );
                            } else {
                                //nincs meg a pufferben ami szinkronitási probléma
                                //TODO: el kell kérni a teljes puffertartalmat
                                Log.e(APPID, "imgconvert: OK8" );
                            }

                        } catch (Exception el){
                            Log.e(APPID, "imgconvert: OK6" );
                        };

                    } else {
                        //jött fájl letároljuk az XMLSII helyre mediaserver.store
                        app.SII = xmlsii;
                        app.APPName = appname;
                        //Log.e(APPID, "DEB1: "+appname);
                        refresh3DObject(app);  //mediaserver.store
                        //Log.e(APPID, "imgconvert: OK5" );
                    }
                }
                //eltűnt objektumok törlése
                Mainview.checkDelete=true; //onRender fog törölni
                try{

                    //deleteNotFoundBuffered3DObject();
                }catch (Exception e6){
                    Log.e(APPID, "DEB3:" );
                }

            } catch (Exception e) {
                Log.e(APPID,"XML Feldolgozasi hiba");
            }
        } else {
            Log.e(APPID,"SETUP.XML in ZIP NOT FOUND!!");
        }
*/
//    }
