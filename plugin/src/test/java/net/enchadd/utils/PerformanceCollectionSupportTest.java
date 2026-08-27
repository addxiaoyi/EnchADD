package net.enchadd.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PerformanceCollectionSupportTest {

    @Test
    void newArrayListWithCapacityCreatesUsableListWithMinimumCapacity() {
        ArrayList<String> list = PerformanceCollectionSupport.newArrayListWithCapacity(0);

        list.add("alpha");

        assertEquals(1, list.size());
        assertEquals("alpha", list.getFirst());
    }

    @Test
    void newHashMapWithCapacityCreatesUsableMapForRequestedEntries() {
        HashMap<String, Integer> map = PerformanceCollectionSupport.newHashMapWithCapacity(32);

        for (int i = 0; i < 32; i++) {
            map.put("k" + i, i);
        }

        assertEquals(32, map.size());
        assertTrue(map.containsKey("k31"));
    }
}
