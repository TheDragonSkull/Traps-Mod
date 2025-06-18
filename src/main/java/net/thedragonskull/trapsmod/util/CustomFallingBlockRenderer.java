package net.thedragonskull.trapsmod.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CustomFallingBlockRenderer extends EntityRenderer<FallingBlockEntity> {

    private final BlockRenderDispatcher blockRenderer;

    public CustomFallingBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
    }

    // Changes stained glass panes render type to cutout while falling block so don't appear invisible
    @Override
    public void render(FallingBlockEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {

        BlockState state = entity.getBlockState();

        if (state.getRenderShape() != RenderShape.MODEL) return;

        poseStack.pushPose();
        poseStack.translate(-0.5, 0.0, -0.5);

        RenderType overrideRenderType = state.getBlock() instanceof StainedGlassPaneBlock
                ? RenderType.cutout()
                : null;

        blockRenderer.renderSingleBlock(
                state,
                poseStack,
                bufferSource,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                net.minecraftforge.client.model.data.ModelData.EMPTY,
                overrideRenderType
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FallingBlockEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}


