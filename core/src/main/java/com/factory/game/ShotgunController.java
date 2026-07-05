package com.factory.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.factory.game.Items.Inventory;
import com.factory.game.Items.InventoryUI;
import com.factory.game.Items.Item;
import com.factory.game.Items.ItemStack;
import com.factory.game.World.Animal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ShotgunController {

    public static float DAMAGE = 14f;

    public static final int PELLET_COUNT = 8;

    public static final int MAG_SIZE = 2;

    private static final float FIRE_COOLDOWN = 0.85f;
    private static final float FIRE_ANIMATION_DURATION = 0.22f;
    private static final float RANGE_TILES = 7f;
    private static final float HIT_TOLERANCE_TILES = 0.6f;
    private static final float SPREAD_DEGREES = 16f;

    private static final float BULLET_SIZE_TILES = 1f / 16f;
    private static final float BULLET_SPEED_TILES_PER_SEC = 55f;
    private static final float BULLET_STEP_TILES = 1f / 16f;

    private static final float RELOAD_CHECK_WINDOW = 0.9f;
    private static final float RELOAD_ZONE_WIDTH = 0.16f;
    private static final float RELOAD_PENALTY_TIME = 2.2f;

    private static final float MUZZLE_FLASH_DURATION = 0.08f;
    private static final float HIT_MARKER_DURATION = 0.25f;
    private static final float RECOIL_DURATION = 0.16f;

    private enum ReloadPhase {
        NONE,
        CHECKING,
        PENALTY,
    }

    private static final class Bullet {

        float x, y;
        float prevX, prevY;
        final float dirX, dirY;
        final float damage;
        final float maxRange;
        float traveled = 0f;

        Bullet(
            float x,
            float y,
            float dirX,
            float dirY,
            float damage,
            float maxRange
        ) {
            this.x = x;
            this.y = y;
            this.prevX = x;
            this.prevY = y;
            this.dirX = dirX;
            this.dirY = dirY;
            this.damage = damage;
            this.maxRange = maxRange;
        }
    }

    private final List<Bullet> bullets = new ArrayList<>();

    private final Player player;
    private final Camera camera;
    private final WorldManager world;
    private final InventoryUI inventoryUI;

    private int ammoInMag = MAG_SIZE;

    private ReloadPhase reloadPhase = ReloadPhase.NONE;
    private float reloadElapsed = 0f;
    private float reloadPenaltyTimer = 0f;
    private float reloadZoneStart = 0.4f;
    private boolean reloadPerfectResult = false;
    private float reloadResultFlashTimer = 0f;

    private float fireCooldownTimer = 0f;
    private float muzzleFlashTimer = 0f;
    private float hitMarkerTimer = 0f;
    private float recoilTimer = 0f;

    private float hitMarkerScreenX, hitMarkerScreenY;
    private boolean aimingAtTarget = false;

    private boolean cursorIsCustom = false;
    private Cursor invisibleCursor;

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout();

    private static final int SHOTGUN_SHEET_COLS = 2;
    private static final int SHOTGUN_SHEET_ROWS = 2;
    private static final float SHOTGUN_GRIP_FRACTION = 0.30f;
    private static final float SHOTGUN_VERTICAL_OFFSET_TILES = -0.02f;
    private static final float SHOTGUN_HORIZONTAL_OFFSET_TILES = -0.1f;

    private Texture shotgunTexture;
    private TextureRegion[] shotgunFrames;

    public ShotgunController(
        Player player,
        Camera camera,
        WorldManager world,
        InventoryUI inventoryUI
    ) {
        this.player = player;
        this.camera = camera;
        this.world = world;
        this.inventoryUI = inventoryUI;

        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(
            Gdx.files.internal("JetBrainsMono-Regular.ttf")
        );
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size = 16;
        param.color = Color.WHITE;
        font = gen.generateFont(param);
        gen.dispose();

        Pixmap invisPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        invisPixmap.setColor(0f, 0f, 0f, 0f);
        invisPixmap.fill();
        invisibleCursor = Gdx.graphics.newCursor(invisPixmap, 0, 0);
        invisPixmap.dispose();

        loadShotgunTexture();
    }

    private void loadShotgunTexture() {
        shotgunTexture = new Texture("spritesheets/shotgun.png");
        int fw = shotgunTexture.getWidth() / SHOTGUN_SHEET_COLS;
        int fh = shotgunTexture.getHeight() / SHOTGUN_SHEET_ROWS;
        shotgunFrames = new TextureRegion[SHOTGUN_SHEET_COLS *
        SHOTGUN_SHEET_ROWS];
        for (int row = 0; row < SHOTGUN_SHEET_ROWS; row++) {
            for (int col = 0; col < SHOTGUN_SHEET_COLS; col++) {
                shotgunFrames[row * SHOTGUN_SHEET_COLS + col] =
                    new TextureRegion(
                        shotgunTexture,
                        col * fw,
                        row * fh,
                        fw,
                        fh
                    );
            }
        }
    }

    public void update(float delta, boolean uiBlocking) {
        updateBullets(delta);
        tickTimers(delta);

        boolean holding = isHoldingShotgun();

        if (!holding || uiBlocking) {
            restoreCursor();
            aimingAtTarget = false;
            return;
        }

        applyReticleCursor();
        updateAimAssist();

        if (reloadPhase == ReloadPhase.CHECKING) {
            reloadElapsed += delta;
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                float pos = reloadElapsed / RELOAD_CHECK_WINDOW;
                boolean perfect =
                    pos >= reloadZoneStart &&
                    pos <= reloadZoneStart + RELOAD_ZONE_WIDTH;
                if (perfect) {
                    finishReload(true);
                } else {
                    beginReloadPenalty();
                }
            } else if (reloadElapsed >= RELOAD_CHECK_WINDOW) {
                beginReloadPenalty();
            }
            return;
        }

        if (reloadPhase == ReloadPhase.PENALTY) {
            reloadPenaltyTimer -= delta;
            if (reloadPenaltyTimer <= 0f) {
                finishReload(false);
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            startReload();
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            fire();
        }
    }

    private void tickTimers(float delta) {
        if (fireCooldownTimer > 0f) {
            fireCooldownTimer = Math.max(0f, fireCooldownTimer - delta);
        }
        if (muzzleFlashTimer > 0f) {
            muzzleFlashTimer = Math.max(0f, muzzleFlashTimer - delta);
        }
        if (hitMarkerTimer > 0f) {
            hitMarkerTimer = Math.max(0f, hitMarkerTimer - delta);
        }
        if (recoilTimer > 0f) {
            recoilTimer = Math.max(0f, recoilTimer - delta);
        }
        if (reloadResultFlashTimer > 0f) {
            reloadResultFlashTimer = Math.max(
                0f,
                reloadResultFlashTimer - delta
            );
        }
    }

    private void fire() {
        if (fireCooldownTimer > 0f) return;

        if (ammoInMag <= 0) {
            startReload();
            return;
        }

        ammoInMag--;
        fireCooldownTimer = FIRE_COOLDOWN;
        muzzleFlashTimer = MUZZLE_FLASH_DURATION;
        recoilTimer = RECOIL_DURATION;

        spawnPellets();

        if (ammoInMag <= 0) {
            startReload();
        }
    }

    private void spawnPellets() {
        float originX = player.getWorldX() + Main.TILE_SCALE * 0.5f;
        float originY = player.getWorldY() + Main.TILE_SCALE * 0.5f;

        float[] baseDir = mouseDirectionFrom(originX, originY);
        if (baseDir == null) return;

        float baseAngle = (float) Math.toDegrees(
            Math.atan2(baseDir[1], baseDir[0])
        );

        for (int i = 0; i < PELLET_COUNT; i++) {
            float angle =
                baseAngle +
                MathUtils.random(-SPREAD_DEGREES * 0.5f, SPREAD_DEGREES * 0.5f);
            float rad = (float) Math.toRadians(angle);
            float dirX = (float) Math.cos(rad);
            float dirY = (float) Math.sin(rad);

            float muzzleX = originX + dirX * Main.TILE_SCALE * 0.35f;
            float muzzleY = originY + dirY * Main.TILE_SCALE * 0.35f;

            bullets.add(
                new Bullet(
                    muzzleX,
                    muzzleY,
                    dirX,
                    dirY,
                    DAMAGE,
                    RANGE_TILES * Main.TILE_SCALE
                )
            );
        }
    }

    private void updateBullets(float delta) {
        if (bullets.isEmpty()) return;

        float bulletSize = Main.TILE_SCALE * BULLET_SIZE_TILES;
        float half = bulletSize * 0.5f;
        float speed = BULLET_SPEED_TILES_PER_SEC * Main.TILE_SCALE;
        float step = Main.TILE_SCALE * BULLET_STEP_TILES;

        Iterator<Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            Bullet b = it.next();
            float moveDist = speed * delta;
            b.prevX = b.x;
            b.prevY = b.y;

            while (moveDist > 0f) {
                float s = Math.min(step, moveDist);
                float nx = b.x + b.dirX * s;
                float ny = b.y + b.dirY * s;

                Animal hitAnimal = findAnimalNear(nx, ny);
                if (hitAnimal != null) {
                    hitAnimal.takeDamage(b.damage);
                    hitMarkerScreenX = nx + camera.cameraX;
                    hitMarkerScreenY = ny + camera.cameraY;
                    hitMarkerTimer = HIT_MARKER_DURATION;
                    it.remove();
                    moveDist = -1f;
                    break;
                }

                if (
                    world.isBlockedAt(
                        nx - half,
                        ny - half,
                        nx + half,
                        ny + half
                    )
                ) {
                    it.remove();
                    moveDist = -1f;
                    break;
                }

                b.x = nx;
                b.y = ny;
                b.traveled += s;
                moveDist -= s;

                if (b.traveled >= b.maxRange) {
                    it.remove();
                    moveDist = -1f;
                    break;
                }
            }
        }
    }

    private Animal findAnimalNear(float x, float y) {
        float tolerance = HIT_TOLERANCE_TILES * Main.TILE_SCALE;
        for (Animal a : world.getAnimals()) {
            if (a.isDead()) continue;
            float dx = a.getColliderCenterX() - x;
            float dy = a.getColliderCenterY() - y;
            if (dx * dx + dy * dy <= tolerance * tolerance) return a;
        }
        return null;
    }

    private void updateAimAssist() {
        float originX = player.getWorldX() + Main.TILE_SCALE * 0.5f;
        float originY = player.getWorldY() + Main.TILE_SCALE * 0.5f;

        float[] dir = mouseDirectionFrom(originX, originY);
        if (dir == null) {
            aimingAtTarget = false;
            return;
        }
        float dirX = dir[0];
        float dirY = dir[1];

        float range = RANGE_TILES * Main.TILE_SCALE;
        float tolerance = HIT_TOLERANCE_TILES * Main.TILE_SCALE;

        for (Animal a : world.getAnimals()) {
            if (a.isDead()) continue;
            float ax = a.getColliderCenterX() - originX;
            float ay = a.getColliderCenterY() - originY;
            float proj = ax * dirX + ay * dirY;
            if (proj < 0f || proj > range) continue;
            float perpX = ax - dirX * proj;
            float perpY = ay - dirY * proj;
            float perpDist = (float) Math.sqrt(perpX * perpX + perpY * perpY);
            if (perpDist <= tolerance) {
                aimingAtTarget = true;
                return;
            }
        }
        aimingAtTarget = false;
    }

    private float[] mouseDirectionFrom(float originX, float originY) {
        float mouseScreenX = Gdx.input.getX();
        float mouseScreenY = Gdx.graphics.getHeight() - Gdx.input.getY();
        float mouseWorldX = mouseScreenX - camera.cameraX;
        float mouseWorldY = mouseScreenY - camera.cameraY;

        float dx = mouseWorldX - originX;
        float dy = mouseWorldY - originY;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.0001f) return null;
        return new float[] { dx / len, dy / len };
    }

    private void startReload() {
        if (reloadPhase != ReloadPhase.NONE) return;
        if (ammoInMag >= MAG_SIZE) return;
        if (!player.getInventory().hasItem(Item.SHOTGUN_AMMO, 1)) return;

        reloadPhase = ReloadPhase.CHECKING;
        reloadElapsed = 0f;
        reloadZoneStart = MathUtils.random(
            0.25f,
            1f - RELOAD_ZONE_WIDTH - 0.1f
        );
    }

    private void beginReloadPenalty() {
        reloadPhase = ReloadPhase.PENALTY;
        reloadPenaltyTimer = RELOAD_PENALTY_TIME;
    }

    private void finishReload(boolean perfect) {
        int needed = MAG_SIZE - ammoInMag;
        if (needed > 0) {
            int reserve = countReserveAmmo();
            int take = Math.min(needed, reserve);
            if (take > 0) {
                player.getInventory().removeItem(Item.SHOTGUN_AMMO, take);
                ammoInMag += take;
            }
        }

        reloadPhase = ReloadPhase.NONE;
        reloadElapsed = 0f;
        reloadPenaltyTimer = 0f;
        reloadPerfectResult = perfect;
        reloadResultFlashTimer = 0.8f;
    }

    private int countReserveAmmo() {
        Inventory inv = player.getInventory();
        if (!inv.hasItem(Item.SHOTGUN_AMMO, 1)) return 0;

        int lo = 1;
        int hi = 1;
        while (hi < 100000 && inv.hasItem(Item.SHOTGUN_AMMO, hi)) {
            lo = hi;
            hi *= 2;
        }
        while (lo < hi) {
            int mid = (lo + hi + 1) / 2;
            if (inv.hasItem(Item.SHOTGUN_AMMO, mid)) lo = mid;
            else hi = mid - 1;
        }
        return lo;
    }

    public boolean isHoldingShotgun() {
        int sel = inventoryUI.getSelectedSlot();
        if (sel < 0) return false;
        ItemStack stack = player.getInventory().getSlot(sel);
        return stack != null && stack.getItem() == Item.SHOTGUN;
    }

    public boolean isReloading() {
        return reloadPhase != ReloadPhase.NONE;
    }

    public boolean isPlayerLookingUp() {
        return player.getFacingRow() == 1;
    }

    public boolean shouldDrawShotgunBehindPlayer() {
        return isHoldingShotgun() && isPlayerLookingUp();
    }

    private int currentShotgunFrameIndex() {
        if (fireCooldownTimer <= 0f) return 0;
        float elapsed = FIRE_COOLDOWN - fireCooldownTimer;
        if (elapsed >= FIRE_ANIMATION_DURATION) return 0;
        float progress = elapsed / FIRE_ANIMATION_DURATION;
        int idx = (int) (progress * (SHOTGUN_SHEET_COLS * SHOTGUN_SHEET_ROWS));
        return MathUtils.clamp(
            idx,
            0,
            SHOTGUN_SHEET_COLS * SHOTGUN_SHEET_ROWS - 1
        );
    }

    public void drawShotgun(SpriteBatch batch) {
        if (!isHoldingShotgun() || shotgunFrames == null) return;

        float originX = player.getWorldX() + Main.TILE_SCALE * 0.5f;
        float originY = player.getWorldY() + Main.TILE_SCALE * 0.5f;

        float[] dir = mouseDirectionFrom(originX, originY);

        float renderAngle;
        boolean flip;
        if (dir == null) {
            renderAngle = 0f;
            flip = false;
        } else {
            float rawAngle = (float) Math.toDegrees(Math.atan2(dir[1], dir[0]));
            flip = dir[0] < 0f;
            renderAngle = flip ? rawAngle - 180f : rawAngle;
        }

        TextureRegion frame = shotgunFrames[currentShotgunFrameIndex()];
        if (frame.isFlipX() != flip) {
            frame.flip(true, false);
        }

        float width = Main.TILE_SCALE;
        float height = Main.TILE_SCALE;

        float pivotX =
            originX +
            camera.cameraX +
            Main.TILE_SCALE *
            SHOTGUN_HORIZONTAL_OFFSET_TILES *
            (flip ? -1f : 1f);
        float pivotY =
            originY +
            camera.cameraY +
            Main.TILE_SCALE * SHOTGUN_VERTICAL_OFFSET_TILES;

        float originOffsetX = flip
            ? width * (1f - SHOTGUN_GRIP_FRACTION)
            : width * SHOTGUN_GRIP_FRACTION;
        float drawX = pivotX - originOffsetX;
        float drawY = pivotY - height * 0.5f;

        batch.draw(
            frame,
            drawX,
            drawY,
            originOffsetX,
            height * 0.5f,
            width,
            height,
            1f,
            1f,
            renderAngle
        );
    }

    public int getAmmoInMag() {
        return ammoInMag;
    }

    public void render(SpriteBatch batch) {
        boolean holding = isHoldingShotgun();
        if (!holding && bullets.isEmpty() && hitMarkerTimer <= 0f) return;

        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        drawBullets();

        if (holding) {
            drawReticle();
            if (
                reloadPhase != ReloadPhase.NONE || reloadResultFlashTimer > 0f
            ) {
                drawReloadBar();
            }
        }
        if (hitMarkerTimer > 0f) {
            drawHitMarker();
        }

        batch.begin();
        if (holding) {
            drawAmmoHud(batch);
        }
    }

    private void drawBullets() {
        if (bullets.isEmpty()) return;

        float bulletSize = Main.TILE_SCALE * BULLET_SIZE_TILES;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 0.92f, 0.55f, 0.7f);
        for (Bullet b : bullets) {
            shapeRenderer.line(
                b.prevX + camera.cameraX,
                b.prevY + camera.cameraY,
                b.x + camera.cameraX,
                b.y + camera.cameraY
            );
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f, 0.95f, 0.75f, 1f);
        for (Bullet b : bullets) {
            shapeRenderer.rect(
                b.x + camera.cameraX - bulletSize * 0.5f,
                b.y + camera.cameraY - bulletSize * 0.5f,
                bulletSize,
                bulletSize
            );
        }
        shapeRenderer.end();
    }

    private void drawReticle() {
        float mouseScreenX = Gdx.input.getX();
        float mouseScreenY = Gdx.graphics.getHeight() - Gdx.input.getY();

        float recoilKick =
            recoilTimer > 0f ? (recoilTimer / RECOIL_DURATION) * 5f : 0f;

        float outerR = 12f - recoilKick * 0.5f;
        float innerGap = 5f;
        float armLen = 6f;

        Color color = aimingAtTarget
            ? new Color(1f, 0.25f, 0.25f, 0.95f)
            : new Color(1f, 1f, 1f, 0.85f);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(color);
        shapeRenderer.circle(mouseScreenX, mouseScreenY, outerR, 24);

        shapeRenderer.line(
            mouseScreenX - innerGap - armLen,
            mouseScreenY,
            mouseScreenX - innerGap,
            mouseScreenY
        );
        shapeRenderer.line(
            mouseScreenX + innerGap,
            mouseScreenY,
            mouseScreenX + innerGap + armLen,
            mouseScreenY
        );
        shapeRenderer.line(
            mouseScreenX,
            mouseScreenY - innerGap - armLen,
            mouseScreenX,
            mouseScreenY - innerGap
        );
        shapeRenderer.line(
            mouseScreenX,
            mouseScreenY + innerGap,
            mouseScreenX,
            mouseScreenY + innerGap + armLen
        );
        shapeRenderer.end();

        if (muzzleFlashTimer > 0f) {
            float t = muzzleFlashTimer / MUZZLE_FLASH_DURATION;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1f, 0.85f, 0.3f, t);
            shapeRenderer.circle(mouseScreenX, mouseScreenY, 4f + 4f * t, 12);
            shapeRenderer.end();
        }
    }

    private void drawHitMarker() {
        float t = hitMarkerTimer / HIT_MARKER_DURATION;
        float size = 8f * t + 3f;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 0.3f, 0.2f, t);
        shapeRenderer.line(
            hitMarkerScreenX - size,
            hitMarkerScreenY - size,
            hitMarkerScreenX + size,
            hitMarkerScreenY + size
        );
        shapeRenderer.line(
            hitMarkerScreenX - size,
            hitMarkerScreenY + size,
            hitMarkerScreenX + size,
            hitMarkerScreenY - size
        );
        shapeRenderer.end();
    }

    private void drawReloadBar() {
        float barW = 90f;
        float barH = 10f;
        float centerX =
            player.getWorldX() + camera.cameraX + Main.TILE_SCALE * 0.5f;
        float barY =
            player.getWorldY() + camera.cameraY + Main.TILE_SCALE * 1.6f;
        float barX = centerX - barW * 0.5f;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        if (reloadPhase == ReloadPhase.CHECKING) {
            shapeRenderer.setColor(0.55f, 0.15f, 0.12f, 0.92f);
            shapeRenderer.rect(barX, barY, barW, barH);

            shapeRenderer.setColor(0.25f, 0.85f, 0.30f, 0.95f);
            shapeRenderer.rect(
                barX + reloadZoneStart * barW,
                barY,
                RELOAD_ZONE_WIDTH * barW,
                barH
            );
        } else if (reloadPhase == ReloadPhase.PENALTY) {
            float frac =
                1f -
                MathUtils.clamp(
                    reloadPenaltyTimer / RELOAD_PENALTY_TIME,
                    0f,
                    1f
                );
            shapeRenderer.setColor(0.20f, 0.10f, 0.08f, 0.92f);
            shapeRenderer.rect(barX, barY, barW, barH);
            shapeRenderer.setColor(0.85f, 0.45f, 0.15f, 0.95f);
            shapeRenderer.rect(barX, barY, barW * frac, barH);
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0f, 0f, 0f, 0.9f);
        shapeRenderer.rect(barX, barY, barW, barH);
        shapeRenderer.end();

        if (reloadPhase == ReloadPhase.CHECKING) {
            float pos = MathUtils.clamp(
                reloadElapsed / RELOAD_CHECK_WINDOW,
                0f,
                1f
            );
            float indicatorX = barX + pos * barW;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1f, 1f, 1f, 1f);
            shapeRenderer.rect(indicatorX - 1f, barY - 3f, 2f, barH + 6f);
            shapeRenderer.end();
        }
    }

    private void drawAmmoHud(SpriteBatch batch) {
        float mouseScreenX = Gdx.input.getX();
        float mouseScreenY = Gdx.graphics.getHeight() - Gdx.input.getY();

        String text;
        Color color;

        if (reloadPhase == ReloadPhase.CHECKING) {
            text = "[SPACE] on green!";
            color = new Color(0.95f, 0.90f, 0.35f, 1f);
        } else if (reloadPhase == ReloadPhase.PENALTY) {
            text = "Reloading...";
            color = new Color(0.90f, 0.55f, 0.25f, 1f);
        } else if (reloadResultFlashTimer > 0f) {
            text = reloadPerfectResult ? "Quick reload!" : "Reloaded";
            color = reloadPerfectResult
                ? new Color(0.35f, 0.95f, 0.40f, 1f)
                : new Color(0.85f, 0.85f, 0.85f, 1f);
        } else {
            text = ammoInMag + " / " + MAG_SIZE;
            color =
                ammoInMag == 0
                    ? new Color(0.95f, 0.35f, 0.30f, 1f)
                    : Color.WHITE;
        }

        font.getData().setScale(1.0f);
        font.setColor(color);
        layout.setText(font, text);
        font.draw(
            batch,
            layout,
            mouseScreenX - layout.width * 0.5f,
            mouseScreenY - 20f
        );
        font.setColor(Color.WHITE);
    }

    private void applyReticleCursor() {
        if (cursorIsCustom) return;
        Gdx.graphics.setCursor(invisibleCursor);
        cursorIsCustom = true;
    }

    private void restoreCursor() {
        if (!cursorIsCustom) return;
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        cursorIsCustom = false;
    }

    public void resize(int width, int height) {
        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
    }

    public void dispose() {
        restoreCursor();
        shapeRenderer.dispose();
        font.dispose();
        if (invisibleCursor != null) {
            invisibleCursor.dispose();
            invisibleCursor = null;
        }
        if (shotgunTexture != null) {
            shotgunTexture.dispose();
            shotgunTexture = null;
        }
    }
}
