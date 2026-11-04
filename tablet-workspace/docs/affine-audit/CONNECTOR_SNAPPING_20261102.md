# 画布连线连续边缘吸附算法与实现（2026-11-02）

最终规则：

1. 连线终点可以吸附到矩形四条边上的**任意位置**（全边缘连续吸附），不是只有四个角或四条边中点。
2. 四条边的中点仍是更强的磁吸锚点：进入 24px 范围优先固定到锚点，并显示较大的实心蓝点与光晕。
3. 普通边缘点进入 30px 范围时投影到该边上的最近点，显示实心蓝点与较小光晕；不做折角，保持原本的曲线样式。
4. 所有连线绘制在文本框、卡片之下，多线重合时不会出现白色断线。

## 1. 坐标与阈值

- 世界坐标：`world = (screen - pan) / scale`
- 屏幕像素转世界单位：`px / (density * scale)`
- 锚点阈值：`24px / (density * scale)`
- 连续边缘吸附阈值：`30px / (density * scale)`
- 锚点：左、右、上、下边中点，共 4 个。
- 优先级：锚点强磁吸 > 连续边缘吸附 > 自由落点。

## 2. 点到矩形边缘的最近点

矩形 `left/top/right/bottom`，点 `p`：

- 点在矩形外：把坐标夹紧到矩形范围，夹紧点就是边界最近点。
- 点在矩形内：比较到四条边的垂直距离，取最小者，把对应坐标替换成边坐标。
- 返回最近点、所在边和距离。连续边缘吸附就是把终点设为这个最近点。

```ts
type Vec2 = { x: number; y: number };
type Edge = 'left' | 'right' | 'top' | 'bottom';

const clamp = (v: number, lo: number, hi: number) => Math.max(lo, Math.min(hi, v));

function closestPointOnRectEdge(p: Vec2, r: { x: number; y: number; width: number; height: number }) {
  const left = r.x, right = r.x + r.width, top = r.y, bottom = r.y + r.height;
  const cx = clamp(p.x, left, right), cy = clamp(p.y, top, bottom);
  const outside = p.x < left || p.x > right || p.y < top || p.y > bottom;

  if (outside) {
    const edge: Edge = p.x < left ? 'left' : p.x > right ? 'right' : p.y < top ? 'top' : 'bottom';
    return { point: { x: cx, y: cy }, edge, distance: Math.hypot(p.x - cx, p.y - cy) };
  }

  const d = { left: p.x - left, right: right - p.x, top: p.y - top, bottom: bottom - p.y };
  const edge = (Object.keys(d) as Edge[]).reduce((a, b) => (d[a] <= d[b] ? a : b));
  const point =
    edge === 'left' ? { x: left, y: p.y } :
    edge === 'right' ? { x: right, y: p.y } :
    edge === 'top' ? { x: p.x, y: top } :
    { x: p.x, y: bottom };
  return { point, edge, distance: d[edge] };
}
```

## 3. 四个锚点

```ts
function anchorsOf(r): { port: string; point: Vec2 }[] {
  const cx = r.x + r.width / 2, cy = r.y + r.height / 2;
  return [
    { port: 'left',   point: { x: r.x,           y: cy } },
    { port: 'right',  point: { x: r.x + r.width, y: cy } },
    { port: 'top',    point: { x: cx,            y: r.y } },
    { port: 'bottom', point: { x: cx,            y: r.y + r.height } },
  ];
}
```

## 4. 连续边缘吸附判定

```ts
type Snap =
  | { kind: 'anchor'; nodeId: string; port: string; point: Vec2; distance: number }
  | { kind: 'edge'; nodeId: string; edge: Edge; point: Vec2; distance: number }
  | { kind: 'none'; point: Vec2; distance: number };

function snapEndpoint(
  pointer: Vec2,
  nodes: RectNode[],
  sourceId: string,
  anchorThreshold: number,
  edgeThreshold: number
): Snap {
  let anchor: Snap | null = null;
  let edge: Snap | null = null;

  for (const node of nodes) {
    if (node.id === sourceId) continue;

    for (const port of anchorsOf(node)) {
      const distance = Math.hypot(pointer.x - port.point.x, pointer.y - port.point.y);
      if (distance <= anchorThreshold && distance < (anchor?.distance ?? Infinity)) {
        anchor = { kind: 'anchor', nodeId: node.id, port: port.port, point: port.point, distance };
      }
    }

    const e = closestPointOnRectEdge(pointer, node);
    if (e.distance <= edgeThreshold && e.distance < (edge?.distance ?? Infinity)) {
      edge = { kind: 'edge', nodeId: node.id, edge: e.edge, point: e.point, distance: e.distance };
    }
  }

  return anchor ?? edge ?? { kind: 'none', point: pointer, distance: Infinity };
}
```

