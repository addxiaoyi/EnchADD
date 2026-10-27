const { Rcon } = require("./rcon");
async function wait(ms) { return new Promise(r => setTimeout(r, ms)) }
(async () => {
  const rcon = new Rcon(25583, "enchadd"); await rcon.connect();
  await wait(200);
  const dp = await rcon.cmd("datapack list");
  console.log("DATAPACK:", dp.trim());
  await rcon.cmd("summon minecraft:armor_stand 800 320 900 {NoGravity:1b,Tags:[enchadd_test]}");
  await wait(300);
  await rcon.cmd("item replace entity @e[tag=enchadd_test,limit=1] weapon.mainhand with minecraft:diamond_sword");
  await wait(200);
  const e1 = await rcon.cmd("enchant @e[tag=enchadd_test,limit=1] enchadd:bulwark 1");
  console.log("BULWARK-ON-SWORD:", e1.trim());
  await rcon.cmd("item replace entity @e[tag=enchadd_test,limit=1] weapon.mainhand with minecraft:shield");
  await wait(200);
  const e2 = await rcon.cmd("enchant @e[tag=enchadd_test,limit=1] enchadd:bulwark 3");
  console.log("BULWARK-ON-SHIELD:", e2.trim());
  const e3 = await rcon.cmd("enchant @e[tag=enchadd_test,limit=1] enchadd:immolate 2");
  console.log("IMMOLATE-ON-SHIELD:", e3.trim());
  const e4 = await rcon.cmd("enchant @e[tag=enchadd_test,limit=1] enchadd:telepathy 1");
  console.log("TELEPATHY-ON-SHIELD:", e4.trim());
  rcon.close(); setTimeout(() => process.exit(0), 1000);
})().catch(e => { console.error("FATAL", e.message); process.exit(2) })
