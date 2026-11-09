import { mkdirSync, writeFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';

const root = dirname(fileURLToPath(import.meta.url));
const ivory = '#F1F2E9';
const ink = '#1D2423';
const lime = '#E6E93E';
const muted = '#8A9890';

const concepts = [
  {
    id: '01', name: '折页信标', key: '最贴近现有图标',
    description: '保留“页面内有画布路径”的语义，折页与黄色端点形成明确的识别中心。',
    svg: `<rect width="128" height="128" rx="28" fill="${ink}"/>
      <path d="M27 31H78L100 53V99H27V31Z" fill="#2A3330" stroke="${ivory}" stroke-width="4.5" stroke-linejoin="round"/>
      <path d="M78 31V53H100" fill="none" stroke="${ivory}" stroke-width="4.5" stroke-linejoin="round"/>
      <path d="M38 75L58 58L82 79" fill="none" stroke="${ivory}" stroke-width="4.5" stroke-linecap="round" stroke-linejoin="round"/>
      <circle cx="38" cy="75" r="5" fill="${ivory}"/><circle cx="58" cy="58" r="5" fill="${lime}"/><circle cx="82" cy="79" r="5" fill="${ivory}"/>
      <path d="M38 88H70" stroke="${muted}" stroke-width="3" stroke-linecap="square"/>
      <path d="M19 22H39M19 22V42" fill="none" stroke="${lime}" stroke-width="3"/>`,
  },
  {
    id: '02', name: '界面模块', key: '工业感最强',
    description: '两块功能面板的拼接表达文档与白板；以结构分割替代装饰性线条。',
    svg: `<rect width="128" height="128" rx="28" fill="${ivory}"/>
      <path d="M19 26H66V102H19V26Z" fill="${ink}"/>
      <path d="M72 26H95L109 40V102H72V26Z" fill="#313D39"/>
      <path d="M95 26V40H109" fill="none" stroke="${ivory}" stroke-width="3.5"/>
      <path d="M31 43H54M31 54H48M31 65H54" stroke="${ivory}" stroke-width="3.5" stroke-linecap="square"/>
      <path d="M84 79L94 57L104 79Z" fill="none" stroke="${ivory}" stroke-width="3.5" stroke-linejoin="round"/>
      <rect x="62" y="73" width="15" height="18" fill="${lime}"/>
      <path d="M24 113H61M73 113H105" stroke="${ink}" stroke-width="2" opacity=".5"/>`,
  },
  {
    id: '03', name: '负形页芯', key: '小尺寸最稳',
    description: '大面积图形与负形页芯，减少细线；在桌面缩到 24px 仍有完整轮廓。',
    svg: `<rect width="128" height="128" rx="28" fill="${lime}"/>
      <path d="M28 21H77L100 44V105H28V21Z" fill="${ink}"/>
      <path d="M77 21V44H100" fill="none" stroke="${lime}" stroke-width="4"/>
      <path d="M45 42H66V86H83" fill="none" stroke="${ivory}" stroke-width="9" stroke-linecap="square" stroke-linejoin="miter"/>
      <path d="M44 96H85" stroke="${ivory}" stroke-width="3" opacity=".6"/>
      <rect x="29" y="104" width="71" height="4" fill="${ink}"/>`,
  },
  {
    id: '04', name: '节点页网', key: '白板识别最强',
    description: '把原图标中的三个节点放大为主图形，突出无限画布与连接关系。',
    svg: `<rect width="128" height="128" rx="28" fill="#293330"/>
      <path d="M29 25H86L100 39V101H29V25Z" fill="none" stroke="${ivory}" stroke-width="4.5" stroke-linejoin="round"/>
      <path d="M39 76L64 46L85 75L49 89" fill="none" stroke="${ivory}" stroke-width="5" stroke-linecap="round" stroke-linejoin="round"/>
      <rect x="34" y="70" width="11" height="11" fill="${lime}"/>
      <rect x="58.5" y="40.5" width="11" height="11" fill="${ivory}"/>
      <rect x="79.5" y="69.5" width="11" height="11" fill="${lime}"/>
      <path d="M101 61H109M101 70H109M19 91H26" stroke="${lime}" stroke-width="3"/>`,
  },
  {
    id: '05', name: '双域折片', key: '层次最鲜明',
    description: '白色页面与深色画布纵向叠合，黄色连接件成为两种工作模式的交点。',
    svg: `<rect width="128" height="128" rx="28" fill="#5D6E63"/>
      <path d="M22 22H77V96H22V22Z" fill="${ivory}" stroke="${ink}" stroke-width="4"/>
      <path d="M52 35H98V105H52V35Z" fill="${ink}"/>
      <path d="M31 37H62M31 48H49M31 59H45" stroke="${muted}" stroke-width="3"/>
      <path d="M63 82L75 58L87 82" fill="none" stroke="${ivory}" stroke-width="4.5" stroke-linejoin="round"/>
      <path d="M63 82H87" stroke="${ivory}" stroke-width="4.5"/>
      <rect x="46" y="67" width="12" height="19" fill="${lime}"/>
      <path d="M97 35L107 45V105" fill="none" stroke="${ink}" stroke-width="3"/>`,
  },
  {
    id: '06', name: '轨道文档', key: '终端感最强',
    description: '粗线文档轮廓、导轨与定位信号构成有方向感的工业终端标记。',
    svg: `<rect width="128" height="128" rx="28" fill="${ink}"/>
      <path d="M25 19V109M103 19V109" stroke="${muted}" stroke-width="3"/>
      <path d="M35 23H82L96 37V100H35V23Z" fill="${ivory}"/>
      <path d="M82 23V37H96" fill="none" stroke="${ink}" stroke-width="3"/>
      <path d="M47 49H80M47 59H70" stroke="${ink}" stroke-width="4"/>
      <path d="M48 81L63 67L81 81" fill="none" stroke="${ink}" stroke-width="5" stroke-linecap="square" stroke-linejoin="miter"/>
      <rect x="76" y="76" width="11" height="11" fill="${lime}"/>
      <path d="M17 31H30M98 91H111" stroke="${lime}" stroke-width="4"/>`,
  },
];

const shell = content => `<svg xmlns="http://www.w3.org/2000/svg" width="128" height="128" viewBox="0 0 128 128" role="img">${content}</svg>`;
mkdirSync(root, { recursive: true });
for (const c of concepts) writeFileSync(join(root, `concept-${c.id}.svg`), shell(c.svg));
const cards = concepts.map(c => `<article><div class="hero"><img src="concept-${c.id}.svg" width="164" height="164" alt="${c.name}"></div><div class="detail"><div class="meta">CONCEPT ${c.id} <span>${c.key}</span></div><h2>${c.name}</h2><p>${c.description}</p><div class="sizes"><span>桌面尺寸</span><img src="concept-${c.id}.svg" width="64" height="64" alt=""><img src="concept-${c.id}.svg" width="32" height="32" alt=""><img src="concept-${c.id}.svg" width="24" height="24" alt=""></div><a href="concept-${c.id}.svg" download>下载可编辑 SVG ↗</a></div></article>`).join('');
writeFileSync(join(root, 'index.html'), `<!doctype html><html lang="zh-CN"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>灵页 · 工业主题图标探索</title><style>
*{box-sizing:border-box}body{margin:0;background:#e9ece7;color:#1d2423;font:14px/1.55 Inter,"Microsoft YaHei",sans-serif}main{max-width:1260px;margin:auto;padding:34px 24px 70px}header{background:#1d2423;color:#f1f2e9;padding:28px 34px;margin-bottom:22px;border-left:7px solid #e6e93e}small,.meta{font-weight:800;letter-spacing:.09em}small{color:#e6e93e}h1{font-size:30px;margin:8px 0}header p{color:#bdc7bd;margin:0;max-width:760px}.grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:16px}article{background:#fafbf8;border:1px solid #c7cfc5}.hero{display:grid;place-items:center;height:226px;background:linear-gradient(135deg,#dfe4dc 50%,#303936 50%)}.hero img{filter:drop-shadow(0 11px 14px #131a1740)}.detail{padding:19px 21px 23px}.meta{font-size:11px;color:#65736a;display:flex;justify-content:space-between;gap:8px}.meta span{letter-spacing:0;color:#66756b}h2{font-size:21px;margin:9px 0 5px}article p{margin:0;min-height:64px;color:#5c6a60}.sizes{margin:15px 0;display:flex;align-items:center;gap:12px;min-height:72px}.sizes span{font-size:12px;color:#66756b;margin-right:auto}.sizes img{filter:drop-shadow(0 2px 3px #0002)}a{color:#1d2423;font-weight:700;text-decoration:none;border-bottom:1px solid #1d2423}.note{margin-top:22px;color:#5c6a60}@media(max-width:950px){.grid{grid-template-columns:repeat(2,1fr)}}@media(max-width:600px){.grid{grid-template-columns:1fr}main{padding:18px 12px}header{padding:20px}}
</style></head><body><main><header><small>LINGYE / ICON EXPLORATION 01–06</small><h1>灵页 · 工业主题图标探索</h1><p>保留原图标“文档 + 画布节点”的意义，探索折页、模块、负形、网络和终端结构。六款均为原创矢量设计；尚未替换安卓应用图标。</p></header><div class="grid">${cards}</div><p class="note">请同时看 64px 与 24px：小尺寸是否仍可识别，比放大时的细节更重要。SVG 可下载后在 Figma 中继续改形状和颜色。</p></main></body></html>`);
console.log(`Built ${concepts.length} icon concepts in ${root}`);
