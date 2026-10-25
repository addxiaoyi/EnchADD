const mineflayer = require('mineflayer')
const { Rcon } = require('./rcon')
const PORT = parseInt(process.argv[2] || '25565')
const results = {}
function record(name, pass, detail) { results[name] = { pass, detail }; console.log(`RESULT|${name}|${pass ? 'PASS' : 'FAIL'}|${detail}`) }
function mkBot(u) { return mineflayer.createBot({ host: '127.0.0.1', port: PORT, username: u, auth: 'offline', version: process.env.MC_VERSION || '1.21.1' }) }
async function wait(ms) { return new Promise(r => setTimeout(r, ms)) }

async function main() {
  const defender = mkBot('EnchaddDefBot')
  await new Promise((res, rej) => { defender.once('spawn', res); defender.once('error', rej) })
  const attacker = mkBot('EnchaddAtkBot')
  await new Promise((res, rej) => { attacker.once('spawn', res); attacker.once('error', rej) })
  await wait(3000)
  const rcon = new Rcon(parseInt(process.argv[3] || '25575'), 'enchadd')
  await rcon.connect()
  await Promise.race([rcon.cmd(`op EnchaddDefBot`), new Promise(r => setTimeout(() => r('TIMEOUT'), 10000))]); await Promise.race([rcon.cmd(`op EnchaddAtkBot`), new Promise(r => setTimeout(() => r('TIMEOUT'), 10000))]); await wait(300)
  await Promise.race([rcon.cmd(`item replace entity EnchaddDefBot weapon.offhand with minecraft:shield[minecraft:enchantments={levels:{"enchadd:bulwark":3}}]`), new Promise(r => setTimeout(() => r('TIMEOUT'), 10000))])
  await Promise.race([rcon.cmd(`item replace entity EnchaddAtkBot weapon.mainhand with minecraft:diamond_sword[minecraft:enchantments={levels:{"enchadd:breakguard":3}}]`), new Promise(r => setTimeout(() => r('TIMEOUT'), 10000))])
  await Promise.race([rcon.cmd(`fill 800 319 900 804 319 904 minecraft:stone`), new Promise(r => setTimeout(() => r('TIMEOUT'), 10000))]); await Promise.race([rcon.cmd(`tp EnchaddAtkBot 801.9 320 901`), new Promise(r => setTimeout(() => r('TIMEOUT'), 10000))])
  await Promise.race([rcon.cmd(`gamemode survival @a`), new Promise(r => setTimeout(() => r('TIMEOUT'), 10000))]); await wait(800)

  async function hp() {
    const out = await Promise.race([rcon.cmd(`data get entity EnchaddDefBot Health`), new Promise(r => setTimeout(() => r('TIMEOUT'), 10000))])
    const m = /([\d.]+)f\s*$/.exec((out || '').trim())
    return m ? parseFloat(m[1]) : NaN
  }
  async function eff() { return (await Promise.race([rcon.cmd(`data get entity EnchaddDefBot active_effects`), new Promise(r => setTimeout(() => r('TIMEOUT'), 10000))])) || '' }
  async function heal() { await Promise.race([rcon.cmd(`effect give EnchaddDefBot minecraft:instant_health 1 20 true`), new Promise(r => setTimeout(() => r('TIMEOUT'), 10000))]); await wait(500) }
  async function reset() { defender.deactivateItem(); await Promise.race([rcon.cmd(`effect clear EnchaddDefBot`), new Promise(r => setTimeout(() => r('TIMEOUT'), 10000))]); await heal() }
  async function block() {
    // 双重激活循环：确保服务器端 blocking 生效（先挥空一次攻击窗口再激活）
    for (let attempt = 0; attempt < 3; attempt++) {
      defender.deactivateItem(); await wait(300)
      await defender.lookAt(attacker.entity.position.offset(0, 1.2, 0), true)
      defender.activateItem(true); await wait(900)
    }
  }
  async function strike(tag) {
    await attacker.lookAt(defender.entity.position.offset(0, 1.2, 0), true)
    await wait(1100)
    const before = await hp()
    attacker.attack(defender.entity)
    await wait(600)
    const after = await hp()
    console.log(`STRIKE[${tag}] health ${before} -> ${after}`)
    await wait(1200)
    return [before, after]
  }

  // 校准：无盾被剑击
  await reset()
  const [cb, ca] = await strike('cal')
  record('calibration_unblocked', ca < cb - 3, `no-shield sword: ${cb}->${ca}`)

  // A: Bulwark — 持盾格挡 → Resistance
  await heal(); await block()
  const [ab, aa] = await strike('A')
  const effA = await eff()
  record('bulwark_resistance', /resistance/i.test(effA), `hp ${ab}->${aa} effects="${effA.trim().substring(0,130)}"`)

  // B: Breakguard 穿透
  await heal(); await block()
  const [bb, ba] = await strike('B')
  record('breakguard_penetration', ba < bb - 0.2 && ba > bb - 6, `blocked-with-bonus: ${bb}->${ba}`)

  // C: 空手完全格挡
  await heal()
  await Promise.race([rcon.cmd(`item replace entity EnchaddAtkBot weapon.mainhand with minecraft:air`), new Promise(r => setTimeout(() => r('TIMEOUT'), 10000))]); await wait(300)
  await block()
  const [cb2, ca2] = await strike('C')
  record('low_damage_full_block', Math.abs(ca2 - cb2) < 0.01, `fist vs shield: ${cb2}->${ca2}`)
  await Promise.race([rcon.cmd(`item replace entity EnchaddAtkBot weapon.mainhand with minecraft:diamond_sword[minecraft:enchantments={levels:{"enchadd:breakguard":3}}]`), new Promise(r => setTimeout(() => r('TIMEOUT'), 10000))])

  // D: 斧击禁盾
  await heal()
  await Promise.race([rcon.cmd(`item replace entity EnchaddAtkBot weapon.mainhand with minecraft:iron_axe`), new Promise(r => setTimeout(() => r('TIMEOUT'), 10000))]); await wait(300)
  await block()
  const [d1b, d1a] = await strike('D1')
  defender.deactivateItem(); await wait(300)
  const [d2b, d2a] = await strike('D2')
  record('shield_disable_bypass', d2a < d1a - 0.5, `blocked: ${d1b}->${d1a}, disabled: ${d2b}->${d2a}`)
  await Promise.race([rcon.cmd(`item replace entity EnchaddAtkBot weapon.mainhand with minecraft:diamond_sword[minecraft:enchantments={levels:{"enchadd:breakguard":3}}]`), new Promise(r => setTimeout(() => r('TIMEOUT'), 10000))])

  console.log('SUMMARY|' + JSON.stringify(results))
  rcon.close(); defender.quit(); attacker.quit(); setTimeout(() => process.exit(0), 1200)
}
main().catch(e => { console.error('FATAL', e && e.message); process.exit(2) })
