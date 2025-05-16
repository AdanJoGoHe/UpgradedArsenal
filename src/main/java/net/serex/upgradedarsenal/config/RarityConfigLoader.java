package net.serex.upgradedarsenal.config;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.serex.upgradedarsenal.modifier.Modifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class RarityConfigLoader implements PreparableReloadListener {
    private static final Gson GSON = new Gson();

    public static class RarityData {
        public String color;
        public int weight;
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager resourceManager,
                                          ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler,
                                          Executor backgroundExecutor, Executor gameExecutor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, RarityData> result = new HashMap<>();
            try {
                ResourceLocation id = new ResourceLocation("upgradedarsenal:rarity_config/rarities.json");
                var optional = resourceManager.getResource(id);
                if (optional.isPresent()) {
                    try (InputStream in = optional.get().open()) {
                        Type type = new TypeToken<Map<String, RarityData>>() {}.getType();
                        result = GSON.fromJson(new InputStreamReader(in), type);
                    }
                }
            } catch (Exception e) {
                System.err.println("[RarityConfigLoader] Error leyendo configuración de rarezas: " + e.getMessage());
            }
            return result;
        }, backgroundExecutor).thenCompose(barrier::wait).thenAcceptAsync(rarityMap -> {
            for (Map.Entry<String, RarityData> entry : rarityMap.entrySet()) {
                try {
                    Modifier.Rarity rarity = Modifier.Rarity.valueOf(entry.getKey().toUpperCase());
                    rarity.setWeight(entry.getValue().weight);
                    ChatFormatting parsedColor = ChatFormatting.getByName(entry.getValue().color.toUpperCase());
                    if (parsedColor == null) {
                        System.err.println("[RarityConfigLoader] Color inválido para " + entry.getKey() + ": " + entry.getValue().color + ". Usando GRAY por defecto.");
                        parsedColor = ChatFormatting.GRAY;
                    }
                    rarity.setColor(parsedColor);
                    System.out.println("[RarityConfigLoader] Configurada: " + entry.getKey());
                } catch (Exception e) {
                    System.err.println("[RarityConfigLoader] Error en " + entry.getKey() + ": " + e.getMessage());
                }
            }
        }, gameExecutor);
    }
}

