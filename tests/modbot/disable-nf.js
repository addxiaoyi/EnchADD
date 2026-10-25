const mineflayer = require('mineflayer');
const { Rcon } = require('./rcon');
async function wait(ms) { return new Promise(r => setTimeout(r, ms)) }
async function main() {
  const d = mineflayer.createBot({ host: '127.0.0.1', port: 25567, username: 'EnchaddDefBot', auth: 'offline', version: '1.21.1' });
  await new Promise((res, rej) => { d.once('spawn', res); d.once('error', rej) });
  const a = mineflayer.createBot({ host: '127.0.0.1', port: 25567, username: 'EnchaddAtkBot', auth: 'offline', version: '1.21.1' });
  await new Promise((res, rej) => { a.once('spawn', res); a.once('error', rej) });
  await wait(3000);
  const rcon = new Rcon(25577, 'enchadd'); await rcon.connect();
  await rcon.cmd('op EnchaddDefBot'); await rcon.cmd('op EnchaddAtkBot'); await wait(200);
  await rcon.cmd('item replace entity EnchaddDefBot weapon.offhand with minecraft:shield[minecraft:enchantments={levels:{"enchadd:bulwark":3}}]');
  await rcon.cmd('item replace entity EnchaddAtkBot weapon.mainhand with minecraft:iron_axe');
  await rcon.cmd('tp EnchaddDefBot 801 320 901'); await rcon.cmd('tp EnchaddAtkBot 801.9 320 901');
  await rcon.cmd('effect give EnchaddDefBot minecraft:instant_health 1 20 true');
  await wait(4500);
  const hp = async () => parseFloat(/([\d.]+)f\s*$/.exec((await rcon.cmd('data get entity EnchaddDefBot Health')).trim())[1]);
  await d.lookAt(a.entity.position.offset(0, 1.2, 0), true);
  d.activateItem(true); await wait(1200);
  // 第 1 击：斧击格挡 → 盾禁用 5s
  await a.lookAt(d.entity.position.offset(0, 1.2, 0), true);
  a.attack(d.entity);
  await wait(2800); // axe 冷却 2s + 余量
  const pre = await hp();
  // 第 2 击：盾禁用期间 → 全额伤害
  await rcon.cmd('tp EnchaddDefBot 801 320 901'); await rcon.cmd('tp EnchaddAtkBot 801.9 320 901'); await wait(500); await a.lookAt(d.entity.position.offset(0, 1.2, 0), true);
  a.attack(d.entity);
  await wait(800);
  const post = await hp();
  console.log(`RESULT|shield_disable_bypass|${post < pre - 0.5 ? 'PASS' : 'FAIL'}|blocked ${pre} -> disabled-hit ${post}`);
  rcon.close(); d.quit(); a.quit(); setTimeout(() => process.exit(0), 1200);
}
main().catch(e => { console.error('FATAL', e.message); process.exit(2) })
