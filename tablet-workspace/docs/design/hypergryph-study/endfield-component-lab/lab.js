const types = [
  { id: 'toggle', en: 'TOGGLE', cn: '开关', task: '开启自动吸附；保持状态可辨' },
  { id: 'button', en: 'BUTTON', cn: '按钮', task: '执行、补充、图标与危险动作' },
  { id: 'tabs', en: 'SEGMENT / TABS', cn: '分段切换', task: '在三个画布模式中选择' },
  { id: 'choice', en: 'CHECK / RADIO', cn: '勾选与单选', task: '切换网格；选择导出范围' },
  { id: 'slider', en: 'SLIDER', cn: '滑块', task: '调整 0–100 的画笔粗细值' },
  { id: 'dropdown', en: 'DROPDOWN', cn: '下拉与浮层', task: '选择图形样式并关闭菜单' },
  { id: 'toolbar', en: 'TOOLBAR', cn: '工具与浮板', task: '切换工具并展开属性面板' },
  { id: 'navigation', en: 'NAVIGATION', cn: '侧栏导航', task: '切换页面，区分点按与选中' }
];
const schemes = [
  { id: 'a', short: 'A', name: '极简工业', motion: '140ms / ease-out', description: '线框与边界变化；选中标记稳定，按下只改变局部明度。', easing: 'cubic-bezier(.2,.7,.2,1)', start: '中性线框', middle: '边界加深 + 1px 内移', end: '稳定的底边或侧边标记', repeat: '快速连续点按立即响应并从当前状态反转；无队列。' },
  { id: 'b', short: 'B', name: '机械控制', motion: '190ms / guided travel', description: '滑块与选择片沿固定轨道移动；按压缩短实体阴影。', easing: 'cubic-bezier(.22,.68,.22,1)', start: '滑块在轨道左端', middle: '沿轨道移动，阴影收紧', end: '滑块抵达右端且灯色稳定', repeat: '中途反向时从当前位移反转；不弹跳、不排队。' },
  { id: 'c', short: 'C', name: '数字终端', motion: '120ms / steps(3,end)', description: '状态码、信号灯与三段式边界切换；文字值即时更新。', easing: 'steps(3,end)', start: '状态码 00 + 暗灯', middle: '边界分三步点亮', end: '状态码 01 + 实色灯', repeat: '真实值立即覆盖旧值；装饰分段可被新操作打断。' },
  { id: 'd', short: 'D', name: '模块科技', motion: '210ms / layered settle', description: '相邻模块交接激活面，背景层与下缘结构同时更换。', easing: 'cubic-bezier(.18,.76,.3,1)', start: '左模块承托激活面', middle: '激活面跨槽移动，层次变化', end: '右模块形成新承托', repeat: '从当前模块位置转向新目标；点击只认最后一次。' }
];
const scores = [
  ['视觉辨识度', 3, 4, 5, 5],
  ['状态清晰度', 5, 5, 5, 4],
  ['交互反馈质量', 4, 5, 4, 4],
  ['触控便利性', 5, 5, 4, 4],
  ['高密度适应', 5, 3, 5, 3],
  ['动画性能', 5, 4, 5, 4],
  ['开发维护成本', 5, 3, 4, 2],
  ['工业视觉一致性', 4, 5, 5, 5]
];
const grid = document.getElementById('liveGrid');
const announcer = document.getElementById('announcer');
const say = message => { announcer.textContent = message; };

