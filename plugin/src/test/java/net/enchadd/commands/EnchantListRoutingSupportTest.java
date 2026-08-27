package net.enchadd.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnchantListRoutingSupportTest {

    @Test
    void subcommandValidationIsCaseInsensitiveAndRejectsEmptyInput() {
        assertFalse(EnchantListSubcommands.isValid(new String[0]));
        assertTrue(EnchantListSubcommands.isValid(new String[] {"LIST"}));
        assertTrue(EnchantListSubcommands.isValid(new String[] {"exportjson"}));
        assertFalse(EnchantListSubcommands.isValid(new String[] {"unknown"}));
    }

    @Test
    void exportFamilyDetectionCoversAllExportSubcommands() {
        assertTrue(EnchantListSubcommands.isExport("export"));
        assertTrue(EnchantListSubcommands.isExport("exportjson"));
        assertTrue(EnchantListSubcommands.isExport("exportcsv"));
        assertFalse(EnchantListSubcommands.isExport("list"));
    }

    @Test
    void listRequestParsesLanguageAwarePaginationAndOptions() {
        EnchantListCommandSupport support = new EnchantListCommandSupport(null);

        EnchantListRequest request = EnchantListRequest.from(new String[] {
                "list", "zh", "3", "25", "sort=weight", "order=desc", "namespace=enchadd", "keywords=斩首,吸血"
        }, support);

        assertTrue(request.options().keywords.contains("斩首"));
        assertTrue(request.options().keywords.contains("吸血"));
        org.junit.jupiter.api.Assertions.assertEquals(3, request.page());
        org.junit.jupiter.api.Assertions.assertEquals(25, request.size());
        org.junit.jupiter.api.Assertions.assertEquals("weight", request.options().sort);
        org.junit.jupiter.api.Assertions.assertEquals("desc", request.options().order);
        org.junit.jupiter.api.Assertions.assertEquals("enchadd", request.options().namespace);
    }
}
