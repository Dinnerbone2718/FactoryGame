package com.factory.game;

import com.badlogic.gdx.Gdx;

public class Camera {

    public float cameraX;
    public float cameraY;
    public int VIRTUAL_WIDTH;
    public int VIRTUAL_HEIGHT;

    public Camera() {
        VIRTUAL_WIDTH  = Gdx.graphics.getWidth();
        VIRTUAL_HEIGHT = Gdx.graphics.getHeight();
    }


    public void followPlayer(float playerWorldX, float playerWorldY) {
        cameraX = VIRTUAL_WIDTH  / 2f - playerWorldX - Main.TILE_SCALE / 2f;
        cameraY = VIRTUAL_HEIGHT / 2f - playerWorldY - Main.TILE_SCALE / 2f;
    }


    public int leftTile()   { return (int) Math.floor(-cameraX / Main.TILE_SCALE) - 1; }
    public int bottomTile() { return (int) Math.floor(-cameraY / Main.TILE_SCALE) - 1; }
    public int rightTile()  { return leftTile()   + VIRTUAL_WIDTH  / Main.TILE_SCALE + 2; }
    public int topTile()    { return bottomTile() + VIRTUAL_HEIGHT / Main.TILE_SCALE + 2; }

    public void resize(int width, int height) {
        VIRTUAL_WIDTH  = width;
        VIRTUAL_HEIGHT = height;
    }
}