function specimenMarkup(type) {
  switch (type) {
    case 'toggle': return '<button class="toggle" role="switch" aria-checked="false" aria-label="自动吸附"><span>OFF</span><span>ON</span><i class="indicator" aria-hidden="true"></i></button>';
    case 'button': return '<div class="button-stack"><button class="action primary" data-action="应用">应用</button><button class="action secondary" data-action="更多">更多</button><button class="action icon" data-action="收藏" aria-label="收藏">☆</button><button class="action danger" data-action="移除">移除</button></div>';
    case 'tabs': return '<div class="segments" role="tablist" aria-label="画布模式" style="--index:0%"><i class="moving" aria-hidden="true"></i><button role="tab" aria-selected="true">编辑</button><button role="tab" aria-selected="false">整理</button><button role="tab" aria-selected="false">预览</button></div>';
    case 'choice': return '<div class="choice-set"><button class="choice check" role="checkbox" aria-checked="false"><i class="mark" aria-hidden="true"></i>网格</button><button class="choice radio" role="radio" aria-checked="true"><i class="mark" aria-hidden="true"></i>全部</button><button class="choice radio" role="radio" aria-checked="false"><i class="mark" aria-hidden="true"></i>选中</button></div>';
    case 'slider': return '<div class="range-wrap"><input type="range" min="0" max="100" value="50" aria-label="画笔粗细" style="--fill:50%"><output>50</output></div>';
    case 'dropdown': return '<div class="menu-host"><button class="dropdown-trigger" aria-haspopup="listbox" aria-expanded="false">矩形</button><div class="popup" role="listbox" aria-label="图形样式"><button role="option" aria-selected="true">矩形</button><button role="option" aria-selected="false">圆形</button><button role="option" aria-selected="false">菱形</button></div></div>';
    case 'toolbar': return '<div class="toolbar" role="toolbar" aria-label="画布工具"><button aria-label="选择工具" aria-pressed="true">↖</button><button aria-label="手写笔" aria-pressed="false">✎</button><button aria-label="连线工具" aria-pressed="false">↝</button><button class="tool-menu" aria-label="工具属性" aria-expanded="false">⋯</button><div class="tool-panel"><b>工具属性 / DETAIL</b><button type="button">1 px</button><button type="button">4 px</button></div></div>';
    case 'navigation': return '<div class="nav-demo" role="navigation" aria-label="工作区示例"><button aria-current="page">全部文档</button><button>收藏</button><button>模板</button></div>';
    default: throw new Error(`Unknown component: ${type}`);
  }
}

types.forEach((type, rowIndex) => {
  const row = document.createElement('div');
  row.className = 'matrix-row';
  row.innerHTML = `<div class="type-label"><b>${String(rowIndex + 1).padStart(2, '0')} / ${type.en}</b><span>${type.cn}</span><small>${type.task}</small></div>`;
  schemes.forEach(scheme => {
    const cell = document.createElement('article');
    cell.className = `specimen scheme-${scheme.id}`;
    cell.dataset.id = `${type.id.toUpperCase()} / ${scheme.short}`;
    cell.dataset.type = type.id;
    cell.innerHTML = `<div class="spec-top">${scheme.short} / ${scheme.name}</div><div class="spec-inner">${specimenMarkup(type.id)}</div><div class="spec-foot"><span>INPUT / TOUCH</span><b>${scheme.motion.split(' / ')[0]}</b></div>`;
    row.append(cell);
  });
  grid.append(row);
});

function closePopups(except = null) {
  document.querySelectorAll('.popup.open,.tool-panel.open').forEach(panel => {
    if (panel !== except) panel.classList.remove('open');
  });
  document.querySelectorAll('.dropdown-trigger[aria-expanded=true],.tool-menu[aria-expanded=true]').forEach(trigger => {
    if (!except || !trigger.parentElement.contains(except)) trigger.setAttribute('aria-expanded', 'false');
  });
}