调用：

```ts
const scale = stage.scaleX();
const dpr = window.devicePixelRatio || 1;
const snap = snapEndpoint(worldPointer, nodes, source.id(), 24 / (scale * dpr), 30 / (scale * dpr));
```

- 边缘吸附是连续的：任意边点都可以成为 `point`，不是离散锚点。
- 锚点阈值更小且优先，保证边中点附近仍会吸附到中点。

## 5. 连线路径：保持原本曲线

- 锚点、普通边点、自由落点都使用当前路径样式（默认贝塞尔曲线），不强制折角。
- 贝塞尔控制点沿起点端口方向外扩 `d = clamp(distance * 0.4, 32, 400)`；终点方向由锚点端口或边方向决定。
- 预览使用同一路径样式绘制虚线，所见即所得。

```ts
function sampleBezier(a: Vec2, b: Vec2, startPort: string, endPort: string, steps = 40): Vec2[] {
  const sign = b.x >= a.x ? 1 : -1;
  const d = Math.min(400, Math.max(32, Math.hypot(b.x - a.x, b.y - a.y) * 0.4));
  const dir = (port: string, fallback: number): Vec2 =>
    port === 'left' ? { x: -1, y: 0 } : port === 'right' ? { x: 1, y: 0 } :
    port === 'top' ? { x: 0, y: -1 } : port === 'bottom' ? { x: 0, y: 1 } :
    { x: fallback, y: 0 };
  const s = dir(startPort, sign), e = dir(endPort, -sign);
  const c1 = { x: a.x + s.x * d, y: a.y + s.y * d };
  const c2 = { x: b.x + e.x * d, y: b.y + e.y * d };
  return Array.from({ length: steps + 1 }, (_, i) => {
    const u = i / steps, v = 1 - u;
    return {
      x: v*v*v*a.x + 3*v*v*u*c1.x + 3*v*u*u*c2.x + u*u*u*b.x,
      y: v*v*v*a.y + 3*v*v*u*c1.y + 3*v*u*u*c2.y + u*u*u*b.y,
    };
  });
}
```

## 6. Konva.js 交互示例

```ts
const connectorLayer = new Konva.Layer(); // 先加入，保证在卡片下方
const nodeLayer = new Konva.Layer();
stage.add(connectorLayer, nodeLayer);

let draft: Konva.Line | null = null;
let source: Konva.Rect | null = null;
let sourcePort = 'center';
let indicator: Konva.Circle;

stage.on('pointerdown', (e) => {
  const world = screenToWorld(e.evt);
  source = findNode(world);
  if (!source) return;
  sourcePort = nearestPort(source, world);
  draft = new Konva.Line({ points: [world.x, world.y, world.x, world.y], dash: [8, 6], stroke: '#4F91E8', strokeWidth: 2 });
  connectorLayer.add(draft);
  indicator = new Konva.Circle({ radius: 7, fill: '#4F91E8', stroke: '#fff', strokeWidth: 2, visible: false });
  connectorLayer.add(indicator);
});

stage.on('pointermove', (e) => {
  if (!draft || !source) return;
  const world = screenToWorld(e.evt);
  const scale = stage.scaleX(), dpr = devicePixelRatio || 1;
  const snap = snapEndpoint(world, nodes.map(toRectNode), source.id(), 24 / (scale * dpr), 30 / (scale * dpr));
  const endPort = snap.kind === 'anchor' ? snap.port : snap.kind === 'edge' ? snap.edge : 'center';
  const points = sampleBezier(portPoint(source, sourcePort), snap.point, sourcePort, endPort);
  draft.points(points.flatMap((p) => [p.x, p.y]));
  indicator.position(snap.point).visible(snap.kind !== 'none');
  layer.batchDraw();
});

stage.on('pointerup', () => {
  draft?.destroy(); indicator?.destroy();
  draft = null;
});
```

## 7. 指示器

| 状态 | 终点 | 指示器 |
|---|---|---|
| 四边中点锚点（24px） | 锚点坐标 | 实心蓝点 + 白圈 + 较大光晕 |
| 任意边缘点（30px） | 该边上的最近投影点 | 实心蓝点 + 白圈 + 较小光晕 |
| 自由落点 | 指针坐标 | 空心蓝圈 |

## 8. 多线重合时不再出现白色断线

