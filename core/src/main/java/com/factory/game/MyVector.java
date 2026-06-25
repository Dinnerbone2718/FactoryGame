package com.factory.game;

public class MyVector {
 
    private float x;
    private float y;

    public MyVector(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX(){return this.x;}
    public float getY(){return this.y;}

    public void setX(float x){this.x = x;}
    public void setY(float y){this.y = y;}



    public void normalize(){
        float norm = (float) Math.sqrt(this.x * this.x + this.y * this.y);
        this.x = x/norm;
        this.y = y/norm;
    }

}
