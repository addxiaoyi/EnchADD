---
name: "java-lint-cleanup"
description: "Automatically detects and fixes Java lint warnings and compilation errors in Minecraft plugin projects. Invoke when IDE shows lint warnings, compilation errors, or unused imports/variables in Java files."
---

# Java Lint Cleanup

This skill automatically detects and fixes Java lint warnings and compilation errors in Minecraft plugin projects.

## When to Use

Invoke this skill when:
- IDE shows lint warnings or compilation errors
- User provides diagnostic output from IDE or build tools
- Need to clean up unused imports, variables, or fields
- Build fails due to compilation errors
- Need to fix deprecated API usage warnings

## Common Issues and Solutions

### 1. Unused Imports
**Problem:** Import statements that are never used
**Solution:** Remove the unused import statements

### 2. Unused Variables/Fields
**Problem:** Variables or fields declared but never used
**Solutions:**
- Remove the unused declaration if truly unused
- Add `@SuppressWarnings("unused")` annotation if reserved for future use
- Use the variable if it should be used

### 3. Deprecated API Usage
**Problem:** Using deprecated Bukkit/Paper APIs
**Solutions:**
- Replace with modern alternatives (Adventure API for text components)
- Add `@SuppressWarnings("deprecation")` if deprecated API is required
- Update to newer Bukkit API equivalents

### 4. Test File Package Mismatch
**Problem:** Test file package doesn't match expected package structure
**Solution:** Use fully qualified class names or ensure package declaration matches directory structure

### 5. Compilation Errors (Missing Symbols)
**Problem:** Class or method cannot be resolved
**Solutions:**
- Add missing imports
- Fix class name typos
- Ensure all dependencies are properly imported

## Workflow

1. **Analyze Diagnostics**: Review IDE/build tool output to identify issues
2. **Categorize Issues**: Group by type (unused code, deprecated API, compilation errors)
3. **Fix Issues**: Apply appropriate fixes to each category
4. **Verify Build**: Run compilation to ensure all issues resolved
5. **Iterate**: Repeat until no errors remain

## Commands

```bash
# Run build to check for errors
./gradlew build --no-daemon

# Compile only
./gradlew compileJava --no-daemon

# Run tests
./gradlew test --no-daemon
```

## Example Fixes

### Unused Import
```java
// Before
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;  // Unused

// After (remove unused)
import org.bukkit.Material;
```

### Unused Field with SuppressWarning
```java
// Before
private int maxRecordsPerEntity = 100;

// After (keep for future use)
@SuppressWarnings("unused")
private int maxRecordsPerEntity = 100;
```

### Deprecated API
```java
// Before (deprecated)
meta.setLore(lore);

// After (or suppress if replacement not available)
@SuppressWarnings("deprecation")
meta.setLore(lore);
```

## Best Practices

1. **Don't Remove Needed Code**: Ensure fields/variables aren't used before removing
2. **Preserve API Compatibility**: Don't break existing functionality when cleaning up
3. **Add Comments**: Document why `@SuppressWarnings` is used
4. **Verify Build**: Always run build after changes to confirm fixes work
5. **Handle Dependencies**: Ensure removing imports doesn't break compilation

## Integration with IDE

This skill works with diagnostic output from:
- VS Code Java diagnostics
- IntelliJ IDEA inspections
- Gradle compilation output
- Eclipse JDT compiler warnings
