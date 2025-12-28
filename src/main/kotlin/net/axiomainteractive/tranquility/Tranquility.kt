package net.axiomainteractive.tranquility

import net.axiomainteractive.tranquility.block.ModBlocks
import net.axiomainteractive.tranquility.block.entity.ModBlockEntities
import net.axiomainteractive.tranquility.item.ModItemGroups
import net.axiomainteractive.tranquility.item.ModItems
import net.axiomainteractive.tranquility.screen.ModScreenHandlers
import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object Tranquility : ModInitializer {
    val MOD_ID = "tranquility"
    val logger = LoggerFactory.getLogger(MOD_ID)

	override fun onInitialize() {
        ModItems.registerModItems()
        ModBlocks.registerModBlocks()
        ModItemGroups.registerItemGroups()
        ModBlockEntities.registerBlockEntities()
        ModScreenHandlers.registerScreenHandlers()
		logger.info("Hello Fabric world!")

        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register { server ->
            net.axiomainteractive.tranquility.util.RedstoneChargerManager.tick(server)
        }

        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register { player, world, hand, hitResult ->
            val stack = player.getStackInHand(hand)
            if (stack.item is net.axiomainteractive.tranquility.item.ChargedRedstoneDustItem) {
                val pos = hitResult.blockPos
                val state = world.getBlockState(pos)
                
                // Redstone functionality check
                val isRedstoneFunctional = state.emitsRedstonePower() || 
                                            state.block is net.minecraft.block.RedstoneWireBlock ||
                                            state.block is net.minecraft.block.RedstoneLampBlock ||
                                            state.block is net.minecraft.block.PistonBlock ||
                                            state.block is net.minecraft.block.AbstractRedstoneGateBlock ||
                                            state.block is net.minecraft.block.TntBlock ||
                                            state.block is net.minecraft.block.DispenserBlock ||
                                            state.block is net.minecraft.block.DropperBlock ||
                                            state.block is net.minecraft.block.AbstractRailBlock ||
                                            state.block is net.minecraft.block.DoorBlock ||
                                            state.block is net.minecraft.block.TrapdoorBlock ||
                                            state.block is net.minecraft.block.PillarBlock && state.isOf(net.minecraft.block.Blocks.PISTON) // Extra safety

                if (isRedstoneFunctional) {
                    if (player.itemCooldownManager.isCoolingDown(stack)) {
                        return@register net.minecraft.util.ActionResult.FAIL
                    }

                    if (!world.isClient) {
                        logger.info("Charging functional block: ${state.block}")
                        // Check for Redstone Block explosion
                        if (state.isOf(net.minecraft.block.Blocks.REDSTONE_BLOCK)) {
                            world.createExplosion(null, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, 6.0f, net.minecraft.world.World.ExplosionSourceType.BLOCK)
                        } else {
                            // Professional charging logic
                            net.axiomainteractive.tranquility.util.RedstoneChargerManager.charge(world, pos, 20)
                            
                            // Notify neighbors
                            world.updateNeighbors(pos, state.block)
                            // CRITICAL: Notify the block itself so it can react immediately
                            // We use AIR as the source block because DoorBlock ignores updates from itself
                            state.neighborUpdate(world, pos, net.minecraft.block.Blocks.AIR, null, false)

                            // Special handling for DoubleBlocks (like Doors)
                            if (state.block is net.minecraft.block.DoorBlock) {
                                val half = state.get(net.minecraft.block.DoorBlock.HALF)
                                val otherHalfPos = if (half == net.minecraft.block.enums.DoubleBlockHalf.LOWER) pos.up() else pos.down()
                                val otherHalfState = world.getBlockState(otherHalfPos)
                                
                                logger.info("Door detected (${half}), attempting to charge other half at $otherHalfPos")
                                
                                if (otherHalfState.isOf(state.block)) {
                                    net.axiomainteractive.tranquility.util.RedstoneChargerManager.charge(world, otherHalfPos, 20)
                                    world.updateNeighbors(otherHalfPos, otherHalfState.block)
                                    otherHalfState.neighborUpdate(world, otherHalfPos, net.minecraft.block.Blocks.AIR, null, false)
                                    logger.info("Charged other half of door successfully")
                                }
                            }
                        }
                        
                        world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 2.0f)
                        
                        player.itemCooldownManager.set(stack, 20)

                        if (!player.isCreative) {
                            stack.decrement(1)
                        }
                    }
                    return@register net.minecraft.util.ActionResult.SUCCESS
                } else {
                    return@register net.minecraft.util.ActionResult.PASS
                }
            } else {
                return@register net.minecraft.util.ActionResult.PASS
            }
        }

        net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.register { player, world, hand, entity, hitResult ->
            val stack = player.getStackInHand(hand)
            if (stack.item is net.axiomainteractive.tranquility.item.ChargedRedstoneDustItem && entity is net.minecraft.entity.mob.CreeperEntity) {
                if (player.itemCooldownManager.isCoolingDown(stack)) {
                    return@register net.minecraft.util.ActionResult.FAIL
                }

                if (!world.isClient) {
                    // Charge the creeper
                    val chargedData = net.axiomainteractive.tranquility.mixin.CreeperEntityAccessor.getChargedTrackedData()
                    entity.dataTracker.set(chargedData, true)

                    world.playSound(null, entity.blockPos, net.minecraft.sound.SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, net.minecraft.sound.SoundCategory.NEUTRAL, 1.0f, 2.0f)
                    
                    player.itemCooldownManager.set(stack, 20)

                    if (!player.isCreative) {
                        stack.decrement(1)
                    }
                }
                return@register net.minecraft.util.ActionResult.SUCCESS
            }
            net.minecraft.util.ActionResult.PASS
        }
	}
}