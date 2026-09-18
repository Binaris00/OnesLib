package com.binaris.oneslib;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.util.JsonUtil;

class TestmodAssetsTest {

    private static final Path ASSETS =
            Path.of("..", "testmod", "src", "main", "resources", "assets", "oneslib_test");
    private static final Map<String, List<String>> REQUIRED_ANIMATIONS = Map.of(
            "tails", List.of("idle", "walk", "attack", "fly"),
            "evil_hulk", List.of("idle", "walk", "attack", "attack2", "attack3"),
            "siren_head", List.of("idle", "walk", "beam"),
            "dragon", List.of("idle", "walk", "attack", "fire_breathe"),
            "giant_ogre", List.of("idle", "walk", "attack", "grab and bite"),
            "imp", List.of("idle", "walk", "attack"),
            "foreign", List.of("idle", "walk"));

    @Test
    void modelsParseWithGeckoLib() throws IOException {
        List<Path> files = files("geo/entity");
        assertTrue(!files.isEmpty(), "testmod should ship at least one geo model");

        for (Path file : files) {
            try (Reader reader = Files.newBufferedReader(file)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                Model model = JsonUtil.GEO_GSON.fromJson(root, Model.class);
                assertNotNull(model, file + " should parse as a GeckoLib model");
                assertTrue(model.minecraftGeometry().length > 0, file + " should define geometry");
            }
        }
    }

    @Test
    void animationsParseWithGeckoLib() throws IOException {
        List<Path> files = files("animations/entity");
        assertTrue(!files.isEmpty(), "testmod should ship at least one animation file");

        for (Path file : files) {
            try (Reader reader = Files.newBufferedReader(file)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                BakedAnimations animations = JsonUtil.GEO_GSON.fromJson(root.getAsJsonObject("animations"),
                        BakedAnimations.class);
                assertNotNull(animations, file + " should parse as GeckoLib animations");

                String one = file.getFileName().toString().replace(".animation.json", "");
                for (String name : REQUIRED_ANIMATIONS.getOrDefault(one, List.of("idle", "walk"))) {
                    assertNotNull(animations.getAnimation(name),
                            file + " should contain animation '" + name + "'");
                }
            }
        }
    }

    private static List<Path> files(String directory) throws IOException {
        try (Stream<Path> stream = Files.walk(ASSETS.resolve(directory))) {
            return stream.filter(Files::isRegularFile).toList();
        }
    }
}
