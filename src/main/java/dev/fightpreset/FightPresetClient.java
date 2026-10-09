package dev.fightpreset;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class FightPresetClient implements ClientModInitializer {
    public static boolean overlayEnabled = true;
    public static KeyBinding toggleKey;
    public static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fight_preset.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H,
                KeyBinding.Category.MISC));
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fight_preset.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J,
                KeyBinding.Category.MISC));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                overlayEnabled = !overlayEnabled;
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("Fight Preset overlay: " + (overlayEnabled ? "ON" : "OFF")), true);
                }
            }
            while (menuKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new PresetManagerScreen());
            }
        });
        PresetStore.load();
    }
}
