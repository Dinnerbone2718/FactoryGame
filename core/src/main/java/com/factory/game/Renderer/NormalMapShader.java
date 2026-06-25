package com.factory.game.Renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.factory.game.Camera;

public class NormalMapShader {

    public static final int MAX_LIGHTS = 8;

    private static final String VERT =
        "attribute vec4 a_position;\n" +
        "attribute vec4 a_color;\n" +
        "attribute vec2 a_texCoord0;\n" +
        "uniform mat4 u_projTrans;\n" +
        "varying vec4 v_color;\n" +
        "varying vec2 v_texCoord;\n" +
        "void main() {\n" +
        "    v_color    = a_color;\n" +
        "    v_texCoord = a_texCoord0;\n" +
        "    gl_Position = u_projTrans * a_position;\n" +
        "}\n";

    private static final String FRAG =
        "#ifdef GL_ES\n" +
        "precision mediump float;\n" +
        "#endif\n" +
        "#define MAX_LIGHTS " + MAX_LIGHTS + "\n" +

        "varying vec4 v_color;\n" +
        "varying vec2 v_texCoord;\n" +

        "uniform sampler2D u_texture;\n" +
        "uniform sampler2D u_normalMap;\n" +
        "uniform int   u_hasNormalMap;\n" +
        "uniform int   u_isWater;\n" +
        "uniform vec2  u_waterPixelSize;\n" +


        "uniform float u_waterDepth;\n" +
        "uniform vec4  u_waterEdges;\n" +

        "uniform float u_normalStrength;\n" +
        "uniform vec3  u_ambient;\n" +
        "uniform vec3  u_sunDir;\n" +
        "uniform vec4  u_sunColor;\n" +
        "uniform int   u_numLights;\n" +
        "uniform vec3  u_lightPos[MAX_LIGHTS];\n" +
        "uniform vec4  u_lightColor[MAX_LIGHTS];\n" +
        "uniform float u_lightRadius[MAX_LIGHTS];\n" +
        "uniform float u_time;\n" +
        "uniform vec2  u_cameraOffset;\n" +
        "uniform int   u_isItem;\n" +
        "uniform vec2  u_texPixelSize;\n" +

        "float hash(vec2 p) {\n" +
        "    vec2 q = fract(p * vec2(0.1031, 0.1030));\n" +
        "    q += dot(q, q.yx + 33.33);\n" +
        "    return fract((q.x + q.y) * q.x);\n" +
        "}\n" +

        "float valueNoise(vec2 p) {\n" +
        "    vec2 i = floor(p);\n" +
        "    vec2 f = fract(p);\n" +
        "    f = f * f * (3.0 - 2.0 * f);\n" +
        "    float a = hash(i);\n" +
        "    float b = hash(i + vec2(1.0, 0.0));\n" +
        "    float c = hash(i + vec2(0.0, 1.0));\n" +
        "    float d = hash(i + vec2(1.0, 1.0));\n" +
        "    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);\n" +
        "}\n" +

        "float fbm(vec2 p) {\n" +
        "    float v  = 0.0;\n" +
        "    v += valueNoise(p                          ) * 0.50;\n" +
        "    v += valueNoise(p * 2.1 + vec2(3.70, 1.90)) * 0.30;\n" +
        "    v += valueNoise(p * 4.3 + vec2(7.30, 5.10)) * 0.20;\n" +
        "    return v;\n" +
        "}\n" +

        "void main() {\n" +
        "    vec4 diffuse = texture2D(u_texture, v_texCoord) * v_color;\n" +

        "    if (u_isItem == 1) {\n" +
        "        vec2  ps = u_texPixelSize * 1.5;\n" +
        "        float na = texture2D(u_texture, v_texCoord + vec2( ps.x,  0.0)).a\n" +
        "                 + texture2D(u_texture, v_texCoord + vec2(-ps.x,  0.0)).a\n" +
        "                 + texture2D(u_texture, v_texCoord + vec2( 0.0,   ps.y)).a\n" +
        "                 + texture2D(u_texture, v_texCoord + vec2( 0.0,  -ps.y)).a;\n" +
        "        if (diffuse.a < 0.01) {\n" +
        "            if (na > 0.1) { gl_FragColor = vec4(1.0, 0.88, 0.25, 1.0); return; }\n" +
        "            else { discard; }\n" +
        "        }\n" +
        "        gl_FragColor = diffuse;\n" +
        "        return;\n" +
        "    }\n" +

        "    if (diffuse.a < 0.01) discard;\n" +


        "    if (u_isWater == 1) {\n" +
        "        vec3  waterSkipColor = vec3(54.0/255.0, 135.0/255.0, 130.0/255.0);\n" +
        "        vec3  rawTexColor = texture2D(u_texture, v_texCoord).rgb;\n" +
        "        if (length(rawTexColor - waterSkipColor) < 0.15) {\n" +
        "            gl_FragColor = diffuse;\n" +
        "            return;\n" +
        "        }\n" +



