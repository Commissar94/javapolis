'use strict';

// Ready-made browser shell. Resource counts and production always come from Java.
const $ = id => document.getElementById(id);
const names = { MINE: 'Шахта', SMELTER: 'Плавильня', PRESS: 'Пресс', WAREHOUSE: 'Склад' };
const statuses = { READY: 'Готов к работе', WORKING: 'Производство идёт', NO_INPUT: 'Ожидает сырьё',
  OUTPUT_FULL: 'Выход заполнен', PAUSED: 'Остановлен', NO_RECIPE: 'Рецепт ещё не добавлен', TODO: 'Ждёт твоего Java-кода' };
const resources = { ORE: ['◆', 'руда', 'ore'], INGOT: ['▰', 'слитки', 'ingot'], PLATE: ['▱', 'пластины', 'plate'] };
let state, selected = 2, buildType = null, connectFrom = null, busy = false;
let requestSequence = 0, appliedSequence = 0, animationTick = -1, toastTimeout;
let renderedState = '', renderedInspector = '', pointerDown = false;
const position = m => ({ x: 104 + m.x * 72, y: 107 + m.y * 64 });
const total = values => Object.values(values).reduce((sum, value) => sum + value, 0);

function toast(message) {
  $('toast').textContent = message;
  $('toast').hidden = false;
  clearTimeout(toastTimeout);
  toastTimeout = setTimeout(() => { $('toast').hidden = true; }, 4500);
}

async function request(command) {
  const sequence = ++requestSequence;
  const response = await fetch('/api/factory', command ? {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(command)
  } : { cache: 'no-store' });
  const result = await response.json();
  if (!response.ok) throw new Error(result.message || 'Не получилось выполнить команду');
  if (sequence >= appliedSequence) {
    appliedSequence = sequence;
    state = result;
    $('offline').hidden = true;
    $('connection').classList.remove('offline');
    const fingerprint = JSON.stringify(state);
    if (fingerprint !== renderedState && !pointerDown) {
      render();
      renderedState = fingerprint;
    }
  }
  return result;
}

async function command(action, values = {}) {
  if (busy) return;
  busy = true;
  try { return await request({ action, ...values }); }
  catch (error) { toast(error.message); }
  finally { busy = false; }
}

async function poll() {
  try { if (!busy) await request(); }
  catch (_) { $('offline').hidden = false; $('connection').classList.add('offline'); }
  finally { setTimeout(poll, 500); }
}

function render() {
  const focusedMachine = document.activeElement?.dataset.machine;
  const sums = { ORE: 0, INGOT: 0, PLATE: 0 };
  for (const m of state.machines) for (const resource of Object.keys(sums))
    sums[resource] += (m.input[resource] || 0) + (m.output[resource] || 0);
  $('ore-count').textContent = sums.ORE;
  $('ingot-count').textContent = sums.INGOT;
  $('plate-count').textContent = sums.PLATE;
  $('budget-count').textContent = state.budget;
  $('tick-count').textContent = String(state.tick).padStart(3, '0');
  $('play').textContent = state.running ? 'Ⅱ Пауза' : '▶ Запустить';
  $('speed').textContent = state.speed + '×';
  $('machine-count').textContent = state.machines.length;
  $('mode-label').textContent = state.mode === 'solution' ? 'Эталон' : 'Мой код';
  $('scenario-label').textContent = state.scenario === 'contract' ? 'Контракт' : 'Песочница';
  $('code-error').hidden = !state.error;
  $('code-error').textContent = state.error || '';
  $('events').replaceChildren(...state.events.map(text => {
    const item = document.createElement('li'); item.textContent = text; return item;
  }));
  const plates = state.machines.find(m => m.id === 3)?.input.PLATE || 0;
  const complete = state.contract.complete;
  $('contract-count').textContent = complete ? 'Заказ доставлен' : `${plates} / ${state.contract.target}`;
  $('contract-time').textContent = `Осталось ${Math.max(0, state.contract.deadline - state.tick)} тактов`;
  $('contract-bar').style.width = `${complete ? 100 : Math.min(100, plates / state.contract.target * 100)}%`;
  $('contract-status').textContent = complete ? 'Выполнен ✓' : state.tick > state.contract.deadline ? 'Срок истёк' : 'В работе';
  $('submit').disabled = complete;
  $('contract-note').textContent = state.scenario === 'sandbox'
    ? 'В песочнице можно тренироваться. Для зачёта запусти смену «Контракт».'
    : 'Стартовые здания + 300 кредитов. Поставок извне нет. Время останавливается на такте 100.';
  if (!state.machines.some(m => m.id === selected)) selected = 2;
  renderMap();
  renderInspector();
  renderTool();
  if (focusedMachine) document.querySelector(`[data-machine="${focusedMachine}"]`)?.focus({ preventScroll: true });
}