grid.addEventListener('click', event => {
  const button = event.target.closest('button');
  if (!button) return;
  const specimen = button.closest('.specimen');
  if (!specimen) return;
  const type = specimen.dataset.type;
  const scheme = specimen.dataset.id.slice(-1);

  if (type === 'toggle') {
    const on = button.getAttribute('aria-checked') !== 'true';
    button.setAttribute('aria-checked', String(on));
    say(`${scheme} 开关 ${on ? '开启' : '关闭'}`);
  } else if (type === 'button') {
    button.classList.add('is-firing');
    setTimeout(() => button.classList.remove('is-firing'), 220);
    say(`${scheme} ${button.dataset.action}，按压反馈完成`);
  } else if (type === 'tabs') {
    const parent = button.closest('.segments');
    const tabs = [...parent.querySelectorAll('[role=tab]')];
    tabs.forEach(tab => tab.setAttribute('aria-selected', String(tab === button)));
    parent.style.setProperty('--index', `${tabs.indexOf(button) * 100}%`);
    say(`${scheme} 切换至${button.textContent}`);
  } else if (type === 'choice') {
    if (button.getAttribute('role') === 'checkbox') {
      button.setAttribute('aria-checked', String(button.getAttribute('aria-checked') !== 'true'));
    } else {
      button.parentElement.querySelectorAll('[role=radio]').forEach(radio => radio.setAttribute('aria-checked', String(radio === button)));
    }
    say(`${scheme} ${button.textContent.trim()} 已更新`);
  } else if (type === 'dropdown') {
    const host = button.closest('.menu-host');
    const trigger = host.querySelector('.dropdown-trigger');
    const popup = host.querySelector('.popup');
    if (button === trigger) {
      const opening = !popup.classList.contains('open');
      closePopups();
      popup.classList.toggle('open', opening);
      trigger.setAttribute('aria-expanded', String(opening));
      if (opening) popup.querySelector('button')?.focus();
    } else {
      trigger.firstChild.textContent = button.textContent;
      popup.querySelectorAll('[role=option]').forEach(option => option.setAttribute('aria-selected', String(option === button)));
      closePopups();
      trigger.focus();
      say(`${scheme} 图形样式：${button.textContent}`);
    }
  } else if (type === 'toolbar') {
    const bar = button.closest('.toolbar');
    const panel = bar.querySelector('.tool-panel');
    const trigger = bar.querySelector('.tool-menu');
    if (button === trigger) {
      const opening = !panel.classList.contains('open');
      closePopups();
      panel.classList.toggle('open', opening);
      trigger.setAttribute('aria-expanded', String(opening));
      if (opening) panel.querySelector('button')?.focus();
    } else if (panel.contains(button)) {
      closePopups();
      say(`${scheme} 工具粗细：${button.textContent}`);
    } else {
      bar.querySelectorAll('button[aria-pressed]').forEach(tool => tool.setAttribute('aria-pressed', String(tool === button)));
      closePopups();
      say(`${scheme} ${button.getAttribute('aria-label')}已激活`);
    }
  } else if (type === 'navigation') {
    specimen.querySelectorAll('.nav-demo button').forEach(item => item.removeAttribute('aria-current'));
    button.setAttribute('aria-current', 'page');
    say(`${scheme} 导航选中：${button.textContent}`);
  }
});

grid.addEventListener('input', event => {
  const range = event.target.closest('input[type=range]');
  if (!range) return;
  range.style.setProperty('--fill', `${range.value}%`);
  range.parentElement.querySelector('output').textContent = range.value;
  say(`画笔粗细 ${range.value}`);
});

grid.addEventListener('keydown', event => {
  if (!['ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown'].includes(event.key)) return;
  const tab = event.target.closest('[role=tab]');
  const radio = event.target.closest('[role=radio]');
  const item = tab || radio;
  if (!item) return;
  event.preventDefault();
  const siblings = [...item.parentElement.querySelectorAll(tab ? '[role=tab]' : '[role=radio]')];
  const direction = ['ArrowLeft', 'ArrowUp'].includes(event.key) ? -1 : 1;
  const next = siblings[(siblings.indexOf(item) + direction + siblings.length) % siblings.length];
  next.focus();
  next.click();
});

document.addEventListener('click', event => {
  if (!event.target.closest('.menu-host,.toolbar')) closePopups();
});
document.addEventListener('keydown', event => {
  if (event.key === 'Escape') closePopups();
});

document.querySelectorAll('.view-tab').forEach(tab => {
  tab.addEventListener('click', () => {
    document.querySelectorAll('.view-tab').forEach(item => {
      item.classList.toggle('is-current', item === tab);
      item.removeAttribute('aria-current');
    });
    tab.setAttribute('aria-current', 'page');
    document.querySelectorAll('.view').forEach(view => view.classList.toggle('is-visible', view.id === `${tab.dataset.view}View`));
    closePopups();
  });
});

const reduceMotion = document.getElementById('reduceMotion');
reduceMotion.checked = matchMedia('(prefers-reduced-motion: reduce)').matches;
function syncMotion() { document.body.classList.toggle('reduce-motion', reduceMotion.checked); }
reduceMotion.addEventListener('change', syncMotion);
syncMotion();

