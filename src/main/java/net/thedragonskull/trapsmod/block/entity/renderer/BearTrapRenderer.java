package net.thedragonskull.trapsmod.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.thedragonskull.trapsmod.block.custom.BearTrap;
import net.thedragonskull.trapsmod.block.entity.BearTrapBE;
import net.thedragonskull.trapsmod.block.entity.model.BearTrapModel;
import net.thedragonskull.trapsmod.util.ModTags;
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

    @Override
    public void actuallyRender(PoseStack poseStack, BearTrapBE animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        ItemStack trapItem = animatable.getTrapItem();
        if (!trapItem.isEmpty() && trapItem.is(ModTags.Items.VALID_TRAP_ITEMS)) {

            poseStack.pushPose();

            boolean isBlock = trapItem.getItem() instanceof BlockItem;
            double yOffset = isBlock ? 0 : 0.10;

            poseStack.translate(0, yOffset, 0);
            poseStack.scale(0.75F, 0.75F, 0.75F);

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    animatable.getTrapItem(),
                    ItemDisplayContext.GROUND,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    bufferSource,
                    null,
                    0
            );

            poseStack.popPose();
        }

    }
}
