package dev.fightpreset;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Preset manager. Preset rows and the icon picker are clickable; all changes
 * are persisted in config/fight-preset.json.
 */
public class PresetManagerScreen extends Screen {
    private static final List<String> ICONS = List.of(
            "minecraft:totem_of_undying",
            "minecraft:netherite_sword",
            "minecraft:ender_pearl",
            "minecraft:ender_crystal",
            "minecraft:obsidian",
            "minecraft:golden_apple",
            "minecraft:shield",
            "minecraft:bow",
            "minecraft:crossbow",
            "minecraft:netherite_chestplate",
            "minecraft:experience_bottle",
            "minecraft:firework_rocket"
    );

    private TextFieldWidget nameField;
    private TextFieldWidget customIconField;
    private int selected = -1;
    private int iconPage = 0;
    private boolean showIconPicker = false;
    private int listTop;
    private int listBottom;

    public PresetManagerScreen() {
        super(Text.literal("Fight Presets"));
    }

    @Override
    protected void init() {
        clearChildren();
        int cx = width / 2;
        nameField = new TextFieldWidget(textRenderer, cx - 112, 34, 224, 20, Text.literal("Preset name"));
        nameField.setPlaceholder(Text.literal("Preset name"));
        addDrawableChild(nameField);

        customIconField = new TextFieldWidget(textRenderer, cx - 112, 58, 224, 20, Text.literal("Custom item ID"));
        customIconField.setText(currentIcon());
        customIconField.setPlaceholder(Text.literal("minecraft:totem_of_undying"));
        addDrawableChild(customIconField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Choose icon"), b -> {
            showIconPicker = !showIconPicker;
            init();
        }).dimensions(cx - 112, 83, 106, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Save current inventory"), b -> {
            if (client != null && client.player != null) {
                PresetStore.capture(nameField.getText(), customIconField.getText(), client.player);
                selected = PresetStore.activePreset;
                init();
            }
        }).dimensions(cx + 2, 83, 114, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Activate"), b -> {
            if (selected >= 0 && selected < PresetStore.PRESETS.size()) {
                PresetStore.activePreset = selected;
                PresetStore.saveToDisk();
            }
        }).dimensions(cx - 112, 107, 70, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Update name/icon"), b -> {
            if (selected >= 0 && selected < PresetStore.PRESETS.size()) {
                var p = PresetStore.PRESETS.get(selected);
                p.name = nameField.getText().isBlank() ? p.name : nameField.getText();
                p.icon = validIcon(customIconField.getText()) ? customIconField.getText() : p.icon;
                PresetStore.saveToDisk();
                init();
            }
        }).dimensions(cx - 38, 107, 116, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Delete"), b -> {
            if (selected >= 0 && selected < PresetStore.PRESETS.size()) {
                PresetStore.PRESETS.remove(selected);
                if (PresetStore.PRESETS.isEmpty()) PresetStore.activePreset = -1;
                else if (PresetStore.activePreset >= PresetStore.PRESETS.size()) PresetStore.activePreset = PresetStore.PRESETS.size() - 1;
                selected = -1;
                PresetStore.saveToDisk();
                init();
            }
        }).dimensions(cx + 84, 107, 28, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> close())
                .dimensions(cx - 35, height - 27, 70, 20).build());

        listTop = showIconPicker ? 193 : 145;
        listBottom = height - 39;
    }

    private String currentIcon() {
        if (selected >= 0 && selected < PresetStore.PRESETS.size()) {
            return PresetStore.PRESETS.get(selected).icon;
        }
        return "minecraft:totem_of_undying";
    }

    private boolean validIcon(String id) {
        Identifier identifier = Identifier.tryParse(id == null ? "" : id);
        return identifier != null && Registries.ITEM.containsId(identifier);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        int cx = width / 2;
        context.drawCenteredTextWithShadow(textRenderer, title, cx, 12, 0xFFFFFF);
        context.drawTextWithShadow(textRenderer, "Name", cx - 112, 24, 0xBFBFBF);
        context.drawTextWithShadow(textRenderer, "Icon item ID (or choose below)", cx - 112, 48, 0xBFBFBF);
        context.drawTextWithShadow(textRenderer, "Saved presets — click a row to select it", cx - 112, listTop - 13, 0xFFFFFF);

        if (showIconPicker) {
            context.fill(cx - 112, 130, cx + 112, 184, 0xCC20252B);
            context.drawTextWithShadow(textRenderer, "Choose a symbol:", cx - 106, 133, 0xFFFFFF);
            int index = 0;
            for (String id : ICONS) {
                int col = index % 6;
                int row = index / 6;
                int x = cx - 104 + col * 36;
                int y = 146 + row * 22;
                Item item = Registries.ITEM.get(Identifier.tryParse(id));
                if (item != null) {
                    context.fill(x - 2, y - 2, x + 18, y + 18, 0x5533FF66);
                    context.drawItem(new ItemStack(item), x, y);
                    if (id.equals(customIconField.getText())) {
                        context.fill(x - 2, y - 2, x + 18, y + 18, 0x5533DD66);
                    }
                }
                index++;
            }
        }

        int rowHeight = 22;
        int visible = Math.max(0, (listBottom - listTop) / rowHeight);
        for (int i = 0; i < PresetStore.PRESETS.size() && i < visible; i++) {
            var p = PresetStore.PRESETS.get(i);
            int y = listTop + i * rowHeight;
            boolean isSelected = i == selected;
            boolean isActive = i == PresetStore.activePreset;
            context.fill(cx - 112, y, cx + 112, y + 20,
                    isSelected ? 0xAA3B704B : (isActive ? 0x88446A99 : 0x88404040));
            Item icon = Registries.ITEM.get(Identifier.tryParse(p.icon));
            if (icon != null) context.drawItem(new ItemStack(icon), cx - 106, y + 2);
            String label = (isActive ? "● " : "") + p.name;
            context.drawTextWithShadow(textRenderer, label, cx - 84, y + 6,
                    isSelected ? 0xFFB8FFB8 : 0xFFFFFFFF);
            context.drawTextWithShadow(textRenderer, p.items.size() + " slots", cx + 47, y + 6, 0xFFCCCCCC);
        }
        if (PresetStore.PRESETS.isEmpty()) {
            context.drawTextWithShadow(textRenderer, "No presets yet — save your current inventory above.", cx - 108, listTop + 7, 0xFFCCCCCC);
        }
        context.drawTextWithShadow(textRenderer, "H: toggle overlay  |  J: open menu", 8, height - 14, 0xFFB0B0B0);
    }

  ```java
@Override
public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubleClick) {
    double mouseX = click.x();
    double mouseY = click.y();
    int cx = width / 2;

    if (showIconPicker && mouseY >= 144 && mouseY < 184) {
        int col = (int) ((mouseX - (cx - 104)) / 36);
        int row = (int) ((mouseY - 146) / 22);
        int index = row * 6 + col;

        if (col >= 0 && col < 6 && index >= 0 && index < ICONS.size()) {
            customIconField.setText(ICONS.get(index));
            return true;
        }
    }

    int rowHeight = 22;
    int visible = Math.max(0, (listBottom - listTop) / rowHeight);

    for (int i = 0; i < PresetStore.PRESETS.size() && i < visible; i++) {
        int y = listTop + i * rowHeight;

        if (mouseX >= cx - 112 && mouseX <= cx + 112
                && mouseY >= y && mouseY <= y + 20) {
            selected = i;
            var p = PresetStore.PRESETS.get(i);
            nameField.setText(p.name);
            customIconField.setText(p.icon);
            return true;
        }
    }

    return super.mouseClicked(click, doubleClick);
}
```

    @Override
    public boolean shouldPause() {
        return false;
    }
}
