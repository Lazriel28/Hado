package dev.hado;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import dev.hado.item.*;
import dev.hado.registry.HadoItems;

/**
 * Central item registry. Registration order also fixes the creative-tab order.
 */
public final class HadoContent {
    public static final List<Item> ALL = new ArrayList<>();
    public static int ITEM_COUNT;

    public static ItemGroup GROUP;

    private HadoContent() {
    }

    private static <T extends Item> T add(String name, T item) {
        Registry.register(Registries.ITEM, Hado.id(name), item);
        ALL.add(item);
        return item;
    }

    public static void register() {
        HadoItems.TAMASHII_CORE = add("tamashii_core", new Item(new Item.Settings()));
        HadoItems.IMPACT_CORE = add("impact_core", new Item(new Item.Settings().fireproof()));

        HadoItems.KAZE_KATANA = add("kaze_katana", new KazeKatanaItem());
        HadoItems.RYUSEI_CANNON = add("ryusei_cannon", new RyuseiCannonItem());
        HadoItems.KUROTAMA = add("kurotama", new KurotamaItem());
        HadoItems.SENBON = add("senbon", new SenbonItem());
        HadoItems.METEOR_DRILL = add("meteor_drill", new MeteorDrillItem());
        HadoItems.ZETTAI_BARRIER = add("zettai_barrier", new ZettaiBarrierItem());

        HadoItems.STELLAR_REMOTE = add("stella_remote", new StellarRemoteItem());
        HadoItems.STYLE_SCROLL = add("style_scroll", new StyleScrollItem());

        ITEM_COUNT = ALL.size();

        GROUP = Registry.register(Registries.ITEM_GROUP, Hado.id("arsenal"),
                FabricItemGroup.builder()
                        .icon(() -> new ItemStack(HadoItems.KAZE_KATANA))
                        .displayName(Text.translatable("itemGroup.hado.arsenal"))
                        .entries((context, entries) -> ALL.forEach(entries::add))
                        .build());
    }
}
