package com.binaris.oneslib;

import com.binaris.oneslib.client.config.OnesConfig;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(OnesLib.MOD_ID)
public final class OnesLib {

    public static final String MOD_ID = "oneslib";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OnesLib(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, OnesConfig.SPEC);
        Ones.bootstrap(modBus);
        LOGGER.info("Ones Lib initialized.");
    }
}
