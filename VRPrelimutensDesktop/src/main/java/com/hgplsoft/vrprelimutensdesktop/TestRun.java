package com.hgplsoft.vrprelimutensdesktop;

import android.content.Context;
import android.util.Log;

import java.net.Socket;
import com.hgplsoft.babylonms.*;

/**
 * Created by horvath3ga on 2017.11.04..
 */

public class TestRun {
    static final String ID = "START";
    static String UUIDTestServer = "cf70c42b-93b7-49a9-b8ab-c5afb8d7dd4d";
    static String UUIDAndroid = "cf70c42b-93b7-49a9-b8ab-c5afb8d7dd4d";
    static BabylonMS bms;
    static String IP = "192.168.42.100";
    static int PORT = 9000;
    static int once = 0;

    static byte CONST_ANDROIDCOMMAND_SUBSCRIBE_HWND = 30; //subscribe hwnd
    static byte CONST_ANDROIDCOMMAND_SUBSCRIBE_TYPE = 33; //subscribe type (images,mouse,keyboard....
    static byte CONST_ANDROIDCOMMAND_RETRIEVE_HWND = 37; //Direct retrieve a buffer element if need independently from refresh


    MediaServer2 mediaserver;

    public TestRun(Context ctx) {
        if (once++<1) {
            TestRun2(ctx);
        }
    }

    public void TestRun2(Context ctx){
        mediaserver = new MediaServer2();
        bms = BabylonMS.LaunchMiniShip(IP,PORT,null,UUIDAndroid ,UUIDTestServer,ctx);
        bms.setNewInputFrameEventHandler(new BabylonMS.BMSEventHandler() {
            @Override
            public void Event(BMSEventSessionParameter session) {
                try
                {
                    byte cmd = (byte)session.inputPack.GetField(0).getValue((byte)0);


                    Log.e(ID,"JPG NewInout testframe0");
                }
                catch (Exception e2) { };
            }
        });
        bms.setServerReadyEventHandler(new BabylonMS.BMSEventHandler() {
            @Override
            public void Event(BMSEventSessionParameter session) {
                Log.e(ID,"Connected2");
            }
        });
        bms.setServerConnectedEventHandler(new BabylonMS.EventHandler() {
            @Override
            public void Event() {
                Log.e(ID,"Connected1");
            }
        });
        bms.PrepareGate();//network blo29ing for exit
        while (true){
            try {
                Thread.sleep(100);
            }catch (Exception e){}
        }

    }

    public void CMD_RETRIEVE(BabylonMS bms,byte index){
        BMSPack pack = new BMSPack();
        pack.AddField("CMD",BabylonMS.CONST_FT_INT8).Value(CONST_ANDROIDCOMMAND_RETRIEVE_HWND);
        pack.AddField("INDEX",BabylonMS.CONST_FT_INT8).Value(index);
    }

    public void TestRun1(Context ctx){
        bms = BabylonMS.ShipDocking(IP,PORT,UUIDTestServer,ctx);

        bms.setNewInputFrameEventHandler(new BabylonMS.BMSEventHandler() {
            @Override
            public void Event(BMSEventSessionParameter session) {
                try
                {
                    //Console.Beep();
                    byte[] buffer = session.inputPack.GetField(0).getValue();

                    Log.e(ID,"JPG NewInout testframe0");
                }
                catch (Exception e2) { };
            }
        });
        bms.setClientConnectedEventHandler(new BabylonMS.BMSEventHandler() {
            @Override
            public void Event(BMSEventSessionParameter session) {
                Log.e(ID,"Connected");
            }
        });
        bms.OpenGate(true);//network blo29ing for exit
    }
    static int ii = 0;
}
