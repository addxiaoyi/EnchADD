const mineflayer = require('mineflayer');
const { Rcon } = require('./rcon');
async function wait(ms) { return new Promise(r => setTimeout(r, ms)) }
async function spawnBot(u, port) {
  const b = mineflayer.createBot({ host: '127.0.0.1', port, username: u, auth: 'offline', version: '1.21.1' });
  await new Promise((res, rej) => { b.once('spawn', res); b.once('error', rej) });
  return b;
}
async function main() {
  const port = 25567, rport = 25577;
  const d = await spawnBot('EnchaddDefBot', port);
  let a = await spawnBot('EnchaddAtkBot', port);
  await wait(3000);
  const rcon = new Rcon(rport, 'enchadd'); await rcon.connect();
  await rcon.cmd('op EnchaddDefBot'); await rcon.cmd('op EnchaddAtkBot'); await wait(200);
  await rcon.cmd('item replace entity EnchaddDefBot weapon.offhand with minecraft:shield[minecraft:enchantments={levels:{"enchadd:bulwark":3}}]');
  await rcon.cmd('item replace entity EnchaddAtkBot weapon.mainhand with minecraft:iron_axe');
  await rcon.cmd('tp EnchaddDefBot 801 320 901'); await rcon.cmd('tp EnchaddAtkBot 801.9 320 901');
  await rcon.cmd('effect give EnchaddDefBot minecraft:instant_health 1 20 true');
  await wait(4500);
  const hp = async () => parseFloat(/([\d.]+)f\s*$/.exec((await rcon.cmd('data get entity EnchaddDefBot Health')).trim())[1]);
  // 第 1 击（fresh attacker）→ 格挡 + 禁盾
  await d.lookAt(a.entity.position.offset(0, 1.2, 0), true);
  d.activateItem(true); await wait(1200);
  await a.lookAt(d.entity.position.offset(0, 1.2, 0), true);
  a.attack(d.entity);
  await wait(1500);
  const pre = await hp();
  // attacker 重连（fresh 第一击状态）
  a.quit(); await wait(1500);
  a = await spawnBot('EnchaddAtkBot', port);
  await wait(2500);
  await rcon.cmd('op EnchaddAtkBot'); await wait(200);
  await rcon.cmd('item replace entity EnchaddAtkBot weapon.mainhand with minecraft:iron_axe');
  await rcon.cmd('tp EnchaddAtkBot 801.9 320 901');
  await wait(2500);
  await a.lookAt(d.entity.position.offset(0, 1.2, 0), true);
  a.attack(d.entity);
  await wait(800);
  const post = await hp();
  console.log(`RESULT|shield_disable_bypass|${post < pre - 0.5 ? 'PASS' : 'FAIL'}|blocked ${pre} -> disabled-hit ${post}`);
  rcon.close(); d.quit(); a.quit(); setTimeout(() => process.exit(0), 1200);
}
main().catch(e => { console.error('FATAL', e.message); process.exit(2) })