function renderMap() {
  $('belts').innerHTML = state.links.map(link => {
    const a = position(state.machines.find(m => m.id === link.from));
    const b = position(state.machines.find(m => m.id === link.to));
    const mid = (a.x + b.x) / 2;
    const path = `M${a.x} ${a.y + 10}H${mid}V${b.y + 10}H${b.x}`;
    return `<path class="belt-outline" d="${path}"/><path class="belt" d="${path}"/><path class="belt-stripes" d="${path}"/><path class="belt-arrow" d="M${mid-3} ${b.y+6}l5 4-5 4Z"/>`;
  }).join('');
  $('machines').innerHTML = state.machines.map(m => {
    const p = position(m), active = state.running && m.status === 'WORKING';
    const color = m.status === 'WORKING' ? '#9bc3ad' : m.status === 'TODO' ? '#8ca1b1' : '#d7ac71';
    return `<g class="machine ${m.id === selected ? 'selected' : ''} ${active ? 'operating' : ''}" data-machine="${m.id}" role="button" tabindex="0" aria-label="${names[m.type]} №${m.id}: ${statuses[m.status]}" transform="translate(${p.x},${p.y})">
      <ellipse class="selection" cx="0" cy="13" rx="44" ry="26"/>
      <use href="#building-${m.type}" x="-35" y="-62" width="70" height="82" filter="url(#shadow)"/>
      <text class="number" x="28" y="-52">0${m.id}</text><circle cx="-26" cy="-49" r="3" fill="${color}"/>
      <text class="name" x="0" y="37">${names[m.type]}</text><text class="state" x="0" y="50">${m.type === 'WAREHOUSE' ? total(m.input)+' / '+m.inputCapacity : statuses[m.status]}</text></g>`;
  }).join('');
  if (animationTick !== state.tick) {
    animationTick = state.tick;
    $('particles').replaceChildren();
    if (!matchMedia('(prefers-reduced-motion: reduce)').matches) {
      for (const link of state.links.filter(l => l.moved > 0)) {
        const source = state.machines.find(m => m.id === link.from);
        const a = position(source), b = position(state.machines.find(m => m.id === link.to));
        const svg = 'http://www.w3.org/2000/svg';
        const item = document.createElementNS(svg, 'circle');
        item.setAttribute('r', '3.5');
        item.setAttribute('fill', source.type === 'MINE' ? '#afcbb4' : source.type === 'SMELTER' ? '#e4af88' : '#afd0e0');
        const movement = document.createElementNS(svg, 'animateMotion');
        movement.setAttribute('path', `M${a.x} ${a.y+10}H${(a.x+b.x)/2}V${b.y+10}H${b.x}`);
        movement.setAttribute('dur', '0.45s'); movement.setAttribute('repeatCount', '1');
        item.append(movement); $('particles').append(item);
        movement.beginElement(); setTimeout(() => item.remove(), 500);
      }
    }
  }
}

function stocks(values) {
  return Object.entries(resources).map(([key, [icon, name, css]]) =>
    `<div class="stock" title="${name}"><span class="${css}">${icon}</span><b>${values[key] || 0}</b></div>`).join('');
}

