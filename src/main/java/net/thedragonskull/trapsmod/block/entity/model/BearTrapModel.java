package net.thedragonskull.trapsmod.block.entity.model;

import net.minecraft.resources.ResourceLocation;
import net.thedragonskull.trapsmod.TrapsMod;
import net.thedragonskull.trapsmod.block.entity.BearTrapBE;
import software.bernie.geckolib.model.GeoModel;

public class BearTrapModel extends GeoModel<BearTrapBE> {

    @Override
    public ResourceLocation getModelResource(BearTrapBE animatable) {
        return ResourceLocation.fromNamespaceAndPath(TrapsMod.MOD_ID, "geo/bear_trap.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BearTrapBE animatable) {
        return ResourceLocation.fromNamespaceAndPath(TrapsMod.MOD_ID, "textures/block/bear_trap.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BearTrapBE animatable) {
        return ResourceLocation.fromNamespaceAndPath(TrapsMod.MOD_ID, "animations/bear_trap.animation.json");
    }

}
