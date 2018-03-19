package com.hgplsoft.vrprelimutensdesktop;

/**
 * Created by horvath3ga on 2018.01.25..
 */

public class VRCEShared {

    // https://docs.google.com/document/d/1dY-_8iMouxdg1eSR6ZEnjNl0ZFFSJhA4JIjBhzVUi5Y/edit#bookmark=id.ryr5jhoe9t6b

    static final byte CONST_BABYLON_INSTANCE = 100; //used in BabylonMS and here I reserve the No.100

    public static final byte CONST_COMMAND_EXIST = 0;
    public static final byte CONST_COMMAND_STORE = 1;
    public static final byte CONST_COMMAND_RETRIEVE = 2;
    public static final byte CONST_COMMAND_GETBUFFER = 3;
    public static final byte CONST_COMMAND_RETRIEVE_IDX = 8;

    public static final byte CONST_COMMAND_SCREENSHOT = 11;

    public static final byte CONST_ANDROIDCOMMAND_SUBSCRIBE_HWND = 30; //subscribe hwnd
    public static final byte CONST_ANDROIDCOMMAND_SUBSCRIBE_TYPE = 33; //subscribe type (images,mouse,keyboard....
    public static final byte CONST_ANDROIDCOMMAND_RETRIEVE_HWND = 37; //Direct retrieve a buffer element if need independently from refresh
    public static final byte CONST_ANDROIDCOMMAND_RETRIEVE_ALL = 39; //Elküldi a legfrissebb tartalmat minden feliratkozott képről.(feliratkozások minden Androidhoz előzőleg letárolva)
    public static final byte CONST_ANDROIDCOMMAND_CHANGE_HWND = 43; //Pontosan egy ablak változott
    public static final byte CONST_ANDROIDCOMMAND_IC_EVENT = 46; //InputController esemény (mouse)
    public static final byte CONST_ANDROIDCOMMAND_FOCUS_WINDOW = 47; //Sent to desktop
    public static final byte CONST_ANDROIDCOMMAND_LOST_WINDOW = 48; //Screencontent session closed disconnected so window is closed


    public static final byte CONST_IC_EVENT = 50;
    public static final byte CONST_IC_MODE = 51;


    //Imagebuffer
    public static final byte CONST_MODE_BFADD = 2;
    public static final byte CONST_MODE_BFFOUND = 6;
    public static final byte CONST_MODE_BFMODIFY = 9;

    public static final int CONST_MOUSEBUTTON_LEFT = 1048576;  //From Mousebuttons
    public static final int CONST_MOUSEBUTTON_MIDDLE = 4194304;
    public static final int CONST_MOUSEBUTTON_RIGHT = 2097152;
    public static final int CONST_MOUSEBUTTON_XBUTTON1 = 8388608;
    public static final int CONST_MOUSEBUTTON_XBUTTON2 = 16777216;
    public static final int CONST_MOUSEBUTTON_MASK = 0x3fffffff;
    public static final int CONST_MOUSEBUTTON_VIRTUAL = 0x80000000;
    public static final int CONST_MOUSEBUTTON_DOWN = 0x40000000;   // bit=1 = DOWN


}
