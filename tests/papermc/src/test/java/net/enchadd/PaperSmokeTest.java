package net.enchadd;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperSmokeTest {

    private static final Path COMMAND_SCRIPT = PaperTestProjectPaths.REPO_ROOT.resolve(
            Path.of("tests", "papermc", "src", "test", "resources", "testermc", "commands.txt")
    );
    private static final Path PERF_COMMAND_SCRIPT = PaperTestProjectPaths.REPO_ROOT.resolve(
            Path.of("tests", "papermc", "src", "test", "resources", "testermc", "perf-commands.txt")
    );
    private static final Path LONG_PERF_COMMAND_SCRIPT = PaperTestProjectPaths.REPO_ROOT.resolve(
            Path.of("tests", "papermc", "src", "test", "resources", "testermc", "long-perf-commands.txt")
    );
    private static final Path TESTERMC_FILES_DIR = PaperTestProjectPaths.REPO_ROOT.resolve(
            Path.of("tests", "papermc", "src", "test", "resources", "testermc", "files")
    );
    private static final Path TEST_SERVER_PROPERTIES = TESTERMC_FILES_DIR.resolve("server.properties");
    private static final Path TEST_PLUGIN_CONFIG = TESTERMC_FILES_DIR.resolve(Path.of("plugins", "EnchADD", "config.yml"));
    private static final Path TESTERMC_WORKFLOW = PaperTestProjectPaths.REPO_ROOT.resolve(
            Path.of(".github", "workflows", "testermc.yml")
    );
    private static final Path NIGHTLY_PERF_WORKFLOW = PaperTestProjectPaths.REPO_ROOT.resolve(
            Path.of(".github", "workflows", "nightly-perf-gate.yml")
    );
    private static final Path PAPER_INTEGRATION_RUNNER = PaperTestProjectPaths.REPO_ROOT.resolve(
            Path.of("tests", "papermc", "scripts", "run_paper_integration.py")
    );
    private static final Path CATALOG_GENERATOR = PaperTestProjectPaths.REPO_ROOT.resolve(
            Path.of("tests", "papermc", "scripts", "generate_enchant_catalog.py")
    );
    private static final Path PAPER_PLUGIN_YML = PaperTestProjectPaths.PLUGIN_MAIN_RESOURCES.resolve("paper-plugin.yml");
    private static final Path PLUGIN_YML = PaperTestProjectPaths.PLUGIN_MAIN_RESOURCES.resolve("plugin.yml");
    private static final Path BOOTSTRAP_SOURCE = PaperTestProjectPaths.PLUGIN_MAIN_JAVA.resolve(
            Path.of("net", "enchadd", "EnchADDBootstrap.java")
    );

    @Test
    void paperCommandScriptCoversStartupAndPluginCommands() throws IOException {
        List<String> commands = Files.readAllLines(COMMAND_SCRIPT, StandardCharsets.UTF_8).stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .filter(line -> !line.startsWith("#"))
                .toList();

        assertAll(
                () -> assertTrue(commands.contains("version"), "Command script should print the Paper version"),
                () -> assertTrue(commands.contains("plugins"), "Command script should list loaded plugins"),
                () -> assertTrue(
                        commands.contains("execute in minecraft:overworld run summon minecraft:zombie 0 90 0"),
                        "Command script should force CreatureSpawnEvent compatibility coverage"
                ),
                () -> assertTrue(commands.contains("enchadd help"), "Command script should exercise the help command"),
                () -> assertTrue(commands.contains("enchadd list 1 5"), "Command script should exercise paginated list output"),
                () -> assertTrue(commands.contains("enchadd info airbag"), "Command script should query a concrete enchant"),
                () -> assertTrue(commands.contains("enchadd verify"), "Command script should run behavior assertions"),
                () -> assertTrue(commands.contains("enchadd balance"), "Command script should run anti-abuse balance gate"),
                () -> assertTrue(commands.contains("enchadd reload"), "Command script should exercise atomic runtime reload path"),
                () -> assertTrue(commands.contains("enchadd ci"), "Command script should emit the CI sentinel line"),
                () -> assertTrue(commands.contains("enchadd exportjson"), "Command script should exercise JSON export"),
                () -> assertTrue(commands.contains("enchadd exportcsv"), "Command script should exercise CSV export")
        );
    }

    @Test
    void perfCommandScriptIncludesMsptAndSparkSampling() throws IOException {
        List<String> commands = Files.readAllLines(PERF_COMMAND_SCRIPT, StandardCharsets.UTF_8).stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .filter(line -> !line.startsWith("#"))
                .toList();

        assertAll(
                () -> assertTrue(
                        commands.contains("execute in minecraft:overworld run summon minecraft:zombie 0 90 0"),
                        "Perf command script should force CreatureSpawnEvent compatibility path"
                ),
                () -> assertTrue(commands.contains("enchadd verify"), "Perf command script should verify combat/cooldown/conflict behavior"),
                () -> assertTrue(commands.contains("enchadd balance"), "Perf command script should evaluate anti-abuse pair scores"),
                () -> assertTrue(commands.contains("mspt"), "Perf command script should sample MSPT"),
                () -> assertTrue(commands.stream().anyMatch(line -> line.startsWith("enchadd perf ")), "Perf command script should inject deterministic enchant load"),
                () -> assertTrue(commands.contains("enchadd ci"), "Perf command script should emit EnchADD CI metrics"),
                () -> assertTrue(commands.stream().anyMatch(line -> line.startsWith("spark profiler start")), "Perf command script should include Spark sampler command")
        );
    }

    @Test
    void longPerfCommandScriptDefinesSustainedLoadProfile() throws IOException {
        List<String> commands = Files.readAllLines(LONG_PERF_COMMAND_SCRIPT, StandardCharsets.UTF_8).stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .filter(line -> !line.startsWith("#"))
                .toList();

        long perfBursts = commands.stream().filter(line -> line.startsWith("enchadd perf ")).count();
        long msptSamples = commands.stream().filter("mspt"::equals).count();
        long ciSamples = commands.stream().filter("enchadd ci"::equals).count();
        long sleep90Samples = commands.stream().filter("sleep 90"::equals).count();

        assertAll(
                () -> assertTrue(
                        commands.contains("execute in minecraft:overworld run summon minecraft:zombie 0 90 0"),
                        "Long perf script should force CreatureSpawnEvent coverage before sustained load"
                ),
                () -> assertTrue(commands.contains("enchadd verify"), "Long perf script should include behavior assertions"),
                () -> assertTrue(commands.contains("enchadd balance"), "Long perf script should include anti-abuse balance gate"),
                () -> assertTrue(commands.stream().anyMatch(line -> line.startsWith("spark profiler start")), "Long perf script should include Spark sampler command"),
                () -> assertTrue(perfBursts >= 40, "Long perf script should inject at least 40 perf bursts"),
                () -> assertTrue(msptSamples >= 40, "Long perf script should sample MSPT for each perf burst"),
                () -> assertTrue(ciSamples >= 40, "Long perf script should emit CI metrics across the full soak run"),
                () -> assertTrue(sleep90Samples >= 40, "Long perf script should maintain at least ~60 minutes of soak windows"),
                () -> assertTrue(commands.contains("enchadd ci"), "Long perf script should emit CI metrics")
        );
    }

    @Test
    void pluginDescriptorsExposePaperEntryPoints() throws IOException {
        String paperPlugin = Files.readString(PAPER_PLUGIN_YML, StandardCharsets.UTF_8);
        String pluginYml = Files.readString(PLUGIN_YML, StandardCharsets.UTF_8);

        assertAll(
                () -> assertTrue(paperPlugin.contains("name: EnchADD"), "paper-plugin.yml should declare the plugin name"),
                () -> assertTrue(paperPlugin.contains("bootstrapper: net.enchadd.EnchADDBootstrap"), "paper-plugin.yml should declare the bootstrapper"),
                () -> assertTrue(pluginYml.contains("main: net.enchadd.EnchADD"), "plugin.yml should declare the main plugin class"),
                () -> assertTrue(pluginYml.contains("enchadd:"), "plugin.yml should expose the enchadd command")
        );
    }

    @Test
    void bootstrapInitializesLanguageBeforeEnchantRegistration() throws IOException {
        String source = Files.readString(BOOTSTRAP_SOURCE, StandardCharsets.UTF_8);
        int initIdx = source.indexOf("LangManager.init(");
        int registerIdx = source.indexOf("RegistryEvents.ENCHANTMENT.compose()");

        assertAll(
                () -> assertTrue(initIdx >= 0, "Bootstrap should initialize LangManager for registry-time translations"),
                () -> assertTrue(registerIdx >= 0, "Bootstrap should register enchantments through registry events"),
                () -> assertTrue(initIdx < registerIdx, "LangManager should initialize before enchantment registration")
        );
    }

    @Test
    void paperWorkflowBuildsModuleAndRunsDeterministicSmokeScript() throws IOException {
        String workflow = Files.readString(TESTERMC_WORKFLOW, StandardCharsets.UTF_8);

        assertAll(
                () -> assertTrue(workflow.contains("mvn -B -pl tests/papermc -am test"), "Workflow should run the papermc module tests"),
                () -> assertTrue(workflow.contains("mvn -B -pl plugin -am package -DskipTests"), "Workflow should build the plugin module artifact"),
                () -> assertTrue(workflow.contains("tests/papermc/scripts/run_paper_integration.py"), "Workflow should run the deterministic Paper integration runner"),
                () -> assertTrue(workflow.contains("tests/papermc/scripts/generate_enchant_catalog.py"), "Workflow should generate enchant catalog snapshots from source"),
                () -> assertTrue(workflow.contains("--fail-on-unclassified"), "Workflow should fail when any enchant is missing acquisition tier classification"),
                () -> assertTrue(workflow.contains("! -name '*-sources.jar'"), "Workflow should not select source jars as runtime plugin artifacts"),
                () -> assertTrue(workflow.contains("--commands-file tests/papermc/src/test/resources/testermc/commands.txt"), "Workflow should pass the shared command script"),
                () -> assertTrue(workflow.contains("--fixtures-dir tests/papermc/src/test/resources/testermc/files"), "Workflow should pass fixture files to the integration runner"),
                () -> assertTrue(workflow.contains("actions/upload-artifact@v4"), "Workflow should upload Paper smoke logs for debugging")
        );
    }

    @Test
    void nightlyPerfWorkflowRunsParserWithThresholds() throws IOException {
        String workflow = Files.readString(NIGHTLY_PERF_WORKFLOW, StandardCharsets.UTF_8);

        assertAll(
                () -> assertTrue(workflow.contains("tests/papermc/scripts/run_paper_integration.py"), "Nightly perf workflow should execute Paper integration runner"),
                () -> assertTrue(workflow.contains("--commands-file tests/papermc/src/test/resources/testermc/long-perf-commands.txt"), "Nightly perf workflow should use long perf command script"),
                () -> assertTrue(workflow.contains("concurrency:"), "Nightly perf workflow should define concurrency guard"),
                () -> assertTrue(workflow.contains("cancel-in-progress: false"), "Nightly perf workflow should avoid aborting an in-flight soak run"),
                () -> assertTrue(workflow.contains("timeout-minutes: 150"), "Nightly perf workflow should set an explicit long-run timeout"),
                () -> assertTrue(workflow.contains("tests/papermc/scripts/assert_perf_gate.py"), "Nightly perf workflow should parse and enforce metrics"),
                () -> assertTrue(workflow.contains("tests/papermc/scripts/assert_spark_gate.py"), "Nightly perf workflow should parse and enforce Spark source-share limits"),
                () -> assertTrue(workflow.contains("Redline 1 - Assert 60m performance thresholds"), "Nightly perf workflow should clearly enforce perf redline"),
                () -> assertTrue(workflow.contains("Redline 2 - Assert Spark source-share threshold"), "Nightly perf workflow should clearly enforce spark redline"),
                () -> assertTrue(workflow.contains("--max-p95-tick-ms"), "Nightly perf workflow should declare P95 threshold"),
                () -> assertTrue(workflow.contains("--max-particle-drop-rate"), "Nightly perf workflow should declare particle drop threshold"),
                () -> assertTrue(workflow.contains("--max-particle-peak-window-drop-rate"), "Nightly perf workflow should gate peak window drop rate"),
                () -> assertTrue(workflow.contains("--max-particle-peak-queue"), "Nightly perf workflow should gate peak queue size"),
                () -> assertTrue(workflow.contains("--max-stats-pending-trend-rise"), "Nightly perf workflow should gate stats pending trend rise"),
                () -> assertTrue(workflow.contains("--max-particle-queue-trend-rise"), "Nightly perf workflow should gate particle queue trend rise"),
                () -> assertTrue(workflow.contains("--max-heap-used-trend-rise-mb"), "Nightly perf workflow should gate heap growth trend"),
                () -> assertTrue(workflow.contains("--max-heap-used-mb"), "Nightly perf workflow should cap absolute heap usage"),
                () -> assertTrue(workflow.contains("--max-monitor-alerts"), "Nightly perf workflow should cap runtime alert count"),
                () -> assertTrue(workflow.contains("--max-error-rate-per-min"), "Nightly perf workflow should cap runtime error rate"),
                () -> assertTrue(workflow.contains("--min-tps-1m"), "Nightly perf workflow should enforce TPS floor (1m)"),
                () -> assertTrue(workflow.contains("--min-tps-5m"), "Nightly perf workflow should enforce TPS floor (5m)"),
                () -> assertTrue(workflow.contains("--min-trigger-throughput 100000"), "Nightly perf workflow should enforce long-run throughput floor"),
                () -> assertTrue(workflow.contains("--min-mspt-samples 3"), "Nightly perf workflow should require a minimum MSPT sample count"),
                () -> assertTrue(workflow.contains("--min-ci-samples"), "Nightly perf workflow should require enough CI trend samples"),
                () -> assertTrue(workflow.contains("--min-uptime-seconds 3500"), "Nightly perf workflow should require the soak run to reach at least ~60 minutes"),
                () -> assertTrue(workflow.contains("--require-uptime-monotonic"), "Nightly perf workflow should ensure CI uptime samples are monotonic"),
                () -> assertTrue(workflow.contains("--max-enchadd-share 0.001"), "Nightly perf workflow should enforce Spark source-share redline"),
                () -> assertTrue(workflow.contains("--log \"tests/papermc/target/paper-logs/perf-paper-${{ matrix.paper_version }}.log\""), "Nightly perf workflow should evaluate Spark gate against the 60m soak log"),
                () -> assertTrue(workflow.contains("tests/papermc/scripts/generate_enchant_catalog.py"), "Nightly perf workflow should regenerate enchant catalog snapshots"),
                () -> assertTrue(workflow.contains("--fail-on-unclassified"), "Nightly perf workflow should fail when any enchant is missing acquisition tier classification"),
                () -> assertTrue(workflow.contains("tests/papermc/src/test/resources/testermc/long-perf-commands.txt"), "Nightly perf workflow should rely on deterministic long perf command script")
        );
    }

    @Test
    void integrationRunnerValidatesLanguageAwareCiSentinel() throws IOException {
        String runner = Files.readString(PAPER_INTEGRATION_RUNNER, StandardCharsets.UTF_8);

        assertAll(
                () -> assertTrue(runner.contains("require_ci_field(ci_metrics, \"status\", \"OK\")"), "Paper runner should require CI status=OK"),
                () -> assertTrue(runner.contains("require_ci_min_int(ci_metrics, \"enchants\", 90)"), "Paper runner should guard against missing enchant registration"),
                () -> assertTrue(runner.contains("require_ci_field(ci_metrics, \"hasAirbag\", \"true\")"), "Paper runner should require the airbag sentinel"),
                () -> assertTrue(runner.contains("require_ci_field(ci_metrics, \"activeLang\", \"zh\")"), "Paper runner should verify zh language bootstrapping"),
                () -> assertTrue(runner.contains("require_ci_field(ci_metrics, \"translationReady\", \"true\")"), "Paper runner should verify translated enchant names"),
                () -> assertTrue(runner.contains("require_ci_field(ci_metrics, \"airbagCodepoints\", \"5b89-5168-6c14-56ca\")"), "Paper runner should assert the Chinese Airbag translation codepoints"),
                () -> assertTrue(runner.contains("require_ci_field(ci_metrics, \"braceCodepoints\", \"7a33-67b6\")"), "Paper runner should assert the Chinese Brace translation codepoints"),
                () -> assertTrue(runner.contains("require_ci_min_float(ci_metrics, \"tps1m\", 10.0)"), "Paper runner should assert TPS floor"),
                () -> assertTrue(runner.contains("require_ci_max_float(ci_metrics, \"errorRatePerMin\", 5.0)"), "Paper runner should assert runtime error rate ceiling"),
                () -> assertTrue(runner.contains("parse_tag_metrics(full_log, \"[ENCHADD-VERIFY]\")"), "Paper runner should parse behavior assertion sentinel"),
                () -> assertTrue(runner.contains("require_ci_min_int(verify_metrics, \"conflictChecks\", 10)"), "Paper runner should enforce verify conflict behavior check count"),
                () -> assertTrue(runner.contains("require_ci_min_int(verify_metrics, \"cooldownChecks\", 8)"), "Paper runner should enforce verify cooldown behavior check count"),
                () -> assertTrue(runner.contains("require_ci_min_int(verify_metrics, \"damageChecks\", 10)"), "Paper runner should enforce verify damage behavior check count"),
                () -> assertTrue(runner.contains("require_ci_min_int(verify_metrics, \"boundaryChecks\", 8)"), "Paper runner should enforce verify boundary behavior check count"),
                () -> assertTrue(runner.contains("require_ci_min_int(verify_metrics, \"itemChecks\", 3)"), "Paper runner should enforce verify item persistence check count"),
                () -> assertTrue(runner.contains("require_ci_min_int(verify_metrics, \"total\", 39)"), "Paper runner should enforce verify total check count"),
                () -> assertTrue(runner.contains("parse_tag_metrics(full_log, \"[ENCHADD-BALANCE]\")"), "Paper runner should parse anti-abuse sentinel"),
                () -> assertTrue(runner.contains("require_ci_min_int(balance_metrics, \"pairs\", 30)"), "Paper runner should enforce anti-abuse pair coverage"),
                () -> assertTrue(runner.contains("require_ci_min_int(balance_metrics, \"scored\", 10)"), "Paper runner should enforce anti-abuse scored pair coverage"),
                () -> assertTrue(runner.contains("parse_tag_metrics(full_log, \"[ENCHADD-COMBO-GATE]\")"), "Paper runner should parse combo gate sentinel"),
                () -> assertTrue(runner.contains("require_ci_max_int(combo_gate_metrics, \"violations\", 0)"), "Paper runner should fail when combo gate violations appear"),
                () -> assertTrue(runner.contains("\"could not pass event\""), "Paper runner should fail on Bukkit event dispatch exceptions"),
                () -> assertTrue(runner.contains("\"argument type mismatch\""), "Paper runner should fail on event argument mismatch exceptions"),
                () -> assertTrue(runner.contains("\"nosuchmethoderror\""), "Paper runner should fail on API method linkage errors"),
                () -> assertTrue(runner.contains("\"noclassdeffounderror\""), "Paper runner should fail on missing class linkage errors")
        );
    }

    @Test
    void catalogGeneratorScriptExistsForSourceOfTruthExports() {
        assertTrue(Files.isRegularFile(CATALOG_GENERATOR), "Catalog generator should exist for reproducible enchant exports");
    }

    @Test
    void testermcFixtureFilesExistAndEnableChineseDebugConfig() throws IOException {
        assertAll(
                () -> assertTrue(Files.isRegularFile(TEST_SERVER_PROPERTIES), "TestMC fixture should include server.properties"),
                () -> assertTrue(Files.isRegularFile(TEST_PLUGIN_CONFIG), "TestMC fixture should include EnchADD test config")
        );

        String serverProperties = Files.readString(TEST_SERVER_PROPERTIES, StandardCharsets.UTF_8);
        String pluginConfig = Files.readString(TEST_PLUGIN_CONFIG, StandardCharsets.UTF_8);

        assertAll(
                () -> assertTrue(serverProperties.contains("motd=EnchADD TestMC integration"), "server.properties should mark the integration environment"),
                () -> assertTrue(pluginConfig.contains("debug: true"), "Paper fixture should enable debug logging for EnchADD"),
                () -> assertTrue(pluginConfig.contains("language: zh"), "Paper fixture should force zh translations for CI coverage")
        );
    }
}
