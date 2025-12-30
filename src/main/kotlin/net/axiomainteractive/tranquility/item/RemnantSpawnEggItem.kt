package net.axiomainteractive.tranquility.item

import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnReason
import net.minecraft.entity.mob.MobEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.item.SpawnEggItem
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.math.Direction

class RemnantSpawnEggItem(private val type: EntityType<out MobEntity>, settings: Settings) : SpawnEggItem(type, settings) {

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        val result = super.useOnBlock(context)
        
        if (result == ActionResult.CONSUME || result == ActionResult.SUCCESS) {
             // Spawn 2 more!
             val world = context.world
             if (world is ServerWorld) {
                 val pos = context.blockPos.offset(context.side)
                 
                 // Spawn #2
                 this.type.spawnFromItemStack(world, context.stack, context.player, pos, SpawnReason.TRIGGERED, true, !context.side.equals(Direction.UP))
                 
                 // Spawn #3
                 this.type.spawnFromItemStack(world, context.stack, context.player, pos, SpawnReason.TRIGGERED, true, !context.side.equals(Direction.UP))
             }
        }
        
        return result
    }
}
