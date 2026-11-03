package dev.hado;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import dev.hado.registry.HadoItems;

/**
 * Seeds the arsenal into vanilla chest loot, so a survival world can find HADO
 * gear without the crafting grid as a gateway.

 * <p>The approach is deliberately flat: a static table of chest keys with
 * (item, weight, count, chance) rows, applied through the fabric loot MODIFY
 * event. No datapack JSON, no generated files, nothing to keep in sync with
 * Mojang's table registry — every row is one pool entry on one pool.

 * <p>Design rules:
 * <ul>
 *   <li>Weapons are <strong>rare</strong>: the katana tops out at ~2% per chest
 *       in the tables that carry it.</li>
 *   <li>Cores are <strong>common</strong>: they exist to feed the crafting
 *       recipes, so finding them is always useful.</li>
 *   <li>The style scroll and the starfall remote only show up in "endgame
 *       posture" chests (ancient city, end city) — they are cosmetics, not
 *       progress.</li>
 * </ul>
 */
public final class HadoLoot {
    /** One arsenal item that can roll in a chest pool. */
    private record Row(Item item, int weight, int min, int max, float chance) {
    }

    private record Chest(RegistryKey<LootTable> key, Row[] rows) {
    }

    private static final Row TAMASHII = new Row(HadoItems.TAMASHII_CORE, 10, 1, 2, 0.55F);
    private static final Row IMPACT = new Row(HadoItems.IMPACT_CORE, 10, 1, 1, 0.45F);

    private static final Chest[] CHESTS = {
            new Chest(key("mineshaft"), new Row[]{
                    new Row(HadoItems.SENBON, 6, 2, 4, 0.30F),
                    TAMASHII,
            }),
            new Chest(key("simple_dungeon"), new Row[]{
                    TAMASHII,
                    new Row(HadoItems.SENBON, 4, 3, 6, 0.25F),
                    new Row(HadoItems.KUROTAMA, 1, 1, 1, 0.08F),
            }),
            new Chest(key("desert_pyramid"), new Row[]{
                    TAMASHII, IMPACT,
                    new Row(HadoItems.ZETTAI_BARRIER, 1, 1, 1, 0.06F),
            }),
            new Chest(key("jungle_temple"), new Row[]{
                    TAMASHII,
                    new Row(HadoItems.SENBON, 4, 2, 5, 0.28F),
                    new Row(HadoItems.METEOR_DRILL, 1, 1, 1, 0.05F),
            }),
            new Chest(key("buried_treasure"), new Row[]{
                    TAMASHII, IMPACT,
                    new Row(HadoItems.STYLE_SCROLL, 2, 1, 1, 0.10F),
            }),
            new Chest(key("pillager_outpost"), new Row[]{
                    IMPACT,
                    new Row(HadoItems.SENBON, 5, 3, 6, 0.30F),
            }),
            new Chest(key("stronghold_corridor"), new Row[]{
                    TAMASHII, IMPACT,
                    new Row(HadoItems.KAZE_KATANA, 2, 1, 1, 0.02F),
            }),
            new Chest(key("igloo_chest"), new Row[]{
                    TAMASHII,
                    new Row(HadoItems.ZETTAI_BARRIER, 1, 1, 1, 0.07F),
            }),
            new Chest(key("nether_bridge"), new Row[]{
                    IMPACT,
                    new Row(HadoItems.KUROTAMA, 3, 1, 2, 0.12F),
            }),
            new Chest(key("bastion_treasure"), new Row[]{
                    IMPACT, IMPACT,
                    new Row(HadoItems.RYUSEI_CANNON, 2, 1, 1, 0.04F),
                    new Row(HadoItems.KUROTAMA, 2, 1, 2, 0.10F),
            }),
            new Chest(key("ancient_city"), new Row[]{
                    IMPACT, IMPACT,
                    new Row(HadoItems.KAZE_KATANA, 3, 1, 1, 0.03F),
                    new Row(HadoItems.STYLE_SCROLL, 2, 1, 1, 0.08F),
                    new Row(HadoItems.ZETTAI_BARRIER, 2, 1, 1, 0.08F),
            }),
            new Chest(key("end_city_treasure"), new Row[]{
                    IMPACT, IMPACT,
                    new Row(HadoItems.RYUSEI_CANNON, 2, 1, 1, 0.05F),
                    new Row(HadoItems.STELLAR_REMOTE, 3, 1, 1, 0.15F),
            }),
            new Chest(key("shipwreck_treasure"), new Row[]{
                    TAMASHII, IMPACT,
                    new Row(HadoItems.SENBON, 4, 2, 5, 0.25F),
            }),
            new Chest(key("woodland_mansion"), new Row[]{
                    IMPACT, TAMASHII,
                    new Row(HadoItems.KUROTAMA, 3, 1, 2, 0.12F),
                    new Row(HadoItems.STYLE_SCROLL, 3, 1, 1, 0.12F),
            }),
    };

    private HadoLoot() {
    }

    private static RegistryKey<LootTable> key(String id) {
        return RegistryKey.of(RegistryKeys.LOOT_TABLE,
                net.minecraft.util.Identifier.ofVanilla("chests/" + id));
    }

    public static void register() {
        LootTableEvents.MODIFY.register((key, builder, source) -> {
            for (Chest chest : CHESTS) {
                if (!chest.key().equals(key)) {
                    continue;
                }
                for (Row row : chest.rows()) {
                    builder.pool(LootPool.builder()
                            .with(ItemEntry.builder(row.item())
                                    .weight(row.weight())
                                    .apply(SetCountLootFunction.builder(UniformLootNumberProvider
                                            .create(row.min(), row.max()))))
                            .conditionally(RandomChanceLootCondition.builder(row.chance())));
                }
            }
        });
    }

    /** Testing only: number of chest tables the mod touches. */
    public static int chestsTouched() {
        return CHESTS.length;
    }
}
