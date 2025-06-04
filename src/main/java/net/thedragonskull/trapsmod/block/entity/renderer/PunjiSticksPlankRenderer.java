package net.thedragonskull.trapsmod.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.thedragonskull.trapsmod.block.custom.PunjiSticksPlank;
import net.thedragonskull.trapsmod.block.custom.properties.PlankPart;
import net.thedragonskull.trapsmod.block.entity.PunjiSticksPlankBE;
import net.thedragonskull.trapsmod.block.entity.model.PunjiSticksPlankModel;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class PunjiSticksPlankRenderer extends GeoBlockRenderer<PunjiSticksPlankBE> {

    public PunjiSticksPlankRenderer(BlockEntityRendererProvider.Context ctx) {
        super(new PunjiSticksPlankModel());
    }

    @Override
    public void actuallyRender(PoseStack poseStack, PunjiSticksPlankBE animatable,
                               BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource,
                               VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {

        if (animatable.getBlockState().getValue(PunjiSticksPlank.PLANK_PART) != PlankPart.BASE)
            return;

        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
