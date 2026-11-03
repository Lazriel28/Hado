package dev.hado;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HADO — anime arsenal.
 *
 * <p>Splitting rule: everything that decides <em>what happens</em> lives on the
 * common side (items, damage, world edits); everything that decides <em>how it
 * looks</em> lives on the client and is driven by a single small packet. That
 * keeps the server cheap and stops the visuals from ever costing tick time.
 */
public final class Hado implements ModInitializer {
    public static final String MOD_ID = "hado";
    public static final Logger LOG = LoggerFactory.getLogger("HADO");

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        HadoContent.register();
        HadoNetwork.register();
        HadoLoot.register();
        LOG.info("[HADO] Arsenal forged: {} items in {} vaults.",
                HadoContent.ITEM_COUNT, HadoLoot.chestsTouched());
    }
}
