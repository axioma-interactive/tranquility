package net.axiomainteractive.tranquility.util

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object RedstoneChargerManager {
    // Map of BlockPos to expiration time (world time)
    private val chargedBlocks = mutableMapOf<Pair<String, BlockPos>, Long>()

    fun charge(world: World, pos: BlockPos, ticks: Int) {
        if (world.isClient) return
        val expirationTime = world.time + ticks
        val key = Pair(world.registryKey.value.toString(), pos.toImmutable())
        chargedBlocks[key] = expirationTime
    }

    fun isCharged(world: World, pos: BlockPos): Boolean {
        if (world.isClient) return false
        val key = Pair(world.registryKey.value.toString(), pos.toImmutable())
        val expirationTime = chargedBlocks[key] ?: return false
        
        // Use >= to ensure it expires exactly at the intended time
        if (world.time >= expirationTime) {
            return false
        }
        return true
    }

    fun tick(server: net.minecraft.server.MinecraftServer) {
        val toRemove = mutableListOf<Pair<String, BlockPos>>()
        
        for ((key, expirationTime) in chargedBlocks) {
            val worldKey = key.first
            val world = server.worlds.find { it.registryKey.value.toString() == worldKey }
            // Remove if world no longer exists or time has reached expiration
            if (world == null || world.time >= expirationTime) {
                toRemove.add(key)
            }
        }
        
        for (key in toRemove) {
            chargedBlocks.remove(key)
            val worldKey = key.first
            val pos = key.second
            val world = server.worlds.find { it.registryKey.value.toString() == worldKey }
            if (world != null) {
                val state = world.getBlockState(pos)
                // Force recalculation to turn off components properly
                world.updateNeighbors(pos, state.block)
                // Use AIR to avoid DoorBlock self-update suppression
                state.neighborUpdate(world, pos, net.minecraft.block.Blocks.AIR, null, false)
            }
        }
    }
}
