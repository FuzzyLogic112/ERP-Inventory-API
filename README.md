# ERP-Inventory-API

一个极简的库存管理 REST API：查询库存、商品入库（进货）、商品出库（发货）。基于 Spring Boot 3 + Spring Data JPA + H2 内存数据库，全部逻辑在一个 Java 文件里，适合用来入门 Spring Boot 的“实体 → 仓库 → 控制器”三层结构。

## 技术栈

| 组件 | 版本 / 说明 |
| --- | --- |
| Java | 17 |
| Spring Boot | 3.2.0（`spring-boot-starter-web`、`spring-boot-starter-data-jpa`） |
| 数据库 | H2 内存库（运行时依赖，无需安装） |
| 构建 | Maven |

## 快速开始

```bash
mvn spring-boot:run
```

启动后服务监听 `http://localhost:8080`。

## 接口

所有接口都在 `/api/inventory` 下，已开启跨域（`@CrossOrigin("*")`），前端页面可直接调用。

| 方法 | 路径 | 参数 | 说明 |
| --- | --- | --- | --- |
| GET | `/api/inventory/list` | — | 返回全部商品及库存 |
| POST | `/api/inventory/inbound` | `name`、`count` | 入库：新商品自动创建，老商品累加库存；`count` 必须大于 0 |
| POST | `/api/inventory/outbound` | `name`、`count` | 出库：商品不存在或库存不足时拒绝 |

### 示例

```bash
# 入库 100 件螺丝
curl -X POST "http://localhost:8080/api/inventory/inbound?name=螺丝&count=100"
# ✅ 新商品 [螺丝] 入库成功，当前库存：100

# 出库 30 件
curl -X POST "http://localhost:8080/api/inventory/outbound?name=螺丝&count=30"
# 📦 [螺丝] 成功发货 30 件，剩余库存：70

# 查看库存
curl http://localhost:8080/api/inventory/list
# [{"id":1,"name":"螺丝","stock":70}]
```

## 代码结构

`src/main/java/com/erp/InventoryApp.java` 一个文件包含四部分：

- `InventoryApp`：Spring Boot 启动类
- `Product`：JPA 实体（`id`、`name`、`stock`）
- `ProductRepo`：继承 `JpaRepository`，额外提供 `findByName`
- `InventoryController`：三个 REST 接口

## 注意

- H2 是内存数据库，**服务重启后数据清空**。需要持久化时可在 `application.properties` 里改用文件模式或换成 MySQL / PostgreSQL
- 出库接口目前没有校验 `count > 0`，传入负数会让库存增加，正式使用前建议补上校验
