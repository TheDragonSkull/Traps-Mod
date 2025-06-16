package net.thedragonskull.trapsmod.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.thedragonskull.trapsmod.block.custom.BearTrap;
import net.thedragonskull.trapsmod.block.entity.BearTrapBE;
import net.thedragonskull.trapsmod.block.entity.model.BearTrapModel;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class BearTrapRenderer extends GeoBlockRenderer<BearTrapBE> {

    public BearTrapRenderer(BlockEntityRendererProvider.Context ctx) {
        super(new BearTrapModel());
    }

    @Override
    public void preRender(PoseStack poseStack, BearTrapBE animatable, BakedGeoModel model, MultiBufferSource bufferSource,
                          VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                          float red, float green, float blue, float alpha) {

        if (animatable.getBlockState().getValue(BearTrap.BURIED)) {
            poseStack.translate(0.0D, -0.13D, 0.0D);
        }

        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

}
