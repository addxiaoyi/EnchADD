package net.enchadd.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.function.Consumer;

final class EnchantRegistrationPlan {

    private EnchantRegistrationPlan() {
    }

    record Entry(String key, Consumer<ConfigurationSection> registrar) {
    }

    static List<Entry> normalEntries() {
        return List.of(
                new Entry("soulbound", net.enchadd.enchants.SoulboundEnchant::create),
                new Entry("telepathy", net.enchadd.enchants.TelepathyEnchant::create),
                new Entry("replanting", net.enchadd.enchants.ReplantingEnchant::create),
                new Entry("irrigation", net.enchadd.enchants.IrrigationEnchant::create),
                new Entry("arborist", net.enchadd.enchants.ArboristEnchant::create),
                new Entry("trailblazer", net.enchadd.enchants.TrailblazerEnchant::create),
                new Entry("furrow", net.enchadd.enchants.FurrowEnchant::create),
                new Entry("executioner", net.enchadd.enchants.ExecutionerEnchant::create),
                new Entry("beheading", net.enchadd.enchants.BeheadingEnchant::create),
                new Entry("smelting", net.enchadd.enchants.SmeltingEnchant::create),
                new Entry("stonewake", net.enchadd.enchants.StonewakeEnchant::create),
                new Entry("refine", net.enchadd.enchants.RefineEnchant::create),
                new Entry("rebound", net.enchadd.enchants.ReboundEnchant::create),
                new Entry("insight", net.enchadd.enchants.InsightEnchant::create),
                new Entry("airbag", net.enchadd.enchants.AirbagEnchant::create),
                new Entry("afterglide", net.enchadd.enchants.AfterglideEnchant::create),
                new Entry("skim", net.enchadd.enchants.SkimEnchant::create),
                new Entry("homecoming", net.enchadd.enchants.HomecomingEnchant::create),
                new Entry("volley", net.enchadd.enchants.VolleyEnchant::create),
                new Entry("ward", net.enchadd.enchants.WardEnchant::create),
                new Entry("holdfast", net.enchadd.enchants.HoldfastEnchant::create),
                new Entry("brace", net.enchadd.enchants.BraceEnchant::create),
                new Entry("pivot", net.enchadd.enchants.PivotEnchant::create),
                new Entry("momentum", net.enchadd.enchants.MomentumEnchant::create),
                new Entry("cadence", net.enchadd.enchants.CadenceEnchant::create),
                new Entry("resonance", net.enchadd.enchants.ResonanceEnchant::create),
                new Entry("underdog", net.enchadd.enchants.UnderdogEnchant::create),
                new Entry("poise", net.enchadd.enchants.PoiseEnchant::create),
                new Entry("highground", net.enchadd.enchants.HighgroundEnchant::create),
                new Entry("measured", net.enchadd.enchants.MeasuredEnchant::create),
                new Entry("rally", net.enchadd.enchants.RallyEnchant::create),
                new Entry("cinder", net.enchadd.enchants.CinderEnchant::create),
                new Entry("lodestar", net.enchadd.enchants.LodestarEnchant::create),
                new Entry("starwish", net.enchadd.enchants.StarwishEnchant::create),
                new Entry("waysong", net.enchadd.enchants.WaysongEnchant::create),
                new Entry("delvesense", net.enchadd.enchants.DelvesenseEnchant::create),
                new Entry("tideshell", net.enchadd.enchants.TideshellEnchant::create),
                new Entry("homeward", net.enchadd.enchants.HomewardEnchant::create),
                new Entry("parry", net.enchadd.enchants.ParryEnchant::create),
                new Entry("initiative", net.enchadd.enchants.InitiativeEnchant::create),
                new Entry("wingclip", net.enchadd.enchants.WingclipEnchant::create),
                new Entry("tracer", net.enchadd.enchants.TracerEnchant::create),
                new Entry("breakguard", net.enchadd.enchants.BreakguardEnchant::create),
                new Entry("pursuit", net.enchadd.enchants.PursuitEnchant::create),
                new Entry("fortitude", net.enchadd.enchants.FortitudeEnchant::create),
                new Entry("debilitate", net.enchadd.enchants.DebilitateEnchant::create),
                new Entry("nourish", net.enchadd.enchants.NourishEnchant::create),
                new Entry("fleetfoot", net.enchadd.enchants.FleetfootEnchant::create),
                new Entry("tenderstep", net.enchadd.enchants.TenderstepEnchant::create),
                new Entry("shadowstrike", net.enchadd.enchants.ShadowstrikeEnchant::create),
                new Entry("bulwark", net.enchadd.enchants.BulwarkEnchant::create),
                new Entry("purify", net.enchadd.enchants.PurifyEnchant::create),
                new Entry("lucidity", net.enchadd.enchants.LucidityEnchant::create),
                new Entry("firebreak", net.enchadd.enchants.FirebreakEnchant::create),
                new Entry("frostbrand", net.enchadd.enchants.FrostbrandEnchant::create),
                new Entry("dispel", net.enchadd.enchants.DispelEnchant::create),
                new Entry("hunters_mark", net.enchadd.enchants.HuntersMarkEnchant::create),
                new Entry("undertow", net.enchadd.enchants.UndertowEnchant::create),
                new Entry("ricochet", net.enchadd.enchants.RicochetEnchant::create),
                new Entry("clairvoyance", net.enchadd.enchants.ClairvoyanceEnchant::create),
                new Entry("obscure", net.enchadd.enchants.ObscureEnchant::create),
                new Entry("riposte", net.enchadd.enchants.RiposteEnchant::create),
                new Entry("tremor", net.enchadd.enchants.TremorEnchant::create),
                new Entry("meteor", net.enchadd.enchants.MeteorEnchant::create),
                new Entry("updraft", net.enchadd.enchants.UpdraftEnchant::create),
                new Entry("overwhelm", net.enchadd.enchants.OverwhelmEnchant::create),
                new Entry("hemorrhage", net.enchadd.enchants.HemorrhageEnchant::create),
                new Entry("mortal_wound", net.enchadd.enchants.MortalWoundEnchant::create),
                new Entry("decapitate", net.enchadd.enchants.DecapitateEnchant::create),
                new Entry("immolate", net.enchadd.enchants.ImmolateEnchant::create),
                new Entry("steadfast", net.enchadd.enchants.SteadfastEnchant::create),
                new Entry("quell", net.enchadd.enchants.QuellEnchant::create),
                new Entry("evasion", net.enchadd.enchants.EvasionEnchant::create),
                new Entry("shroud", net.enchadd.enchants.ShroudEnchant::create),
                new Entry("bind", net.enchadd.enchants.BindEnchant::create),
                new Entry("last_stand", net.enchadd.enchants.LastStandEnchant::create),
                new Entry("farshot", net.enchadd.enchants.FarshotEnchant::create),
                new Entry("flare", net.enchadd.enchants.FlareEnchant::create),
                new Entry("trawler", net.enchadd.enchants.TrawlerEnchant::create),
                new Entry("freshcatch", net.enchadd.enchants.FreshcatchEnchant::create),
                new Entry("wingguard", net.enchadd.enchants.WingguardEnchant::create),
                new Entry("barrier", net.enchadd.enchants.BarrierEnchant::create),
                new Entry("tide_runner", net.enchadd.enchants.TideRunnerEnchant::create),
                new Entry("sidestep", net.enchadd.enchants.SidestepEnchant::create),
                new Entry("sunder", net.enchadd.enchants.SunderEnchant::create),
                new Entry("steady_aim", net.enchadd.enchants.SteadyAimEnchant::create),
                new Entry("stillness", net.enchadd.enchants.StillnessEnchant::create)
        );
    }

