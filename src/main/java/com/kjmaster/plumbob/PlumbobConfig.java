package com.kjmaster.plumbob;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

@Config.LangKey("config.plumbob.title")
@Config(modid = Plumbob.MODID, name = "Plumbob", category = "All Settings")
public class PlumbobConfig
{
    @Config.LangKey("config.plumbob.colours")
    @Config.Comment("RGB int values can be listed here")
    public static int[] colours = {Color.BLUE.getRGB(),
            Color.CYAN.getRGB(),
            Color.YELLOW.getRGB(),
            Color.GREEN.getRGB(),
            Color.WHITE.getRGB(),
            Color.ORANGE.getRGB(),
            Color.RED.getRGB()};

    @Config.LangKey("config.plumbob.defaultColour")
    @Config.Comment("RGB int value for default colour")
    public static int defaultColour = Color.GREEN.getRGB();

    @Config.LangKey("config.plumbob.changeColour")
    @Config.Comment("Enable/Disable the changing of colour. Health based must be false")
    public static boolean changeColour = true;

    @Config.LangKey("config.plumbob.healthBased")
    @Config.Comment("Toggle the changing of plumbob colour based on health. Change colour must be false")
    public static boolean healthBased = false;

    @Config.LangKey("config.plumbob.doubleHealth")
    @Config.Comment("Toggle double health mode for games where players have 40 hearts")
    public static boolean doubleHealth = false;

    @Config.LangKey("config.plumbob.ticks")
    @Config.Comment("Amount of render ticks for each colour change")
    @Config.RangeInt(min = 1)
    public static int ticks = 500;

    @Config.LangKey("config.plumbob.yOffset")
    @Config.Comment("Change the y offset for diamond")
    @Config.RangeDouble(min = 2.4, max = 5.4)
    public static double yOffset = 2.4;

    @Config.LangKey("config.plumbob.disMinSpin")
    @Config.Comment("Change the distance required for minimum spin")
    @Config.RangeDouble(min = 1.0, max = 64.0)
    public static double DISTANCE_FOR_MIN_SPIN = 64.0;

    @Config.RangeDouble(min = 1.0, max = 64.0)
    @Config.LangKey("config.plumbob.disMaxSpin")
    @Config.Comment("Change the distance required for max spin")
    public static double DISTANCE_FOR_MAX_SPIN = 32.0;

    @Config.RangeDouble(min = 0.0, max = 64.0)
    @Config.LangKey("config.plumbob.disMinLev")
    @Config.Comment("Change the distance required for min levitation")
    public static double DISTANCE_FOR_MIN_LEVITATE = 0.0;

    @Config.RangeDouble(min = 0.0, max = 64.0)
    @Config.LangKey("config.plumbob.disMaxLev")
    @Config.Comment("Change the distance required for max levitation")
    public static double DISTANCE_FOR_MAX_LEVITATE = 64.0;


    @Config.RangeDouble(min = 0.0, max = 1.0)
    @Config.LangKey("config.plumbob.minLevHeight")
    @Config.Comment("Change the minimum levitation height")
    public static double MIN_LEVITATE_HEIGHT = 0.0;

    @Config.RangeDouble(min = 0.0, max = 1.0)
    @Config.LangKey("config.plumbob.maxLevHeight")
    @Config.Comment("Change the maximum levitation height")
    public static double MAX_LEVITATE_HEIGHT = 0.5;

    @Mod.EventBusSubscriber(modid = Plumbob.MODID)
    public static class ConfigSyncHandler
    {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
        {
            if (event.getModID().equals(Plumbob.MODID))
            {
                ConfigManager.sync(Plumbob.MODID, Config.Type.INSTANCE);
                PlumbobRender.gemColour = new Color(defaultColour);
            }
        }
    }
}
