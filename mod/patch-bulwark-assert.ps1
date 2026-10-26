$p = "D:\qwq\项目\Enchantio-master\mod\generate-enchant-resources.gradle"
$lines = [System.IO.File]::ReadAllLines($p)
$idx = -1
for ($i=0; $i -lt $lines.Count; $i++) { if ($lines[$i].Contains("Fabric Bulwark must only trigger")) { $idx = $i - 1; break } }
$DQ = [char]34
$lines[$idx] = "            assert ((is1_21_1 || isG2) ? bulwarkMixin.text.contains('method = " + $DQ + "isDamageSourceBlocked" + $DQ + "') && bulwarkMixin.text.contains('Boolean.TRUE.equals(callback.getReturnValue())') : bulwarkMixin.text.contains('method = " + $DQ + "applyItemBlocking') && bulwarkMixin.text.contains('callback.getReturnValue() > 0.0f')):"
[System.IO.File]::WriteAllLines($p, $lines)
"patched: $($lines[$idx].Substring(0,100))"
