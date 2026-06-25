package com.factory.game;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.factory.game.Items.Inventory;
import com.factory.game.Items.Item;
import com.factory.game.Renderer.DayNightCycle;
import com.factory.game.World.Animal;

public class DevConsole {

    private boolean visible     = false;
    private String  cmdText     = "";

    private float   cursorTimer = 0f;
    private boolean cursorOn    = true;

    private final List<String> suggestions   = new ArrayList<>();
    private int                suggestionIdx = -1;

    private String feedbackMsg   = "";
    private float  feedbackTimer = 0f;
    private static final float FEEDBACK_DURATION = 2.5f;

    private final Player       player;
    private final Inventory    inventory;
    private final WorldManager worldManager;
    private DayNightCycle      dayNightCycle = new DayNightCycle();

    private final BitmapFont    font;
    private final ShapeRenderer shape;

    private int scrollOffset = 0;

    private int screenW, screenH;

    private static final float PANEL_W      = 980f;
    private static final float PANEL_H      = 310f;
    private static final float FIELD_H      = 46f;
    private static final float PADDING      = 28f;
    private static final float SUGGEST_H    = 36f;
    private static final int   MAX_SUGGESTS = 6;

    private static final Color C_BG          = new Color(0.08f, 0.08f, 0.10f, 0.93f);
    private static final Color C_BORDER      = new Color(0.55f, 0.85f, 1.00f, 0.80f);
    private static final Color C_FIELD_BG    = new Color(0.14f, 0.14f, 0.18f, 1.00f);
    private static final Color C_FIELD_FOCUS = new Color(0.18f, 0.22f, 0.30f, 1.00f);
    private static final Color C_SUGGEST_BG  = new Color(0.12f, 0.14f, 0.20f, 0.95f);
    private static final Color C_SUGGEST_SEL = new Color(0.20f, 0.45f, 0.75f, 0.95f);
    private static final Color C_LABEL       = new Color(0.60f, 0.80f, 1.00f, 1.00f);
    private static final Color C_TEXT        = new Color(0.95f, 0.95f, 1.00f, 1.00f);
    private static final Color C_OK          = new Color(0.40f, 1.00f, 0.55f, 1.00f);
    private static final Color C_ERR         = new Color(1.00f, 0.35f, 0.35f, 1.00f);
    private static final Color C_TITLE       = new Color(0.55f, 0.85f, 1.00f, 1.00f);
    private static final Color C_CREATIVE    = new Color(1.00f, 0.80f, 0.20f, 1.00f);
    private static final Color C_SYNTAX      = new Color(0.70f, 0.85f, 0.60f, 1.00f);
    private static final Color C_HINT        = new Color(0.55f, 0.60f, 0.70f, 1.00f);

    @FunctionalInterface
    private interface CommandHandler {
        String execute(String args);
    }

    private static class CommandEntry {
        final String         name;
        final String         usage;
        final CommandHandler handler;
        CommandEntry(String name, String usage, CommandHandler handler) {
            this.name    = name;
            this.usage   = usage;
            this.handler = handler;
        }
    }

    private final List<CommandEntry> commands = new ArrayList<>();

    public DevConsole(Player player, DayNightCycle dayNightCycle, WorldManager worldManager) {
        this.player        = player;
        this.inventory     = player.getInventory();
        this.dayNightCycle = dayNightCycle;
        this.worldManager  = worldManager;
        this.shape         = new ShapeRenderer();

        FreeTypeFontGenerator gen   = new FreeTypeFontGenerator(
                Gdx.files.internal("JetBrainsMono-Regular.ttf"));
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size  = 16;
        param.color = Color.WHITE;
        this.font = gen.generateFont(param);
        gen.dispose();

        screenW = Gdx.graphics.getWidth();
        screenH = Gdx.graphics.getHeight();

        registerCommands();
    }

