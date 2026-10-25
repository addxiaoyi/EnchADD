const { Rcon } = require('./rcon');
(async () => {
  const rcon = new Rcon(parseInt(process.argv[2]||'25575'), 'enchadd'); await rcon.connect();
  console.log('DEF:', await rcon.cmd('data get entity EnchaddDefBot Pos'));
  console.log('ATK:', await rcon.cmd('data get entity EnchaddAtkBot Pos'));
  console.log('DEF-ON-GROUND:', await rcon.cmd('data get entity EnchaddDefBot OnGround'));
  console.log('DEF-HEALTH:', await rcon.cmd('data get entity EnchaddDefBot Health'));
  rcon.close(); setTimeout(() => process.exit(0), 800);
})().catch(e => { console.error('FATAL', e.message); process.exit(2) })
