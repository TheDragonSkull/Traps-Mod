package net.thedragonskull.trapsmod.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.thedragonskull.trapsmod.block.custom.PunjiSticksPlank;
import net.thedragonskull.trapsmod.block.custom.properties.PlankPart;
import net.thedragonskull.trapsmod.block.entity.BearTrapBE;
import net.thedragonskull.trapsmod.block.entity.PunjiSticksPlankBE;
import net.thedragonskull.trapsmod.block.entity.model.BearTrapModel;
import net.thedragonskull.trapsmod.block.entity.model.PunjiSticksPlankModel;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class BearTrapRenderer extends GeoBlockRenderer<BearTrapBE> {

    public BearTrapRenderer(BlockEntityRendererProvider.Context ctx) {
        super(new BearTrapModel());
    }
}