    private void registerCommands() {

        commands.add(new CommandEntry(
            "give",
            "give <item name> <quantity>",
            (args) -> {
                if (args.isEmpty())
                    return "ERR:Usage: give <item name> <quantity>";

                String[] tokens  = args.trim().split("\\s+");
                int      qty     = 1;
                String   itemArg = args.trim();

                if (tokens.length >= 2) {
                    String last = tokens[tokens.length - 1];
                    try {
                        qty = Integer.parseInt(last);
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < tokens.length - 1; i++) {
                            if (i > 0) sb.append(' ');
                            sb.append(tokens[i]);
                        }
                        itemArg = sb.toString();
                    } catch (NumberFormatException e) {
                        qty     = 1;
                        itemArg = args.trim();
                    }
                }

                if (qty <= 0) return "ERR:Quantity must be > 0";

                Item found = resolveItem(itemArg);
                if (found == null)
                    return "ERR:Unknown item: \"" + itemArg + "\"";

                inventory.addItem(found, qty);
                return "OK:Gave " + qty + "x " + found.getDisplayName();
            }
        ));

        commands.add(new CommandEntry(
            "tp",
            "tp <tileX> <tileY>",
            (args) -> {
                if (args.isEmpty())
                    return "ERR:Usage: tp <tileX> <tileY>";

                String[] coords = args.trim().split("\\s+");
                if (coords.length < 2)
                    return "ERR:Usage: tp <tileX> <tileY>";

                float tx, ty;
                try { tx = Float.parseFloat(coords[0]); }
                catch (NumberFormatException e) { return "ERR:X must be a number"; }
                try { ty = Float.parseFloat(coords[1]); }
                catch (NumberFormatException e) { return "ERR:Y must be a number"; }

                player.setPosition(tx * com.factory.game.Main.TILE_SCALE,
                                   ty * com.factory.game.Main.TILE_SCALE);
                return "OK:Teleported to tile (" + (int)tx + ", " + (int)ty + ")";
            }
        ));

        commands.add(new CommandEntry(
            "settime",
            "settime <0.0-1.0>",
            (args) -> {
                if (args.isEmpty())
                    return "ERR:Usage: settime <0.0-1.0>";

                float t;
                try {
                    t = Float.parseFloat(args.trim());
                } catch (NumberFormatException e) {
                    return "ERR:Invalid value - must be a number between 0.0 and 1.0";
                }

                if (t < 0f || t > 1f)
                    return "ERR:Value must be between 0.0 and 1.0";

                dayNightCycle.setTimeOfDay(t);
                return String.format("OK:Time set to %.3f", t);
            }
        ));

        commands.add(new CommandEntry(
            "clear",
            "clear",
            (args) -> {
                for (Item item : Item.values()) {
                    int count = inventory.countItem(item);
                    if (count > 0) inventory.removeItem(item, count);
                }
                return "OK:Inventory cleared";
            }
        ));

        commands.add(new CommandEntry(
            "creative",
            "creative",
            (args) -> {
                com.factory.game.Main.CREATIVE_MODE = !com.factory.game.Main.CREATIVE_MODE;
                return com.factory.game.Main.CREATIVE_MODE
                    ? "OK:Creative mode ON"
                    : "OK:Creative mode OFF — console will close on next toggle";
            }
        ));