function resetLive() {
  grid.querySelectorAll('.toggle').forEach(item => item.setAttribute('aria-checked', 'false'));
  grid.querySelectorAll('.segments').forEach(item => {
    item.style.setProperty('--index', '0%');
    item.querySelectorAll('[role=tab]').forEach((tab, index) => tab.setAttribute('aria-selected', String(index === 0)));
  });
  grid.querySelectorAll('.choice.check').forEach(item => item.setAttribute('aria-checked', 'false'));
  grid.querySelectorAll('.choice.radio').forEach(item => item.setAttribute('aria-checked', String(item.textContent.trim() === '全部')));
  grid.querySelectorAll('input[type=range]').forEach(item => {
    item.value = '50'; item.style.setProperty('--fill', '50%'); item.parentElement.querySelector('output').textContent = '50';
  });
  grid.querySelectorAll('.dropdown-trigger').forEach(item => item.firstChild.textContent = '矩形');
  grid.querySelectorAll('.popup [role=option]').forEach(item => item.setAttribute('aria-selected', String(item.textContent === '矩形')));
  grid.querySelectorAll('.toolbar').forEach(bar => bar.querySelectorAll('button[aria-pressed]').forEach((button, index) => button.setAttribute('aria-pressed', String(index === 0))));
  grid.querySelectorAll('.nav-demo').forEach(nav => nav.querySelectorAll('button').forEach((button, index) => index === 0 ? button.setAttribute('aria-current', 'page') : button.removeAttribute('aria-current')));
  closePopups();
}

let replayTimers = [];
function replayAfter(delay, action) { replayTimers.push(setTimeout(action, delay)); }
grid.addEventListener('pointerdown', () => {
  replayTimers.forEach(clearTimeout);
  replayTimers = [];
  document.body.classList.remove('replaying');
});
document.getElementById('replayAll').addEventListener('click', () => {
  replayTimers.forEach(clearTimeout);
  replayTimers = [];
  resetLive();
  const liveTab = document.querySelector('[data-view=live]');
  if (!liveTab.classList.contains('is-current')) liveTab.click();
  if (reduceMotion.checked) {
    say('已复位；减少动态效果开启');
    return;
  }
  document.body.classList.remove('replaying');
  void document.body.offsetWidth;
  document.body.classList.add('replaying');
  const delay = 90;
  replayAfter(delay, () => grid.querySelectorAll('.toggle').forEach(toggle => toggle.setAttribute('aria-checked', 'true')));
  replayAfter(delay + 120, () => grid.querySelectorAll('.segments').forEach(segments => {
    segments.style.setProperty('--index', '100%');
    segments.querySelectorAll('[role=tab]').forEach((tab, index) => tab.setAttribute('aria-selected', String(index === 1)));
  }));
  replayAfter(delay + 220, () => grid.querySelectorAll('.button-stack .primary').forEach(button => button.classList.add('is-firing')));
  replayAfter(delay + 350, () => grid.querySelectorAll('.choice.check').forEach(button => button.setAttribute('aria-checked', 'true')));
  replayAfter(delay + 440, () => grid.querySelectorAll('input[type=range]').forEach(range => {
    range.value = '75'; range.style.setProperty('--fill', '75%'); range.parentElement.querySelector('output').textContent = '75';
  }));
  replayAfter(delay + 540, () => grid.querySelectorAll('.toolbar').forEach(bar => bar.querySelectorAll('button[aria-pressed]').forEach((button, index) => button.setAttribute('aria-pressed', String(index === 1)))));
  replayAfter(delay + 620, () => grid.querySelectorAll('.nav-demo').forEach(nav => nav.querySelectorAll('button').forEach((button, index) => index === 1 ? button.setAttribute('aria-current', 'page') : button.removeAttribute('aria-current'))));
  replayAfter(delay + 760, () => {
    grid.querySelectorAll('.button-stack .primary').forEach(button => button.classList.remove('is-firing'));
    document.body.classList.remove('replaying');
  });
  say('已复位并重播主要组件状态');
});

const motionGrid = document.getElementById('motionGrid');
schemes.forEach(scheme => {
  const card = document.createElement('article');
  card.className = 'motion-card';
  card.innerHTML = `<span class="letter">${scheme.short}</span><h3>${scheme.name}</h3><p>${scheme.description}</p><div class="timeline"><span></span><b>${scheme.motion.split(' / ')[0]}</b></div><p>起点：${scheme.start}<br>中段：${scheme.middle}<br>终点：${scheme.end}</p><p style="margin-top:8px">${scheme.repeat}</p><code>${scheme.easing}</code>`;
  motionGrid.append(card);
});
const scoreBody = document.querySelector('#scoreTable tbody');
scores.forEach(([criterion, ...values]) => {
  const row = document.createElement('tr');
  row.innerHTML = `<td>${criterion}</td>${values.map(value => `<td>${value} / 5</td>`).join('')}`;
  scoreBody.append(row);
});
