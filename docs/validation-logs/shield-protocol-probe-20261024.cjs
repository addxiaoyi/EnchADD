const mc = require('minecraft-protocol');
const {Rcon} = require('rcon-client');
const fs = require('node:fs');
const delay = ms => new Promise(resolve => setTimeout(resolve, ms));
async function main() {
  const rcon = await Rcon.connect({host:'127.0.0.1',port:25642,password:'enchadd-local-shield-test'});
  const commands = [];
  async function cmd(command) { const reply=await rcon.send(command); commands.push({command,reply}); return reply; }
  const bot=mc.createClient({host:'127.0.0.1',port:25641,username:'ShieldProbe',version:'1.21.1',auth:'offline'});
  bot.on('error', e=>console.error(e));
  bot.on('packet', (packet, meta)=>{
    if(meta.name==='ping') bot.write('pong',{id:packet.id});
  });
  bot.on('position', p=>{bot.write('teleport_confirm',{teleportId:p.teleportId});bot.write('position_look',{x:p.x,y:p.y,z:p.z,yaw:p.yaw,pitch:p.pitch,onGround:true});});
  await new Promise((resolve,reject)=>{bot.once('login',resolve);setTimeout(()=>reject(new Error('login timeout')),20000);});
  await delay(1500);
  await cmd('gamerule naturalRegeneration false');
  await cmd('gamerule doMobSpawning false');
  await cmd('time set night');
  await cmd('fill -3 99 -3 3 99 5 minecraft:stone');
  await cmd('tp ShieldProbe 0 100 0 0 0');
  await cmd('gamemode survival ShieldProbe');
  await cmd('summon minecraft:zombie 0 100 2 {Tags:["shield_attacker"],NoAI:1b,Silent:1b,PersistenceRequired:1b}');
  let sequence=0;
  function release() {bot.write('block_dig',{status:5,location:{x:0,y:0,z:0},face:0,sequence:sequence++});}
  async function block(level=0) {
    release();
    await cmd(`item replace entity ShieldProbe weapon.offhand with minecraft:shield${level?'[minecraft:enchantments={levels:{"enchadd:bulwark":'+level+'}}]':''}`);
    bot.write('use_item',{hand:1,sequence:sequence++,rotation:{x:0,y:0}});
    await delay(500);
  }
  async function health() {return await cmd('data get entity ShieldProbe Health');}
  async function effects() {return await cmd('data get entity ShieldProbe active_effects');}
  const rows=[];
  for(let level=0;level<=3;level++) {
    await cmd('effect clear ShieldProbe');
    await cmd('effect give ShieldProbe minecraft:instant_health 1 10');
    await cmd(`item replace entity @e[tag=shield_attacker,limit=1] weapon.mainhand with minecraft:diamond_sword${level?'[minecraft:enchantments={levels:{"enchadd:breakguard":'+level+'}}]':''}`);
    await delay(700);
    await block();
    const before=await health();
    const damage=await cmd('damage ShieldProbe 10 minecraft:mob_attack by @e[tag=shield_attacker,limit=1]');
    const after=await health();
    rows.push({test:'breakguard',level,before,after,damage});
  }
  await cmd('item replace entity @e[tag=shield_attacker,limit=1] weapon.mainhand with minecraft:diamond_sword');
  await cmd('effect clear ShieldProbe');
  await delay(4500);
  await block(3);
  const zero=await cmd('damage ShieldProbe 0 minecraft:mob_attack by @e[tag=shield_attacker,limit=1]');
  rows.push({test:'bulwark-zero',reply:zero,effects:await effects()});
  await cmd('damage ShieldProbe 10 minecraft:mob_attack by @e[tag=shield_attacker,limit=1]');
  rows.push({test:'bulwark-positive',effects:await effects()});
  await cmd('effect clear ShieldProbe');
  await delay(1100);
  await cmd('damage ShieldProbe 10 minecraft:mob_attack by @e[tag=shield_attacker,limit=1]');
  rows.push({test:'bulwark-cooldown',effects:await effects()});
  await delay(3300);
  await cmd('damage ShieldProbe 10 minecraft:mob_attack by @e[tag=shield_attacker,limit=1]');
  rows.push({test:'bulwark-after-cooldown',effects:await effects()});
  fs.writeFileSync('shield-results.json',JSON.stringify({rows,commands},null,2));
  console.log(JSON.stringify(rows,null,2));
  bot.end(); await rcon.end();
}
main().then(()=>process.exit(0)).catch(e=>{console.error(e);process.exit(1);});
