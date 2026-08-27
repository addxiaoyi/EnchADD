package net.enchadd;

import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Entity;
import org.mockito.Mockito;

final class TestDamageSources {

    private TestDamageSources() {
    }

    static DamageSource directPlayerDamage(Entity causingEntity) {
        DamageSource source = Mockito.mock(DamageSource.class);
        Mockito.when(source.isIndirect()).thenReturn(false);
        Mockito.when(source.getCausingEntity()).thenReturn(causingEntity);
        return source;
    }
}
