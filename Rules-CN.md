# Carpet CRS Addition - 规则文档

## 规则列表

### 使用1.21.2弹射物逻辑（UseV1212ProjectileLogic）

将1.21.2的弹射物工作逻辑移植到1.21.1

- 类型: `boolean`
- 默认值: `false`
- 参考选项: `false`, `true`
- 分类: `CRS`, `移植`

---

### 末影珍珠可加载区块（PearlCanLoadingChunks）

末影珍珠可以像1.21.2+那样加载区块

- 类型: `boolean`
- 默认值: `false`
- 参考选项: `false`, `true`
- 分类: `CRS`, `移植`

---

### 末影龙每次掉落满经验（DragonAlwaysDropsMaxExperience）

末影龙每次死亡都像首次击杀一样掉落12000经验

- 类型: `boolean`
- 默认值: `false`
- 参考选项: `false`, `true`
- 分类: `CRS`, `创造`

---

### 经验吸收速度无上限（RemoveExperienceCooldown）

移除经验球吸收冷却，使玩家可以瞬间吸收所有经验球

- 类型: `boolean`
- 默认值: `false`
- 参考选项: `false`, `true`
- 分类: `CRS`, `创造`, `实验性`

---

### 使用1.21.6烟花逻辑（UseV1212FireworkLogic）

将1.21.6的烟花工作逻辑移植到1.21.1

- 类型: `boolean`
- 默认值: `false`
- 参考选项: `false`, `true`
- 分类: `CRS`, `移植`

---

### 优化船（optimizedBoat）

优化船的行为使得船在3秒静止后进入半休眠状态,半休眠状态时船只执行怪物的上下船检测,当玩家上船时船则恢复完整行为

- 类型: `boolean`
- 默认值: `false`
- 参考选项: `false`, `true`
- 分类: `CRS`, `优化`, `实验性`

---

### 优化船限制（optimizedBoatConstraints）

当 256x512 区域内船数量超过该值时启用半休眠优化，0为无区域限制

- 类型: `int`
- 默认值: `1500`
- 参考选项: `1500`, `2000`, `2500`, `3000`
- 分类: `CRS`, `优化`, `实验性`

---

### 物品高亮（ItemHighlight）

使得物品高亮

- 类型: `boolean`
- 默认值: `false`
- 参考选项: `false`, `true`
- 分类: `CRS`, `创造`