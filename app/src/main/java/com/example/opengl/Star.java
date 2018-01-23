package com.example.opengl;

import java.util.Random;

/**
 * Created by chen.
 */
public class Star extends GameObject
{
    Random r;

    public Star(int mapWidth,int mapHeight)
    {
        setType(Type.STAR);
        r = new Random();
        setWorldLocation(r.nextInt(mapWidth),r.nextInt(mapHeight));

        float[] starVertices = new float[]{0,0,0};
        setVertices(starVertices);
    }

    public void update()
    {
        int n = r.nextInt(1000);
        if(n==0)
        {
            if(isActive)
                setActive(false);
            else
                setActive(true);
        }
    }
}