function renderInspector() {
  const m = state.machines.find(machine => machine.id === selected);
  if (!m) return;
  const r = m.recipe;
  const recipe = m.type === 'MINE' ? '<b class="ore">◆ +1 руда</b><span>каждый такт</span>'
    : r ? `<b>${r.inputAmount} ${resources[r.input][0]}</b><span>→</span><b>${r.outputAmount} ${resources[r.output][0]}</b><span>${r.duration} такта</span>`
    : m.type === 'WAREHOUSE' ? '<b>▥ Общий склад</b><span>200 мест</span>' : '<b>Нет рецепта</b><span>этап 4</span>';
  const links = state.links.filter(link => link.from === m.id || link.to === m.id);
  const fingerprint = JSON.stringify([m, links, state.scenario]);
  if (fingerprint === renderedInspector) return;
  renderedInspector = fingerprint;
  const focused = document.activeElement;
  const focusSelector = $('inspector').contains(focused)
    ? focused.id ? `#${focused.id}` : focused.dataset.action ? `[data-action="${focused.dataset.action}"]`
      : focused.dataset.disconnect ? `[data-disconnect="${focused.dataset.disconnect}"]` : null
    : null;
  $('inspector').innerHTML = `<div class="inspector-top"><div class="eyebrow">ЗДАНИЕ / ${String(m.id).padStart(2,'0')}</div><h2>${names[m.type]}</h2><span class="inspector-status">● ${m.type === 'WAREHOUSE' ? 'Принимает продукцию' : statuses[m.status]}</span><svg class="machine-preview" viewBox="0 0 88 100" aria-hidden="true"><use href="#building-${m.type}" width="88" height="100"/></svg></div>
    <div class="inspector-body"><p class="row-label">${m.type === 'WAREHOUSE' ? 'НАЗНАЧЕНИЕ' : 'ПРОИЗВОДСТВО'}</p><div class="recipe-box">${recipe}</div><p class="production-note">${m.type === 'WAREHOUSE' ? 'Один лимит на все виды ресурсов.' : `Выпущено: ${m.produced} · Прогресс: ${m.progress} / ${m.duration}`}</p>
    <div class="progress-track"><div style="width:${Math.min(100,m.progress/m.duration*100)}%"></div></div>
    <div class="inventory-title"><span>ВХОД / СКЛАД</span><span>${total(m.input)} / ${m.inputCapacity}</span></div><div class="inventory-values">${stocks(m.input)}</div>
    ${m.type !== 'WAREHOUSE' ? `<div class="inventory-title"><span>ВЫХОД</span><span>${total(m.output)} / ${m.outputCapacity}</span></div><div class="inventory-values">${stocks(m.output)}</div>` : ''}
    <div class="inspector-actions">${m.type !== 'WAREHOUSE' ? `<button class="button secondary" data-action="toggle">${m.enabled ? 'Ⅱ Остановить здание' : '▶ Включить здание'}</button><button class="button secondary" id="connect-machine">↗ Провести конвейер</button>` : ''}
      ${state.scenario === 'sandbox' && ['SMELTER','WAREHOUSE'].includes(m.type) ? '<button class="small-action" data-action="supply">Учебная поставка: +3 руды</button>' : ''}
      ${m.id > 3 ? '<button class="small-action" data-action="remove">Демонтировать пустое здание</button>' : ''}</div>
    <div class="connections-title">КОНВЕЙЕРЫ · ${links.length}</div>${links.map(link => `<div class="link-row"><span>№${link.from} → №${link.to}</span><button data-disconnect="${link.from},${link.to}" title="Убрать конвейер" aria-label="Убрать конвейер ${link.from} → ${link.to}">×</button></div>`).join('')}
    </div><div class="inspector-foot">В начале такта конвейер переносит продукцию.<br>Затем станки выполняют свою работу.</div>`;
  if (focusSelector) $('inspector').querySelector(focusSelector)?.focus({ preventScroll: true });
}

