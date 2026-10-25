const mineflayer = require('mineflayer');
const { Rcon } = require('./rcon');
async function wait(ms) { return new Promise(r => setTimeout(r, ms)) }
(async () => {
  const d = mineflayer.createBot({ host: '127.0.0.1', port: 25567, username: 'EnchaddDefBot', auth: 'offline', version: '1.21.1' });
  await new Promise((res, rej) => { d.once('spawn', res); d.once('error', rej) });
  await wait(2500);
  const rcon = new Rcon(25577, 'enchadd'); await rcon.connect();
  await rcon.cmd('op EnchaddDefBot'); await wait(200);
  await rcon.cmd('item replace entity EnchaddDefBot weapon.offhand with minecraft:shield[minecraft:enchantments={levels:{"enchadd:bulwark":3}}]');
  await wait(400);
  console.log('INV:', await rcon.cmd('data get entity EnchaddDefBot Inventory'));
  console.log('SYNC:', await rcon.cmd('data get entity EnchaddDefBot equipment'));
  rcon.close(); d.quit(); setTimeout(() => process.exit(0), 1000);
})().catch(e => { console.error('FATAL', e.message); process.exit(2) })
