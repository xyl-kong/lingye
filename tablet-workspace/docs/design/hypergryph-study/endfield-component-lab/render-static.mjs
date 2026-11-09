import { writeFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';

const dir = dirname(fileURLToPath(import.meta.url));
const kinds = [
  ['TOGGLE', '开关'], ['BUTTON', '按钮'], ['SEGMENT', '分段切换'],
  ['CHECK / RADIO', '勾选与单选'], ['SLIDER', '滑块'], ['DROPDOWN', '下拉与浮层'],
  ['TOOLBAR', '工具与浮板'], ['NAVIGATION', '侧栏导航']
];
const schemes = [['A', '极简工业'], ['B', '机械控制'], ['C', '数字终端'], ['D', '模块科技']];
const states = ['Default', 'Hover', 'Pressed', 'Active', 'Disabled', 'Transition'];
const color = { ink: '#202722', paper: '#f5f7f1', line: '#879488', faint: '#dce3d9', signal: '#ebe936', white: '#ffffff', gray: '#687568', danger: '#a13b34' };
const esc = text => String(text).replaceAll('&','&amp;').replaceAll('<','&lt;').replaceAll('>','&gt;');
const rect = (x,y,w,h,fill,stroke='none',sw=1,extra='') => `<rect x="${x}" y="${y}" width="${w}" height="${h}" fill="${fill}" stroke="${stroke}" stroke-width="${sw}" ${extra}/>`;
const line = (x1,y1,x2,y2,stroke=color.line,sw=1,extra='') => `<line x1="${x1}" y1="${y1}" x2="${x2}" y2="${y2}" stroke="${stroke}" stroke-width="${sw}" ${extra}/>`;
const txt = (x,y,value,size=12,fill=color.ink,weight=600,anchor='start',family='system-ui, sans-serif') => `<text x="${x}" y="${y}" font-size="${size}" fill="${fill}" font-weight="${weight}" text-anchor="${anchor}" font-family="${family}">${esc(value)}</text>`;
const circle = (x,y,r,fill,stroke='none',sw=1) => `<circle cx="${x}" cy="${y}" r="${r}" fill="${fill}" stroke="${stroke}" stroke-width="${sw}"/>`;
const polygon = (points,fill,stroke='none',sw=1) => `<polygon points="${points}" fill="${fill}" stroke="${stroke}" stroke-width="${sw}"/>`;
function svg(content,width,height){return `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}" viewBox="0 0 ${width} ${height}" role="img"><rect width="100%" height="100%" fill="#e8eae3"/>${content}</svg>`;}

function component(kind, scheme, state, x, y, w=224, h=74) {
  const active = state === 'Active';
  const mid = state === 'Transition';
  const hover = state === 'Hover';
  const pressed = state === 'Pressed';
  const disabled = state === 'Disabled';
  const ix = x + 14, iy = y + 13;
  let out = '';
  const ink = disabled ? '#8e998e' : color.ink;
  const accent = disabled ? '#bec6b8' : color.signal;
  const edge = hover ? ink : color.line;
  const shift = pressed ? 2 : 0;

  if (kind === 0) {
    const sx=ix+46, sy=iy+4, sw=118, sh=43;
    if (scheme==='A') {
      out += polygon(`${sx},${sy} ${sx+sw-8},${sy} ${sx+sw},${sy+8} ${sx+sw},${sy+sh} ${sx},${sy+sh}`,active?ink:color.white,edge);
      out += rect(sx,sy+sh-4,active?sw:mid?sw*.52:0,4,accent);
      out += txt(sx+17,sy+27,active?'ON':'OFF',12,active?color.white:ink,700);
    } else if (scheme==='B') {
      out += rect(sx,sy,sw,sh,color.faint,ink,2);
      for(let i=9;i<sw;i+=15)out+=line(sx+i,sy+4,sx+i,sy+sh-4,'#bec8bb',1);
      out += rect(sx+4+(active?80:mid?42:0),sy+4+shift,30,sh-8,active?accent:color.white,ink,1);
      out += txt(sx+sw/2,sy+27,active?'ON':'OFF',11,ink,700,'middle');
    } else if (scheme==='C') {
      out += rect(sx,sy,sw,sh,ink);
      out += rect(sx,sy,4,sh,active?accent:color.line);
      out += txt(sx+12,sy+14,`PWR / ${active?'01':'00'}`,8,active?accent:'#a4b0a2',600,'start','monospace');
      out += txt(sx+12,sy+31,active?'ONLINE':'STANDBY',11,active?accent:color.white,700,'start','monospace');
      out += circle(sx+sw-19,sy+23,6,active?accent:'#5f6b60');
    } else {
      out += rect(sx,sy,sw,sh,color.faint);
      out += rect(sx+4+(active?57:mid?30:0),sy+4,sw/2-6,sh-8,active?accent:ink);
      out += txt(sx+sw*.26,sy+27,'OFF',10,active?ink:color.white,700,'middle');
      out += txt(sx+sw*.75,sy+27,'ON',10,active?ink:ink,700,'middle');
    }
  } else if (kind === 1) {
    const labels=['应用','更多','☆','移除'];
    for(let j=0;j<4;j++){
      const bx=ix+15+(j%2)*91,by=iy+Math.floor(j/2)*29,bw=84,bh=25;
      const fill=j===0?(active||pressed?accent:mid?'#737e69':ink):scheme==='C'?color.faint:color.white;
      const border=scheme==='B'?2:1;
      if(scheme==='D')out+=polygon(`${bx},${by} ${bx+bw-7},${by} ${bx+bw},${by+7} ${bx+bw},${by+bh} ${bx},${by+bh}`,fill,edge,border);
      else out+=rect(bx+shift,by+shift,bw,bh,fill,edge,border);
      if(scheme==='B')out+=rect(bx+shift+2,by+shift+bh+1,bw,3,'#a7b2a4');
      if(scheme==='C')out+=rect(bx+3,by+bh-3,bw-6,2,active?accent:color.line);
      if(scheme==='A'&&j===0)out+=rect(bx,by,3,bh,accent);
      out+=txt(bx+bw/2+shift,by+17+shift,labels[j],11,j===0&&!active&&!pressed?color.white:j===3?color.danger:ink,700,'middle');
    }
  } else if (kind === 2) {
    const sx=ix+8, sy=iy+6, sw=188, sh=42, step=sw/3;
    out+=rect(sx,sy,sw,sh,scheme==='C'?ink:color.faint,edge,scheme==='B'?2:1);
    if(scheme==='B')out+=rect(sx+3+(active?step:mid?step*.5:0),sy+3,step-6,sh-6,accent,ink);
    for(let j=0;j<3;j++){
      if(scheme==='D')out+=rect(sx+j*step+3,sy+3,step-6,sh-6,j===(active?1:0)?ink:color.white);
      if(scheme==='A'&&j===(active?1:0))out+=rect(sx+j*step+4,sy+sh-4,step-8,4,accent);
      if(scheme==='C'&&j===(active?1:0))out+=rect(sx+j*step+4,sy+sh-5,step-8,3,accent);
      out+=txt(sx+step*(j+.5),sy+26,['编辑','整理','预览'][j],11,scheme==='C'?(j===(active?1:0)?accent:color.white):scheme==='D'&&j===(active?1:0)?color.white:ink,700,'middle');
    }
  } else if (kind === 3) {
    const labels=['网格','全部','选中'];
    for(let j=0;j<3;j++){
      const cx=ix+21+j*66,cy=iy+26,r=10,on=j===1||active&&j===0;
      if(j===0||scheme!=='B')out+=rect(cx-r,cy-r,20,20,scheme==='C'?ink:scheme==='D'&&on?ink:color.white,edge);
      else out+=circle(cx,cy,r,color.white,edge);
      if(on||mid&&j===0){if(scheme==='B')out+=line(cx-5,cy,cx-1,cy+4,ink,3)+line(cx-1,cy+4,cx+6,cy-5,ink,3);else if(scheme==='C')out+=circle(cx,cy,mid&&j===0?2:4,accent);else out+=rect(cx-5,cy-5,mid&&j===0?5:10,10,scheme==='D'?accent:ink);}
      out+=txt(cx,cy+28,labels[j],10,ink,600,'middle');
    }
  } else if (kind === 4) {
    const sx=ix+8,sy=iy+27,sw=165,amount=active?.75:mid?.62:.5;
    out+=rect(sx,sy-4,sw,8,scheme==='C'?ink:color.faint,edge);
    if(scheme==='B')for(let j=0;j<9;j++)out+=line(sx+j*20,sy-10,sx+j*20,sy+10,'#b2bfb1',1);
    if(scheme==='D')for(let j=0;j<8;j++)out+=rect(sx+j*21,sy-4,18,8,j/8<amount?ink:color.faint);
    else out+=rect(sx,sy-3,sw*amount,6,scheme==='C'?accent:ink);
    if(scheme==='C')out+=rect(sx+sw*amount-2,sy-12,4,24,color.white);
    else out+=rect(sx+sw*amount-10+shift,sy-13+shift,20,26,scheme==='A'?ink:accent,ink,2);
    out+=txt(sx+sw+11,sy+5,active?'75':'50',15,ink,800,'start','monospace');
  } else if (kind === 5) {
    const sx=ix+13,sy=iy+6,sw=178,sh=43;
    if(scheme==='D')out+=polygon(`${sx},${sy} ${sx+sw-9},${sy} ${sx+sw},${sy+9} ${sx+sw},${sy+sh} ${sx},${sy+sh}`,color.white,edge);
    else out+=rect(sx,sy,sw,sh,scheme==='C'?ink:color.white,edge,scheme==='B'?2:1);
    if(scheme==='D')out+=rect(sx+sw-13,sy+8,5,sh-16,accent);
    if(scheme==='C')out+=txt(sx+9,sy+14,'SHAPE / 01',8,accent,600,'start','monospace');
    out+=txt(sx+12,sy+29,'矩形',12,scheme==='C'?color.white:ink,700);
    out+=txt(sx+sw-17,sy+29,active?'⌃':'⌄',17,scheme==='C'?accent:ink,700,'middle');
    if(active||mid){out+=rect(sx+6,sy+sh+3,sw-12,17,scheme==='C'?ink:color.white,edge);out+=txt(sx+14,sy+sh+16,'矩形　　圆形　　菱形',9,scheme==='C'?color.white:ink,600);}
  } else if (kind === 6) {
    const sx=ix+8,sy=iy+8,sw=190,sh=46;
    out+=rect(sx,sy,sw,sh,scheme==='C'?ink:color.paper,edge,scheme==='B'?2:1);
    if(scheme==='B')out+=rect(sx+4,sy+sh,sw-4,3,'#a8b6a7');
    for(let j=0;j<4;j++){
      const bx=sx+4+j*45,bfill=j===(active?1:0)?scheme==='D'?accent:ink:'transparent';
      out+=rect(bx,sy+4,41,sh-8,bfill,scheme==='D'?'#9faa9d':'none');
      if(j===(active?1:0))out+=rect(bx+3,sy+sh-6,mid?18:35,3,accent);
      out+=txt(bx+20,sy+29,['↖','✎','↝','⋯'][j],19,j===(active?1:0)?scheme==='D'?ink:color.white:scheme==='C'?color.white:ink,600,'middle');
    }
  } else if (kind === 7) {
    const sx=ix+14,sy=iy+4,sw=180,sh=18;
    for(let j=0;j<3;j++){
      const selected=j===(active?1:0),by=sy+j*21;
      out+=rect(sx+(scheme==='B'&&selected?3:0),by,sw,sh,selected?scheme==='C'?ink:scheme==='D'?ink:color.faint:color.paper,selected?edge:'none');
      if(selected||mid&&j===1){
        if(scheme==='A')out+=rect(sx,by,4,sh,ink);
        if(scheme==='B')out+=rect(sx-5,by+4,8,10,accent);
        if(scheme==='C')out+=rect(sx,by,3,sh,accent);
        if(scheme==='D')out+=rect(sx+sw-8,by,8,sh,accent);
      }
      const label=['全部文档','收藏','模板'][j];
      out+=txt(sx+11,by+13,scheme==='C'?`${String(j).padStart(2,'0')} / ${label}`:label,10,selected&&(scheme==='C'||scheme==='D')?color.white:ink,700);
    }
  }
  if (disabled) out = `<g opacity=".46">${out}</g>`;
  if (pressed) {
    if (scheme==='A') out += rect(x+8,y+h-7,w-16,2,ink);
    if (scheme==='B') out += rect(x+w-29,y+7,17,4,ink);
    if (scheme==='C') out += txt(x+w-12,y+14,'ACK',9,ink,700,'end','monospace');
    if (scheme==='D') out += rect(x+w-18,y+h-19,8,8,accent,ink);
  }
  if (hover) out += rect(x+3,y+3,w-6,h-6,'none',ink,1);
  if (mid) out += txt(x+w-11,y+h-9,'50%',9,color.gray,600,'end','monospace');
  return out;
}

function comparison(){
  const W=1308,H=1337,left=177,cellW=273,rowH=149,top=113;
  let out=rect(0,0,W,66,color.ink)+rect(24,18,8,27,color.signal)+txt(45,42,'COMPONENT DESIGN / 4 DIRECTIONS',24,color.white,800)+txt(W-26,39,'END.FIELD-INSPIRED / ORIGINAL STUDY',10,'#b6c0b4',600,'end','monospace');
  out+=rect(0,66,W,47,'#d6dcd3')+txt(26,95,'08 COMPONENT FAMILIES',12,color.ink,700,'start','monospace');
  schemes.forEach(([letter,name],j)=>{const x=left+j*cellW;out+=rect(x,69,cellW-6,41,color.ink)+rect(x+8,78,22,22,color.signal)+txt(x+19,94,letter,16,color.ink,800,'middle')+txt(x+41,94,name,13,color.white,700);});
  kinds.forEach(([en,cn],i)=>{const y=top+i*rowH;out+=rect(0,y,W,rowH,i%2?'#edf0e9':'#f5f7f1');out+=rect(18,y+11,3,122,color.line)+txt(29,y+39,String(i+1).padStart(2,'0'),22,color.ink,800,'start','monospace')+txt(29,y+65,en,13,color.ink,700,'start','monospace')+txt(29,y+88,cn,13,color.gray,600);schemes.forEach(([letter],j)=>{const x=left+j*cellW;out+=rect(x,y+9,cellW-7,126,color.white,'#bec8bc');out+=txt(x+14,y+30,`${letter} / ACTIVE`,10,color.gray,700,'start','monospace');out+=component(i,letter,'Active',x+12,y+37,242,80);});});
  return svg(out,W,H);
}

function stateAtlas(){
  const W=1528,left=225,cellW=214,rowH=92,sectionH=452,top=116,H=top+kinds.length*sectionH+26;
  let out=rect(0,0,W,70,color.ink)+rect(24,19,8,30,color.signal)+txt(46,46,'STATE ATLAS / 08 × 04 × 06',27,color.white,800)+txt(W-23,43,'STATIC KEYFRAMES — NOT USER TEST DATA',11,'#c5cdc2',600,'end','monospace');
  states.forEach((state,j)=>{const x=left+j*cellW;out+=rect(x,74,cellW-5,36,color.ink)+txt(x+12,98,state.toUpperCase(),11,j===3?color.signal:color.white,700,'start','monospace');});
  kinds.forEach(([en,cn],i)=>{
    const sectionY=top+i*sectionH;
    out+=rect(0,sectionY,W,sectionH-6,i%2?'#e8ece4':'#f4f6ef');
    out+=rect(0,sectionY,W,43,'#d2dbd0')+rect(17,sectionY+10,5,22,color.signal)+txt(32,sectionY+29,`${String(i+1).padStart(2,'0')} / ${en} · ${cn}`,16,color.ink,800);
    schemes.forEach(([letter,name],k)=>{
      const y=sectionY+49+k*rowH;
      out+=rect(17,y,196,rowH-5,k%2?'#f5f7f1':'#fff','#bec9bb')+rect(28,y+14,31,31,color.ink)+txt(43.5,y+36,letter,21,color.signal,800,'middle')+txt(68,y+34,name,13,color.ink,700);
      states.forEach((state,j)=>{
        const x=left+j*cellW;
        out+=rect(x,y,cellW-5,rowH-5,color.white,'#c6cfc3');
        out+=component(i,letter,state,x+1,y+7,cellW-8,74);
      });
    });
  });
  return svg(out,W,H);
}

function motionSheet(){
  const W=1190,H=815,left=170,step=326,top=95,row=165;
  let out=rect(0,0,W,70,color.ink)+rect(23,20,8,29,color.signal)+txt(46,46,'MOTION BREAKDOWN / SWITCH',26,color.white,800)+txt(W-24,43,'0% → 50% → 100%',12,'#b9c4b7',700,'end','monospace');
  ['START / 0%','TRANSITION / 50%','END / 100%'].forEach((label,j)=>out+=txt(left+j*step+8,top+11,label,12,color.gray,700,'start','monospace'));
  schemes.forEach(([letter,name],i)=>{
    const y=top+28+i*row;
    out+=rect(0,y,W,row-7,i%2?'#e9eee5':'#f6f8f1')+rect(18,y+16,35,35,color.ink)+txt(35,y+41,letter,24,color.signal,800,'middle')+txt(65,y+39,name,17,color.ink,800);
    out+=txt(18,y+74,['边缘推进','轨道位移','三段点亮','模块交接'][i],11,color.gray,600);
    ['Default','Transition','Active'].forEach((state,j)=>{
      const x=left+j*step;
      out+=rect(x,y+19,244,86,color.white,'#bcc8bb')+component(0,letter,state,x+10,y+26,224,70);
      if(j<2){out+=line(x+251,y+62,x+step-19,y+62,color.ink,2)+polygon(`${x+step-19},${y+62} ${x+step-29},${y+57} ${x+step-29},${y+67}`,color.ink);}
    });
    out+=line(left,y+124,left+step*2+244,y+124,color.line,2)+circle(left,y+124,5,color.ink)+circle(left+step,y+124,5,color.gray)+circle(left+step*2+244,y+124,5,color.signal,color.ink);
    out+=txt(left,y+145,['140ms / ease-out','190ms / guided travel','120ms / steps(3,end)','210ms / layered settle'][i],11,color.gray,700,'start','monospace');
  });
  return svg(out,W,H);
}

writeFileSync(join(dir,'comparison.svg'),comparison(),'utf8');
writeFileSync(join(dir,'states.svg'),stateAtlas(),'utf8');
writeFileSync(join(dir,'motion.svg'),motionSheet(),'utf8');
console.log('Rendered comparison.svg, states.svg and motion.svg');