        "        const float MAX_DEPTH = 25.0;\n" +
        "        vec3  shoreColor = vec3(79.0/255.0, 164.0/255.0, 184.0/255.0);\n" +
        "        vec3  deepColor  = vec3(79.0/300.0, 164.0/300.0, 184.0/300.0);\n" +
        "        float t = clamp(u_waterDepth / MAX_DEPTH, 0.0, 1.0);\n" +
        "        t = t * t;\n" +
        "        vec3 baseColor = mix(shoreColor, deepColor, t);\n" +

        "        vec2 worldPx = gl_FragCoord.xy - u_cameraOffset;\n" +

        "        vec2 cell = floor(worldPx / 4.0);\n" +

        "        float n1 = valueNoise(cell * 0.10 + vec2( u_time * 0.35,  u_time * 0.20));\n" +
        "        float n2 = valueNoise(cell * 0.07 + vec2(-u_time * 0.18,  u_time * 0.28));\n" +
        "        float ripple = n1 * 0.55 + n2 * 0.45;\n" +

        "        float depthFade = 1.0 - t * 0.75;\n" +
        "        float step      = 0.11 * depthFade;\n" +


        "        float bias = (t - 0.5) * 0.8;\n" +
        "        float lo   = 0.38 + bias;\n" +
        "        float hi   = 0.62 + bias;\n" +
        "        float brightness;\n" +
        "        if      (ripple < lo) brightness = -step;\n" +
        "        else if (ripple < hi) brightness =  0.0;\n" +
        "        else                  brightness =  step;\n" +

        "        diffuse.rgb = clamp(baseColor + brightness, 0.0, 1.0);\n" +
        "    }\n" +

        "    if (u_hasNormalMap == 0) { gl_FragColor = diffuse; return; }\n" +
        "    vec3 rawNormal = normalize(texture2D(u_normalMap, v_texCoord).rgb * 2.0 - 1.0);\n" +
        "    vec3 normal    = normalize(mix(vec3(0.0, 0.0, 1.0), rawNormal, u_normalStrength));\n" +

        "    vec3 lit = u_ambient * diffuse.rgb;\n" +

        "    float sunDot = max(dot(normal, u_sunDir), 0.0);\n" +
        "    lit += diffuse.rgb * u_sunColor.rgb * (u_sunColor.a * sunDot);\n" +

        "    vec3 lightAccum = vec3(0.0);\n" +
        "    for (int i = 0; i < MAX_LIGHTS; i++) {\n" +
        "        if (i < u_numLights) {\n" +
        "            vec2  toLight = u_lightPos[i].xy - gl_FragCoord.xy;\n" +
        "            float dist    = length(toLight);\n" +
        "            if (dist < u_lightRadius[i]) {\n" +
        "                float atten = 1.0 - dist / u_lightRadius[i];\n" +
        "                atten = atten * atten;\n" +
        "                vec3  lDir  = normalize(vec3(toLight, u_lightPos[i].z));\n" +
        "                float diff  = max(dot(normal, lDir), 0.0);\n" +
        "                lightAccum += diffuse.rgb * u_lightColor[i].rgb\n" +
        "                           * (u_lightColor[i].a * diff * atten);\n" +
        "            }\n" +
        "        }\n" +
        "    }\n" +

        "    lightAccum = lightAccum / (lightAccum + vec3(0.5));\n" +
        "    lit += lightAccum;\n" +
        "    lit = min(lit, diffuse.rgb);\n" +

        "    gl_FragColor = vec4(lit, diffuse.a);\n" +
        "}\n";

    public final ShaderProgram program;

    private float elapsedTime = 0f;

    public NormalMapShader() {
        ShaderProgram.pedantic = false;
        program = new ShaderProgram(VERT, FRAG);
        if (!program.isCompiled())
            Gdx.app.error("NormalMapShader", program.getLog());
    }


    public void update(float delta) {
        elapsedTime += delta;
    }

    public void syncLights(LightRenderer lights, Camera cam, Color skyAmbient) {
        program.bind();

        program.setUniformf("u_time", elapsedTime);

        program.setUniformf("u_cameraOffset", cam.cameraX, cam.cameraY);
        program.setUniformi("u_isItem",       0);
        program.setUniformf("u_normalStrength", 0.8f);

        program.setUniformf("u_ambient",  skyAmbient.r, skyAmbient.g, skyAmbient.b);
        program.setUniformf("u_sunDir",   0f, 1f, 0f);
        program.setUniformf("u_sunColor", 1f, 1f, 1f, 0.7f);

        var srcs = lights.getLightSources();
        int n = Math.min(srcs.size(), MAX_LIGHTS);
        program.setUniformi("u_numLights", n);

        float lightZ = 80f;
        for (int i = 0; i < n; i++) {
            LightSource s = srcs.get(i);
            float sx = s.getWorldPixelX() + cam.cameraX;
            float sy = s.getWorldPixelY() + cam.cameraY;
            Color c  = s.getColor();
            program.setUniformf("u_lightPos["    + i + "]", sx, sy, lightZ);
            program.setUniformf("u_lightColor["  + i + "]", c.r, c.g, c.b, c.a);
            program.setUniformf("u_lightRadius[" + i + "]", s.getRadius());
        }
    }

    public void dispose() { program.dispose(); }
}