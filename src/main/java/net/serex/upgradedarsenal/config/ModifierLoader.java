package net.serex.upgradedarsenal.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;
import net.serex.upgradedarsenal.ModifierJson;
import net.serex.upgradedarsenal.modifier.Modifier;
import net.serex.upgradedarsenal.modifier.Modifiers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ModifierLoader implements PreparableReloadListener {
    private static final Gson GSON = new Gson();

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager resourceManager,
                                          ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler,
                                          Executor backgroundExecutor, Executor gameExecutor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<ResourceLocation, JsonObject> jsonObjects = new HashMap<>();
            String folder = "modifiers"; // Carpeta dentro de data/<namespace>/

            try {
                var resources = resourceManager.listResources(folder, fileName -> fileName.toString().endsWith(".json"));

                for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
                    try (InputStream stream = entry.getValue().open()) {
                        System.out.println("loaded: ");
                        System.out.println(entry.getKey());
                        System.out.println(entry.getValue());

                        JsonObject json = GSON.fromJson(new InputStreamReader(stream), JsonObject.class);
                        jsonObjects.put(entry.getKey(), json);
                    }
                }
            } catch (IOException e) {
                System.err.println("[ModifierLoader] Error leyendo modificadores: " + e.getMessage());
            }
            return jsonObjects;
        }, backgroundExecutor).thenCompose(barrier::wait).thenAcceptAsync(jsonObjects -> {
            Modifiers.clearPools();
            for (Map.Entry<ResourceLocation, JsonObject> entry : jsonObjects.entrySet()) {
                try {
                    ModifierJson json = GSON.fromJson(entry.getValue(), ModifierJson.class);

                    Modifier.ModifierBuilder builder = new Modifier.ModifierBuilder(
                            new ResourceLocation(entry.getKey().getNamespace(), json.id),
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
                    System.out.println("[ModifierLoader] Cargado: " + json.id);
                } catch (Exception e) {
                    System.err.println("[ModifierLoader] Fallo en: " + entry.getKey() + " -> " + e.getMessage());
                }
            }
        }, gameExecutor);
    }
}
