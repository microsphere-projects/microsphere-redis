# Microsphere Redis 文档

> 英文为默认语言，中文译文存放于平行的 `zh/` 目录，文件名与英文文档完全一致。

Microsphere Redis 是一个增强的 Redis 集成框架，在 Spring Data Redis 之上提供了非侵入式命令拦截、事件发布、动态连接工厂以及基于 Kafka 的 Redis 复制能力。

本文档集覆盖项目的**两条发布线**：

| **文档目录** | **Git 分支** | **版本线** | **Spring Data Redis 兼容范围** | **最新发布版本** |
|--------------|--------------|------------|-------------------------------|------------------|
| `current/`   | `release`    | 0.2.x      | 3.0.0 – 3.5.x（Spring Boot 3.x，Java 17+） | `0.2.14` |
| `1.x/`       | `release-1.x`| 0.1.x      | 2.1.0 – 2.7.x（Spring Boot 2.x，Java 8+）  | `0.1.14` |

## 文档结构

```
docs/
├── README.md                    # 英文引导页（默认语言）
├── zh/README.md                 # 本文件
├── user-guide/                  # 用户文档（英文，默认语言）
│   ├── README.md                # 用户文档索引
│   ├── zh/README.md             # 用户文档索引（中文）
│   ├── current/                 # `release` 分支（0.2.x）用户文档
│   │   ├── microsphere-redis-parent.md
│   │   ├── microsphere-redis-dependencies.md
│   │   ├── microsphere-redis-core.md
│   │   ├── microsphere-redis-generator.md
│   │   ├── microsphere-redis-spring.md
│   │   ├── microsphere-redis-spring-test.md
│   │   ├── microsphere-redis-replicator-spring.md
│   │   ├── microsphere-redis-spring-boot.md
│   │   ├── microsphere-redis-spring-cloud.md
│   │   └── zh/                  # 同名中文文档
│   └── 1.x/                     # `release-1.x` 分支（0.1.x）用户文档
│       └── ...                  # 同样的模块文档，含 zh/ 目录
└── developer-guide/             # 开发者文档（英文，默认语言）
    ├── README.md                # 开发者文档索引
    ├── zh/README.md             # 开发者文档索引（中文）
    ├── current/                 # `release` 分支开发者文档
    │   └── ...                  # 同样的模块文档，含 zh/ 目录
    └── 1.x/                     # `release-1.x` 分支开发者文档
        └── ...
```

## 我应该读哪份文档？

### [用户文档](../user-guide/zh/README.md)

面向把 Microsphere Redis 当作依赖库使用的**应用开发者**。按 Maven 模块组织，包含：

- API 解释说明（类、注解、事件、拦截器、序列化器）
- 可运行的代码示例（取材于项目 test-cases）
- 版本信息与版本兼容范围

### [开发者文档](../developer-guide/zh/README.md)

面向研究或扩展框架的**贡献者与维护者**。按 Maven 模块组织，包含：

- 架构设计与包级源码分析
- 实现中使用的设计模式
- 复杂流程的 Mermaid 流程图（命令拦截、事件发布、复制管线等）
- 模块间依赖关系的 Mermaid 流程图

## Maven 模块总览

| **模块**                                | **层次**   | **用途** |
|-----------------------------------------|------------|----------|
| **microsphere-redis-parent**            | 构建       | 父 POM、BOM 版本管理、Spring Cloud 版本 profile |
| **microsphere-redis-dependencies**      | 构建       | 全项目统一的 Bill of Materials |
| **microsphere-redis-core**              | 基础层     | Redis 命令元数据模型与工具类（不依赖 Spring） |
| **microsphere-redis-generator**         | 工具       | 基于 Javadoc Doclet 生成元数据 YAML |
| **microsphere-redis-spring**            | 核心集成   | 命令拦截、事件、模板包装器、序列化器 |
| **microsphere-redis-spring-test**       | 测试支持   | 带真实 Redis 连接的测试基类 |
| **microsphere-redis-replicator-spring** | 数据复制   | 基于 Kafka 的 Redis 命令复制 |
| **microsphere-redis-spring-boot**       | Boot 集成  | `@ConditionalOnRedis*` 注解与自动配置 |
| **microsphere-redis-spring-cloud**      | Cloud 集成 | 配置属性变更向 Redis 配置的传播 |

## 许可证

Apache License 2.0。
