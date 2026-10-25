const mineflayer = require('mineflayer');
const { Rcon } = require('./rcon');
async function wait(ms) { return new Promise(r => setTimeout(r, ms)) }
async function main() {
  const port = 25567, rport = 25577;
  const d = await (async () => { const b = mineflayer.createBot({ host: '127.0.0.1', port, username: 'EnchaddDefBot', auth: 'offline', version: process.env.MC_VERSION || '1.21.1' }); await new Promise((res, rej) => { b.once('spawn', res); b.once('error', rej) }); return b })();
  const a = await (async () => { const b = mineflayer.createBot({ host: '127.0.0.1', port, username: 'EnchaddAtkBot', auth: 'offline', version: process.env.MC_VERSION || '1.21.1' }); await new Promise((res, rej) => { b.once('spawn', res); b.once('error', rej) }); return b })();
  await wait(3000);
  const rcon = new Rcon(rport, 'enchadd'); await rcon.connect();
  await rcon.cmd('op EnchaddDefBot'); await rcon.cmd('op EnchaddAtkBot'); await wait(200);
  await rcon.cmd('item replace entity EnchaddDefBot weapon.offhand with minecraft:shield[minecraft:damage=0]');
  await rcon.cmd('item replace entity EnchaddAtkBot weapon.mainhand with minecraft:iron_axe');
  await rcon.cmd('tp EnchaddDefBot 801 320 901'); await rcon.cmd('tp EnchaddAtkBot 801.9 320 901');
  await wait(4500);
  const hp = async () => parseFloat(/([\d.]+)f\s*$/.exec((await rcon.cmd('data get entity EnchaddDefBot Health')).trim())[1]);
  const dmg = async () => { const o = await rcon.cmd('data get entity EnchaddDefBot Inventory'); const m = /damage:\s*(\d+)/.exec(o||''); return m ? parseInt(m[1]) : 0 };
  await d.lookAt(a.entity.position.offset(0, 1.2, 0), true);
  d.activateItem(true); await wait(1200);
  const dmg0 = await dmg();
  await a.lookAt(d.entity.position.offset(0, 1.2, 0), true);
  a.attack(d.entity); await wait(1000);
  const dmg1 = await dmg();
  const h1 = await hp();
  console.log(`D1: shield damage ${dmg0} -> ${dmg1}, health=${h1}`);
  // 第 2 击在禁盾 5 秒窗口内（fresh 状态中断最小化）
  await a.lookAt(d.entity.position.offset(0, 1.2, 0), true);
  a.attack(d.entity); await wait(700);
  const dmg2 = await dmg();
  const h2 = await hp();
  console.log(`D2 (within 5s window): shield damage ${dmg1} -> ${dmg2}, health=${h2}`);
  console.log(`RESULT|shield_disable_bypass|${h2 < h1 - 0.5 ? 'PASS' : 'FAIL'}|blocked(hp ${h1}, shield -${dmg1-dmg0}) then disabled-hit(hp ${h2}, shield -${dmg2-dmg1})`);
  rcon.close(); d.quit(); a.quit(); setTimeout(() => process.exit(0), 1200);
}
main().catch(e => { console.error('FATAL', e.message); process.exit(2) })
