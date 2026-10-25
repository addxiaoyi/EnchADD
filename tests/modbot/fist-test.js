const mineflayer = require('mineflayer');
const { Rcon } = require('./rcon');
async function wait(ms) { return new Promise(r => setTimeout(r, ms)) }
async function main() {
  const defender = mineflayer.createBot({ host: '127.0.0.1', port: parseInt(process.argv[2]||'25566'), username: 'EnchaddDefBot', auth: 'offline', version: '1.21.1' });
  await new Promise((res, rej) => { defender.once('spawn', res); defender.once('error', rej) });
  const attacker = mineflayer.createBot({ host: '127.0.0.1', port: parseInt(process.argv[2]||'25566'), username: 'EnchaddAtkBot', auth: 'offline', version: '1.21.1' });
  await new Promise((res, rej) => { attacker.once('spawn', res); attacker.once('error', rej) });
  await wait(3000);
  const rcon = new Rcon(parseInt(process.argv[3]||'25575'), 'enchadd'); await rcon.connect();
  await rcon.cmd('op EnchaddDefBot'); await rcon.cmd('op EnchaddAtkBot'); await wait(300);
  const hp = async () => parseFloat(/([\d.]+)f\s*$/.exec((await rcon.cmd('data get entity EnchaddDefBot Health')).trim())[1]);
  // 搭建固定测试平台：y=200 石台 5x5，超空洞
  await rcon.cmd('fill 800 199 800 804 199 804 minecraft:stone');
  await rcon.cmd('effect clear EnchaddDefBot');
  await rcon.cmd('attribute EnchaddDefBot minecraft:generic.max_health base set 20');
  await rcon.cmd('effect give EnchaddDefBot minecraft:instant_health 1 20 true');
  await rcon.cmd('item replace entity EnchaddDefBot weapon.offhand with minecraft:shield');
  await rcon.cmd('item replace entity EnchaddAtkBot weapon.mainhand with minecraft:air');
  await rcon.cmd('tp EnchaddDefBot 801 200 801'); await rcon.cmd('tp EnchaddAtkBot 801.9 200 801');
  await wait(1200);
  const before = await hp();
  await defender.lookAt(attacker.entity.position.offset(0, 1.2, 0), true);
  defender.activateItem(true);
  await wait(1200);
  await attacker.lookAt(defender.entity.position.offset(0, 1.2, 0), true);
  attacker.attack(defender.entity);
  await wait(800);
  const after = await hp();
  console.log(`RESULT|low_damage_full_block|${Math.abs(after-before) < 0.01 ? 'PASS' : 'FAIL'}|fist vs shield on platform: ${before} -> ${after}`);
  rcon.close(); defender.quit(); attacker.quit(); setTimeout(() => process.exit(0), 1200);
}
main().catch(e => { console.error('FATAL', e.message); process.exit(2) })