    static List<Entry> curseEntries() {
        return List.of(
                new Entry("panic", net.enchadd.enchants.PanicEnchant::create),
                new Entry("gluttony", net.enchadd.enchants.GluttonyEnchant::create),
                new Entry("vampirism", net.enchadd.enchants.VampirismEnchant::create),
                new Entry("insomnia", net.enchadd.enchants.InsomniaEnchant::create),
                new Entry("greed", net.enchadd.enchants.GreedEnchant::create),
                new Entry("rashness", net.enchadd.enchants.RashnessEnchant::create),
                new Entry("retching", net.enchadd.enchants.RetchingEnchant::create),
                new Entry("thirst", net.enchadd.enchants.ThirstEnchant::create),
                new Entry("fragility", net.enchadd.enchants.FragilityEnchant::create),
                new Entry("misfortune", net.enchadd.enchants.MisfortuneEnchant::create),
                new Entry("lethargy", net.enchadd.enchants.LethargyEnchant::create),
                new Entry("gravitation", net.enchadd.enchants.GravitationEnchant::create),
                new Entry("brittle", net.enchadd.enchants.BrittleEnchant::create),
                new Entry("backfire", net.enchadd.enchants.BackfireEnchant::create),
                new Entry("shatter", net.enchadd.enchants.ShatterEnchant::create)
        );
    }
}
