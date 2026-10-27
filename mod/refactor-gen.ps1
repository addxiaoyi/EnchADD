$p = "D:\qwq\项目\Enchantio-master\mod\generate-enchant-resources.gradle"
$c = [System.Text.Encoding]::UTF8.GetString([System.IO.File]::ReadAllBytes($p))

# 1. isLegacy 定义
$old1 = "def isG3int = project.projectDir.name.endsWith('1.21.6') || project.projectDir.name.endsWith('1.21.7') || project.projectDir.name.endsWith('1.21.8') || project.projectDir.name.endsWith('1.21.9') || project.projectDir.name.endsWith('1.21.10')"
$new1 = $old1 + [char]13 + [char]10 + "def isLegacy = is1_21_1 || isG2 || isG3int"
$c = $c.Replace($old1, $new1)

# 2. 统一替换
$c = $c.Replace("(is1_21_1 || isG2 || isG3int)", "(isLegacy)")
$c = $c.Replace("is1_21_1 || isG2 || isG3int", "isLegacy")

# 3. synth-tag 确定性命名
$DQ = [char]34
$old3 = "def makeSynthTag = { List<String> tagRefs ->" + [char]13 + [char]10 + "    setTagSeq = setTagSeq + 1" + [char]13 + [char]10 + "    def key = " + $DQ + "synth-" + $DQ + " + setTagSeq" + [char]13 + [char]10 + "    def values = tagRefs.collect { " + $DQ + "#" + $DQ + " + it.replaceFirst(" + $DQ + "^minecraft:" + $DQ + ", " + $DQ + "minecraft:" + $DQ + ") }" + [char]13 + [char]10 + "    synthTagQueue << [key: key, values: values]" + [char]13 + [char]10 + "    return " + $DQ + "#enchadd:" + $DQ + " + key" + [char]13 + [char]10 + "}"
$new3 = "def makeSynthTag = { List<String> tagRefs ->" + [char]13 + [char]10 + "    def key = " + $DQ + "synth-" + $DQ + " + tagRefs.sort().collect { it.replace(" + $DQ + "minecraft:" + $DQ + ", " + $DQ + $DQ + ").replaceAll(" + $DQ + "[^a-z0-9]" + $DQ + ", " + $DQ + "-" + $DQ + ") }.join(" + $DQ + "-" + $DQ + ")" + [char]13 + [char]10 + "    def values = tagRefs.collect { " + $DQ + "#" + $DQ + " + it }" + [char]13 + [char]10 + "    synthTagQueue << [key: key, values: values]" + [char]13 + [char]10 + "    return " + $DQ + "#enchadd:" + $DQ + " + key" + [char]13 + [char]10 + "}"
if ($c.Contains($old3)) { $c = $c.Replace($old3, $new3); "synth renamed" } else { "synth OLD NOT FOUND" }

[System.IO.File]::WriteAllText($p, $c, (New-Object System.Text.UTF8Encoding($false)))
"isLegacy count: $(([regex]::Matches($c, 'isLegacy')).Count)"
