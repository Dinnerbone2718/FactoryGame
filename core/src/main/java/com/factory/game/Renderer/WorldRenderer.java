package com.factory.game.Renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.factory.game.Bubble;
import com.factory.game.Camera;
import com.factory.game.Items.DroppedItem;
import com.factory.game.Main;
import com.factory.game.PenguinDude;
import com.factory.game.Player;
import com.factory.game.PooDude;
import com.factory.game.World.Animal;
import com.factory.game.World.Chunk;
import com.factory.game.World.ObjectSpriteCache;
import com.factory.game.World.PlacedObject;
import com.factory.game.World.WorldObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class WorldRenderer {

    private final SpriteBatch batch;
    private final BitmapFont font;
    private final LightRenderer lightRenderer;
    private final NormalMapShader normalShader;
    private final Texture flatNormal;
    private final LiquidMachineUI liquidUI;
    private final FootprintManager footprintManager;
    private float totalTime = 0f;

    private final Color naturalLight = new Color();
    private final Color shaderSkyAmbient = new Color();

    public WorldRenderer() {
        batch = new SpriteBatch();
        normalShader = new NormalMapShader();
        batch.setShader(normalShader.program);

        font = new BitmapFont();
        font.setColor(Color.WHITE);

        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        lightRenderer = new LightRenderer(w, h);

        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGB888);
        px.setColor(0.5f, 0.5f, 1f, 1f);
        px.fill();
        flatNormal = new Texture(px);
        px.dispose();

        liquidUI = new LiquidMachineUI(font);
        footprintManager = new FootprintManager();
    }

    public LightRenderer getLightRenderer() {
        return lightRenderer;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public FootprintManager getFootprintManager() {
        return footprintManager;
    }

    public boolean isLiquidMachineHovered() {
        return liquidUI.isAnyHovered();
    }

    public void applyDayNight(DayNightCycle cycle) {
        naturalLight.set(cycle.getNaturalLight());
        shaderSkyAmbient.set(cycle.getShaderSkyAmbient());
        lightRenderer.setSunAmbient(naturalLight);
    }

    public void render(
        Collection<Chunk> chunks,
        Camera camera,
        Player player,
        List<DroppedItem> droppedItems,
        float delta,
        PenguinDude penguinDude,
        PooDude pooDude,
        List<Animal> animals,
        List<Bubble> bubbles
    ) {
        totalTime += delta;

        lightRenderer.setSunAmbient(naturalLight);
        footprintManager.update(delta);

        normalShader.update(Gdx.graphics.getDeltaTime());
        normalShader.syncLights(lightRenderer, camera, shaderSkyAmbient);

        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
        flatNormal.bind(1);
        normalShader.program.setUniformi("u_normalMap", 1);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        batch.begin();

        List<Chunk> chunkList = new ArrayList<>(chunks);
        Collections.sort(chunkList, Collections.reverseOrder());
        for (Chunk chunk : chunkList) chunk.drawTiles(batch, camera);

        footprintManager.draw(batch, camera);

        List<WorldObject> allObjects = new ArrayList<>();
        for (Chunk chunk : chunkList)
            allObjects.addAll(chunk.getVisibleObjects(camera));
        allObjects.sort((a, b) -> Integer.compare(b.getY(), a.getY()));

        List<WorldObject> layer1Objects = new ArrayList<>();
        List<WorldObject> layer2Objects = new ArrayList<>();
        for (WorldObject obj : allObjects) {
            if (obj.hasShadowBody()) layer2Objects.add(obj);
            else layer1Objects.add(obj);
        }

        for (WorldObject obj : layer1Objects) obj.draw(batch, camera);

        List<PlacedObject> allPlaced = new ArrayList<>();
        for (Chunk chunk : chunkList)
            allPlaced.addAll(chunk.getVisiblePlacedObjects(camera));

        List<PlacedObject> floorObjects = new ArrayList<>();
        List<PlacedObject> placedLayer1 = new ArrayList<>();
        List<PlacedObject> placedLayer2 = new ArrayList<>();
        for (PlacedObject obj : allPlaced) {
            if (obj.isFloor()) floorObjects.add(obj);
            else if (obj.isSolid() || obj.hasShadowBody()) placedLayer2.add(
                obj
            );
            else placedLayer1.add(obj);
        }

        for (PlacedObject obj : floorObjects) obj.draw(batch, camera);
        for (PlacedObject obj : placedLayer1) obj.draw(batch, camera);

        List<Object> sortedEntities = new ArrayList<>();
        sortedEntities.addAll(layer2Objects);
        sortedEntities.addAll(placedLayer2);
        sortedEntities.addAll(droppedItems);
        sortedEntities.addAll(animals);
        sortedEntities.addAll(bubbles);
        sortedEntities.add(penguinDude);
        sortedEntities.add(pooDude);
        sortedEntities.add(player);

        sortedEntities.sort((a, b) ->
            Float.compare(getRenderY(b), getRenderY(a))
        );

        for (Object entity : sortedEntities) {
            if (entity instanceof WorldObject) ((WorldObject) entity).draw(
                batch,
                camera
            );
            else if (entity instanceof PlacedObject) (
                (PlacedObject) entity
            ).draw(batch, camera);
            else if (entity instanceof DroppedItem) ((DroppedItem) entity).draw(
                batch,
                camera
            );
            else if (entity instanceof PenguinDude) ((PenguinDude) entity).draw(
                batch,
                camera
            );
            else if (entity instanceof PooDude) ((PooDude) entity).draw(
                batch,
                camera
            );
            else if (entity instanceof Animal) ((Animal) entity).draw(
                batch,
                camera
            );
            else if (entity instanceof Bubble) ((Bubble) entity).draw(
                batch,
                camera
            );
            else if (entity instanceof Player) ((Player) entity).draw(
                batch,
                camera
            );
        }

        liquidUI.render(batch, chunks, camera);

        font.draw(
            batch,
            String.format(
                "FPS: %d   Chunks: %d",
                (int) (1f / delta),
                chunks.size()
            ),
            10,
            Gdx.graphics.getHeight() - 10
        );

        batch.end();
    }

    private static float getRenderY(Object entity) {
        if (entity instanceof WorldObject) return (
            ((WorldObject) entity).getY() * Main.TILE_SCALE
        );
        if (entity instanceof PlacedObject) return (
            (PlacedObject) entity
        ).getRenderY();
        if (entity instanceof DroppedItem) return (
            (DroppedItem) entity
        ).getRenderY();
        if (entity instanceof PenguinDude) return (
            (PenguinDude) entity
        ).getWorldY();
        if (entity instanceof PooDude) return ((PooDude) entity).getWorldY();

        if (entity instanceof Animal) return ((Animal) entity).getRenderY();
        if (entity instanceof Bubble) return ((Bubble) entity).getWorldY();
        if (entity instanceof Player) return ((Player) entity).getRenderY();
        return 0f;
    }

    public TextureRegion getWhitePixel() {
        return ObjectSpriteCache.whitePixel;
    }

    public void resize(int width, int height) {
        batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        lightRenderer.resize(width, height);
    }

    public void dispose() {
        batch.dispose();
        font.dispose();
        lightRenderer.dispose();
        normalShader.dispose();
        flatNormal.dispose();
    }
}
