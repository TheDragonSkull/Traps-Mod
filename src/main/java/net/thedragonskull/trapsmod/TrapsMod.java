package net.thedragonskull.trapsmod;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.thedragonskull.trapsmod.block.ModBlocks;
import net.thedragonskull.trapsmod.block.entity.ModBlockEntities;
import net.thedragonskull.trapsmod.block.entity.renderer.BearTrapRenderer;
import net.thedragonskull.trapsmod.block.entity.renderer.PunjiSticksPlankRenderer;
import net.thedragonskull.trapsmod.item.ModCreativeModeTabs;
import net.thedragonskull.trapsmod.item.ModItems;
import net.thedragonskull.trapsmod.network.PacketHandler;
import net.thedragonskull.trapsmod.sound.ModSounds;
import net.thedragonskull.trapsmod.trap_variants.BearTrapExclusionRegistry;
import net.thedragonskull.trapsmod.trap_variants.ModBearTrapVariants;
import net.thedragonskull.trapsmod.trap_variants.ModTrapExclusions;
import net.thedragonskull.trapsmod.trap_variants.ModTrapTemptData;
import net.thedragonskull.trapsmod.util.CustomFallingBlockRenderer;
import org.slf4j.Logger;

@Mod(TrapsMod.MOD_ID)
public class TrapsMod
{
    public static final String MOD_ID = "trapsmod";
    private static final Logger LOGGER = LogUtils.getLogger();

    public TrapsMod(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModSounds.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            event.enqueueWork(PacketHandler::register);
            event.enqueueWork(ModBearTrapVariants::registerVariant);
            event.enqueueWork(ModTrapTemptData::registerDefaults);
            event.enqueueWork(ModTrapExclusions::registerDefaults);
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                BlockEntityRenderers.register(ModBlockEntities.PUNJI_STICKS_PLANK_BE.get(), PunjiSticksPlankRenderer::new);
                BlockEntityRenderers.register(ModBlockEntities.BEAR_TRAP_BE.get(), BearTrapRenderer::new);

                EntityRenderers.register(EntityType.FALLING_BLOCK, CustomFallingBlockRenderer::new);
            });

        }
    }
}
