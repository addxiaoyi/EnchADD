const mineflayer = require('mineflayer');
const { Rcon } = require('./rcon');
async function wait(ms) { return new Promise(r => setTimeout(r, ms)) }
async function main() {
  const d = mineflayer.createBot({ host: '127.0.0.1', port: 25567, username: 'EnchaddDefBot', auth: 'offline', version: process.env.MC_VERSION || '1.21.1' });
  await new Promise((res, rej) => { d.once('spawn', res); d.once('error', rej) });
  const a = mineflayer.createBot({ host: '127.0.0.1', port: 25567, username: 'EnchaddAtkBot', auth: 'offline', version: process.env.MC_VERSION || '1.21.1' });
  await new Promise((res, rej) => { a.once('spawn', res); a.once('error', rej) });
  await wait(3000);
  const rcon = new Rcon(25577, 'enchadd'); await rcon.connect();
  await rcon.cmd('op EnchaddDefBot'); await rcon.cmd('op EnchaddAtkBot'); await wait(200);
  await rcon.cmd('item replace entity EnchaddDefBot weapon.offhand with minecraft:air');
  await rcon.cmd('item replace entity EnchaddAtkBot weapon.mainhand with minecraft:diamond_sword');
  await rcon.cmd('tp EnchaddDefBot 801 320 901'); await rcon.cmd('tp EnchaddAtkBot 801.9 320 901');
  await wait(4500);
  console.log('DEF-POS:', await rcon.cmd('data get entity EnchaddDefBot Pos'));
  console.log('ATK-POS:', await rcon.cmd('data get entity EnchaddAtkBot Pos'));
  const hp = async () => parseFloat(/([\d.]+)f\s*$/.exec((await rcon.cmd('data get entity EnchaddDefBot Health')).trim())[1]);
  const before = await hp();
  await a.lookAt(d.entity.position.offset(0, 1.2, 0), true);
  console.log('LOOK-DONE, yaw=', a.entity.yaw, 'pitch=', a.entity.pitch, 'dist=', a.entity.position.distanceTo(d.entity.position));
  a.attack(d.entity);
  await wait(700);
  const after = await hp();
  console.log(`CAL(noshield): ${before} -> ${after}`);
  rcon.close(); d.quit(); a.quit(); setTimeout(() => process.exit(0), 1000);
}
main().catch(e => { console.error('FATAL', e.message); process.exit(2) })