问题原因：每条连线原先按“白色描边 → 彩色线”依次绘制。后一条连线的白色描边会覆盖前一条已经画好的彩色线，重合位置就显示成白色/断线。

修复：

- 拆成两遍绘制：
  1. 先为所有连线绘制选中光晕和白色描边；
  2. 再为所有连线绘制彩色线、端点标记、标签和动画。
- 这样任何连线的白边都不可能盖住其他连线的彩色线；重合位置仍显示连线颜色。
- 代码：`drawConnectorUnderlay`（白边/光晕）与 `drawConnectorContent`（彩线/标记/标签），先遍历所有 connector 画 underlay，再统一画 content。

## 9. 不遮挡文本框和卡片

- 绘制顺序：Frame 背景 → 全部连线（白边两遍 + 彩线）→ 文本框/卡片/图片/笔迹 → 选择框、端口、预览、蓝点。
- `hitTest` 延迟连线命中：卡片/文本覆盖区域优先响应卡片。
- 被卡片遮挡的连线段不可见、不可点选。

## 10. 本应用实现映射

- `ConnectorSnapping.kt`：4 锚点、连续边缘最近点、24px 锚点优先级、30px 边缘阈值。
- `ConnectorInteraction.kt`：`ConnectorDraft` 携带 `snapKind/snapEdge`。
- `MainActivity.kt`：
  - **新建连线**拖拽移动调用 `ConnectorSnapping.snap(...)`，阈值 `24f/(density*scale)`、`30f/(density*scale)`。
  - **已有连线端点拖动**也调用同一个 `snap(...)`：拖动中实时把端点预览更新为吸附点，松手时锚点写入形状端口，普通边点写入精确坐标和 `endPort`。
  - **普通边点跟随形状移动**：`Page.moveSelections` 在平移形状前，收集落在该形状边界 2 世界单位内的自由端点；形状平移后，这些端点按相同位移一起移动。这样不改变页面格式 25，也能让任意边点在移动矩形时跟随。
  - 锚点吸附绑定目标端口；边缘吸附投影到任意边点并写 `endPort`，不写 `endShape`。
  - 所有落点保持 `settings.connectorPathStyle`，不强制 step。
  - 连线两遍绘制，避免白边互相覆盖。
- 绘制顺序与命中测试见第 8、9 节。

## 11. 验证

- `cs-continuous.png`：指针在上边元素上边缘外 25px，终点连续吸附到上边缘任意点（非中点），显示实心蓝点。
- `cs-edge-any.png` / `cs-edge-any-end.png`：任意边点蓝点与原本曲线连线，无折角。
- `cs-blue4.png`：四个边中点锚点的大光晕蓝点。
- `overlap-1.png` / `overlap-1-crop.png`：多条连线重合处保持灰色，无白色断线。
- `occl-1.png`：连线被卡片遮挡，只在卡片外侧显示。
- `cs-final3.png`：最终画布状态。
- `ep-3.png` / `ep-3-end.png`：已有连线的端点被拖到空白处，端点跟随指针移动；端点拖动与新建共用同一吸附函数。
- `follow-2.png` / `follow-3.png`：自由端点落在卡片边上后，移动卡片，端点随卡片平移；`follow-4.png` 为撤销移动后的恢复状态。

## 12. 限制

1. 普通边点跟随是运行时坐标推断：移动形状时，端点若落在该形状边界 2 世界单位内，就按相同位移跟随。它是**平移跟随**，形状缩放、旋转时不会按比例重新定位。
2. 由于任意边位置不写入持久字段，重启后仍通过“端点是否在形状边界上”的坐标判断继续跟随；如果端点被手动移到离边较远的位置，就不再跟随。
3. 图片支持连续边缘吸附，但没有锚点。
4. 连线被卡片遮挡的段不可见、不可点选；未实现自动绕障。
5. 阈值按屏幕像素计算，缩放或密度变化时换算为世界单位。
6. 验证在测试页 `未命名页` 上新增了测试连线并移动/撤销了卡片；页面文件数仍为 193。

## 13. 数据与发布

- 页面格式 25；193 页、18 历史、活动文档 193、回收站 0。
- 359 项 JVM 测试 0 失败；`ConnectorSnappingTest` 14 项、`ConnectorFollowTest` 6 项。
- 发布：`releases/lingye-v0.15-connector-edge-follow-release-20261102.apk`，2,643,649 字节，SHA-256 `2251E11442C24E7CC1AD79855C9122ADD88F7464E66E7B6BCB97ECB1410F2B60`。
