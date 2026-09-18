package com.binaris.oneslib.test;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(TestMod.MOD_ID)
public final class TestMod {

    public static final String MOD_ID = "oneslib_test";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TestMod() {
        TestOnes.register();
        LOGGER.info("Ones Lib testmod initialized.");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
