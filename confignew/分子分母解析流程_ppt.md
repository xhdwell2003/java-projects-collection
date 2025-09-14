---
marp: true
theme: uncover
---

<style>
h1, h2 {
  color: #005A9C; /* A nice blue color */
  text-shadow: 1px 1px 2px #fff;
}
img[alt~="icon"] {
    width: 128px;
    height: 128px;
    float: right;
    margin: 20px;
    opacity: 0.7;
}
</style>

![bg brightness:.4 blur:2px](https://source.unsplash.com/random/1920x1080?data,abstract,network)

# **分子分母粒度推断**
### *数据处理流程解析*

---

## 推断分子力度 - 概览

![alt icon](https://icongr.am/clarity/flow-chart.svg?size=128&color=005A9C)

- 找到输出列中的`GB`、`DF`列
- **无 GB 列**: 汇总处理
- **有 GB 列**: 维度/单只处理

---

## 场景一: 无 GB 列 (汇总)

- **判断**: 输出列中没有 `GB` 列
- **结论**: 汇总粒度
- **数据项**:
    - 分子: `sum:` 开头
    - 比例型分母: `mean:` 开头

---

## 场景二: 有 GB 列 (维度汇总)

- **前置**: 查维度字典
- **判断**: `GB` 列的每项都在维度列表内
- **结论**: 维度汇总
- **数据项**:
    - 分子: `df` 中 `sum:` 开头的
    - 分母: 汇总粒度, `mean:` 开头

---

## 场景三: 有 GB 列 (单只)

- **判断**: `GB` 列在单只字典中
- **结论**: 单只粒度
- **比例型**:
    - 分子: `df` 中 `sum:` 开头的
    - 分母: 汇总粒度, `mean:` 开头

---

## 场景三: 单只 - 非比例型

- **DF 只有一项**:
    - `sum:` 开头 -> **数值型**
    - `head:` 开头 -> **禁投型**
- **DF 有两项**:
    - 输出结果间比较
    - 第一个为 `head:`
    - 第二个为过滤后剩下的 `head:`

---

## 场景三: 单只 - 非比例型 (续)

- **DF 有三项 (日期区间)**:
    - 来源于 `interval:` 开头的字段
    - **格式**: `A#B`
    - **输出**:
        - A (`head:`)
        - B (`head:`)

---

## 场景四: 分子分母相同度量

- **比例型 (特殊)**:
    - `index` 度量树无 `out`
    - 输出项以 "持仓数量/" 或 "持仓市值/" 开头
    - **-> 分子分母条件相同**
- **普通情况**:
    - 根据 `filter_expr.expr` 中的 `out` 拆分
    - **-> 前面为分子条件, 后面为分母条件**

---

## 总结

![alt icon](https://icongr.am/clarity/check-list.svg?size=128&color=005A9C)

1.  **识别 `GB` 列** 是判断粒度的关键.
2.  **无 `GB`** -> 汇总.
3.  **有 `GB`** -> 查字典, 判断是 **维度汇总** 还是 **单只**.
4.  根据 **比例/非比例** 及 **DF项数** 确定具体取数逻辑.
5.  **相同度量** 场景有特殊处理逻辑.
