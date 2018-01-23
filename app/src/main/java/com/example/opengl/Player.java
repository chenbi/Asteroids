package com.example.opengl;

import android.graphics.PointF;

/**
 * Created by chen.
 */
public class Player extends MovingObject
{
    boolean isThrusting;
    private boolean turnRight= false;
    private boolean turnLeft= false;

    Boundaries cp;
    PointF[] points;   // to pass the shipVertices to the collision package

    public Player (float worldLocationX, float worldLocationY)
    {
        super();
        setType(Type.SHIP);
//        setWorldLocation(worldLocationX,worldLocationY);
        setWorldLocation(10,10);
        float width = 15;
        float length = 20;
        setSize(width,length);

        setMaxSpeed(50);

        float halfW = width/2;
        float halfL = length/2;

        float [] shipVertices = new float[]{
                -halfW,-halfL,0,
                halfW,-halfL,0,
                0,halfL,0
        };
        setVertices(shipVertices);

        // initialize the collision package
        points = new PointF[6];
        points[0] = new PointF(-halfW,-halfL);
        points[2] = new PointF(halfW,-halfL);
        points[4] = new PointF(0,halfL);
        // add mid-point of the line to improve the accuracy
        points[1]= new PointF( (points[0].x+points[2].x)/2,(points[0].y+points[2].y)/2);
        points[3]= new PointF((points[2].x+points[4].x)/2,(points[2].y+points[4].y)/2);
        points[5]= new PointF((points[4].x+points[0].x)/2,(points[4].y+points[0].y)/2);

        cp = new Boundaries (points,getWorldLocation(),length/2,getFacingAngle());
    }

    public void update(long fps)
    {
        float speed = getSpeed();
        if(isThrusting)
        {
            if(speed < getMaxSpeed())
                setSpeed(speed+5);
        }
        else
        {
            if(speed > 0)
                setSpeed(speed - 3);
            else
                setSpeed(0);
        }
        setxVelocity((float)(speed * Math.cos( Math.toRadians(getFacingAngle()+90))));
        setyVelocity((float)(speed * Math.sin( Math.toRadians(getFacingAngle()+90))));

        if(turnLeft)
            setRotationRate(360);
        else if(turnRight)
        {
            setRotationRate(-360);
        }
        else
        {
            setRotationRate(0);
        }
        move(fps);

        //update collision package
        cp.facingAngle = getFacingAngle();
        cp.worldLocation = getWorldLocation();
    }

    public boolean pullTrigger()
    {
        return true;
    }
    public void setTurnRight (boolean turnRight)
    {
        this.turnRight=turnRight;
    }
    public void setTurnLeft (boolean turnLeft)
    {
        this.turnLeft=turnLeft;
    }
    public void toggleThrust()
    {
        isThrusting = !isThrusting;
    }
}
