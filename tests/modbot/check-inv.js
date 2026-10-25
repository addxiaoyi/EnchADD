const mineflayer = require('mineflayer');
const { Rcon } = require('./rcon');
(async () => {
  const b = mineflayer.createBot({ host: '127.0.0.1', port: 25565, username: 'EnchaddDefBot', auth: 'offline', version: process.env.MC_VERSION || '1.21.1' });
  await new Promise((res, rej) => { b.once('spawn', res); b.once('error', rej) });
  await new Promise(r => setTimeout(r, 2000));
  const rcon = new Rcon(25575, 'enchadd'); await rcon.connect();
  await rcon.cmd('op EnchaddDefBot'); await new Promise(r=>setTimeout(r,300));
  await rcon.cmd('clear EnchaddDefBot'); await new Promise(r=>setTimeout(r,300));
  let out = await rcon.cmd('item replace entity EnchaddDefBot weapon.offhand with minecraft:shield[minecraft:enchantments={levels:{"enchadd:bulwark":3}}]');
  console.log('REPLACE:', out);
  await new Promise(r=>setTimeout(r,300));
  out = await rcon.cmd('data get entity EnchaddDefBot Inventory'); console.log('INV:', out);
  out = await rcon.cmd('data get entity EnchaddDefBot equipment'); console.log('EQUIP:', out);
  rcon.close(); b.quit(); setTimeout(()=>process.exit(0), 1000);
})().catch(e => { console.error('FATAL', e.message); process.exit(2) })
