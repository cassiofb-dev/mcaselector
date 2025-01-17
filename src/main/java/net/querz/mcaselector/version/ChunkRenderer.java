package net.querz.mcaselector.version;

import net.querz.nbt.tag.CompoundTag;

public interface ChunkRenderer {

	void drawChunk(CompoundTag root, ColorMapping colorMapping, int x, int z, int[] pixelBuffer, int[] waterPixels, short[] terrainHeights, short[] waterHeights, boolean water);
}
