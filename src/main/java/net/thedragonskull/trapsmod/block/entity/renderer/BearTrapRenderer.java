package net.thedragonskull.trapsmod.block.entity.renderer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.thedragonskull.trapsmod.block.entity.BearTrapBE;
import net.thedragonskull.trapsmod.block.entity.model.BearTrapModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class BearTrapRenderer extends GeoBlockRenderer<BearTrapBE> {

    public BearTrapRenderer(BlockEntityRendererProvider.Context ctx) {
        super(new BearTrapModel());
    }
}