function renderTool() {
  $('map-wrap').classList.toggle('building-mode', !!buildType);
  document.querySelectorAll('[data-build]').forEach(button => button.classList.toggle('active', button.dataset.build === buildType));
  document.querySelectorAll('.cell').forEach(cell => cell.setAttribute('tabindex', buildType ? '0' : '-1'));
  $('cancel-tool').hidden = !buildType && !connectFrom;
  $('map-help').textContent = buildType ? `${names[buildType]}: выбери свободную клетку. Esc — отмена.`
    : connectFrom ? `Конвейер от здания №${connectFrom}: выбери получателя. Esc — отмена.`
    : 'Выбери здание, чтобы увидеть, что происходит внутри.';
}

function cancelTool() { buildType = null; connectFrom = null; renderTool(); }

async function chooseMapTarget(target) {
  const building = target.closest('[data-machine]');
  if (building) {
    const id = Number(building.dataset.machine);
    if (connectFrom) { await command('connect', { id: connectFrom, target: id }); cancelTool(); }
    else { selected = id; buildType = null; render(); }
    return;
  }
  const cell = target.closest('[data-cell]');
  if (cell && buildType) {
    const [x, y] = cell.dataset.cell.split(',').map(Number);
    const result = await command('build', { type: buildType, x, y });
    if (result) selected = result.machines[result.machines.length - 1].id;
    cancelTool(); if (state) render();
  }
}

for (let y = 0; y < 7; y++) for (let x = 0; x < 12; x++) {
  const cell = document.createElementNS('http://www.w3.org/2000/svg','rect');
  cell.setAttribute('x',String(68+x*72)); cell.setAttribute('y',String(75+y*64));
  cell.setAttribute('width','72'); cell.setAttribute('height','64'); cell.setAttribute('rx','5');
  cell.setAttribute('class','cell'); cell.setAttribute('role','button'); cell.setAttribute('tabindex','-1');
  cell.setAttribute('aria-label',`Клетка ${x+1}, ${y+1}`); cell.dataset.cell=`${x},${y}`; $('cells').append(cell);
}
$('factory-map').addEventListener('click', event => chooseMapTarget(event.target));
$('factory-map').addEventListener('keydown', event => { if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); chooseMapTarget(event.target); } });
document.querySelectorAll('[data-build]').forEach(button => button.addEventListener('click', () => { buildType = button.dataset.build; connectFrom = null; renderTool(); }));
$('cancel-tool').addEventListener('click',cancelTool);
document.addEventListener('keydown', event => { if (event.key === 'Escape') cancelTool(); });
document.addEventListener('pointerdown', () => { pointerDown = true; });
document.addEventListener('pointerup', () => { pointerDown = false; });
document.addEventListener('pointercancel', () => { pointerDown = false; });
window.addEventListener('blur', () => { pointerDown = false; });
$('play').addEventListener('click', () => { if (state) command(state.running ? 'pause' : 'play'); });
$('step').addEventListener('click', () => command('step',{ amount: 1 }));
$('speed').addEventListener('click', () => { if (state) command('speed',{ amount: state.speed === 4 ? 1 : state.speed*2 }); });
$('submit').addEventListener('click', () => command('submit',{ id: 3 }));
$('inspector').addEventListener('click', async event => {
  const action = event.target.closest('[data-action]');
  if (action) { await command(action.dataset.action, { id: selected }); return; }
  const disconnect = event.target.closest('[data-disconnect]');
  if (disconnect) { const [id,target] = disconnect.dataset.disconnect.split(',').map(Number); await command('disconnect',{id,target}); return; }
  if (event.target.closest('#connect-machine')) { connectFrom = selected; buildType = null; renderTool(); }
});
for (const id of ['open-workshop','open-workshop-bottom']) $(id).addEventListener('click', () => $('workshop').showModal());
$('new-shift').addEventListener('click', () => $('shift-dialog').showModal());
document.querySelectorAll('[data-close]').forEach(button => button.addEventListener('click', () => $(button.dataset.close).close()));
document.querySelectorAll('[data-scenario]').forEach(button => button.addEventListener('click', async () => {
  const result = await command('reset',{scenario:button.dataset.scenario});
  if (result) { selected=2; animationTick=-1; cancelTool(); $('shift-dialog').close(); render(); }
}));
poll();
