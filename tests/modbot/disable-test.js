const mineflayer = require('mineflayer')
const { Rcon } = require('./rcon')
async function wait(ms) { return new Promise(r => setTimeout(r, ms)) }
async function main() {
  const defender = mineflayer.createBot({ host: '127.0.0.1', port: parseInt(process.argv[2]||'25566'), username: 'EnchaddDefBot', auth: 'offline', version: '1.21.1' })
  await new Promise((res, rej) => { defender.once('spawn', res); defender.once('error', rej) })
  const attacker = mineflayer.createBot({ host: '127.0.0.1', port: parseInt(process.argv[2]||'25566'), username: 'EnchaddAtkBot', auth: 'offline', version: '1.21.1' })
  await new Promise((res, rej) => { attacker.once('spawn', res); attacker.once('error', rej) })
  await wait(3000)
  const rcon = new Rcon(parseInt(process.argv[3]||'25575'), 'enchadd'); await rcon.connect()
  await rcon.cmd('op EnchaddDefBot'); await rcon.cmd('op EnchaddAtkBot'); await wait(300)
  const hp = async () => parseFloat(/([\d.]+)f\s*$/.exec((await rcon.cmd('data get entity EnchaddDefBot Health')).trim())[1])
  await rcon.cmd('effect clear EnchaddDefBot')
  await rcon.cmd('attribute EnchaddDefBot minecraft:generic.max_health base set 20')
  await rcon.cmd('effect give EnchaddDefBot minecraft:instant_health 1 20 true')
  await rcon.cmd('item replace entity EnchaddDefBot weapon.offhand with minecraft:shield[minecraft:enchantments={levels:{"enchadd:bulwark":3}}]')
  await rcon.cmd('item replace entity EnchaddAtkBot weapon.mainhand with minecraft:iron_axe')
  await rcon.cmd('tp EnchaddDefBot 600 78 600'); await rcon.cmd('tp EnchaddAtkBot 600.9 78 600')
  await wait(1000)
  // 持盾
  await defender.lookAt(attacker.entity.position.offset(0, 1.2, 0), true)
  defender.activateItem(true)
  await wait(1200)
  // 第一击：斧击 → 盾应被禁用（仍有格挡，无伤）
  await attacker.lookAt(defender.entity.position.offset(0, 1.2, 0), true)
  attacker.attack(defender.entity)
  await wait(2500) // iron_axe 攻速 1.0 → 2s 冷却
  const pre = await hp()
  // 第二击：盾已禁用 5 秒内，defender 仍试图持盾但无效 → 全额伤害
  await attacker.lookAt(defender.entity.position.offset(0, 1.2, 0), true)
  attacker.attack(defender.entity)
  await wait(800)
  const post = await hp()
  console.log(`RESULT|shield_disable_bypass|${post < pre - 0.5 ? 'PASS' : 'FAIL'}|blocked ${pre} -> disabled-hit ${post} (expect full ~9 dmg)`)
  rcon.close(); defender.quit(); attacker.quit(); setTimeout(() => process.exit(0), 1200)
}
main().catch(e => { console.error('FATAL', e.message); process.exit(2) })
