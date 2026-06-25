package com.factory.game.World;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.factory.game.Items.CraftingManager;
import com.factory.game.Items.GoblinoBarterRecipe;
import com.factory.game.Items.GoblinoGiftRecipe;
import com.factory.game.Items.GoblinoRecipe;
import com.factory.game.Items.Inventory;
import com.factory.game.Items.Item;


public class GoblinoHutUI {

    private static final int PANEL_W    = 480;
    private static final int PAD        = 20;
    private static final int IMG_SIZE   = 80;

    private static final int HEADER_H   = 150;
    private static final int TAB_BAR_H  = 38;
    private static final int FEED_H     = 44;
    private static final int TRADE_H    = 86;
    private static final int GIFT_ROW_H = 50;
    private static final int FOOTER_H   = 46;

    private static final float FEEDBACK_DURATION = 1.8f;


    private static final int NEGOTIATION_OVERLAY_H = 200;

    private static final int STEP_BTN_W       = 48;
    private static final int STEP_BTN_H       = 28;
    private static final int STEP_BTN_SPACING = 6;
    private static final int STEP_BTN_TOTAL_W =
            4 * STEP_BTN_W + 3 * STEP_BTN_SPACING;  

    private static final int ACTION_SUBMIT_W = 130;
    private static final int ACTION_CANCEL_W = 90;
    private static final int ACTION_H        = 34;
    private static final int ACTION_GAP      = 10;
    private static final int ACTION_TOTAL_W  =
            ACTION_SUBMIT_W + ACTION_CANCEL_W + ACTION_GAP;

    private enum Tab { TRADE, BARTER, GIFT }

    private boolean   visible    = false;
    private boolean   justOpened = false;
    private WorldObject hut      = null;
    private Inventory playerInv  = null;

    private List<GoblinoRecipe>       recipes       = null;
    private List<GoblinoBarterRecipe> barterRecipes = null;
    private List<GoblinoGiftRecipe>   giftRecipes   = null;

    private GoblinoMood mood      = null;
    private Tab         activeTab = Tab.TRADE;

    private final GoblinoHutManager manager;

    private int     hovered       = -1;
    private String  feedbackText  = null;
    private boolean feedbackGood  = false;
    private float   feedbackTimer = 0f;


    private boolean negotiationActive    = false;
    private int     negotiationRecipeIdx = -1;
    private int     negotiationOffer     = 1;
    private int     negotiationAskPrice  = 1;
    private int     negotiationOfferMin  = 1;
    private int     negotiationOfferMax  = 1;

    private final ShapeRenderer shape;
    private final BitmapFont    titleFont;
    private final BitmapFont    labelFont;
    private final BitmapFont    tinyFont;
    private final GlyphLayout   scratch = new GlyphLayout();

    private final Map<GoblinoMood, Texture> moodTextures = new EnumMap<>(GoblinoMood.class);

    private int panelX, panelY, panelH;
    private int sw, sh;

    private static final String[] TAB_LABELS = { "Coin Trade", "Barter", "Gift" };


    public GoblinoHutUI(GoblinoHutManager manager) {
        this.manager = manager;
        shape = new ShapeRenderer();

        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(
                Gdx.files.internal("JetBrainsMono-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter p =
                new FreeTypeFontGenerator.FreeTypeFontParameter();

        p.size = 26; p.color = Color.WHITE;
        titleFont = gen.generateFont(p);

        p.size = 18; p.color = Color.WHITE;
        labelFont = gen.generateFont(p);

        p.size = 13; p.color = Color.WHITE;
        tinyFont = gen.generateFont(p);

        gen.dispose();

        for (GoblinoMood m : GoblinoMood.values()) {
            Texture tex = new Texture(Gdx.files.internal(m.texturePath));
            tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            moodTextures.put(m, tex);
        }
    }



    public void open(WorldObject hut, Inventory playerInventory) {
        this.hut           = hut;
        this.playerInv     = playerInventory;
        this.recipes       = manager.getRecipes(hut);
        this.barterRecipes = manager.getBarterRecipes(hut);
        this.giftRecipes   = CraftingManager.getAllGoblinoGiftRecipes();
        this.mood          = manager.getMood(hut);
        this.visible       = true;
        this.justOpened    = true;
        this.activeTab     = Tab.TRADE;
        this.hovered       = -1;
        this.feedbackText  = null;
        this.feedbackTimer = 0f;
        closeNegotiationOverlay();
        relayout();
    }

    public void close() {
        visible   = false;
        hut       = null;
        mood      = null;
        hovered   = -1;
        activeTab = Tab.TRADE;
        closeNegotiationOverlay();
    }

    public boolean isVisible() { return visible; }

    public void resize(int w, int h) {
        sw = w; sh = h;
        shape.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
        if (visible) relayout();
    }

    public void update(float delta) {
        if (!visible) return;
        if (feedbackTimer > 0f) feedbackTimer = Math.max(0f, feedbackTimer - delta);
    }



    public void handleInput() {
        if (!visible || hut == null) return;
        if (justOpened) { justOpened = false; return; }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)
         || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (negotiationActive) closeNegotiationOverlay();
            else                   close();
            return;
        }

        if (negotiationActive) {
            handleNegotiationInput();
            return;
        }

        int mx = Gdx.input.getX();
        int my = sh - Gdx.input.getY();

        hovered = -1;
        int rowCount = getActiveRowCount();
        for (int i = 0; i < rowCount; i++) {
            int[] r = rowRect(i);
            if (hit(mx, my, r[0], r[1], r[2], r[3])) {
                hovered = i;
                break;
            }
        }

        boolean leftClick  = Gdx.input.justTouched();
        boolean rightClick = Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT);
        if (!leftClick && !rightClick) return;

