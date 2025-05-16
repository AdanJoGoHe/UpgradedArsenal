package net.serex.upgradedarsenal.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;
import net.serex.upgradedarsenal.Main;
import net.serex.upgradedarsenal.ModifierJson;
import net.serex.upgradedarsenal.modifier.Modifier;
import net.serex.upgradedarsenal.modifier.Modifiers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class ModifierLoader {
    private static final Gson GSON = new Gson();
    private static final String MODIFIERS_PATH = "data/itemmodifiers/modifiers/";

    public static void loadAll() {
        System.out.println("[ModifierLoader] Loading embedded modifiers...");
        Modifiers.clearPools();

        try {
            URL url = ModifierLoader.class.getClassLoader().getResource(MODIFIERS_PATH);
            if (url == null) {
                System.err.println("[ModifierLoader] Modifiers path not found: " + MODIFIERS_PATH);
                return;
            }

            URI uri = url.toURI();
            Path root;

            if ("jar".equals(uri.getScheme())) {
                FileSystem fs = FileSystems.newFileSystem(uri, Map.of());
                root = fs.getPath(MODIFIERS_PATH);
            } else {
                root = Paths.get(uri);
            }

            Files.walk(root).filter(p -> p.toString().endsWith(".json")).forEach(path -> {
                try (InputStream stream = Files.newInputStream(path);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {

                    JsonObject json = GSON.fromJson(reader, JsonObject.class);
                    loadModifierFromJson(json, path.getFileName().toString());

                } catch (Exception e) {
                    System.err.println("[ModifierLoader] Error reading: " + path + " → " + e.getMessage());
                }
            });

        } catch (Exception e) {
            System.err.println("[ModifierLoader] Fatal error loading modifiers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void loadModifierFromJson(JsonObject jsonObject, String fileName) {
        try {
            ModifierJson json = GSON.fromJson(jsonObject, ModifierJson.class);

            Modifier.ModifierBuilder builder = new Modifier.ModifierBuilder(
                    new ResourceLocation(Main.MODID, json.id),
                    json.name,
                    Modifier.ModifierType.valueOf(json.type.toUpperCase())
            ).setRarity(Modifier.Rarity.valueOf(json.rarity.toUpperCase()));

            for (ModifierJson.AttributeJson attr : json.attributes) {
                ResourceLocation attrId = new ResourceLocation(attr.attribute);
                Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(attrId);
                if (attribute != null) {
                    AttributeModifier.Operation op = AttributeModifier.Operation.valueOf(attr.operation.toUpperCase());
                    builder.addModifier(() -> attribute, new Modifier.AttributeModifierSupplier(attr.value, op));
                }
            }

            Modifiers.register(builder.build());
            System.out.println("[ModifierLoader] Loaded: " + json.id);
        } catch (Exception e) {
            System.err.println("[ModifierLoader] Failed to load " + fileName + ": " + e.getMessage());
        }
    }
}

