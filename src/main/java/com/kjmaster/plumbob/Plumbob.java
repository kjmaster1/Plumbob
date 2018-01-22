package com.kjmaster.plumbob;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

import java.awt.*;

@Mod(modid = Plumbob.MODID, name = Plumbob.NAME, version = Plumbob.VERSION, clientSideOnly = true)
public class Plumbob
{
    public static final String MODID = "plumbob";
    public static final String NAME = "Plumbob";
    public static final String VERSION = "1.0.1";

    public static Logger LOGGER;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        LOGGER = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new PlumbobRender());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        PlumbobRender.gemColour = new Color(PlumbobConfig.defaultColour);
    }
}
