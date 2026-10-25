const mineflayer = require('mineflayer');
const { Rcon } = require('./rcon');
async function wait(ms) { return new Promise(r => setTimeout(r, ms)) }
(async () => {
  const d = mineflayer.createBot({ host: '127.0.0.1', port: 25566, username: 'EnchaddDefBot', auth: 'offline', version: '1.21.1' });
  await new Promise((res, rej) => { d.once('spawn', res); d.once('error', rej) });
  const a = mineflayer.createBot({ host: '127.0.0.1', port: 25566, username: 'EnchaddAtkBot', auth: 'offline', version: '1.21.1' });
  await new Promise((res, rej) => { a.once('spawn', res); a.once('error', rej) });
  await wait(3000);
  const rcon = new Rcon(25576, 'enchadd'); await rcon.connect();
  await rcon.cmd('op EnchaddDefBot'); await rcon.cmd('op EnchaddAtkBot'); await wait(200);
  await rcon.cmd('tp EnchaddDefBot 801 320 901'); await rcon.cmd('tp EnchaddAtkBot 801.9 320 901');
  await wait(4500);
  console.log('DEF:', await rcon.cmd('data get entity EnchaddDefBot Pos'));
  console.log('ATK:', await rcon.cmd('data get entity EnchaddAtkBot Pos'));
  console.log('ONGROUND-D:', await rcon.cmd('data get entity EnchaddDefBot OnGround'));
  console.log('ONGROUND-A:', await rcon.cmd('data get entity EnchaddAtkBot OnGround'));
  // 手动攻击测试
  const before = parseFloat(/([\d.]+)f\s*$/.exec((await rcon.cmd('data get entity EnchaddDefBot Health')).trim())[1]);
  await a.lookAt(d.entity.position.offset(0, 1.2, 0), true);
  a.attack(d.entity);
  await wait(600);
  const after = parseFloat(/([\d.]+)f\s*$/.exec((await rcon.cmd('data get entity EnchaddDefBot Health')).trim())[1]);
  console.log(`MANUAL-STRIKE (no shield equip? d has shield): ${before} -> ${after}`);
  console.log('INV:', await rcon.cmd('data get entity EnchaddDefBot Inventory'));
  rcon.close(); d.quit(); a.quit(); setTimeout(() => process.exit(0), 1000);
})().catch(e => { console.error('FATAL', e.message); process.exit(2) })
