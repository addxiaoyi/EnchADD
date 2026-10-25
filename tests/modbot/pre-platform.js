const { Rcon } = require('./rcon');
(async () => {
  const rcon = new Rcon(parseInt(process.argv[2]||'25575'), 'enchadd'); await rcon.connect();
  await rcon.cmd('fill 800 319 900 804 319 904 minecraft:stone');
  await rcon.cmd('fill 800 320 900 804 330 904 minecraft:air');
  await rcon.cmd('fill 799 320 899 805 321 905 minecraft:air');
  console.log('PLATFORM READY (no barrier)');
  rcon.close(); setTimeout(() => process.exit(0), 800);
})().catch(e => { console.error('FATAL', e.message); process.exit(2) })
