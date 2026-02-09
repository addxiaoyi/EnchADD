package com.enadd.core.api;

import java.util.Map;
import java.util.Set;


public interface IEnchantmentConflictRules {
    Map<String, Set<String>> getConflictRules();
    boolean areConflicting(String enchantment1, String enchantment2);
    Set<String> getConflicts(String enchantmentId);
    String getCategory(String enchantmentId);
}
