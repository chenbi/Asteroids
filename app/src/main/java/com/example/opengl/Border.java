package com.example.opengl;

/**
 * Created by chen.
 */
public class Border extends GameObject
{
    public Border(float mapWidth,float mapHeight)
    {
        setType(Type.BORDER);

        setWorldLocation(mapWidth/2,mapHeight/2);
//        setWorldLocation(0,0);

        float w = mapWidth;
        float h = mapHeight;
//        setSize(w,h);  // seems tht width and length in the GameObject is useless

        float[] borderVertices = new float[]{
                -w/2,-h/2,0,
                w/2,-h/2,0,

                w/2,-h/2,0,
                w/2,h/2,0,

                w/2,h/2,0,
                -w/2,h/2,0,

                -w/2,h/2,0,
                -w/2,-h/2,0
        };
        setVertices(borderVertices);
    }
}
