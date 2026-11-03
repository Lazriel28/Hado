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

import dev.hado.marker.HadoAnchorStampItem;
import dev.hado.marker.HadoChimeBellItem;
import dev.hado.marker.HadoCounterScribeItem;
import dev.hado.marker.HadoDriftCompassItem;
import dev.hado.marker.HadoGlimmerLanternItem;
import dev.hado.marker.HadoHomingKiteItem;
import dev.hado.marker.HadoLoomThreadItem;
import dev.hado.marker.HadoMarkerBeaconItem;
import dev.hado.marker.HadoMarkerPenItem;
import dev.hado.marker.HadoMarkerRemoteItem;
import dev.hado.marker.HadoMirrorLensItem;
import dev.hado.marker.HadoRecurveMapItem;
import dev.hado.marker.HadoSwiftEdgeItem;
import dev.hado.item.KazeKatanaItem;
import dev.hado.item.KurotamaItem;
import dev.hado.item.MeteorDrillItem;
import dev.hado.item.RyuseiCannonItem;
import dev.hado.item.SenbonItem;
import dev.hado.item.StellarRemoteItem;
import dev.hado.item.StyleScrollItem;
import dev.hado.item.ZettaiBarrierItem;
import dev.hado.registry.HadoItems;

/** Central item registry, including the marker-system items. */
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

        // marker system
        HadoItems.MARKER_REMOTE = add("marker_remote", new HadoMarkerRemoteItem());
        HadoItems.MARKER_BEACON = add("marker_beacon", new HadoMarkerBeaconItem());
        HadoItems.SWIFT_EDGE = add("swift_edge", new HadoSwiftEdgeItem());
        HadoItems.RECURVE_MAP = add("recurve_map", new HadoRecurveMapItem());
        HadoItems.DRIFT_COMPASS = add("drift_compass", new HadoDriftCompassItem());
        HadoItems.LOOM_THREAD = add("loom_thread", new HadoLoomThreadItem());
        HadoItems.CHIME_BELL = add("chime_bell", new HadoChimeBellItem());
        HadoItems.GLIMMER_LANTERN = add("glimmer_lantern", new HadoGlimmerLanternItem());
        HadoItems.HOMING_KITE = add("homing_kite", new HadoHomingKiteItem());
        HadoItems.ANCHOR_STAMP = add("anchor_stamp", new HadoAnchorStampItem());
        HadoItems.COUNTER_SCRIBE = add("counter_scribe", new HadoCounterScribeItem());
        HadoItems.MIRROR_LENS = add("mirror_lens", new HadoMirrorLensItem());
        HadoItems.MARKER_PEN = add("marker_pen", new HadoMarkerPenItem());
        HadoItems.MARKER_BEACON = add("marker_beacon", new HadoMarkerBeaconItem());
        HadoItems.SWIFT_EDGE = add("swift_edge", new HadoSwiftEdgeItem());
        HadoItems.RECURVE_MAP = add("recurve_map", new HadoRecurveMapItem());
        HadoItems.DRIFT_COMPASS = add("drift_compass", new HadoDriftCompassItem());
        HadoItems.LOOM_THREAD = add("loom_thread", new HadoLoomThreadItem());
        HadoItems.CHIME_BELL = add("chime_bell", new HadoChimeBellItem());
        HadoItems.GLIMMER_LANTERN = add("glimmer_lantern", new HadoGlimmerLanternItem());
        HadoItems.HOMING_KITE = add("homing_kite", new HadoHomingKiteItem());
        HadoItems.ANCHOR_STAMP = add("anchor_stamp", new HadoAnchorStampItem());
        HadoItems.COUNTER_SCRIBE = add("counter_scribe", new HadoCounterScribeItem());
        HadoItems.MIRROR_LENS = add("mirror_lens", new HadoMirrorLensItem());
        HadoItems.MARKER_PEN = add("marker_pen", new HadoMarkerPenItem());

        ITEM_COUNT = ALL.size();

        GROUP = Registry.register(Registries.ITEM_GROUP, Hado.id("arsenal"),
                FabricItemGroup.builder()
                        .icon(() -> new ItemStack(HadoItems.KAZE_KATANA))
                        .displayName(Text.translatable("itemGroup.hado.arsenal"))
                        .entries((context, entries) -> ALL.forEach(entries::add))
                        .build());
    }
}
