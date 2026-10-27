const { Rcon } = require("./rcon");
async function wait(ms) { return new Promise(r => setTimeout(r, ms)) }
(async () => {
  const rcon = new Rcon(25580, "enchadd"); await rcon.connect();
  await wait(300);
  // ?? 1: ?????
  const dp = await rcon.cmd("datapack list");
  console.log("DATAPACK:", dp.trim());
  // ?? 2: EnchADD ????????? enchant ?????????? summon armor stand?
  await rcon.cmd("kill @e[type=minecraft:armor_stand]");
  await rcon.cmd("summon minecraft:armor_stand 0 200 0 {NoGravity:1b,Tags:[enchadd_test]}");
  await wait(300);
  await rcon.cmd("item replace entity @e[tag=enchadd_test,limit=1] weapon.mainhand with minecraft:diamond_sword");
  await wait(200);
  const e1 = await rcon.cmd("enchant @e[tag=enchadd_test,limit=1] enchadd:telepathy 1");
  console.log("TELEPATHY-ON-SWORD:", e1.trim());
  await rcon.cmd("item replace entity @e[tag=enchadd_test,limit=1] weapon.mainhand with minecraft:shield");
  await wait(200);
  const e2 = await rcon.cmd("enchant @e[tag=enchadd_test,limit=1] enchadd:bulwark 3");
  console.log("BULWARK-ON-SHIELD:", e2.trim());
  // ?? 3: ?????????? NBT ???
  const g = await rcon.cmd("give @p minecraft:enchanted_book[minecraft:stored_enchantments={\"enchadd:telepathy\":1}] 1");
  console.log("BOOK-TELEPATHY:", g.trim());
  rcon.close(); setTimeout(() => process.exit(0), 1000);
})().catch(e => { console.error("FATAL", e.message); process.exit(2) })
