package net.axiomainteractive.tranquility.world.gen.chunk

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.block.Blocks
import net.axiomainteractive.tranquility.block.ModBlocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.ChunkRegion
import net.minecraft.world.HeightLimitView
import net.minecraft.world.Heightmap
import net.minecraft.world.biome.source.BiomeAccess
import net.minecraft.world.biome.source.BiomeSource
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.StructureWorldAccess
import net.minecraft.world.gen.StructureAccessor
import net.minecraft.world.gen.chunk.Blender
import net.minecraft.world.gen.chunk.ChunkGenerator
import net.minecraft.world.gen.chunk.VerticalBlockSample
import net.minecraft.world.gen.noise.NoiseConfig
import java.util.concurrent.CompletableFuture
import kotlin.math.abs

class MengerSpongeChunkGenerator(biomeSource: BiomeSource) : ChunkGenerator(biomeSource) {
    companion object {
        val CODEC: MapCodec<MengerSpongeChunkGenerator> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                BiomeSource.CODEC.fieldOf("biome_source").forGetter { it.biomeSource }
            ).apply(instance, ::MengerSpongeChunkGenerator)
        }
    }

    override fun getCodec(): MapCodec<out ChunkGenerator> {
        return CODEC
    }

    override fun carve(
        chunkRegion: ChunkRegion,
        seed: Long,
        noiseConfig: NoiseConfig,
        biomeAccess: BiomeAccess,
        structureAccessor: StructureAccessor,
        chunk: Chunk,
    ) {
    }

    override fun buildSurface(
        region: ChunkRegion,
        structureAccessor: StructureAccessor,
        noiseConfig: NoiseConfig,
        chunk: Chunk
    ) {
    }

    override fun generateFeatures(
        world: StructureWorldAccess,
        chunk: Chunk,
        structureAccessor: StructureAccessor
    ) {
    }

    override fun populateEntities(region: ChunkRegion) {
    }

    override fun getWorldHeight(): Int {
        return 464
    }

    override fun populateNoise(
        blender: Blender,
        noiseConfig: NoiseConfig,
        structureAccessor: StructureAccessor,
        chunk: Chunk
    ): CompletableFuture<Chunk> {
        val chunkPos = chunk.pos
        val startX = chunkPos.startX
        val startZ = chunkPos.startZ
        
        val mutablePos = BlockPos.Mutable()
        val aluminiumBlock = ModBlocks.ALUMINIUM_BLOCK.defaultState
        val bedrock = Blocks.BEDROCK.defaultState
        val stone = Blocks.STONE.defaultState
        val water = Blocks.WATER.defaultState
        val air = Blocks.AIR.defaultState

        // Standard world height -64 to 320 -> Extended to 400
        val minY = -64
        val maxY = 400

        for (x in 0 until 16) {
            for (z in 0 until 16) {
                for (y in minY until maxY) {
                    val worldX = startX + x
                    val worldY = y
                    val worldZ = startZ + z
                    
                    mutablePos.set(x, y, z)
                    
                    if (y == 0) {
                        chunk.setBlockState(mutablePos, bedrock, 0)
                    } else if (y >= 1 && y <= 69) {
                        chunk.setBlockState(mutablePos, stone, 0)
                    } else if (y == 70) {
                         chunk.setBlockState(mutablePos, water, 0)
                    } else if (y >= 222) {
                         // Fractal starts after 150 blocks gap.
                         // Align Y to start at 0 relative to 222.
                         // Scale by 3 to shrink the fractal features (make them solid at level 1, and map level N+1 to world level N).
                        if (isMengerSponge(worldX * 3, (worldY - 222) * 3, worldZ * 3)) {
                            chunk.setBlockState(mutablePos, aluminiumBlock, 0)
                        }
                    }
                    // Else it's air by default
                }
            }
        }

        return CompletableFuture.completedFuture(chunk)
    }

    private fun isMengerSponge(x: Int, y: Int, z: Int): Boolean {
        var tx = abs(x)
        var ty = abs(y)
        var tz = abs(z)

        while (tx > 0 || ty > 0 || tz > 0) {
            var ones = 0
            if (tx % 3 == 1) ones++
            if (ty % 3 == 1) ones++
            if (tz % 3 == 1) ones++

            if (ones >= 2) return false // It's a hole

            tx /= 3
            ty /= 3
            tz /= 3
        }
        return true // It's solid
    }

    override fun getSeaLevel(): Int {
        return 70
    }

    override fun getMinimumY(): Int {
        return -64
    }

    override fun getHeight(
        x: Int,
        z: Int,
        heightmap: Heightmap.Type,
        world: HeightLimitView,
        noiseConfig: NoiseConfig
    ): Int {
        for (y in 399 downTo 222) {
             if (isMengerSponge(x * 3, (y - 222) * 3, z * 3)) return y
        }
        // If no fractal hit, check floor
        if (70 >= -64) return 70 // Water level
        return -64
    }

    override fun getColumnSample(
        x: Int,
        z: Int,
        world: HeightLimitView,
        noiseConfig: NoiseConfig
    ): VerticalBlockSample {
        return VerticalBlockSample(-64, arrayOf(Blocks.AIR.defaultState))
    }

    override fun appendDebugHudText(
        text: MutableList<String>,
        noiseConfig: NoiseConfig,
        pos: BlockPos
    ) {
    }
}
