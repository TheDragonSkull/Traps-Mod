package net.thedragonskull.trapsmod.block.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.thedragonskull.trapsmod.TrapsMod;
import net.thedragonskull.trapsmod.block.entity.PunjiSticksPlankBE;
import software.bernie.geckolib.model.GeoModel;

public class PunjiSticksPlankModel extends GeoModel<PunjiSticksPlankBE> {

    @Override
    public ResourceLocation getModelResource(PunjiSticksPlankBE animatable) {
        return ResourceLocation.fromNamespaceAndPath(TrapsMod.MOD_ID, "geo/punji_sticks_plank.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PunjiSticksPlankBE animatable) {
        return ResourceLocation.fromNamespaceAndPath(TrapsMod.MOD_ID, "textures/block/punji_sticks_plank.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PunjiSticksPlankBE animatable) {
        return ResourceLocation.fromNamespaceAndPath(TrapsMod.MOD_ID, "animations/punji_sticks_plank.animation.json");
    }

}
