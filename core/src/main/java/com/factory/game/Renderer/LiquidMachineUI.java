package com.factory.game.Renderer;

import java.util.Collection;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.factory.game.Camera;
import com.factory.game.Main;
import com.factory.game.World.Chunk;
import com.factory.game.World.LiquidTank;
import com.factory.game.World.LiquidType;
import com.factory.game.World.ObjectSpriteCache;
import com.factory.game.World.PlacedObject;
import com.factory.game.World.PlacedObjectCache;

public class LiquidMachineUI {

    private static final float BAR_W        = 44f;
    private static final float BAR_H        = 6f;
    private static final float ABOVE_SPRITE = 6f;

    private static final Color BAR_BG     = new Color(0.45f, 0.45f, 0.45f, 0.90f);
    private static final Color ROLE_OUT   = new Color(0.30f, 0.88f, 0.52f, 1.00f);
    private static final Color ROLE_IN    = new Color(0.38f, 0.65f, 1.00f, 1.00f);
    private static final Color ROLE_STORE = new Color(0.78f, 0.62f, 0.28f, 1.00f);
    private static final Color TEXT_DIM   = new Color(0.55f, 0.55f, 0.62f, 1.00f);

    private final BitmapFont    font;
    private final GlyphLayout   layout;
    private final TextureRegion pixel;

    private boolean anyHovered = false;

    public LiquidMachineUI(BitmapFont font) {
        this.font   = font;
        this.layout = new GlyphLayout();
        this.pixel  = ObjectSpriteCache.whitePixel;
    }

    public boolean isAnyHovered() { return anyHovered; }

    public void render(SpriteBatch batch, Collection<Chunk> chunks, Camera camera) {
        anyHovered = false;

        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

        for (Chunk chunk : chunks) {
            for (PlacedObject obj : chunk.getPlacedObjects()) {
                if (!obj.isLiquidMachine() || obj.isPipe()) continue;
                LiquidTank tank = obj.getLiquidTank();
                if (tank == null) continue;

                PlacedObjectCache.SpriteConfig cfg = PlacedObjectCache.getConfig(obj.type);
                float screenX    = obj.getX() * Main.TILE_SCALE + camera.cameraX;
                float screenY    = obj.getY() * Main.TILE_SCALE + camera.cameraY;
                float spriteW    = (cfg != null ? cfg.drawWidth  : 16f) / 16f * Main.TILE_SCALE;
                float spriteH    = (cfg != null ? cfg.drawHeight : 16f) / 16f * Main.TILE_SCALE;
                float spriteOffX = (cfg != null ? cfg.offsetX    : 0f)  / 16f * Main.TILE_SCALE;
                float spriteOffY = (cfg != null ? cfg.offsetY    : 0f)  / 16f * Main.TILE_SCALE;

                boolean hovered = mouseX >= screenX + spriteOffX
                               && mouseX <= screenX + spriteOffX + spriteW
                               && mouseY >= screenY + spriteOffY
                               && mouseY <= screenY + spriteOffY + spriteH;

                if (!hovered) continue;
                anyHovered = true;

                float barX = screenX + (Main.TILE_SCALE - BAR_W) * 0.5f;
                float barY = screenY + spriteOffY + spriteH + ABOVE_SPRITE;

                batch.setColor(BAR_BG);
                batch.draw(pixel, barX, barY, BAR_W, BAR_H);

                LiquidType liquid = tank.getType();
                float fill = tank.getFillRatio();
                if (liquid != null && fill > 0f) {
                    batch.setColor(liquid.color);
                    batch.draw(pixel, barX, barY, BAR_W * fill, BAR_H);
                }

                batch.setColor(Color.WHITE);

                String label = roleLabel(tank.getRole());
                Color  color = roleColor(tank.getRole());
                font.getData().setScale(0.55f);
                font.setColor(color);
                layout.setText(font, label);
                font.draw(batch, label,
                        barX + (BAR_W - layout.width) * 0.5f,
                        barY + BAR_H + layout.height + 2f);
            }
        }

        font.getData().setScale(1f);
        font.setColor(Color.WHITE);
    }

    private static String roleLabel(LiquidTank.Role role) {
        switch (role) {
            case PRODUCER: return "OUT";
            case RECEIVER: return "IN";
            case STORAGE:  return "STORAGE";
            default:       return "???";
        }
    }

    private static Color roleColor(LiquidTank.Role role) {
        switch (role) {
            case PRODUCER: return ROLE_OUT;
            case RECEIVER: return ROLE_IN;
            case STORAGE:  return ROLE_STORE;
            default:       return TEXT_DIM;
        }
    }
}