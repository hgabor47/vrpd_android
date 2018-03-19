package com.hgplsoft.vrprelimutensdesktop;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.materials.textures.TextureManager;
import org.rajawali3d.primitives.Cube;

import static com.hgplsoft.vrprelimutensdesktop.MyRenderer.APPID;

/**
 * Created by horvath3ga on 2017.10.06..
 */

public class PDWindow extends PDObject3D{
    Object3D cu;
    Object3D border;
    Material bordermat;
    public double bmpwidth;    //bmpwidth
    public double bmpheight;   //bmpheight
    String Code;
    public double pixangleY;
    public double pixangleX;
    public double posy;
    public double posx; //síkfelületen meghatározott helyzete az objektumnak. Ezt kellkonvertálni a ROTX,ROTY értékekké
    boolean _found;  //Belső használatra: az XMLben érkező CODE (handle) kódok true értékre állítják.
    // Az XML kiértékelés végén megnézem, hogy van e false ugyanis azt ki kell törölni, mert az az ablak már nincs
    public double AppSize;
    public double Depth;
    private PDObjectType Type;


    private void Construct(PDObjectType Type,Object3D origo,float size,String Code,double Depth){
        _found=true;
        this.Type=Type;
        this.Depth = Depth;
        this.Code = Code;
        mat = new Material();
        if (Type == PDObjectType.Arrow){
            cu = new Arrow(size);
            cu.setRotation(-45,0,0);
            mat.setColor(Color.RED);
        }
        if (Type == PDObjectType.Window) {
            cu = new Cube(size);
            cu.setRotation(180,0,0);
            mat.setColor(Color.BLACK);
            border = new Cube(size+0.1f);
            //border.setRotation(180,0,0);
            bordermat = new Material();
            bordermat.setColor(Color.WHITE);
            border.setMaterial(bordermat);
            border.setPosition(0,0,-1f);
            cu.addChild(border);
        }
        setScaleZ(Depth);
        this.addChild(cu);      // C add CU
        dummyx.addChild(this);  //o3 add C
        cu.setMaterial(mat);
        mat.setColorInfluence(0);
        setPosition(0,0,-15);  //defa
        origo.addChild(Base());   //origo add o1
    }
    public PDWindow(PDObjectType Type,Object3D origo,float size,String Code,double Depth, double NearZ){
        super(NearZ);
        Construct(Type,origo,size,Code,Depth);
        try {
            if (Type == PDObjectType.Arrow){
                tex = new Texture("bitmap" + Code, BitmapFactory.decodeResource(MyRenderer.res, R.raw.arrow));
            }else {
                tex = new Texture("bitmap" + Code, BitmapFactory.decodeResource(MyRenderer.res, R.raw.blank));
            }
            bmpwidth = tex.getBitmap().getWidth();
            bmpheight =tex.getBitmap().getHeight();
            mat.addTexture(tex);
        }catch (Exception e){}
    }
    private void Construct(PDObjectType Type,Object3D origo,float size,String Code,double Depth,Object3D originalobject,boolean backface){
        _found=true;
        this.Type=Type;
        this.Depth = Depth;
        this.Code = Code;

        if (Type == PDObjectType.Arrow){
            //mat = new Material();
            cu = originalobject;
            cu.setRotation(-45,0,0);

            //cu.setRotation(0,0,90);
            //mat.setColor(Color.RED);
        }
        if (Type == PDObjectType.Window) {
            mat = new Material();
            cu = originalobject;
            cu.setRotation(180,0,0);
            mat.setColor(Color.BLACK);
            if (backface) {
                border = new Cube(size + 0.1f);
                //border.setRotation(180,0,0);
                bordermat = new Material();
                bordermat.setColor(Color.WHITE);
                border.setMaterial(bordermat);
                border.setPosition(0, 0, -1f);
                cu.addChild(border);
            }
            cu.setMaterial(mat);
            mat.setColorInfluence(0);
        }
        setScaleZ(Depth);
        this.addChild(cu);      // C add CU
        dummyx.addChild(this);  //o3 add C
        //cu.setMaterial(mat);
        //mat.setColorInfluence(0);
        setPosition(0,0,-15);  //defa
        origo.addChild(Base());   //origo add o1
    }
    //Backface option for not arrow only
    public PDWindow(PDObjectType Type,Object3D origo,float size,String Code,double Depth, double NearZ, Object3D originalobject,boolean backface){
        super(NearZ);
        Construct(Type,origo,size,Code,Depth,originalobject,backface);
    }
    public void LengthFromOrigo(double length){
        setPosition(0,0,-length);
    }

    @Override
    public void setMaterial(Material material) {
        cu.setMaterial(material);
    }

    @Override
    public Material getMaterial() {
        return cu.getMaterial();
    }

    @Override
    public void destroy(){  //TODO Ezt meg pontositani kell
        if (cu!=null){
            cu.destroy();}
        cu = null;
        bordermat = null;
        super.destroy();
    }

    public void changeTexture(Bitmap bmp) {
        //if (obj.cu.getMaterial()==null) {
        if (tex != null) {
            try {
                int cnt = cu.getMaterial().getTextureList().size();
                if (cnt > 1) {
//                  Log.e(APPID, "DEB9:Moretexture");
                }
                if (cnt > 0) {
                    double origwidth = tex.getBitmap().getWidth();
                    double origheight = tex.getBitmap().getHeight();
                    if ((origwidth == bmp.getWidth()) && (origheight==bmp.getHeight())){
                        tex.getBitmap().recycle();
                        tex.setBitmap(bmp);
                        TextureManager.getInstance().replaceTexture(tex);
                    } else {
                        try {
                            Material material = cu.getMaterial();
                            String texname = tex.getTextureName();
                            material.removeTexture(tex);
                            tex.getBitmap().recycle();
                            tex = new Texture(texname, bmp);
                            material.addTexture(tex);
                            TextureManager.getInstance().replaceTexture(tex);
                            //tex.registerMaterial(material);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        bmpwidth = bmp.getWidth();
                        bmpheight = bmp.getHeight();
                    }

                  //Log.e(APPID, "DEB9:replacetexture");
                }
            } catch (Exception e7) {
             Log.e(APPID, "DEB9:ERR?Texture");
            }
        }
    }

    public void reSize(double AppSize, double Depth){
        // méret beállítása átlagos terület legyen 6*6 = 36 ezen belül az arányok változnak
        // pl 1280x300 úgy viszonyul a 6x6 hoz hogy
        // képlet:  sqrt((a*b)/(Appsize*appsize))
        double oszto = Math.sqrt((bmpwidth * bmpheight) / (AppSize * AppSize));
        if (oszto > 0) {
            setScale(bmpwidth / oszto, bmpheight / oszto, Depth);
            pixangleX=ContentWidthPixelToAngle(getScaleX(),getZ(),bmpwidth);
            pixangleY=ContentWidthPixelToAngle(getScaleY(),getZ(),bmpheight);
        }
    }


}

enum PDObjectType {
    Window,
    Arrow
}

/*
    public PDWindow(PDObjectType Type,Object3D origo,float size,String Code,Bitmap bmp,double Depth){
        super();
        Construct(Type,origo,size,Code,Depth);
        try {
            tex = new Texture("bitmap" + Code, bmp);
            bmpwidth = bmp.getWidth();
            bmpheight = bmp.getHeight();
            mat.addTexture(tex);
        }catch (Exception e){}
    }

 */