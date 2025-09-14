# Paths of assets for development

### `tranquility/blocks`

```json
{
  "model": {
    "type": "minecraft:model",
    "model": "tranquility:block/aluminium_block"
  }
}
```
### `tranquility/blockstates`

```json
{
  "variants": {
    "": {
      "model": "tranquility:block/aluminium_block"
    }
  }
}
```

### `tranquility/items`

```json
{
  "model": {
    "type": "minecraft:model",
    "model": "tranquility:block/aluminium_block"
  }
}
```

### `tranquility/models/block`
```json
{
  "parent": "minecraft:block/cube_all",
    "textures": {
      "all": "tranquility:block/aluminium_block"
  }
}
```
### `tranquility/models/item`
```json
{
  "parent": "tranquility:block/aluminium_block"
}
```

### `ModBlocks.kt`

```kt
    val ALUMINIUM_BLOCK: Block = registerBlock("aluminium_block", AbstractBlock.Settings.create()
        .strength(4f)
        .requiresTool()
        .sounds(BlockSoundGroup.IRON)
    )
```

### `data/tranquility/loot_table/blocks`

```json
{
  "type": "minecraft:block",
  "pools": [
    {
      "bonus_rolls": 0.0,
      "conditions": [
        {
          "condition": "minecraft:survives_explosion"
        }
      ],
      "entries": [
        {
          "type": "minecraft:item",
          "name": "tranquility:masonic_cobblestone"
        }
      ],
      "rolls": 1.0
    }

  ]
}
```