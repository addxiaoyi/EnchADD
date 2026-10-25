const { Rcon } = require('./rcon');
(async () => {
  const rcon = new Rcon(25577, 'enchadd'); await rcon.connect();
  await rcon.cmd('op EnchaddDefBot'); await new Promise(r=>setTimeout(r,300));
  console.log('HP-RAW:', JSON.stringify(await rcon.cmd('data get entity EnchaddDefBot Health')));
  console.log('SEL-RAW:', JSON.stringify(await rcon.cmd('list')));
  rcon.close(); setTimeout(() => process.exit(0), 800);
})().catch(e => { console.error('FATAL', e.message); process.exit(2) })
