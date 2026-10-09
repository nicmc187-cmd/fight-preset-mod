package dev.fightpreset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Type;
import java.util.*;

public final class PresetStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("fight-preset.json");
    public static final List<Preset> PRESETS = new ArrayList<>();
    public static int activePreset = -1;

    private PresetStore() {}

    public static void load() {
        if (!Files.exists(FILE)) return;
        try (Reader reader = Files.newBufferedReader(FILE)) {
            Type type = new TypeToken<List<Preset>>(){}.getType();
            List<Preset> loaded = GSON.fromJson(reader, type);
            if (loaded != null) { PRESETS.clear(); PRESETS.addAll(loaded); }
        } catch (Exception ignored) {}
    }

    public static void saveToDisk() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE)) { GSON.toJson(PRESETS, writer); }
        } catch (Exception ignored) {}
    }

    public static void capture(String name, String icon, net.minecraft.client.network.ClientPlayerEntity player) {
        Preset p = new Preset();
        p.name = name == null || name.isBlank() ? "Fight Preset " + (PRESETS.size() + 1) : name;
        p.icon = icon == null || icon.isBlank() ? "minecraft:totem_of_undying" : icon;
        for (int i = 0; i < 41; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            p.items.put(i, stack.isEmpty() ? "" : Registries.ITEM.getId(stack.getItem()).toString());
        }
        if (activePreset >= 0 && activePreset < PRESETS.size()) PRESETS.set(activePreset, p);
        else { PRESETS.add(p); activePreset = PRESETS.size() - 1; }
        saveToDisk();
    }

    public static ItemStack itemFor(Preset p, int playerInventoryIndex) {
        String id = p.items.get(playerInventoryIndex);
        if (id == null || id.isBlank()) return ItemStack.EMPTY;
        Item item = Registries.ITEM.get(Identifier.tryParse(id));
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    public static class Preset {
        public String name = "Fight Preset";
        public String icon = "minecraft:totem_of_undying";
        public Map<Integer, String> items = new HashMap<>();
    }
}
