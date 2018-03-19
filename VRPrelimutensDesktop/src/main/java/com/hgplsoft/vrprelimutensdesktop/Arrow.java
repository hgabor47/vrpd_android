package com.hgplsoft.vrprelimutensdesktop;

import android.graphics.Color;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;

/**
 * Created by horvath3ga on 2017.08.20..
 */

public class Arrow extends Object3D  {

    private float mSize;
    private boolean mIsSkybox;
    private boolean mCreateTextureCoords;
    private boolean mCreateVertexColorBuffer;

    /**
     * Creates a cube primitive. Calling this constructor will create texture coordinates but no vertex color buffer.
     * @param size		The size of the cube.
     */
    public Arrow(float size) {
        this(size, false, false, true, false, true);
    }

    /**
     * Creates a cube primitive. Calling this constructor will create texture coordinates but no vertex color buffer.
     *
     * @param size			The size of the cube.
     * @param isSkybox		A boolean that indicates whether this is a skybox or not. If set to true the normals will
     * 						be inverted.
     */
    public Arrow(float size, boolean isSkybox) {
        this(size, isSkybox, true, true, false, true);
    }

    /**
     * Creates a cube primitive. Calling this constructor will create texture coordinates but no vertex color buffer.
     *
     * @param size					The size of the cube.
     * @param isSkybox				A boolean that indicates whether this is a skybox or not. If set to true the normals will
     * 								be inverted.
     * @param hasCubemapTexture		A boolean that indicates a cube map texture will be used (6 textures) or a regular
     * 								single texture.
     */
    public Arrow(float size, boolean isSkybox, boolean hasCubemapTexture)
    {
        this(size, isSkybox, hasCubemapTexture, true, false, true);
    }

    /**
     * Creates a cube primitive.
     *
     * @param size						The size of the cube.
     * @param isSkybox					A boolean that indicates whether this is a skybox or not. If set to true the normals will
     * 									be inverted.
     * @param hasCubemapTexture			A boolean that indicates a cube map texture will be used (6 textures) or a regular
     * 									single texture.
     * @param createTextureCoordinates	A boolean that indicates whether the texture coordinates should be calculated or not.
     * @param createVertexColorBuffer	A boolean that indicates whether a vertex color buffer should be created or not.
     * @param createVBOs                A boolean that indicates whether the VBOs should be created immediately.
     */
    public Arrow(float size, boolean isSkybox, boolean hasCubemapTexture, boolean createTextureCoordinates,
                 boolean createVertexColorBuffer, boolean createVBOs) {
        super();
        /*
        MatArrow = new Material();
        MatArrow.setColor(Color.RED);
        dummyx.addChild(this);
        this.setMaterial(MatArrow);
        setPosition(0, 0, -distance); //from origo
        RotZ(-45);
        */
        mIsSkybox = isSkybox;
        mSize = size;
        mHasCubemapTexture = hasCubemapTexture;
        mCreateTextureCoords = createTextureCoordinates;
        mCreateVertexColorBuffer = createVertexColorBuffer;
        init(createVBOs);
    }



    private void init(boolean createVBOs)
    {
        float halfSize = mSize * .5f;
        float Size5 = halfSize * .5f;
        float s1 = mSize * 1.0f;

        //  **0**
        //  *****
        //  12*56
        //  *****
        //  *3*4*


        float[] vertices = { //Hegye az origo
                // -- front
                0,0,0,      -halfSize,-halfSize,0,      -Size5,-halfSize,0,        -Size5,-mSize,0,      Size5,-mSize,0,      Size5,-halfSize,0,     halfSize,-halfSize,0,
                //back
                0,0,-mSize,      halfSize,-halfSize,-mSize,       Size5,-halfSize,-mSize,     Size5,-mSize,-mSize,      -Size5,-mSize,-mSize,     -Size5,-halfSize,-mSize,     -halfSize,-halfSize,-mSize

        };
        //  **0**
        //  *****
        //  12*56
        //  *****
        //  *3*4*
        int[] indices = {
                0, 1, 2, 0, 2, 3,0,3,4,0,4,5,0,5,6
        };

        float[] vertices2 = {
                // -- back
                halfSize, halfSize, halfSize, 			-halfSize, halfSize, halfSize,
                -halfSize, -halfSize, halfSize,			halfSize, -halfSize, halfSize, // 0-1-halfSize-3 front

                halfSize, halfSize, halfSize, 			halfSize, -halfSize, halfSize,
                halfSize, -halfSize, -halfSize, 		halfSize, halfSize, -halfSize,// 0-3-4-5 right
                // -- front
                halfSize, -halfSize, -halfSize, 		-halfSize, -halfSize, -halfSize,
                -halfSize, halfSize, -halfSize,			halfSize, halfSize, -halfSize,// 4-7-6-5 back

                -halfSize, halfSize, halfSize, 			-halfSize, halfSize, -halfSize,
                -halfSize, -halfSize, -halfSize,		-halfSize,	-halfSize, halfSize,// 1-6-7-halfSize left

                halfSize, halfSize, halfSize, 			halfSize, halfSize, -halfSize,
                -halfSize, halfSize, -halfSize, 		-halfSize, halfSize, halfSize, // top

                halfSize, -halfSize, halfSize, 			-halfSize, -halfSize, halfSize,
                -halfSize, -halfSize, -halfSize,		halfSize, -halfSize, -halfSize,// bottom
        };


        float[] textureCoords = null;
        float[] textureCoords2 = null;
        float[] skyboxTextureCoords = null;

        if (mCreateTextureCoords && !mIsSkybox && !mHasCubemapTexture)
        {
            textureCoords = new float[]
                    {
                            0, 1, 1, 1, 1, 0, 0, 0, // front
                            0, 1, 1, 1, 1, 0, 0, 0, // back
                    };

        }
        else if (mIsSkybox && !mHasCubemapTexture)
        {
            skyboxTextureCoords = new float[] {
                    .25f, .3333f, .5f, .3333f, .5f, .6666f, .25f, .6666f, // back
                    .25f, .3333f, .25f, .6666f, 0, .6666f, 0, .3333f, // left
                    1, .6666f, .75f, .6666f, .75f, .3333f, 1, .3333f, // front
                    .5f, .3333f, .75f, .3333f, .75f, .6666f, .5f, .6666f, // right
                    .25f, .3333f, .25f, 0, .5f, 0, .5f, .3333f, // up
                    .25f, .6666f, .5f, .6666f, .5f, 1, .25f, 1 // down
            };
        }

        float[] colors = null;
        float[] colors2 = null;
        if (mCreateVertexColorBuffer)
        {
            colors = new float[] {
                    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
            };
        }

        float n = 1;

        float[] normals = {
                0, 0, n, 0, 0, n, 0, 0, n, 0, 0, n, 0, 0, n,0, 0, n,0, 0, n, // front
                0, 0, -n, 0, 0, -n, 0, 0, -n, 0, 0, -n,0, 0, -n,0, 0, -n,0, 0, -n, // back
        };



        setData(vertices, normals, mIsSkybox || mHasCubemapTexture ? skyboxTextureCoords : textureCoords, colors, indices, createVBOs);

        if(mIsSkybox) setDoubleSided(true);

        vertices = null;
        normals = null;
        skyboxTextureCoords = null;
        textureCoords = null;
        colors = null;
        indices = null;
    }

}
