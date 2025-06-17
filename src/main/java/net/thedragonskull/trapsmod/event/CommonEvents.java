package net.thedragonskull.trapsmod.event;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.thedragonskull.trapsmod.TrapsMod;
import net.thedragonskull.trapsmod.trap_variants.TrapTemptGoal;
import net.thedragonskull.trapsmod.trap_variants.TrapTemptRegistry;

@Mod.EventBusSubscriber(modid = TrapsMod.MOD_ID)
public class CommonEvents {

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof PathfinderMob mob)) return;

        EntityType<?> type = mob.getType();

        if (TrapTemptRegistry.hasTemptItem(type)) {
            Ingredient ingredient = TrapTemptRegistry.getTemptIngredientFor(type);
            mob.goalSelector.addGoal(10, new TrapTemptGoal(mob, 1.0D, ingredient));
        }
    }


}
