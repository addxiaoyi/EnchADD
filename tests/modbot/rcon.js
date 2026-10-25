const net = require('net')

function rconPacket(id, type, body) {
  const bodyBuf = Buffer.from(body, 'utf8')
  const len = 4 + 4 + bodyBuf.length + 2
  const buf = Buffer.alloc(4 + len)
  buf.writeInt32LE(len, 0)
  buf.writeInt32LE(id, 4)
  buf.writeInt32LE(type, 8)
  bodyBuf.copy(buf, 12)
  buf.writeInt16LE(0, 12 + bodyBuf.length)
  return buf
}

class Rcon {
  constructor(port = 25575, password = 'enchadd') { this.port = port; this.password = password; this.id = 1 }
  connect() {
    return new Promise((resolve, reject) => {
      this.sock = net.connect(this.port, '127.0.0.1', () => {
        this.sock.write(rconPacket(1, 3, this.password))
      })
      let phase = 0
      let acc = Buffer.alloc(0)
      this.sock.on('data', d => {
        acc = Buffer.concat([acc, d])
        while (acc.length >= 4) {
          const len = acc.readInt32LE(0)
          if (acc.length < 4 + len) break
          const pkt = acc.subarray(4, 4 + len)
          const id = pkt.readInt32LE(0)
          const body = pkt.subarray(8, pkt.length - 2).toString('utf8')
          acc = acc.subarray(4 + len)
          if (phase === 0) {
            if (id === -1) { reject(new Error('rcon auth failed')); return }
            phase = 1; resolve(this)
          } else {
            if (this._wait) { const w = this._wait; this._wait = null; w(body) }
          }
        }
      })
      this.sock.on('error', e => { if (phase === 0) reject(e) })
    })
  }
  cmd(c) {
    return new Promise(resolve => {
      this._wait = resolve
      this.sock.write(rconPacket(this.id++, 2, c))
    })
  }
  close() { try { this.sock.end() } catch (e) {} }
}
module.exports = { Rcon }