        if (leftClick) {
            int   tabBarY   = panelY + panelH - HEADER_H - TAB_BAR_H;
            int   tabW      = PANEL_W / 3;
            Tab[] tabValues = Tab.values();
            for (int i = 0; i < tabValues.length; i++) {
                if (hit(mx, my, panelX + i * tabW, tabBarY, tabW, TAB_BAR_H)) {
                    if (activeTab != tabValues[i]) {
                        activeTab = tabValues[i];
                        hovered   = -1;
                        relayout();
                    }
                    return;
                }
            }
        }

        if (hovered >= 0) {
            if (activeTab == Tab.TRADE && rightClick) {
                openNegotiationOverlay(hovered);
            } else if (leftClick) {
                switch (activeTab) {
                    case TRADE:  handleTradeClick(hovered);  break;
                    case BARTER: handleBarterClick(hovered); break;
                    case GIFT:   handleGiftClick(hovered);   break;
                }
            }
            return;
        }

        if (leftClick && !hit(mx, my, panelX, panelY, PANEL_W, panelH)) {
            close();
        }
    }

    private void handleTradeClick(int idx) {
        if (recipes == null || idx >= recipes.size()) return;
        GoblinoRecipe recipe       = recipes.get(idx);
        int           effectiveCost = manager.getEffectiveCost(recipe, hut);
        int           coins        = playerInv.countItem(Item.GOBLINO_COIN);

        if (coins >= effectiveCost) {
            playerInv.removeItem(Item.GOBLINO_COIN, effectiveCost);
            playerInv.addItem(recipe.getOutputItem(), recipe.getOutputQuantity());
            setFeedback("Trade complete!", true);
        } else {
            setFeedback("Not enough Goblino Coins!", false);
        }
    }

    private void handleBarterClick(int idx) {
        if (barterRecipes == null || idx >= barterRecipes.size()) return;
        GoblinoBarterRecipe recipe = barterRecipes.get(idx);

        if (recipe.canBarter(playerInv)) {
            recipe.barter(playerInv);
            setFeedback("Barter complete!", true);
        } else {
            int have = playerInv.countItem(recipe.getInputItem());
            setFeedback("Need " + recipe.getInputQuantity() + "x "
                      + recipe.getInputItem().getDisplayName()
                      + " (have " + have + ")", false);
        }
    }

    private void handleGiftClick(int idx) {
        if (giftRecipes == null || idx >= giftRecipes.size()) return;
        GoblinoGiftRecipe recipe = giftRecipes.get(idx);

        if (recipe.canGift(playerInv)) {
            recipe.gift(playerInv);
            manager.addHappiness(hut, recipe.getHappinessGain());
            mood = manager.getMood(hut);
            setFeedback(recipe.getResponseText(), true);
        } else {
            setFeedback("You don't have " + recipe.getGiftItem().getDisplayName() + " to give!", false);
        }
    }


    private void openNegotiationOverlay(int recipeIdx) {
        if (recipes == null || recipeIdx >= recipes.size()) return;
        GoblinoRecipe recipe   = recipes.get(recipeIdx);
        int           askPrice = manager.getEffectiveCost(recipe, hut);

        negotiationRecipeIdx = recipeIdx;
        negotiationAskPrice  = askPrice;
        negotiationOfferMin  = 1;
        negotiationOfferMax  = Math.max(1, askPrice - 1);
        negotiationOffer     = Math.max(negotiationOfferMin,
                               Math.min(negotiationOfferMax,
                                        Math.round(askPrice * 0.80f)));
        negotiationActive    = true;
        hovered              = -1;   
    }

    private void closeNegotiationOverlay() {
        negotiationActive    = false;
        negotiationRecipeIdx = -1;
    }

    private void adjustOffer(int delta) {
        negotiationOffer = Math.max(negotiationOfferMin,
                           Math.min(negotiationOfferMax, negotiationOffer + delta));
    }


    private void handleNegotiationInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            submitNegotiation();
            return;
        }

        if (!Gdx.input.justTouched()) return;

        int mx = Gdx.input.getX();
        int my = sh - Gdx.input.getY();

        int overlayW = PANEL_W - 60;
        int ox       = panelX + 30;
        int oy       = panelY + FOOTER_H + 4;

        int stepStartX = ox + (overlayW - STEP_BTN_TOTAL_W) / 2;
        int stepY      = oy + 60;
        int[] deltas   = { -10, -1, 1, 10 };
        for (int i = 0; i < 4; i++) {
            int bx = stepStartX + i * (STEP_BTN_W + STEP_BTN_SPACING);
            if (hit(mx, my, bx, stepY, STEP_BTN_W, STEP_BTN_H)) {
                adjustOffer(deltas[i]);
                return;
            }
        }

        int actionStartX = ox + (overlayW - ACTION_TOTAL_W) / 2;
        int actionY      = oy + 16;

        if (hit(mx, my, actionStartX, actionY, ACTION_SUBMIT_W, ACTION_H)) {
            submitNegotiation();
            return;
        }

        int cancelX = actionStartX + ACTION_SUBMIT_W + ACTION_GAP;
        if (hit(mx, my, cancelX, actionY, ACTION_CANCEL_W, ACTION_H)) {
            closeNegotiationOverlay();
        }
    }

    private void submitNegotiation() {
        if (negotiationRecipeIdx < 0 || recipes == null
                || negotiationRecipeIdx >= recipes.size()) {
            closeNegotiationOverlay();
            return;
        }

        GoblinoRecipe    recipe = recipes.get(negotiationRecipeIdx);
        NegotiationResult result = manager.attemptNegotiation(
                hut, recipe, negotiationOffer, playerInv);

        mood = manager.getMood(hut);

        if (result == NegotiationResult.SUCCESS) {
            setFeedback("Deal! Paid " + negotiationOffer + " coins.", true);
        } else {
            setFeedback("Goblino refused! He's getting angrier...", false);
        }

        closeNegotiationOverlay();
    }



    public void render(SpriteBatch batch) {
        if (!visible || hut == null || mood == null) return;

        int coins = playerInv.countItem(Item.GOBLINO_COIN);

        batch.end();
        shape.setProjectionMatrix(batch.getProjectionMatrix());

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.07f, 0.09f, 0.07f, 0.97f);
        shape.rect(panelX, panelY, PANEL_W, panelH);
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Line);
        moodBorderColor(shape);
        shape.rect(panelX, panelY, PANEL_W, panelH);
        shape.end();

        batch.begin();

        int imgX = panelX + PANEL_W - PAD - IMG_SIZE;
        int imgY = panelY + panelH - PAD - IMG_SIZE;
        batch.draw(moodTextures.get(mood), imgX, imgY, IMG_SIZE, IMG_SIZE);

        int innerX   = panelX + PAD;
        int innerW   = PANEL_W - PAD * 2;
        int cursorY  = panelY + panelH - PAD;

        titleFont.setColor(0.40f, 0.92f, 0.32f, 1f);
        scratch.setText(titleFont, "Goblino Shop");
        titleFont.draw(batch, "Goblino Shop", innerX, cursorY);
        cursorY -= (int) scratch.height + 8;

        String subLabel = "Cave hut at (" + hut.getX() + ", " + hut.getY() + ")";
        tinyFont.setColor(0.28f, 0.50f, 0.20f, 1f);
        scratch.setText(tinyFont, subLabel);
        tinyFont.draw(batch, subLabel, innerX, cursorY);
        cursorY -= (int) scratch.height + 10;

        setFontMoodColor(tinyFont, 1f);
        tinyFont.draw(batch, mood.label, innerX, cursorY);
        scratch.setText(tinyFont, mood.label);
        cursorY -= (int) scratch.height + 4;

        setFontMoodColor(tinyFont, 0.70f);
        tinyFont.draw(batch, mood.priceNote, innerX, cursorY);
        scratch.setText(tinyFont, mood.priceNote);
        cursorY -= (int) scratch.height + 12;

        if (activeTab == Tab.GIFT) {
            int happiness = manager.getHappiness(hut);
            String happStr;
            if (happiness >= GoblinoHutManager.HAPPINESS_TIER_2) {
                happStr = "Max happiness reached!";
                labelFont.setColor(0.38f, 1.00f, 0.38f, 1f);
            } else {
                int nextTier = happiness >= GoblinoHutManager.HAPPINESS_TIER_1
                             ? GoblinoHutManager.HAPPINESS_TIER_2
                             : GoblinoHutManager.HAPPINESS_TIER_1;
                happStr = "Happiness: " + happiness + " / " + nextTier;
                labelFont.setColor(0.85f, 0.50f, 0.75f, 1f);
            }
            labelFont.draw(batch, happStr, innerX, cursorY);
        } else {
            boolean hasSomeCoins = coins > 0;
            String  coinStr      = "Coins: " + coins;
            labelFont.setColor(hasSomeCoins ? 1.00f : 0.42f,
                               hasSomeCoins ? 0.78f : 0.40f,
                               hasSomeCoins ? 0.12f : 0.10f, 1f);
            labelFont.draw(batch, coinStr, innerX, cursorY);
        }
        cursorY -= 16;

        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.26f, 0.48f, 0.16f, 0.55f);
        shape.rect(innerX, cursorY, innerW, 1);
        shape.end();
        batch.begin();

        renderTabBar(batch, innerX, innerW);

        int feedTop = panelY + panelH - HEADER_H - TAB_BAR_H;
        int feedBot = feedTop - FEED_H;

        if (feedbackText != null && feedbackTimer > 0f) {
            float alpha = Math.min(1f, feedbackTimer / 0.25f);
            batch.end();
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(feedbackGood ? 0.06f : 0.26f,
                           feedbackGood ? 0.26f : 0.06f,
                           feedbackGood ? 0.06f : 0.06f,
                           0.55f * alpha);
            shape.rect(innerX, feedBot + 4, innerW, FEED_H - 8);
            shape.end();
            batch.begin();

            labelFont.setColor(feedbackGood ? 0.38f : 1.00f,
                               feedbackGood ? 1.00f : 0.28f,
                               feedbackGood ? 0.38f : 0.22f,
                               alpha);
            scratch.setText(labelFont, feedbackText);
            labelFont.draw(batch, feedbackText,
                    innerX + (innerW - scratch.width) * 0.5f,
                    feedBot + FEED_H * 0.5f + scratch.height * 0.5f + 2);

        } else if (activeTab == Tab.GIFT) {
            renderHappinessProgressBand(batch, innerX, innerW, feedBot, feedTop);
        }

        switch (activeTab) {
            case TRADE:
                if (recipes != null) {
                    for (int i = 0; i < recipes.size(); i++) renderTradeRow(batch, i, coins);
                }
                break;
            case BARTER:
                if (barterRecipes != null) {
                    for (int i = 0; i < barterRecipes.size(); i++) renderBarterRow(batch, i);
                }
                break;
            case GIFT:
                if (giftRecipes != null) {
                    for (int i = 0; i < giftRecipes.size(); i++) renderGiftRow(batch, i);
                }
                break;
        }

        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.26f, 0.48f, 0.16f, 0.55f);
        shape.rect(innerX, panelY + FOOTER_H, innerW, 1);
        shape.end();
        batch.begin();

        tinyFont.setColor(0.26f, 0.40f, 0.18f, 0.85f);
        if (negotiationActive) {
            tinyFont.draw(batch, "ESC: cancel offer  |  Enter: submit", innerX, panelY + 24);
        } else {
            tinyFont.draw(batch, "E or ESC to close", innerX, panelY + 24);
        }

        if (negotiationActive) {
            renderNegotiationOverlay(batch);
        }
    }



    private void renderTradeRow(SpriteBatch batch, int i, int playerCoins) {
        GoblinoRecipe recipe       = recipes.get(i);
        int           effectiveCost = manager.getEffectiveCost(recipe, hut);
        int[]         r            = rowRect(i);
        boolean       canAfford    = playerCoins >= effectiveCost;
        boolean       isHovered    = (i == hovered);
        int           rowMidY      = r[1] + r[3] / 2;

        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);

        if (isHovered) {
            shape.setColor(canAfford ? 0.12f : 0.18f,
                           canAfford ? 0.24f : 0.06f,
                           canAfford ? 0.08f : 0.06f, 0.50f);
            shape.rect(r[0] + 2, r[1], r[2] - 4, r[3]);
        }

        shape.setColor(canAfford ? 0.30f : 0.42f,
                       canAfford ? 0.78f : 0.20f,
                       canAfford ? 0.20f : 0.16f,
                       canAfford ? 0.90f : 0.50f);
        shape.rect(r[0] + 2, r[1] + 6, 3, r[3] - 12);

        shape.setColor(0.16f, 0.28f, 0.10f, 0.38f);
        shape.rect(r[0] + 10, r[1] + r[3] - 1, r[2] - 20, 1);
        shape.end();

        batch.begin();

        String nameStr = recipe.getOutputItem().getDisplayName();
        labelFont.setColor(canAfford ? 0.95f : 0.40f,
                           canAfford ? 0.88f : 0.40f,
                           canAfford ? 0.72f : 0.32f, 1f);
        scratch.setText(labelFont, nameStr);
        labelFont.draw(batch, nameStr, r[0] + 14, rowMidY + scratch.height + 5);

        String qtyStr = "\u00d7" + recipe.getOutputQuantity();
        tinyFont.setColor(canAfford ? 0.58f : 0.28f,
                          canAfford ? 0.76f : 0.30f,
                          canAfford ? 0.48f : 0.22f, 1f);
        tinyFont.draw(batch, qtyStr, r[0] + 14, rowMidY - 2);

        String costStr = effectiveCost + " coins";
        labelFont.setColor(canAfford ? 1.00f : 0.36f,
                           canAfford ? 0.75f : 0.30f,
                           canAfford ? 0.10f : 0.08f, 1f);
        scratch.setText(labelFont, costStr);
        labelFont.draw(batch, costStr,
                r[0] + r[2] - scratch.width - 12,
                rowMidY + scratch.height + 5);

        if (isHovered) {
            String actionHint = canAfford ? "L: buy   R: haggle" : "need more coins";
            tinyFont.setColor(canAfford ? 0.55f : 0.50f,
                              canAfford ? 0.78f : 0.26f,
                              canAfford ? 0.32f : 0.20f, 0.85f);
            scratch.setText(tinyFont, actionHint);
            tinyFont.draw(batch, actionHint,
                    r[0] + r[2] - scratch.width - 12,
                    rowMidY - 2);
        }
    }

    private void renderBarterRow(SpriteBatch batch, int i) {
        GoblinoBarterRecipe recipe    = barterRecipes.get(i);
        int[]               r        = rowRect(i);
        boolean             canBarter = recipe.canBarter(playerInv);
        boolean             isHov    = (i == hovered);
        int                 rowMidY  = r[1] + r[3] / 2;
        int                 have     = playerInv.countItem(recipe.getInputItem());

        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);

        if (isHov) {
            shape.setColor(canBarter ? 0.08f : 0.18f,
                           canBarter ? 0.20f : 0.06f,
                           canBarter ? 0.20f : 0.06f, 0.50f);
            shape.rect(r[0] + 2, r[1], r[2] - 4, r[3]);
        }

        shape.setColor(canBarter ? 0.12f : 0.22f,
                       canBarter ? 0.68f : 0.30f,
                       canBarter ? 0.65f : 0.26f,
                       canBarter ? 0.90f : 0.50f);
        shape.rect(r[0] + 2, r[1] + 6, 3, r[3] - 12);

        shape.setColor(0.10f, 0.24f, 0.22f, 0.38f);
        shape.rect(r[0] + 10, r[1] + r[3] - 1, r[2] - 20, 1);
        shape.end();

        batch.begin();

        String inStr = recipe.getInputItem().getDisplayName() + " \u00d7" + recipe.getInputQuantity();
        labelFont.setColor(canBarter ? 0.90f : 0.40f,
                           canBarter ? 0.85f : 0.40f,
                           canBarter ? 0.70f : 0.32f, 1f);
        scratch.setText(labelFont, inStr);
        labelFont.draw(batch, inStr, r[0] + 14, rowMidY + scratch.height + 5);

        String haveStr = "have: " + have;
        tinyFont.setColor(canBarter ? 0.45f : 0.28f,
                          canBarter ? 0.72f : 0.30f,
                          canBarter ? 0.68f : 0.26f, 1f);
        tinyFont.draw(batch, haveStr, r[0] + 14, rowMidY - 2);

        String arrow = "=>";
        labelFont.setColor(canBarter ? 0.30f : 0.20f,
                           canBarter ? 0.72f : 0.28f,
                           canBarter ? 0.68f : 0.24f, 0.90f);
        scratch.setText(labelFont, arrow);
        labelFont.draw(batch, arrow,
                r[0] + r[2] * 0.5f - scratch.width * 0.5f,
                rowMidY + scratch.height + 5);

        String outStr = recipe.getOutputItem().getDisplayName() + " \u00d7" + recipe.getOutputQuantity();
        labelFont.setColor(canBarter ? 0.38f : 0.25f,
                           canBarter ? 0.92f : 0.32f,
                           canBarter ? 0.55f : 0.22f, 1f);
        scratch.setText(labelFont, outStr);
        labelFont.draw(batch, outStr,
                r[0] + r[2] - scratch.width - 12,
                rowMidY + scratch.height + 5);

        if (isHov) {
            String hint = canBarter ? "click to barter" : "need " + recipe.getInputQuantity();
            tinyFont.setColor(canBarter ? 0.28f : 0.48f,
                              canBarter ? 0.78f : 0.24f,
                              canBarter ? 0.72f : 0.20f, 0.80f);
            scratch.setText(tinyFont, hint);
            tinyFont.draw(batch, hint,
                    r[0] + r[2] - scratch.width - 12,
                    rowMidY - 2);
        }
    }

    private void renderGiftRow(SpriteBatch batch, int i) {
        GoblinoGiftRecipe recipe  = giftRecipes.get(i);
        int[]             r       = rowRect(i);
        boolean           canGift = recipe.canGift(playerInv);
        boolean           isHov   = (i == hovered);
        int               rowMidY = r[1] + r[3] / 2;
        int               have    = playerInv.countItem(recipe.getGiftItem());

        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);

        if (isHov) {
            shape.setColor(canGift ? 0.18f : 0.16f,
                           canGift ? 0.08f : 0.06f,
                           canGift ? 0.12f : 0.06f, 0.50f);
            shape.rect(r[0] + 2, r[1], r[2] - 4, r[3]);
        }

        shape.setColor(canGift ? 0.82f : 0.35f,
                       canGift ? 0.25f : 0.18f,
                       canGift ? 0.42f : 0.18f,
                       canGift ? 0.90f : 0.45f);
        shape.rect(r[0] + 2, r[1] + 5, 3, r[3] - 10);

        shape.setColor(0.22f, 0.10f, 0.16f, 0.38f);
        shape.rect(r[0] + 10, r[1] + r[3] - 1, r[2] - 20, 1);
        shape.end();

        batch.begin();

        String nameStr = recipe.getGiftItem().getDisplayName()
                       + (recipe.getQuantity() > 1 ? " \u00d7" + recipe.getQuantity() : "");
        labelFont.setColor(canGift ? 0.95f : 0.40f,
                           canGift ? 0.80f : 0.38f,
                           canGift ? 0.72f : 0.30f, 1f);
        scratch.setText(labelFont, nameStr);
        labelFont.draw(batch, nameStr, r[0] + 14, rowMidY + scratch.height + 4);

        String gainStr = "+" + recipe.getHappinessGain() + " mood";
        tinyFont.setColor(canGift ? 0.85f : 0.35f,
                          canGift ? 0.28f : 0.18f,
                          canGift ? 0.48f : 0.20f, 1f);
        tinyFont.draw(batch, gainStr, r[0] + 14, rowMidY - 1);

        String haveStr = "have: " + have;
        labelFont.setColor(canGift ? 0.68f : 0.32f,
                           canGift ? 0.75f : 0.28f,
                           canGift ? 0.60f : 0.22f, 1f);
        scratch.setText(labelFont, haveStr);
        labelFont.draw(batch, haveStr,
                r[0] + r[2] - scratch.width - 12,
                rowMidY + scratch.height + 4);

        if (isHov) {
            String hint = canGift ? "click to give" : "not enough";
            tinyFont.setColor(canGift ? 0.85f : 0.50f,
                              canGift ? 0.28f : 0.22f,
                              canGift ? 0.48f : 0.18f, 0.80f);
            scratch.setText(tinyFont, hint);
            tinyFont.draw(batch, hint,
                    r[0] + r[2] - scratch.width - 12,
                    rowMidY - 1);
        }
    }


    private void renderNegotiationOverlay(SpriteBatch batch) {
        int overlayW = PANEL_W - 60;   
        int ox       = panelX + 30;
        int oy       = panelY + FOOTER_H + 4;
        int oh       = NEGOTIATION_OVERLAY_H;

        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.65f);
        shape.rect(panelX, panelY, PANEL_W, panelH);

        shape.setColor(0.08f, 0.08f, 0.07f, 0.97f);
        shape.rect(ox, oy, overlayW, oh);
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Line);
        moodBorderColor(shape);
        shape.rect(ox, oy, overlayW, oh);
        shape.end();
        batch.begin();

        labelFont.setColor(0.88f, 0.72f, 0.22f, 1f);
        String title = "Counter-Offer";
        scratch.setText(labelFont, title);
        labelFont.draw(batch, title,
                ox + (overlayW - scratch.width) * 0.5f, oy + 180);

        String moodHint = getMoodNegotiationHint();
        setFontMoodColor(tinyFont, 0.75f);
        scratch.setText(tinyFont, moodHint);
        tinyFont.draw(batch, moodHint,
                ox + (overlayW - scratch.width) * 0.5f, oy + 160);

        tinyFont.setColor(0.55f, 0.48f, 0.36f, 0.85f);
        String askStr = "Goblino asks: " + negotiationAskPrice + " coins";
        scratch.setText(tinyFont, askStr);
        tinyFont.draw(batch, askStr,
                ox + (overlayW - scratch.width) * 0.5f, oy + 143);

        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.28f, 0.20f, 0.10f, 0.55f);
        shape.rect(ox + 24, oy + 130, overlayW - 48, 1);
        shape.end();
        batch.begin();

        boolean canAffordOffer = playerInv.countItem(Item.GOBLINO_COIN) >= negotiationOffer;
        labelFont.setColor(canAffordOffer ? 1.00f : 0.72f,
                           canAffordOffer ? 0.88f : 0.32f,
                           canAffordOffer ? 0.22f : 0.22f, 1f);
        String offerStr = "Your offer: " + negotiationOffer + " coins";
        scratch.setText(labelFont, offerStr);
        labelFont.draw(batch, offerStr,
                ox + (overlayW - scratch.width) * 0.5f, oy + 106);

        int   stepStartX = ox + (overlayW - STEP_BTN_TOTAL_W) / 2;
        int   stepY      = oy + 60;
        int[] deltas     = { -10, -1, 1, 10 };
        String[] stepLbl = { "-10", "-1", "+1", "+10" };

        for (int i = 0; i < 4; i++) {
            int     bx       = stepStartX + i * (STEP_BTN_W + STEP_BTN_SPACING);
            boolean canApply = (negotiationOffer + deltas[i] >= negotiationOfferMin)
                            && (negotiationOffer + deltas[i] <= negotiationOfferMax);

            batch.end();
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(canApply ? 0.16f : 0.10f,
                           canApply ? 0.13f : 0.08f,
                           canApply ? 0.08f : 0.06f, 0.92f);
            shape.rect(bx, stepY, STEP_BTN_W, STEP_BTN_H);
            shape.end();

            shape.begin(ShapeRenderer.ShapeType.Line);
            shape.setColor(canApply ? 0.48f : 0.22f,
                           canApply ? 0.38f : 0.18f,
                           canApply ? 0.18f : 0.10f, 0.85f);
            shape.rect(bx, stepY, STEP_BTN_W, STEP_BTN_H);
            shape.end();
            batch.begin();

            tinyFont.setColor(canApply ? 0.92f : 0.38f,
                              canApply ? 0.82f : 0.32f,
                              canApply ? 0.50f : 0.20f, 1f);
            scratch.setText(tinyFont, stepLbl[i]);
            tinyFont.draw(batch, stepLbl[i],
                    bx + (STEP_BTN_W  - scratch.width)  * 0.5f,
                    stepY + STEP_BTN_H * 0.5f + scratch.height * 0.5f);
        }

        int actionStartX = ox + (overlayW - ACTION_TOTAL_W) / 2;
        int actionY      = oy + 16;

        boolean canSubmit = canAffordOffer;
        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(canSubmit ? 0.12f : 0.09f,
                       canSubmit ? 0.22f : 0.09f,
                       canSubmit ? 0.08f : 0.07f, 0.94f);
        shape.rect(actionStartX, actionY, ACTION_SUBMIT_W, ACTION_H);
        shape.end();
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(canSubmit ? 0.32f : 0.20f,
                       canSubmit ? 0.65f : 0.22f,
                       canSubmit ? 0.20f : 0.12f, 0.85f);
        shape.rect(actionStartX, actionY, ACTION_SUBMIT_W, ACTION_H);
        shape.end();
        batch.begin();

        labelFont.setColor(canSubmit ? 0.38f : 0.28f,
                           canSubmit ? 0.90f : 0.32f,
                           canSubmit ? 0.28f : 0.20f, 1f);
        String submitLbl = "Submit Offer";
        scratch.setText(labelFont, submitLbl);
        labelFont.draw(batch, submitLbl,
                actionStartX + (ACTION_SUBMIT_W - scratch.width)  * 0.5f,
                actionY      + ACTION_H          * 0.5f + scratch.height * 0.5f);

        int cancelX = actionStartX + ACTION_SUBMIT_W + ACTION_GAP;
        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.14f, 0.08f, 0.08f, 0.94f);
        shape.rect(cancelX, actionY, ACTION_CANCEL_W, ACTION_H);
        shape.end();
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(0.44f, 0.18f, 0.14f, 0.85f);
        shape.rect(cancelX, actionY, ACTION_CANCEL_W, ACTION_H);
        shape.end();
        batch.begin();

        labelFont.setColor(0.82f, 0.28f, 0.20f, 1f);
        String cancelLbl = "Cancel";
        scratch.setText(labelFont, cancelLbl);
        labelFont.draw(batch, cancelLbl,
                cancelX + (ACTION_CANCEL_W - scratch.width)  * 0.5f,
                actionY + ACTION_H          * 0.5f + scratch.height * 0.5f);
    }

    private String getMoodNegotiationHint() {
        switch (mood) {
            case HAPPY: return "Goblino seems in a generous mood...";
            case SAD:   return "Goblino grumbles but listens...";
            case ANGRY: return "Goblino barely tolerates your offer.";
            default:    return "";
        }
    }



    private void renderTabBar(SpriteBatch batch, int innerX, int innerW) {
        int tabBarY = panelY + panelH - HEADER_H - TAB_BAR_H;
        int tabW    = PANEL_W / 3;
        Tab[] tabValues = Tab.values();

        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < tabValues.length; i++) {
            boolean active = (activeTab == tabValues[i]);
            shape.setColor(active ? 0.14f : 0.09f,
                           active ? 0.26f : 0.12f,
                           active ? 0.10f : 0.08f, 1f);
            shape.rect(panelX + i * tabW, tabBarY, tabW - 1, TAB_BAR_H);
        }
        int ai = activeTab.ordinal();
        shape.setColor(0.32f, 0.82f, 0.26f, 1f);
        shape.rect(panelX + ai * tabW, tabBarY + TAB_BAR_H - 2, tabW - 1, 2);
        shape.end();

        batch.begin();
        for (int i = 0; i < TAB_LABELS.length; i++) {
            boolean active = (activeTab == tabValues[i]);
            float   cx     = panelX + i * tabW + tabW * 0.5f;
            float   cy     = tabBarY + TAB_BAR_H * 0.5f;
            labelFont.setColor(active ? 0.88f : 0.38f,
                               active ? 0.94f : 0.52f,
                               active ? 0.70f : 0.38f, 1f);
            scratch.setText(labelFont, TAB_LABELS[i]);
            labelFont.draw(batch, TAB_LABELS[i],
                    cx - scratch.width * 0.5f,
                    cy + scratch.height * 0.5f + 1f);
        }

        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.24f, 0.45f, 0.14f, 0.50f);
        shape.rect(innerX, tabBarY - 1, innerW, 1);
        shape.end();
        batch.begin();
    }

    private void renderHappinessProgressBand(SpriteBatch batch,
                                              int innerX, int innerW,
                                              int feedBot, int feedTop) {
        int         happiness = manager.getHappiness(hut);
        GoblinoMood eff       = manager.getMood(hut);

        boolean atMax    = happiness >= GoblinoHutManager.HAPPINESS_TIER_2;
        int     nextTier = happiness >= GoblinoHutManager.HAPPINESS_TIER_1
                         ? GoblinoHutManager.HAPPINESS_TIER_2
                         : GoblinoHutManager.HAPPINESS_TIER_1;
        float fillFrac   = atMax ? 1f : Math.max(0f, (float) happiness / nextTier);

        int barW = innerW - 90;
        int barH = 8;
        int barX = innerX;
        int barY = feedBot + 10;

        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.14f, 0.10f, 0.14f, 0.75f);
        shape.rect(barX, barY, barW, barH);
        setShapeMoodColor(shape, eff, 0.85f);
        shape.rect(barX, barY, barW * fillFrac, barH);
        shape.end();
        batch.begin();

        setFontMoodColor(tinyFont, 1f);
        String moodLabel = eff.label;
        scratch.setText(tinyFont, moodLabel);
        tinyFont.draw(batch, moodLabel, barX + barW + 6, barY + barH - 1 + scratch.height);

        String progressStr;
        if (atMax) {
            progressStr = "Goblino is at max happiness!";
        } else {
            GoblinoMood nextMood = GoblinoMood.values()[Math.max(0, eff.ordinal() - 1)];
            progressStr = happiness + " / " + nextTier + " pts to reach " + nextMood.label;
        }
        tinyFont.setColor(0.58f, 0.42f, 0.60f, 0.85f);
        tinyFont.draw(batch, progressStr, barX, feedTop - 4);
    }



    private int[] rowRect(int i) {
        int rowH         = (activeTab == Tab.GIFT) ? GIFT_ROW_H : TRADE_H;
        int tradeAreaTop = panelY + panelH - HEADER_H - TAB_BAR_H - FEED_H;
        int rowBottom    = tradeAreaTop - (i + 1) * rowH;
        return new int[]{ panelX + 2, rowBottom, PANEL_W - 4, rowH - 2 };
    }

    private int getActiveRowCount() {
        switch (activeTab) {
            case TRADE:  return (recipes       != null) ? recipes.size()       : 0;
            case BARTER: return (barterRecipes != null) ? barterRecipes.size() : 0;
            case GIFT:   return (giftRecipes   != null) ? giftRecipes.size()   : 0;
            default:     return 0;
        }
    }

    private void relayout() {
        sw     = Gdx.graphics.getWidth();
        sh     = Gdx.graphics.getHeight();
        int rowH     = (activeTab == Tab.GIFT) ? GIFT_ROW_H : TRADE_H;
        int rowCount = getActiveRowCount();
        panelH = HEADER_H + TAB_BAR_H + FEED_H + rowCount * rowH + FOOTER_H;
        panelX = (sw - PANEL_W) / 2;
        panelY = Math.max(8, (sh - panelH) / 2);
        shape.getProjectionMatrix().setToOrtho2D(0, 0, sw, sh);
    }

    private void moodBorderColor(ShapeRenderer sr) {
        switch (mood) {
            case HAPPY: sr.setColor(0.28f, 0.80f, 0.28f, 1f); break;
            case SAD:   sr.setColor(0.30f, 0.45f, 0.90f, 1f); break;
            case ANGRY: sr.setColor(0.90f, 0.22f, 0.14f, 1f); break;
            default:    sr.setColor(0.32f, 0.56f, 0.22f, 1f); break;
        }
    }

    private void setFontMoodColor(BitmapFont font, float alpha) {
        switch (mood) {
            case HAPPY: font.setColor(0.35f, 0.98f, 0.40f, alpha); break;
            case SAD:   font.setColor(0.48f, 0.60f, 1.00f, alpha); break;
            case ANGRY: font.setColor(1.00f, 0.28f, 0.18f, alpha); break;
            default:    font.setColor(0.75f, 0.75f, 0.75f, alpha); break;
        }
    }

    private void setShapeMoodColor(ShapeRenderer sr, GoblinoMood m, float alpha) {
        switch (m) {
            case HAPPY: sr.setColor(0.35f, 0.98f, 0.40f, alpha); break;
            case SAD:   sr.setColor(0.48f, 0.60f, 1.00f, alpha); break;
            case ANGRY: sr.setColor(1.00f, 0.28f, 0.18f, alpha); break;
            default:    sr.setColor(0.75f, 0.75f, 0.75f, alpha); break;
        }
    }

    private boolean hit(int mx, int my, int rx, int ry, int rw, int rh) {
        return mx >= rx && mx <= rx + rw && my >= ry && my <= ry + rh;
    }

    private void setFeedback(String text, boolean good) {
        feedbackText  = text;
        feedbackGood  = good;
        feedbackTimer = FEEDBACK_DURATION;
    }



    public void dispose() {
        shape.dispose();
        titleFont.dispose();
        labelFont.dispose();
        tinyFont.dispose();
        for (Texture tex : moodTextures.values()) tex.dispose();
    }
}