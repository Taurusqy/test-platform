-- ============================================================
-- 测试用例&缺陷管理平台 数据库建表脚本
-- 数据库：SQLite
-- 表关系：项目(1) -> 需求(N) -> 测试用例(N) -> Bug(N)
-- ============================================================

-- 1. 项目表：最顶层实体，一个项目包含多个测试需求
CREATE TABLE IF NOT EXISTS project (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,  -- 项目ID，自增主键
    name        TEXT    NOT NULL,                   -- 项目名称
    description TEXT,                               -- 项目描述
    status      TEXT    DEFAULT '进行中',           -- 项目状态：进行中/已完成/已暂停
    start_date  TEXT,                               -- 项目开始时间
    end_date    TEXT,                               -- 项目结束时间
    create_time TEXT    DEFAULT (datetime('now', 'localtime')),  -- 创建时间
    update_time TEXT    DEFAULT (datetime('now', 'localtime'))   -- 更新时间
);

-- 2. 测试需求表：归属某个项目，一个需求对应多个测试用例
CREATE TABLE IF NOT EXISTS test_requirement (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,  -- 需求ID
    req_no      TEXT,                               -- 需求编号，如 REQ-001
    project_id  INTEGER NOT NULL,                   -- 所属项目ID（外键关联 project.id）
    title       TEXT    NOT NULL,                   -- 需求标题
    content     TEXT,                               -- 需求详细内容
    priority    TEXT    DEFAULT '中',               -- 优先级：高/中/低
    status      TEXT    DEFAULT '待分析',           -- 需求状态：待分析/分析中/已实现
    create_time TEXT    DEFAULT (datetime('now', 'localtime')),
    update_time TEXT    DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);

-- 3. 测试用例表：归属某个需求，执行后可产生Bug
CREATE TABLE IF NOT EXISTS test_case (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,  -- 用例ID
    case_no         TEXT,                               -- 用例编号，如 TC-001
    requirement_id  INTEGER NOT NULL,                   -- 所属需求ID（外键关联 test_requirement.id）
    module          TEXT,                               -- 所属模块
    title           TEXT    NOT NULL,                   -- 用例标题
    test_purpose    TEXT,                               -- 测试目的
    precondition    TEXT,                               -- 前置条件
    steps           TEXT,                               -- 测试步骤（多行文本）
    expected_result TEXT,                               -- 预期结果
    priority        TEXT    DEFAULT 'P1',               -- 优先级：P0(冒烟)/P1(核心)/P2(重要)/P3(次要)
    case_type       TEXT    DEFAULT '功能测试',         -- 用例类型：功能测试/性能测试/接口测试/安全测试/兼容性测试/UI测试
    status          TEXT    DEFAULT '未执行',           -- 执行状态：未执行/通过/失败/阻塞
    actual_result   TEXT,                               -- 实际结果（执行时填写）
    attachment      TEXT,                               -- 附件（截图等，存相对路径）
    remark          TEXT,                               -- 备注
    executor        TEXT,                               -- 执行人
    execute_time    TEXT,                               -- 执行时间
    create_time     TEXT    DEFAULT (datetime('now', 'localtime')),
    update_time     TEXT    DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (requirement_id) REFERENCES test_requirement(id) ON DELETE CASCADE
);

-- 4. Bug缺陷表：通常由失败的测试用例产生，可关联到具体用例
CREATE TABLE IF NOT EXISTS bug (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,  -- BugID
    bug_no          TEXT,                               -- Bug编号，如 BUG-001
    case_id         INTEGER,                            -- 关联的测试用例ID（可为空，允许直接提Bug）
    project_id      INTEGER NOT NULL,                   -- 所属项目ID（冗余字段，方便按项目查Bug）
    module          TEXT,                               -- 所属模块
    title           TEXT    NOT NULL,                   -- Bug标题
    description     TEXT,                               -- Bug详细描述
    reproduce_steps TEXT,                               -- 重现步骤
    expected_result TEXT,                               -- 预期结果
    actual_result   TEXT,                               -- 实际结果
    environment     TEXT,                               -- 测试环境，如 Chrome120/Win11/V1.2
    affected_version TEXT,                             -- 影响版本，如 V1.0、V1.2
    bug_type        TEXT,                               -- Bug类型：代码错误/界面优化/配置问题/安全问题/性能问题/其他
    attachment      TEXT,                               -- 附件截图
    severity        TEXT    DEFAULT '一般',             -- 严重程度：致命/严重/一般/轻微/建议
    bug_priority    TEXT    DEFAULT 'P2',               -- Bug优先级：P0(立即)/P1(本版本)/P2(下版本)/P3(有空再改)
    status          TEXT    DEFAULT '新建',             -- Bug状态：新建/待修复/已修复/已验证/关闭
    reporter        TEXT,                               -- 提交人
    assignee        TEXT,                               -- 修复人
    create_time     TEXT    DEFAULT (datetime('now', 'localtime')),
    update_time     TEXT    DEFAULT (datetime('now', 'localtime')),
    FOREIGN KEY (case_id) REFERENCES test_case(id) ON DELETE SET NULL,
    FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);

-- 示例数据由 DataInitializer.java 在启动时通过 Java 代码插入
-- （避免 SQL 文件在 Windows 下的编码问题导致中文乱码）
