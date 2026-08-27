package net.enchadd.commands;

record EnchantListRequest(int page, int size, EnchantListOptions options) {

    static EnchantListRequest from(String[] args, EnchantListCommandSupport support) {
        boolean hasLang = support.hasLangArg(args);
        int[] pageSize = support.resolvePageSize(args, hasLang);
        EnchantListOptions options = support.parseOptions(args, hasLang ? 4 : 3);
        return new EnchantListRequest(pageSize[0], pageSize[1], options);
    }
}
