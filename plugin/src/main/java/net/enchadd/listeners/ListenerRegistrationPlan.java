package net.enchadd.listeners;

import net.kyori.adventure.key.Key;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

final class ListenerRegistrationPlan {

    private ListenerRegistrationPlan() {
    }

    record Entry(Key key, ListenerFactory factory) {
    }

    @FunctionalInterface
    interface ListenerFactory {
        Listener create(JavaPlugin plugin);
    }

    static @NotNull List<Entry> allEntries() {
        List<Entry> entries = new ArrayList<>();
        entries.addAll(utilityEntries());
        entries.addAll(toolEntries());
        entries.addAll(combatEntries());
        entries.addAll(rangedEntries());
        entries.addAll(armorEntries());
        entries.addAll(curseEntries());
        return List.copyOf(entries);
    }

    private static @NotNull List<Entry> utilityEntries() {
        return List.of(
                entry("soulbound", SoulboundListener::new),
                entry("telepathy", plugin -> new TelepathyListener(plugin)),
                entry("homecoming", HomecomingListener::new),
                entry("waysong", WaysongListener::new),
                entry("homeward", HomewardListener::new),
                entry("delvesense", DelvesenseListener::new),
                entry("tideshell", TideshellListener::new),
                entry("trawler", TrawlerListener::new),
                entry("freshcatch", FreshcatchListener::new)
        );
    }

    private static @NotNull List<Entry> toolEntries() {
        return List.of(
                entry("replanting", ReplantingListener::new),
                entry("irrigation", IrrigationListener::new),
                entry("arborist", ArboristListener::new),
                entry("trailblazer", TrailblazerListener::new),
                entry("furrow", FurrowListener::new),
                entry("smelting", SmeltingListener::new),
                entry("stonewake", StonewakeListener::new),
                entry("rebound", ReboundListener::new),
                entry("insight", InsightListener::new),
                entry("refine", RefineListener::new)
        );
    }

    private static @NotNull List<Entry> combatEntries() {
        return List.of(
                entry("executioner", ExecutionerListener::new),
                entry("beheading", BeheadingListener::new),
                entry("shadowstrike", ShadowstrikeListener::new),
                entry("fortitude", FortitudeListener::new),
                entry("momentum", MomentumListener::new),
                entry("cadence", CadenceListener::new),
                entry("resonance", ResonanceListener::new),
                entry("underdog", UnderdogListener::new),
                entry("poise", PoiseListener::new),
                entry("highground", HighgroundListener::new),
                entry("measured", MeasuredListener::new),
                entry("rally", RallyListener::new),
                entry("cinder", CinderListener::new),
                entry("lodestar", LodestarListener::new),
                entry("starwish", StarwishListener::new),
                entry("parry", ParryListener::new),
                entry("initiative", InitiativeListener::new),
                entry("wingclip", WingclipListener::new),
                entry("tracer", TracerListener::new),
                entry("breakguard", BreakguardListener::new),
                entry("pursuit", PursuitListener::new),
                entry("nourish", NourishListener::new),
                entry("riposte", RiposteListener::new),
                entry("tremor", TremorListener::new),
                entry("meteor", MeteorListener::new),
                entry("updraft", UpdraftListener::new),
                entry("overwhelm", OverwhelmListener::new),
                entry("debilitate", DebilitateListener::new),
                entry("hemorrhage", HemorrhageListener::new),
                entry("mortal_wound", MortalWoundListener::new),
                entry("immolate", ImmolateListener::new),
                entry("frostbrand", FrostbrandListener::new),
                entry("sunder", SunderListener::new),
                entry("decapitate", DecapitateListener::new)
        );
    }

    private static @NotNull List<Entry> rangedEntries() {
        return List.of(
                entry("volley", VolleyListener::new),
                entry("hunters_mark", HuntersMarkListener::new),
                entry("undertow", UndertowListener::new),
                entry("ricochet", RicochetListener::new),
                entry("clairvoyance", ClairvoyanceListener::new),
                entry("obscure", ObscureListener::new),
                entry("steady_aim", SteadyAimListener::new),
                entry("stillness", StillnessListener::new),
                entry("farshot", FarshotListener::new),
                entry("flare", FlareListener::new)
        );
    }

    private static @NotNull List<Entry> armorEntries() {
        return List.of(
                entry("airbag", AirbagListener::new),
                entry("afterglide", AfterglideListener::new),
                entry("skim", SkimListener::new),
                entry("ward", WardListener::new),
                entry("holdfast", HoldfastListener::new),
                entry("brace", BraceListener::new),
                entry("pivot", PivotListener::new),
                entry("bulwark", BulwarkListener::new),
                entry("purify", PurifyListener::new),
                entry("lucidity", LucidityListener::new),
                entry("firebreak", FirebreakListener::new),
                entry("steadfast", SteadfastListener::new),
                entry("quell", QuellListener::new),
                entry("evasion", EvasionListener::new),
                entry("shroud", ShroudListener::new),
                entry("bind", BindListener::new),
                entry("last_stand", LastStandListener::new),
                entry("wingguard", WingguardListener::new),
                entry("barrier", BarrierListener::new),
                entry("tide_runner", TideRunnerListener::new),
                entry("sidestep", SidestepListener::new),
                entry("fleetfoot", FleetfootListener::new),
                entry("tenderstep", TenderstepListener::new)
        );
    }

    private static @NotNull List<Entry> curseEntries() {
        return List.of(
                entry("panic", PanicListener::new),
                entry("gluttony", GluttonyListener::new),
                entry("vampirism", plugin -> new VampirismListener(plugin)),
                entry("insomnia", InsomniaListener::new),
                entry("greed", GreedListener::new),
                entry("rashness", RashnessListener::new),
                entry("retching", RetchingListener::new),
                entry("thirst", ThirstListener::new),
                entry("fragility", FragilityListener::new),
                entry("misfortune", MisfortuneListener::new),
                entry("lethargy", LethargyListener::new),
                entry("gravitation", GravitationListener::new),
                entry("brittle", BrittleListener::new),
                entry("backfire", BackfireListener::new),
                entry("dispel", DispelListener::new),
                entry("shatter", ShatterListener::new)
        );
    }

    private static Entry entry(@NotNull String key, @NotNull Supplier<Listener> supplier) {
        return new Entry(Key.key(key), plugin -> supplier.get());
    }

    private static Entry entry(@NotNull String key, @NotNull ListenerFactory factory) {
        return new Entry(Key.key(key), factory);
    }
}
