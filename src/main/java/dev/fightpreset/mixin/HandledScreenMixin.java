package dev.fightpreset.mixin;

import dev.fightpreset.FightPresetClient;
import dev.fightpreset.PresetStore;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void fightPreset$renderOverlay(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!FightPresetClient.overlayEnabled || PresetStore.activePreset < 0 ||
                PresetStore.activePreset >= PresetStore.PRESETS.size()) return;

        HandledScreen<?> screen = (HandledScreen<?>)(Object)this;
        var preset = PresetStore.PRESETS.get(PresetStore.activePreset);
        for (Slot slot : screen.getScreenHandler().slots) {
            if (!(slot.inventory instanceof PlayerInventory)) continue;
            int index = slot.getIndex();
            if (index < 0 || index >= 41) continue;

            ItemStack expected = PresetStore.itemFor(preset, index);
            ItemStack actual = slot.getStack();
          HandledScreenAccessor accessor = (HandledScreenAccessor) screen;
int x = accessor.fightPreset$getX() + slot.x;
int y = accessor.fightPreset$getY() + slot.y;

            if (expected.isEmpty()) {
                if (!actual.isEmpty()) {
                    context.fill(x, y, x + 16, y + 16, 0x66E04444);
                }
                continue;
            }

            if (actual.isEmpty()) {
                // Green means this slot needs the expected item. Draw a dimmed
                // ghost icon so it remains recognizable without hiding the slot.
                context.fill(x, y, x + 16, y + 16, 0x6644DD66);
                context.drawItem(expected, x, y);
                context.fill(x, y, x + 16, y + 16, 0x99000000);
                continue;
            }

            if (ItemStack.areItemsAndComponentsEqual(actual, expected)) {
                context.fill(x, y, x + 16, y + 16, 0x6633CC55);
            } else {
                context.fill(x, y, x + 16, y + 16, 0x66E04444);
                // Ghost expected item is layered on the wrong item; darken it
                // afterward to create a transparent/ghost-like appearance.
                context.drawItem(expected, x, y);
                context.fill(x, y, x + 16, y + 16, 0xAA000000);
            }
        }
    }
}