        commands.add(new CommandEntry(
            "spawn",
            "spawn <animal> [tileX tileY]",
            (args) -> {
                if (args.isEmpty())
                    return "ERR:Usage: spawn <animal> [tileX tileY]";

                String[] tokens = args.trim().split("\\s+");
                Animal.Type animalType = resolveAnimalType(tokens[0]);
                if (animalType == null)
                    return "ERR:Unknown animal: \"" + tokens[0] + "\"";

                float wx, wy;
                if (tokens.length >= 3) {
                    try {
                        wx = Float.parseFloat(tokens[1]) * Main.TILE_SCALE;
                        wy = Float.parseFloat(tokens[2]) * Main.TILE_SCALE;
                    } catch (NumberFormatException e) {
                        return "ERR:Tile coordinates must be numbers";
                    }
                } else {
                    wx = player.getWorldX();
                    wy = player.getWorldY();
                }

                worldManager.spawnAnimal(animalType, wx, wy);

                String loc = (tokens.length >= 3)
                    ? "at tile (" + tokens[1] + ", " + tokens[2] + ")"
                    : "at player position";
                return "OK:Spawned " + animalType.name().toLowerCase() + " " + loc;
            }
        ));
    }

    public boolean isVisible() { return visible; }

    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.GRAVE)) {
            if (visible) close();
            else         open();
            return;
        }
        if (!visible) return;

        cursorTimer += Gdx.graphics.getDeltaTime();
        if (cursorTimer >= 0.53f) { cursorTimer = 0f; cursorOn = !cursorOn; }

        if (feedbackTimer > 0f)
            feedbackTimer -= Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { close(); return; }

        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            if (suggestionIdx >= 0 && !suggestions.isEmpty()) {
                cmdText       = suggestions.get(suggestionIdx);
                suggestionIdx = -1;
                suggestions.clear();
                refreshSuggestions();
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            if (!suggestions.isEmpty()) {
                suggestionIdx = (suggestionIdx + 1) % suggestions.size();
                if (suggestionIdx >= scrollOffset + MAX_SUGGESTS)
                    scrollOffset = suggestionIdx - MAX_SUGGESTS + 1;
                else if (suggestionIdx < scrollOffset)
                    scrollOffset = 0;
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            if (!suggestions.isEmpty()) {
                suggestionIdx = (suggestionIdx - 1 + suggestions.size()) % suggestions.size();
                if (suggestionIdx < scrollOffset)
                    scrollOffset = suggestionIdx;
                else if (suggestionIdx >= scrollOffset + MAX_SUGGESTS)
                    scrollOffset = suggestionIdx - MAX_SUGGESTS + 1;
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            execute();
            return;
        }

        handleTyping();
    }

    public void render(SpriteBatch batch) {
        if (!visible) return;

        float px = screenW * 0.08f;
        float py = screenH * 0.57f;

        float cmdFieldX = px + PADDING;
        float cmdFieldW = PANEL_W - PADDING * 2f;
        float cmdFieldY = py + 90f;

        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shape.getProjectionMatrix().setToOrtho2D(0, 0, screenW, screenH);

        shape.begin(ShapeRenderer.ShapeType.Filled);
        setColor(shape, C_BG);
        shape.rect(px, py, PANEL_W, PANEL_H);
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Line);
        setColor(shape, C_BORDER);
        shape.rect(px, py, PANEL_W, PANEL_H);
        shape.end();

        drawFieldBg(cmdFieldX, cmdFieldY, cmdFieldW, FIELD_H, true);

        if (!suggestions.isEmpty()) {
            float sx   = cmdFieldX;
            float sw   = cmdFieldW;
            int   show = Math.min(suggestions.size(), MAX_SUGGESTS);
            float sy   = py - show * SUGGEST_H;

            shape.begin(ShapeRenderer.ShapeType.Filled);
            for (int i = 0; i < show; i++) {
                int logicalIdx = i + scrollOffset;
                float ry = py - (i + 1) * SUGGEST_H;
                setColor(shape, (logicalIdx == suggestionIdx) ? C_SUGGEST_SEL : C_SUGGEST_BG);
                shape.rect(sx, ry, sw, SUGGEST_H);
            }
            shape.end();

            shape.begin(ShapeRenderer.ShapeType.Line);
            setColor(shape, C_BORDER);
            shape.rect(sx, sy, sw, show * SUGGEST_H);
            shape.end();
        }

        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();

        float titleY = py + PANEL_H - 18f;
        font.setColor(C_TITLE);
        font.draw(batch, "DEVELOPER CONSOLE", px + PADDING, titleY);
        if (Main.CREATIVE_MODE) {
            font.setColor(C_CREATIVE);
            font.draw(batch, "[CREATIVE]", px + PANEL_W - 160f, titleY);
        }

        float labelY = cmdFieldY + FIELD_H + 18f;
        font.setColor(C_LABEL);
        font.draw(batch, "Command", cmdFieldX, labelY);

        font.setColor(C_TEXT);
        font.draw(batch,
            cmdText + (cursorOn ? "|" : ""),
            cmdFieldX + 10f, cmdFieldY + FIELD_H - 13f);

        float hintY = cmdFieldY - 22f;
        String syntaxHint = getCurrentSyntaxHint();
        if (syntaxHint != null) {
            font.setColor(C_SYNTAX);
            font.draw(batch, syntaxHint, cmdFieldX, hintY);
        } else {
            font.setColor(C_HINT);
            font.draw(batch,
                "TAB = autocomplete    UP/DOWN = navigate    ENTER = run    ESC = close",
                cmdFieldX, hintY);
        }

        if (feedbackTimer > 0f && !feedbackMsg.isEmpty()) {
            font.setColor(feedbackMsg.startsWith("OK:") ? C_OK : C_ERR);
            font.draw(batch, feedbackMsg.substring(3), cmdFieldX, hintY - 26f);
        }

        if (!suggestions.isEmpty()) {
            int show = Math.min(suggestions.size() - scrollOffset, MAX_SUGGESTS);
            for (int i = 0; i < show; i++) {
                int logicalIdx = i + scrollOffset;
                float ry = py - (i + 1) * SUGGEST_H + SUGGEST_H - 10f;
                font.setColor(logicalIdx == suggestionIdx ? Color.WHITE : C_TEXT);
                font.draw(batch, suggestions.get(logicalIdx), cmdFieldX + 8f, ry);
            }
        }
    }

    public void resize(int w, int h) {
        screenW = w;
        screenH = h;
    }

    public void dispose() {
        font.dispose();
        shape.dispose();
    }

    private void open() {
        visible       = true;
        cmdText       = "";
        suggestionIdx = -1;
        suggestions.clear();
        feedbackMsg   = "";
        feedbackTimer = 0f;
        refreshSuggestions();
    }

    private void close() {
        visible = false;
        suggestions.clear();
    }

    private void execute() {
        String raw = cmdText.trim();
        if (raw.isEmpty()) return;

        if (suggestionIdx >= 0 && !suggestions.isEmpty()) {
            cmdText       = suggestions.get(suggestionIdx);
            raw           = cmdText.trim();
            suggestionIdx = -1;
            suggestions.clear();
        }

        String[] parts = raw.split("\\s+", 2);
        String   verb  = parts[0].toLowerCase();
        String   args  = (parts.length > 1) ? parts[1].trim() : "";

        CommandEntry entry = findCommand(verb);
        if (entry == null) {
            setFeedback("ERR:Unknown command: \"" + verb + "\"");
            return;
        }

        setFeedback(entry.handler.execute(args));
    }

    private void handleTyping() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            if (!cmdText.isEmpty()) {
                cmdText = cmdText.substring(0, cmdText.length() - 1);
                refreshSuggestions();
            }
            return;
        }

        String typed = pollTypedChar();
        if (typed == null) return;

        cmdText += typed;
        refreshSuggestions();
    }

    private String pollTypedChar() {
        boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                     || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);

        for (int k = Input.Keys.A; k <= Input.Keys.Z; k++) {
            if (Gdx.input.isKeyJustPressed(k)) {
                char c = (char) ('a' + (k - Input.Keys.A));
                return shift ? String.valueOf(Character.toUpperCase(c)) : String.valueOf(c);
            }
        }
        for (int k = Input.Keys.NUM_0; k <= Input.Keys.NUM_9; k++) {
            if (Gdx.input.isKeyJustPressed(k))
                return String.valueOf((char) ('0' + (k - Input.Keys.NUM_0)));
        }
        for (int k = Input.Keys.NUMPAD_0; k <= Input.Keys.NUMPAD_9; k++) {
            if (Gdx.input.isKeyJustPressed(k))
                return String.valueOf((char) ('0' + (k - Input.Keys.NUMPAD_0)));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE))                return " ";
        if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS))                return shift ? "_" : "-";
        if (Gdx.input.isKeyJustPressed(Input.Keys.APOSTROPHE) && !shift) return "'";
        if (Gdx.input.isKeyJustPressed(Input.Keys.PERIOD)     && !shift) return ".";
        return null;
    }

    private void refreshSuggestions() {
        suggestions.clear();
        suggestionIdx = -1;

        if (cmdText.isEmpty()) {
            for (CommandEntry e : commands) suggestions.add(e.name);
            return;
        }

        String   lower = cmdText.toLowerCase();
        String[] parts = lower.split("\\s+", 2);
        String   verb  = parts[0];

        if (parts.length == 1) {
            for (CommandEntry e : commands)
                if (e.name.startsWith(verb)) suggestions.add(e.name);
            return;
        }

        if (findCommand(verb) == null) return;

        if (verb.equals("spawn")) {
            String prefix = parts[1].trim().split("\\s+")[0];
            for (Animal.Type t : Animal.Type.values()) {
                String name = t.name().toLowerCase();
                if (name.startsWith(prefix))
                    suggestions.add("spawn " + name);
            }
            return;
        }

        if (!verb.equals("give")) return;

        String   argPart   = parts[1];
        String[] argTokens = argPart.split("\\s+");
        boolean  lastIsNum = false;
        if (argTokens.length > 1) {
            try { Integer.parseInt(argTokens[argTokens.length - 1]); lastIsNum = true; }
            catch (NumberFormatException ignored) {}
        }
        String itemPrefix = lastIsNum
            ? String.join(" ", java.util.Arrays.copyOf(argTokens, argTokens.length - 1))
            : argPart;

        for (Item item : Item.values()) {
            if (item.getDisplayName().toLowerCase().startsWith(itemPrefix))
                suggestions.add(verb + " " + item.getDisplayName());
        }
        for (Item item : Item.values()) {
            String en = item.name().toLowerCase().replace('_', ' ');
            if (en.startsWith(itemPrefix)) {
                String candidate = verb + " " + item.getDisplayName();
                if (!suggestions.contains(candidate)) suggestions.add(candidate);
            }
        }
    }

    private String getCurrentSyntaxHint() {
        if (cmdText.isEmpty()) return null;
        String verb = cmdText.trim().split("\\s+")[0].toLowerCase();
        CommandEntry entry = findCommand(verb);
        return entry != null ? entry.usage : null;
    }

    private Item resolveItem(String arg) {
        String lower = arg.toLowerCase().trim();
        for (Item item : Item.values())
            if (item.getDisplayName().toLowerCase().equals(lower)) return item;
        for (Item item : Item.values()) {
            if (item.name().toLowerCase().replace('_', ' ').equals(lower)) return item;
            if (item.name().toLowerCase().equals(lower.replace(' ', '_'))) return item;
        }
        for (Item item : Item.values())
            if (item.getDisplayName().toLowerCase().startsWith(lower)) return item;
        return null;
    }

    private Animal.Type resolveAnimalType(String arg) {
        String lower = arg.toLowerCase().trim();
        for (Animal.Type t : Animal.Type.values())
            if (t.name().toLowerCase().equals(lower)) return t;
        for (Animal.Type t : Animal.Type.values())
            if (t.name().toLowerCase().startsWith(lower)) return t;
        return null;
    }

    private CommandEntry findCommand(String verb) {
        for (CommandEntry e : commands)
            if (e.name.equals(verb)) return e;
        return null;
    }

    private void setFeedback(String msg) {
        feedbackMsg   = msg;
        feedbackTimer = FEEDBACK_DURATION;
    }

    private void drawFieldBg(float x, float y, float w, float h, boolean focused) {
        shape.begin(ShapeRenderer.ShapeType.Filled);
        setColor(shape, focused ? C_FIELD_FOCUS : C_FIELD_BG);
        shape.rect(x, y, w, h);
        shape.end();
        shape.begin(ShapeRenderer.ShapeType.Line);
        setColor(shape, focused ? C_BORDER : new Color(0.35f, 0.35f, 0.45f, 1f));
        shape.rect(x, y, w, h);
        shape.end();
    }

    private static void setColor(ShapeRenderer sr, Color c) {
        sr.setColor(c.r, c.g, c.b, c.a);
    }
}