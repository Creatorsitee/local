package com.example.service

object DefaultSite {

  val HTML: String = """<!DOCTYPE html>
<html lang="id">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="color-scheme" content="dark">
<title>LocalHost Server</title>
<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}
body {
  background-color: #000000;
  color: #ffffff;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  line-height: 1.5;
  -webkit-font-smoothing: antialiased;
}
header {
  border-bottom: 1px solid #222222;
  background-color: #050505;
  position: sticky;
  top: 0;
  z-index: 100;
  padding: 16px 24px;
}
.header-inner {
  max-width: 1100px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}
.brand-title {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: -0.5px;
  color: #ffffff;
  text-transform: uppercase;
}
.badge-protocol {
  font-size: 11px;
  font-family: monospace;
  font-weight: 600;
  padding: 2px 8px;
  border: 1px solid #333333;
  border-radius: 4px;
  background: #111111;
  color: #cccccc;
}
.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  border: 1px solid #333333;
  border-radius: 999px;
  background: #0a0a0a;
  font-size: 12px;
  font-weight: 600;
  font-family: monospace;
}
.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ffffff;
  display: inline-block;
}
.status-dot.offline {
  background: #666666;
}
nav {
  max-width: 1100px;
  margin: 0 auto;
  display: flex;
  gap: 8px;
  padding: 16px 24px 0 24px;
  border-bottom: 1px solid #1a1a1a;
  overflow-x: auto;
}
.tab-btn {
  background: transparent;
  border: none;
  border-bottom: 2px solid transparent;
  color: #888888;
  padding: 10px 16px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: color 0.15s, border-color 0.15s;
}
.tab-btn:hover {
  color: #ffffff;
}
.tab-btn.active {
  color: #ffffff;
  border-bottom-color: #ffffff;
}
main {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px;
}
.tab-panel {
  display: none;
}
.tab-panel.active {
  display: block;
}
.grid-metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}
.card {
  background: #0a0a0a;
  border: 1px solid #222222;
  border-radius: 8px;
  padding: 20px;
  transition: border-color 0.2s;
}
.card:hover {
  border-color: #383838;
}
.card-label {
  font-size: 12px;
  color: #888888;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 600;
  margin-bottom: 8px;
}
.card-value {
  font-size: 24px;
  font-weight: 700;
  color: #ffffff;
  font-family: monospace;
}
.card-subtext {
  font-size: 12px;
  color: #666666;
  margin-top: 6px;
}
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.btn {
  background: #ffffff;
  color: #000000;
  border: 1px solid #ffffff;
  border-radius: 4px;
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  text-decoration: none;
  transition: background 0.15s, color 0.15s;
}
.btn:hover {
  background: #d8d8d8;
  border-color: #d8d8d8;
}
.btn-secondary {
  background: #111111;
  color: #ffffff;
  border: 1px solid #333333;
}
.btn-secondary:hover {
  background: #222222;
  border-color: #555555;
}
.btn-danger {
  background: transparent;
  color: #cccccc;
  border: 1px solid #444444;
  border-radius: 4px;
  font-size: 12px;
  padding: 5px 10px;
  cursor: pointer;
}
.btn-danger:hover {
  background: #222222;
  color: #ffffff;
  border-color: #777777;
}
.input-text, .input-textarea {
  width: 100%;
  background: #050505;
  border: 1px solid #2a2a2a;
  border-radius: 4px;
  color: #ffffff;
  padding: 10px 14px;
  font-size: 14px;
  font-family: inherit;
  outline: none;
  margin-bottom: 12px;
}
.input-text:focus, .input-textarea:focus {
  border-color: #ffffff;
}
.input-textarea {
  resize: vertical;
  min-height: 80px;
}
.form-group {
  margin-bottom: 14px;
}
.form-label {
  display: block;
  font-size: 12px;
  color: #888888;
  margin-bottom: 6px;
  font-weight: 600;
}
.terminal-box {
  background: #050505;
  border: 1px solid #222222;
  border-radius: 6px;
  padding: 14px;
  font-family: monospace;
  font-size: 12px;
  color: #e0e0e0;
  height: 260px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-all;
}
.terminal-line {
  margin-bottom: 4px;
  line-height: 1.4;
}
.terminal-time {
  color: #666666;
  margin-right: 8px;
}
.db-item {
  background: #080808;
  border: 1px solid #1f1f1f;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}
.db-item-content {
  flex: 1;
  min-width: 0;
}
.db-item-title {
  font-weight: 600;
  color: #ffffff;
  font-size: 14px;
  margin-bottom: 4px;
}
.db-item-meta {
  font-size: 11px;
  font-family: monospace;
  color: #666666;
  margin-bottom: 6px;
}
.db-item-body {
  font-size: 13px;
  color: #cccccc;
  word-break: break-word;
}
.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #1a1a1a;
}
.file-item:last-child {
  border-bottom: none;
}
.file-name {
  color: #ffffff;
  font-size: 14px;
  font-family: monospace;
  text-decoration: none;
}
.file-name:hover {
  text-decoration: underline;
}
.badge-connected {
  color: #ffffff;
  border: 1px solid #ffffff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  font-family: monospace;
}
.badge-disconnected {
  color: #777777;
  border: 1px solid #444444;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  font-family: monospace;
}
.empty-state {
  padding: 32px 16px;
  text-align: center;
  color: #666666;
  font-size: 13px;
  border: 1px dashed #222222;
  border-radius: 6px;
}
footer {
  border-top: 1px solid #1a1a1a;
  padding: 24px;
  text-align: center;
  font-size: 12px;
  color: #555555;
  margin-top: 40px;
}
</style>
</head>
<body>

<header>
  <div class="header-inner">
    <div class="brand">
      <div class="brand-title">LocalHost</div>
      <div class="badge-protocol">HTTP &amp; WS</div>
    </div>
    <div id="server-status-pill" class="status-pill">
      <span class="status-dot"></span> MENYAMBUNG
    </div>
  </div>
</header>

<nav>
  <button class="tab-btn active" data-tab="overview" onclick="switchTab('overview')">Ringkasan</button>
  <button class="tab-btn" data-tab="database" onclick="switchTab('database')">Database Real-Time</button>
  <button class="tab-btn" data-tab="websocket" onclick="switchTab('websocket')">WebSocket</button>
  <button class="tab-btn" data-tab="files" onclick="switchTab('files')">Berkas Server</button>
  <button class="tab-btn" data-tab="api" onclick="switchTab('api')">REST API</button>
</nav>

<main>
  <div id="tab-overview" class="tab-panel active">
    <div class="grid-metrics">
      <div class="card">
        <div class="card-label">Waktu Operasional</div>
        <div id="metric-uptime" class="card-value">00:00:00</div>
        <div class="card-subtext">Penghitung aktif real-time</div>
      </div>
      <div class="card">
        <div class="card-label">Klien WebSocket</div>
        <div id="metric-clients" class="card-value">0</div>
        <div class="card-subtext">Koneksi tersambung saat ini</div>
      </div>
      <div class="card">
        <div class="card-label">Latensi HTTP</div>
        <div id="metric-latency" class="card-value">- ms</div>
        <div class="card-subtext">Waktu respon bolak-balik</div>
      </div>
      <div class="card">
        <div class="card-label">Waktu Server</div>
        <div id="metric-time" class="card-value">-</div>
        <div class="card-subtext">Sinkronisasi jam sistem</div>
      </div>
    </div>

    <div class="card" style="margin-bottom: 24px;">
      <div class="section-title">
        <span>Informasi Server</span>
        <button class="btn btn-secondary" onclick="fetchStatus()">Muat Ulang</button>
      </div>
      <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(200px,1fr));gap:16px;font-size:13px;">
        <div>
          <span style="color:#666666;display:block;margin-bottom:4px;">Port Portabel</span>
          <span id="server-port-text" style="font-family:monospace;font-weight:600;">8080</span>
        </div>
        <div>
          <span style="color:#666666;display:block;margin-bottom:4px;">Protokol Jaringan</span>
          <span style="font-family:monospace;font-weight:600;">HTTP/1.1 &amp; RFC 6455</span>
        </div>
        <div>
          <span style="color:#666666;display:block;margin-bottom:4px;">Mesin Database</span>
          <span style="font-family:monospace;font-weight:600;">JSON File Store (Real-Time)</span>
        </div>
        <div>
          <span style="color:#666666;display:block;margin-bottom:4px;">Endpoint Database</span>
          <span style="font-family:monospace;font-weight:600;">/api/db/{koleksi}</span>
        </div>
      </div>
    </div>
  </div>

  <div id="tab-database" class="tab-panel">
    <div class="card" style="margin-bottom: 24px;">
      <div class="section-title">
        <span>Tambah Data ke Database</span>
      </div>
      <div class="form-group">
        <label class="form-label">Nama Koleksi</label>
        <div style="display:flex;gap:10px;">
          <input type="text" id="db-col-input" class="input-text" value="notes" style="max-width:240px;margin-bottom:0;" placeholder="notes">
          <button class="btn btn-secondary" onclick="loadDb()">Buka Koleksi</button>
        </div>
      </div>
      <div class="form-group">
        <label class="form-label">Judul / Kunci</label>
        <input type="text" id="db-title-input" class="input-text" placeholder="Masukkan judul data">
      </div>
      <div class="form-group">
        <label class="form-label">Isi / Nilai Konten</label>
        <textarea id="db-body-input" class="input-textarea" placeholder="Tuliskan isi catatan atau data JSON"></textarea>
      </div>
      <button class="btn" onclick="saveDbItem()">Simpan Data</button>
    </div>

    <div class="card">
      <div class="section-title">
        <span>Daftar Data Koleksi</span>
        <span id="db-count-badge" class="badge-protocol">0 data</span>
      </div>
      <div id="db-items-list">
        <div class="empty-state">Memuat data...</div>
      </div>
    </div>
  </div>

  <div id="tab-websocket" class="tab-panel">
    <div class="card" style="margin-bottom: 24px;">
      <div class="section-title">
        <span>Koneksi WebSocket</span>
        <span id="ws-connection-badge" class="badge-disconnected">MENYAMBUNG</span>
      </div>
      <div style="display:flex;gap:10px;margin-bottom:16px;">
        <input type="text" id="ws-message-input" class="input-text" style="margin-bottom:0;" placeholder="Tulis pesan untuk dikirim via WebSocket">
        <button class="btn" onclick="sendWsMessage()">Kirim</button>
        <button class="btn btn-secondary" onclick="pingWs()">Uji Ping</button>
      </div>
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;">
        <span class="form-label" style="margin-bottom:0;">Log Aktivitas &amp; Siaran Data</span>
        <button class="btn-secondary" style="font-size:11px;padding:3px 8px;border-radius:3px;cursor:pointer;" onclick="clearWsTerminal()">Bersihkan</button>
      </div>
      <div id="ws-terminal" class="terminal-box"></div>
    </div>
  </div>

  <div id="tab-files" class="tab-panel">
    <div class="card">
      <div class="section-title">
        <span>Berkas di Direktori Web</span>
        <span id="files-count-badge" class="badge-protocol">0 berkas</span>
      </div>
      <div id="files-list-container">
        <div class="empty-state">Memuat berkas...</div>
      </div>
    </div>
  </div>

  <div id="tab-api" class="tab-panel">
    <div class="card" style="margin-bottom: 24px;">
      <div class="section-title">
        <span>Pengujian REST API Langsung</span>
      </div>
      <div style="display:flex;flex-wrap:wrap;gap:8px;margin-bottom:16px;">
        <button class="btn btn-secondary" onclick="testApi('/api/status', 'GET')">GET /api/status</button>
        <button class="btn btn-secondary" onclick="testApi('/api/time', 'GET')">GET /api/time</button>
        <button class="btn btn-secondary" onclick="testApi('/api/files', 'GET')">GET /api/files</button>
        <button class="btn btn-secondary" onclick="testApi('/api/db/' + activeCollection, 'GET')">GET /api/db</button>
        <button class="btn btn-secondary" onclick="testApi('/api/echo', 'POST', { pesan: 'Halo dari klien', waktu: Date.now() })">POST /api/echo</button>
      </div>
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;">
        <span class="form-label" style="margin-bottom:0;">Respon Server</span>
        <div style="display:flex;gap:12px;font-size:12px;font-family:monospace;">
          <span style="color:#666666;">Status: <b id="api-status-code" style="color:#ffffff;">-</b></span>
          <span style="color:#666666;">Durasi: <b id="api-duration" style="color:#ffffff;">-</b></span>
        </div>
      </div>
      <div id="api-response-output" class="terminal-box" style="height:200px;">Pilih salah satu endpoint di atas untuk menguji respon server secara langsung.</div>
    </div>
  </div>
</main>

<footer>
  LocalHost Web Server &bull; Desain Minimalis &bull; Real-Time
</footer>

<script>
let currentTab = 'overview';
let activeCollection = 'notes';
let ws = null;
let currentUptime = 0;

function switchTab(tabId) {
  currentTab = tabId;
  document.querySelectorAll('.tab-btn').forEach(b => {
    b.classList.toggle('active', b.getAttribute('data-tab') === tabId);
  });
  document.querySelectorAll('.tab-panel').forEach(p => {
    p.classList.toggle('active', p.id === 'tab-' + tabId);
  });
  if (tabId === 'database') loadDb();
  if (tabId === 'files') loadFiles();
}

function updateUptimeDisplay() {
  const h = Math.floor(currentUptime / 3600);
  const m = Math.floor((currentUptime % 3600) / 60);
  const s = currentUptime % 60;
  const pad = n => n.toString().padStart(2, '0');
  const el = document.getElementById('metric-uptime');
  if (el) el.textContent = pad(h) + ':' + pad(m) + ':' + pad(s);
}

async function fetchStatus() {
  const t0 = performance.now();
  try {
    const res = await fetch('/api/status');
    const t1 = performance.now();
    const latency = Math.round(t1 - t0);
    const data = await res.json();
    currentUptime = data.uptimeSeconds || 0;
    updateUptimeDisplay();

    const latencyEl = document.getElementById('metric-latency');
    if (latencyEl) latencyEl.textContent = latency + ' ms';

    const clientsEl = document.getElementById('metric-clients');
    if (clientsEl) clientsEl.textContent = data.webSocketClients !== undefined ? data.webSocketClients : '0';

    const portEl = document.getElementById('server-port-text');
    if (portEl) portEl.textContent = data.port || '8080';

    const pill = document.getElementById('server-status-pill');
    if (pill) {
      pill.innerHTML = '<span class="status-dot"></span> ONLINE (' + (data.port || 8080) + ')';
    }
  } catch (err) {
    const pill = document.getElementById('server-status-pill');
    if (pill) {
      pill.innerHTML = '<span class="status-dot offline"></span> OFFLINE';
    }
  }
}

async function fetchTime() {
  try {
    const res = await fetch('/api/time');
    const data = await res.json();
    const timeEl = document.getElementById('metric-time');
    if (timeEl && data.utcIso) {
      const d = new Date(data.utcIso);
      timeEl.textContent = d.toLocaleTimeString();
    }
  } catch (err) {}
}

function connectWebSocket() {
  const proto = location.protocol === 'https:' ? 'wss:' : 'ws:';
  const url = proto + '//' + location.host + '/ws';
  try {
    ws = new WebSocket(url);
    ws.onopen = () => {
      logWs('Koneksi tersambung ke ' + url);
      const wsStatus = document.getElementById('ws-connection-badge');
      if (wsStatus) {
        wsStatus.textContent = 'TERHUBUNG';
        wsStatus.className = 'badge-connected';
      }
    };
    ws.onmessage = (e) => {
      logWs('Pesan: ' + e.data);
      try {
        const payload = JSON.parse(e.data);
        if (payload.event === 'database_change') {
          if (currentTab === 'database') {
            loadDb();
          }
        }
      } catch (_) {}
    };
    ws.onclose = () => {
      logWs('Koneksi terputus. Menyambung ulang...');
      const wsStatus = document.getElementById('ws-connection-badge');
      if (wsStatus) {
        wsStatus.textContent = 'TERPUTUS';
        wsStatus.className = 'badge-disconnected';
      }
      setTimeout(connectWebSocket, 3000);
    };
    ws.onerror = () => {
      logWs('Galat koneksi WebSocket');
    };
  } catch (err) {
    setTimeout(connectWebSocket, 3000);
  }
}

function sendWsMessage() {
  const input = document.getElementById('ws-message-input');
  if (!input) return;
  const msg = input.value.trim();
  if (!msg) return;
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    alert('WebSocket belum terhubung.');
    return;
  }
  ws.send(msg);
  logWs('Terkirim: ' + msg);
  input.value = '';
}

function pingWs() {
  if (!ws || ws.readyState !== WebSocket.OPEN) {
    alert('WebSocket belum terhubung.');
    return;
  }
  const sendTime = Date.now();
  ws.send(JSON.stringify({ type: 'ping', timestamp: sendTime }));
  logWs('Uji ping terkirim: ' + sendTime);
}

function logWs(text) {
  const term = document.getElementById('ws-terminal');
  if (!term) return;
  const now = new Date().toLocaleTimeString();
  const div = document.createElement('div');
  div.className = 'terminal-line';
  div.innerHTML = '<span class="terminal-time">[' + now + ']</span> ' + escapeHtml(text);
  term.appendChild(div);
  term.scrollTop = term.scrollHeight;
}

function clearWsTerminal() {
  const term = document.getElementById('ws-terminal');
  if (term) term.innerHTML = '';
}

async function loadDb() {
  const colInput = document.getElementById('db-col-input');
  if (colInput && colInput.value.trim()) {
    activeCollection = colInput.value.trim();
  }
  const listEl = document.getElementById('db-items-list');
  const countEl = document.getElementById('db-count-badge');
  if (!listEl) return;
  try {
    const res = await fetch('/api/db/' + encodeURIComponent(activeCollection));
    const items = await res.json();
    if (countEl) countEl.textContent = (Array.isArray(items) ? items.length : 0) + ' data';
    if (!Array.isArray(items) || items.length === 0) {
      listEl.innerHTML = '<div class="empty-state">Belum ada data dalam koleksi "' + escapeHtml(activeCollection) + '". Tambahkan data baru di atas.</div>';
      return;
    }
    listEl.innerHTML = '';
    items.forEach(it => {
      const card = document.createElement('div');
      card.className = 'db-item';
      const title = it.title || it.name || it.id;
      const content = it.content || it.body || it.description || JSON.stringify(it);
      const created = it.created_at ? new Date(it.created_at).toLocaleString() : '-';
      card.innerHTML = 
        '<div class="db-item-content">' +
          '<div class="db-item-title">' + escapeHtml(title) + '</div>' +
          '<div class="db-item-meta">ID: ' + escapeHtml(it.id || '-') + ' | ' + escapeHtml(created) + '</div>' +
          '<div class="db-item-body">' + escapeHtml(content) + '</div>' +
        '</div>' +
        '<button class="btn-danger" onclick="deleteDbItem(\'' + it.id + '\')">Hapus</button>';
      listEl.appendChild(card);
    });
  } catch (err) {
    listEl.innerHTML = '<div class="empty-state">Gagal memuat data koleksi.</div>';
  }
}

async function saveDbItem() {
  const titleInput = document.getElementById('db-title-input');
  const bodyInput = document.getElementById('db-body-input');
  const title = titleInput ? titleInput.value.trim() : '';
  const body = bodyInput ? bodyInput.value.trim() : '';
  if (!title && !body) {
    alert('Masukkan judul atau isi data.');
    return;
  }
  const payload = {
    title: title,
    content: body,
    created_at: Date.now()
  };
  try {
    const res = await fetch('/api/db/' + encodeURIComponent(activeCollection), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (res.ok) {
      if (titleInput) titleInput.value = '';
      if (bodyInput) bodyInput.value = '';
      loadDb();
    } else {
      alert('Gagal menyimpan data.');
    }
  } catch (err) {
    alert('Galat jaringan saat menyimpan data.');
  }
}

async function deleteDbItem(id) {
  if (!confirm('Hapus data ini?')) return;
  try {
    const res = await fetch('/api/db/' + encodeURIComponent(activeCollection) + '?id=' + encodeURIComponent(id), {
      method: 'DELETE'
    });
    if (res.ok) {
      loadDb();
    } else {
      alert('Gagal menghapus data.');
    }
  } catch (err) {
    alert('Galat jaringan saat menghapus data.');
  }
}

async function loadFiles() {
  const container = document.getElementById('files-list-container');
  const countEl = document.getElementById('files-count-badge');
  if (!container) return;
  try {
    const res = await fetch('/api/files');
    const data = await res.json();
    const files = data.files || [];
    if (countEl) countEl.textContent = files.length + ' berkas';
    if (files.length === 0) {
      container.innerHTML = '<div class="empty-state">Tidak ada berkas di direktori web.</div>';
      return;
    }
    container.innerHTML = '';
    files.forEach(f => {
      const item = document.createElement('div');
      item.className = 'file-item';
      item.innerHTML = 
        '<div style="display:flex;align-items:center;gap:12px;">' +
          '<a class="file-name" href="/' + encodeURIComponent(f) + '" target="_blank">' + escapeHtml(f) + '</a>' +
        '</div>' +
        '<a class="btn btn-secondary" style="font-size:11px;padding:4px 10px;" href="/' + encodeURIComponent(f) + '" target="_blank">Buka</a>';
      container.appendChild(item);
    });
  } catch (err) {
    container.innerHTML = '<div class="empty-state">Gagal memuat berkas.</div>';
  }
}

async function testApi(endpoint, method, body) {
  const out = document.getElementById('api-response-output');
  const statusEl = document.getElementById('api-status-code');
  const timeEl = document.getElementById('api-duration');
  if (!out) return;
  out.textContent = 'Memuat permintaan...';
  const t0 = performance.now();
  try {
    const options = { method: method || 'GET' };
    if (body) {
      options.headers = { 'Content-Type': 'application/json' };
      options.body = JSON.stringify(body);
    }
    const res = await fetch(endpoint, options);
    const t1 = performance.now();
    const dur = Math.round(t1 - t0);
    const text = await res.text();
    if (statusEl) statusEl.textContent = res.status + ' ' + res.statusText;
    if (timeEl) timeEl.textContent = dur + ' ms';
    try {
      const json = JSON.parse(text);
      out.textContent = JSON.stringify(json, null, 2);
    } catch (_) {
      out.textContent = text;
    }
  } catch (err) {
    if (statusEl) statusEl.textContent = 'GALAT';
    if (timeEl) timeEl.textContent = '-';
    out.textContent = 'Gagal memproses permintaan: ' + err.message;
  }
}

function escapeHtml(str) {
  if (typeof str !== 'string') return String(str);
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

window.addEventListener('DOMContentLoaded', () => {
  fetchStatus();
  fetchTime();
  loadFiles();
  connectWebSocket();
  setInterval(() => {
    currentUptime++;
    updateUptimeDisplay();
  }, 1000);
  setInterval(fetchStatus, 2000);
  setInterval(fetchTime, 10000);
});
</script>
</body>
</html>"""
}
