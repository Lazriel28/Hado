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
 * Seeds the arsenal and the marker-system items into vanilla chest loot.
 *
 * <p>Marker-system items are uncommon coordination/targeting tools: cores remain
 * common, weapons remain rare, and the marker remote and its companion tools can
 * appear in a wider set of chests than the endgame cosmetics of the original ten.
 */
public final class HadoLoot {
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
                    new Row(HadoItems.LOOM_THREAD, 3, 1, 2, 0.10F),
            }),
            new Chest(key("simple_dungeon"), new Row[]{
                    TAMASHII,
                    new Row(HadoItems.SENBON, 4, 3, 6, 0.25F),
                    new Row(HadoItems.KUROTAMA, 1, 1, 1, 0.08F),
                    new Row(HadoItems.ANCHOR_STAMP, 2, 1, 1, 0.06F),
            }),
            new Chest(key("desert_pyramid"), new Row[]{
                    TAMASHII, IMPACT,
                    new Row(HadoItems.ZETTAI_BARRIER, 1, 1, 1, 0.06F),
                    new Row(HadoItems.GLIMMER_LANTERN, 2, 1, 2, 0.10F),
            }),
            new Chest(key("jungle_temple"), new Row[]{
                    TAMASHII,
                    new Row(HadoItems.SENBON, 4, 2, 5, 0.28F),
                    new Row(HadoItems.METEOR_DRILL, 1, 1, 1, 0.05F),
                    new Row(HadoItems.DRIFT_COMPASS, 2, 1, 1, 0.05F),
            }),
            new Chest(key("buried_treasure"), new Row[]{
                    TAMASHII, IMPACT,
                    new Row(HadoItems.STYLE_SCROLL, 2, 1, 1, 0.10F),
                    new Row(HadoItems.MARKER_PEN, 2, 1, 2, 0.08F),
            }),
            new Chest(key("pillager_outpost"), new Row[]{
                    IMPACT,
                    new Row(HadoItems.SENBON, 5, 3, 6, 0.30F),
                    new Row(HadoItems.MARKER_BEACON, 2, 1, 2, 0.08F),
            }),
            new Chest(key("stronghold_corridor"), new Row[]{
                    TAMASHII, IMPACT,
                    new Row(HadoItems.KAZE_KATANA, 2, 1, 1, 0.02F),
                    new Row(HadoItems.RECURVE_MAP, 2, 1, 1, 0.04F),
            }),
            new Chest(key("igloo_chest"), new Row[]{
                    TAMASHII,
                    new Row(HadoItems.ZETTAI_BARRIER, 1, 1, 1, 0.07F),
                    new Row(HadoItems.CHIME_BELL, 2, 1, 2, 0.10F),
            }),
            new Chest(key("nether_bridge"), new Row[]{
                    IMPACT,
                    new Row(HadoItems.KUROTAMA, 3, 1, 2, 0.12F),
                    new Row(HadoItems.DRIFT_COMPASS, 2, 1, 2, 0.08F),
            }),
            new Chest(key("bastion_treasure"), new Row[]{
                    IMPACT, IMPACT,
                    new Row(HadoItems.RYUSEI_CANNON, 2, 1, 1, 0.04F),
                    new Row(HadoItems.KUROTAMA, 2, 1, 2, 0.10F),
                    new Row(HadoItems.HOMING_KITE, 2, 1, 1, 0.06F),
            }),
            new Chest(key("ancient_city"), new Row[]{
                    IMPACT, IMPACT,
                    new Row(HadoItems.KAZE_KATANA, 3, 1, 1, 0.03F),
                    new Row(HadoItems.STYLE_SCROLL, 2, 1, 1, 0.08F),
                    new Row(HadoItems.ZETTAI_BARRIER, 2, 1, 1, 0.08F),
                    new Row(HadoItems.COUNTER_SCRIBE, 2, 1, 1, 0.04F),
            }),
            new Chest(key("end_city_treasure"), new Row[]{
                    IMPACT, IMPACT,
                    new Row(HadoItems.RYUSEI_CANNON, 2, 1, 1, 0.05F),
                    new Row(HadoItems.STELLAR_REMOTE, 3, 1, 1, 0.15F),
                    new Row(HadoItems.MIRROR_LENS, 2, 1, 1, 0.06F),
                    new Row(HadoItems.MARKER_REMOTE, 1, 1, 1, 0.05F),
                    new Row(HadoItems.SWIFT_EDGE, 1, 1, 1, 0.05F),
                    new Row(HadoItems.RECURVE_MAP, 1, 1, 1, 0.05F),
                    new Row(HadoItems.DRIFT_COMPASS, 1, 1, 1, 0.05F),
                    new Row(HadoItems.LOOM_THREAD, 1, 1, 1, 0.06F),
                    new Row(HadoItems.CHIME_BELL, 1, 1, 1, 0.06F),
                    new Row(HadoItems.GLIMMER_LANTERN, 1, 1, 1, 0.08F),
                    new Row(HadoItems.HOMING_KITE, 1, 1, 1, 0.06F),
                    new Row(HadoItems.ANCHOR_STAMP, 1, 1, 1, 0.06F),
                    new Row(HadoItems.COUNTER_SCRIBE, 1, 1, 1, 0.06F),
                    new Row(HadoItems.LOOM_THREAD, 1, 1, 1, 0.06F),
                    new Row(HadoItems.CHIME_BELL, 1, 1, 1, 0.06F),
                    new Row(HadoItems.GLIMMER_LANTERN, 1, 1, 1, 0.08F),
                    new Row(HadoItems.HOMING_KITE, 1, 1, 1, 0.06F),
                    new Row(HadoItems.ANCHOR_STAMP, 1, 1, 1, 0.06F),
                    new Row(HadoItems.COUNTER_SCRIBE, 1, 1, 1, 0.06F),
                    new Row(HadoItems.MIRROR_LENS, 1, 1, 1, 0.06F),
                    new Row(HadoItems.MARKER_PEN, 1, 1, 1, 0.06F),
            }),
            new Chest(key("shipwreck_treasure"), new Row[]{
                    TAMASHII, IMPACT,
                    new Row(HadoItems.SENBON, 4, 2, 5, 0.25F),
                    new Row(HadoItems.LOOM_THREAD, 3, 1, 3, 0.10F),
            }),
            new Chest(key("woodland_mansion"), new Row[]{
                    IMPACT, TAMASHII,
                    new Row(HadoItems.KUROTAMA, 3, 1, 2, 0.12F),
                    new Row(HadoItems.STYLE_SCROLL, 3, 1, 1, 0.12F),
                    new Row(HadoItems.MARKER_REMOTE, 2, 1, 1, 0.05F),
                    new Row(HadoItems.GLIMMER_LANTERN, 2, 1, 2, 0.08F),
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
