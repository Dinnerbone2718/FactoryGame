package com.factory.game.Renderer;

import com.badlogic.gdx.graphics.Color;


public class DayNightCycle {


    public static final int DAY_LENGTH_TICKS = 60*60*12;



    public static final Color DEFAULT_AMBIENT   = new Color(0.15f, 0.15f, 0.25f, 0.10f);
    public static final Color DEFAULT_SUN_COLOR = new Color(1.00f, 0.95f, 0.80f, 0.80f);
    public static final float DEFAULT_SUN_ANGLE = -90f;


    public static final Color CAVE_AMBIENT    = new Color(0.04f, 0.04f, 0.12f, 0.0f);
    public static final Color SURFACE_AMBIENT = new Color(0.06f, 0.05f, 0.04f, 0.88f);


    private static final Color NIGHT_AMBIENT = new Color(0.08f, 0.05f, 0.1f, 0.92f);
    private static final Color DUSK_DAWN_AMBIENT = new Color(0.22f, 0.11f, 0.04f, 0.49f);
    private static final Color DAY_AMBIENT   = new Color(1.00f,  0.98f,  0.92f,  0.98f);

    private static final Color SHADER_AMB_NIGHT = new Color(0.05f, 0.05f, 0.10f, 0.00f);
    private static final Color SHADER_AMB_DUSK_DAWN  = new Color(0.35f, 0.20f, 0.07f, 0.50f);
    private static final Color SHADER_AMB_DAY   = new Color(0.65f, 0.65f, 0.65f, 1.00f);



    private int   tick      = 60*60*4;   
    private float sunFactor = 0f;   

    private final Color naturalLight     = new Color();
    private final Color shaderSkyAmbient = new Color();

    private boolean inCave = false;

    public void tick() {
        tick = (tick + 1) % DAY_LENGTH_TICKS;
        if (!inCave) recompute();


    }

    public void setTick(int tick) {
        this.tick = ((tick % DAY_LENGTH_TICKS) + DAY_LENGTH_TICKS) % DAY_LENGTH_TICKS;
        recompute();
    }

    public int   getTick()      { return tick; }
    public float getSunFactor() { return sunFactor; }

    public Color getSkyColor()          { return shaderSkyAmbient; }

    public Color getNaturalLight()     { return naturalLight;     }
    public Color getShaderSkyAmbient() { return shaderSkyAmbient; }

    public void setTimeOfDay(float t) {
        tick = (int) (t * DAY_LENGTH_TICKS);
    }

    public float getTimeOfDay() {
        return (float) tick / DAY_LENGTH_TICKS;
    }

    public void setCave() {
        inCave = true;
        naturalLight    .set(NIGHT_AMBIENT);
        shaderSkyAmbient.set(SHADER_AMB_NIGHT);
    }

    public void clearCave() {
        inCave = false;
        recompute();
    }


    private void recompute() {
        float t = (float) tick / DAY_LENGTH_TICKS;

        float rawSun = (float) Math.sin(t * 2 * Math.PI - Math.PI / 2);
        sunFactor = Math.max(0f, rawSun + 0.85f) / 1.85f;
        sunFactor = (float) Math.pow(sunFactor, 1.5);






        if (t < 0.15f) {
            naturalLight    .set(NIGHT_AMBIENT);
            shaderSkyAmbient.set(SHADER_AMB_NIGHT);

        } else if (t < 0.35f) {
            float phase = (t - 0.15f) / 0.20f;
            if (phase < 0.5f) {
                naturalLight    .set(NIGHT_AMBIENT)       .lerp(DUSK_DAWN_AMBIENT,    phase * 2f);
                shaderSkyAmbient.set(SHADER_AMB_NIGHT)    .lerp(SHADER_AMB_DUSK_DAWN, phase * 2f);
            } else {
                naturalLight    .set(DUSK_DAWN_AMBIENT)   .lerp(DAY_AMBIENT,           (phase - 0.5f) * 2f);
                shaderSkyAmbient.set(SHADER_AMB_DUSK_DAWN).lerp(SHADER_AMB_DAY,        (phase - 0.5f) * 2f);
            }

        } else if (t < 0.65f) {
            naturalLight    .set(DAY_AMBIENT);
            shaderSkyAmbient.set(SHADER_AMB_DAY);

        } else if (t < 0.85f) {
            float phase = (t - 0.65f) / 0.20f;
            if (phase < 0.5f) {
                naturalLight    .set(DAY_AMBIENT)         .lerp(DUSK_DAWN_AMBIENT,    phase * 2f);
                shaderSkyAmbient.set(SHADER_AMB_DAY)      .lerp(SHADER_AMB_DUSK_DAWN, phase * 2f);
            } else {
                naturalLight    .set(DUSK_DAWN_AMBIENT)   .lerp(NIGHT_AMBIENT,         (phase - 0.5f) * 2f);
                shaderSkyAmbient.set(SHADER_AMB_DUSK_DAWN).lerp(SHADER_AMB_NIGHT,      (phase - 0.5f) * 2f);
            }

        } else {
            naturalLight    .set(NIGHT_AMBIENT);
            shaderSkyAmbient.set(SHADER_AMB_NIGHT);
        }


    }

    
}