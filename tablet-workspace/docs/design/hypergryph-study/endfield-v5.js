const app = document.getElementById('app');
const canvas = document.querySelector('.canvas');
const feedback = document.querySelector('.save');
const canvasMenu = document.getElementById('canvasMenu');
const createMenu = document.getElementById('createMenu');

function fit() {
  const scale = Math.min(innerWidth / 1600, innerHeight / 1000);
  app.style.transform = `scale(${scale})`;
  app.parentElement.style.width = `${1600 * scale}px`;
  app.parentElement.style.height = `${1000 * scale}px`;
}
addEventListener('resize', fit);
fit();

let feedbackTimer;
function notice(message) {
  feedback.textContent = message;
  clearTimeout(feedbackTimer);
  feedbackTimer = setTimeout(() => feedback.textContent = '已保存', 2100);
}

const menuButtons = [...document.querySelectorAll('button[data-menu]')];
function closeMenus() {
  canvasMenu.classList.remove('open');
  createMenu.classList.remove('open');
  menuButtons.forEach(button => button.setAttribute('aria-expanded', 'false'));
}
menuButtons.forEach(button => {
  button.setAttribute('aria-haspopup', 'menu');
  button.setAttribute('aria-expanded', 'false');
  button.addEventListener('click', event => {
    event.stopPropagation();
    closeProperty();
    const menu = button.dataset.menu === 'create' ? createMenu : canvasMenu;
    const opening = !menu.classList.contains('open');
    closeMenus();
    menu.classList.toggle('open', opening);
    button.setAttribute('aria-expanded', String(opening));
    if (opening) menu.querySelector('button')?.focus();
    notice(opening ? '菜单已展开' : '菜单已收起');
  });
});

document.querySelectorAll('.menu button').forEach(button => {
  button.addEventListener('click', event => {
    event.stopPropagation();
    closeMenus();
    notice(`${button.textContent.trim()} · 预览交互`);
  });
});

document.querySelectorAll('.side-row').forEach(button => {
  button.addEventListener('click', () => {
    if (button.closest('.pages')) {
      document.querySelectorAll('.pages .side-row').forEach(item => item.removeAttribute('data-selected'));
      button.dataset.selected = 'true';
    }
    notice(`${button.textContent.trim()} · 导航预览`);
  });
});

document.querySelectorAll('.bottom-group .tool').forEach(button => {
  button.dataset.tip = button.title;
  if (button.dataset.menu) return;
  button.addEventListener('click', () => {
    if (!['撤销', '重做'].includes(button.title)) {
      document.querySelectorAll('.bottom-group .tool.on').forEach(item => item.classList.remove('on'));
      button.classList.add('on');
      document.querySelectorAll('.bottom-group .tool:not([data-menu])').forEach(item => item.setAttribute('aria-pressed', String(item === button)));
    }
    notice(`当前工具：${button.title}`);
  });
});

const propertyPanel = document.createElement('section');
propertyPanel.className = 'property-panel';
propertyPanel.setAttribute('aria-label', '工具属性预览');
canvas.append(propertyPanel);
let propertyTrigger = null;

function closeProperty() {
  propertyPanel.classList.remove('open');
  propertyTrigger?.removeAttribute('data-pending');
  propertyTrigger?.setAttribute('aria-expanded', 'false');
  propertyTrigger = null;
}

const propertyOptions = {
  '图形类型': ['矩形', '圆形', '菱形', '三角形'],
  '填充颜色': ['白色', '炭黑', '信号黄', '透明'],
  '边框': ['实线', '虚线', '无边框'],
  '边框粗细': ['1 px', '2 px', '4 px', '8 px']
};
const swatchColors = { '白色': '#ffffff', '炭黑': '#222824', '信号黄': '#eceb34', '透明': 'transparent' };

function openProperty(button) {
  const title = button.title;
  const wasOpen = propertyPanel.classList.contains('open') && propertyTrigger === button;
  closeMenus();
  closeProperty();
  if (wasOpen) return;
  propertyTrigger = button;
  button.dataset.pending = 'true';
  button.setAttribute('aria-haspopup', 'dialog');
  button.setAttribute('aria-expanded', 'true');
  const heading = document.createElement('div');
  heading.className = 'property-heading';
  heading.textContent = title;
  const closeButton = document.createElement('button');
  closeButton.textContent = '×';
  closeButton.setAttribute('aria-label', '关闭属性');
  closeButton.addEventListener('click', event => {
    event.stopPropagation();
    closeProperty();
    button.focus();
  });
  heading.append(closeButton);
  const options = document.createElement('div');
  options.className = 'property-options';
  const selected = button.dataset.value || propertyOptions[title][0];
  propertyOptions[title].forEach(value => {
    const option = document.createElement('button');
    option.setAttribute('aria-pressed', String(value === selected));
    if (swatchColors[value]) {
      const swatch = document.createElement('span');
      swatch.className = 'swatch';
      swatch.style.background = swatchColors[value] === 'transparent' ? 'repeating-linear-gradient(45deg,#ddd 0 3px,#fff 3px 6px)' : swatchColors[value];
      option.append(swatch);
    }
    option.append(document.createTextNode(value));
    option.addEventListener('click', event => {
      event.stopPropagation();
      button.dataset.value = value;
      options.querySelectorAll('button').forEach(item => item.setAttribute('aria-pressed', String(item === option)));
      if (title === '边框粗细') button.textContent = value;
      notice(`${title}：${value} · 预览状态`);
    });
    options.append(option);
  });
  const note = document.createElement('div');
  note.className = 'property-note';
  note.textContent = '仅展示控件状态；白板对象与数据保持原样。';
  propertyPanel.replaceChildren(heading, options, note);
  propertyPanel.classList.add('open');
  options.querySelector('button')?.focus();
}

document.querySelectorAll('.context-group .tool').forEach(button => {
  button.dataset.tip = button.title;
  if (button.dataset.menu) return;
  button.addEventListener('click', event => {
    event.stopPropagation();
    if (propertyOptions[button.title]) openProperty(button);
    else notice(`${button.title} · 预览交互`);
  });
});

document.addEventListener('click', event => {
  if (!propertyPanel.contains(event.target)) closeProperty();
  if (!canvasMenu.contains(event.target) && !createMenu.contains(event.target)) closeMenus();
});
document.addEventListener('keydown', event => {
  if (event.key === 'Escape') {
    closeMenus();
    closeProperty();
  }
});
