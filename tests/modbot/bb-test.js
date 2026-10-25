const mineflayer = require('mineflayer');
const { Rcon } = require('./rcon');
async function wait(ms) { return new Promise(r => setTimeout(r, ms)) }
async function main() {
  const port = parseInt(process.argv[2]||'25566'), rport = parseInt(process.argv[3]||'25575');
  const d = mineflayer.createBot({ host: '127.0.0.1', port, username: 'EnchaddDefBot', auth: 'offline', version: '1.21.1' });
  await new Promise((res, rej) => { d.once('spawn', res); d.once('error', rej) });
  const a = mineflayer.createBot({ host: '127.0.0.1', port, username: 'EnchaddAtkBot', auth: 'offline', version: '1.21.1' });
  await new Promise((res, rej) => { a.once('spawn', res); a.once('error', rej) });
  await wait(3000);
  const rcon = new Rcon(rport, 'enchadd'); await rcon.connect();
  await rcon.cmd('op EnchaddDefBot'); await rcon.cmd('op EnchaddAtkBot'); await wait(200);
  await rcon.cmd('item replace entity EnchaddDefBot weapon.offhand with minecraft:shield[minecraft:enchantments={levels:{"enchadd:bulwark":3}}]');
  await rcon.cmd('item replace entity EnchaddAtkBot weapon.mainhand with minecraft:diamond_sword[minecraft:enchantments={levels:{"enchadd:breakguard":3}}]');
  await rcon.cmd('tp EnchaddDefBot 801 320 901'); await rcon.cmd('tp EnchaddAtkBot 801.9 320 901');
  await rcon.cmd('effect give EnchaddDefBot minecraft:instant_health 1 20 true');
  await wait(4500);
  const hp = async () => parseFloat(/([\d.]+)f\s*$/.exec((await rcon.cmd('data get entity EnchaddDefBot Health')).trim())[1]);
  // 持盾（offhand flag）
  await d.lookAt(a.entity.position.offset(0, 1.2, 0), true);
  d.activateItem(true);
  await wait(1200);
  // 攻击一次（fresh spawn 无冷却）
  await a.lookAt(d.entity.position.offset(0, 1.2, 0), true);
  const before = await hp();
  a.attack(d.entity);
  await wait(700);
  const after = await hp();
  const eff = await rcon.cmd('data get entity EnchaddDefBot active_effects');
  const res = /resistance/i.test(eff);
  console.log(`RESULT|bulwark_resistance|${res ? 'PASS' : 'FAIL'}|hp ${before}->${after}, effects="${eff.trim().substring(0,120)}"`);
  console.log(`RESULT|breakguard_penetration|${after < before - 0.2 ? 'PASS' : 'FAIL'}|blocked-with-bonus: ${before}->${after}`);
  rcon.close(); d.quit(); a.quit(); setTimeout(() => process.exit(0), 1200);
}
main().catch(e => { console.error('FATAL', e.message); process.exit(2) })
