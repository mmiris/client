# ubuntu 用户级安装规范

## 路径隔离原则：

二进制文件：统一放置在 `$HOME/.local/bin`。

Node.js 环境：统一放置在 `$HOME/.n`，通过 `N_PREFIX` 控制。

Python 环境：通过 uv 管理，工具本身位于 `$HOME/.local/bin`，Python 解释器由 uv 管理在 `$HOME/.local/share/uv`。

无 Root 原则：所有安装步骤严禁使用 sudo，确保权限始终属于当前执行用户。

环境变量收敛：所有路径修改应集中在 .bashrc 的末尾，并添加明确的注释块，便于后续自动化清理。

幂等性：脚本应支持重复执行而不报错、不重复添加环境变量。

# 自动化安装脚本(uv & n)

```bash
#!/bin/bash

# =================================================================
# Description: 用户级工具安装脚本 (uv, n, node)
# Author: Senior CI/CD Architect
# =================================================================

set -e

echo "开始安装用户级工具..."

# 1. 创建必要的目录
mkdir -p "$HOME/.local/bin"
mkdir -p "$HOME/.n"

# 2. 安装 uv (Python 管理工具)
if ! command -v uv &> /dev/null; then
    echo "正在安装 uv..."
    curl -LsSf https://astral.sh/uv/install.sh | sh
else
    echo "uv 已存在，跳过安装。"
fi

# 3. 安装 n (Node 版本管理工具)
export N_PREFIX="$HOME/.n"
export PATH="$N_PREFIX/bin:$PATH"

if ! command -v n &> /dev/null; then
    echo "正在安装 n 并部署 Node LTS..."
    curl -L https://raw.githubusercontent.com/tj/n/master/bin/n -o n_installer
    # 使用 bash 执行下载的 n 脚本，安装到 N_PREFIX
    bash n_installer lts
    rm n_installer
else
    echo "n 已存在，尝试更新 Node LTS..."
    n lts
fi

# 4. 配置环境变量 (写入 .bashrc)
CONFIG_MARKER="# [USER_TOOLS_ENVIRONMENT_CONFIG]"
if ! grep -q "$CONFIG_MARKER" "$HOME/.bashrc"; then
    echo "配置环境变量到 .bashrc..."
    {
        echo ""
        echo "$CONFIG_MARKER"
        echo "export N_PREFIX=\"\$HOME/.n\""
        echo "export PATH=\"\$HOME/.local/bin:\$N_PREFIX/bin:\$PATH\""
        echo "# [USER_TOOLS_ENVIRONMENT_END]"
    } >> "$HOME/.bashrc"
    echo "环境变量配置完成。"
else
    echo "环境变量已配置，跳过。"
fi

echo "----------------------------------------------------"
echo "安装完成！请执行: source ~/.bashrc 使配置生效。"
echo "验证命令: node -v, uv --version"
echo "----------------------------------------------------"
```


# 卸载/清理脚本

```bash
#!/bin/bash

# =================================================================
# Description: 用户级工具卸载脚本
# =================================================================

echo "开始卸载用户级工具..."

# 1. 定义要删除的目录
USER_DIRS=(
    "$HOME/.n"
    "$HOME/.local/share/uv"
    "$HOME/.local/bin/uv"
    "$HOME/.local/bin/uvx"
)

for dir in "${USER_DIRS[@]}"; do
    if [ -e "$dir" ]; then
        echo "正在删除: $dir"
        rm -rf "$dir"
    fi
done

# 2. 清理 .bashrc 中的环境变量配置块
if [ -f "$HOME/.bashrc" ]; then
    echo "正在清理 .bashrc 中的配置块..."
    # 使用 sed 删除标记块之间的内容
    sed -i '/# \[USER_TOOLS_ENVIRONMENT_CONFIG\]/,/# \[USER_TOOLS_ENVIRONMENT_END\]/d' "$HOME/.bashrc"
fi

# 3. 清理 uv 可能自行添加的路径 (可选)
sed -i '/\.local\/bin/d' "$HOME/.bashrc" 2>/dev/null || true

echo "----------------------------------------------------"
echo "卸载完成！"
echo "注意：由于 uv 可能会在 .bashrc 中添加额外的脚本，请手动检查并清理。"
echo "请重新打开终端或执行 source ~/.bashrc。"
echo "----------------------------------------------------"
```



# 用户级环境管理工具：uv & n 指令手册

> **设计理念**：
> - **uv**: 由 Rust 编写的 Python 极速工具链，旨在替代 pip, pip-tools, 和 pyenv。
> - **n**: 极其简便的 Node.js 版本管理器，通过控制文件软链接实现版本切换。

---

## 一、 uv (Python 管理工具)

`uv` 的核心优势在于“快”和“单文件二进制”。在 Tier 3 动态容器中，它是预研和构建阶段的神器。

### 1. Python 版本管理
- `uv python install 3.11` : 安装指定版本的 Python（安装在用户目录下）。
- `uv python list` : 查看系统可用的及 uv 已安装的 Python 版本。
- `uv python find` : 查找当前环境正在使用的 Python 路径。

### 2. 项目与虚拟环境
- `uv init my-project` : 初始化一个新项目。
- `uv venv` : 在当前目录创建 `.venv` 虚拟环境。
- `uv sync` : 根据 `pyproject.toml` 自动同步环境依赖。

### 3. 包管理 (pip 的极速替代品)
- `uv pip install <package>` : 安装 Python 包。
- `uv pip freeze` : 输出当前的依赖版本。
- `uv pip compile pyproject.toml -o requirements.txt` : 锁定依赖版本。

### 4. 临时工具执行
- `uvx <tool> [args]` : (类似 npx) 不安装工具，直接在临时环境运行 Python 工具。
  - *示例*: `uvx ruff check .` (运行代码检查)

---

## 二、 n (Node.js 版本管理)

`n` 的逻辑非常简单：下载 Node 二进制文件到指定目录，并将其软链接到 `bin` 路径。

### 1. 安装 Node
- `n lts` : 安装/切换至最新的 LTS 版本。
- `n stable` : 安装/切换至最新的稳定版。
- `n 18.16.0` : 安装/切换至具体版本。

### 2. 版本切换与交互
- `n` : 直接输入 `n` 不带参数，进入交互式界面，通过上下键切换已安装的版本。
- `n ls` : 列出本地已安装的所有 Node 版本。
- `n ls-remote` : 列出官方所有可安装的版本。

### 3. 环境清理
- `n rm 16.13.0` : 删除特定的已安装版本。
- `n prune` : 删除除当前正在使用的版本外，其他所有的本地缓存版本。

### 4. 指定版本运行
- `n run 14.18.1 app.js` : 使用特定版本运行脚本，而不切换系统全局版本。

---

## 三、 建议 (Best Practices)

1. **环境变量优先级**：
   在 `.bashrc` 中，务必确保 `PATH="$N_PREFIX/bin:$HOME/.local/bin:$PATH"`。这样你手动安装的版本会优先于 Ubuntu 系统自带的旧版本。

2. **Tier 3 优化**：
   在动态执行节点中，优先使用 `uv pip install` 而非 `pip install`。在处理大规模扫描工具的依赖（如 `pandas`, `numpy` 等重型库）时，`uv` 的速度提升通常在 5-10 倍左右。

3. **n 的权限检查**：
   因为我们使用的是 `N_PREFIX`（用户级安装），所以执行 `n` 命令时**永远不要加 sudo**。一旦误用 sudo，会导致 `$HOME/.n` 目录权限变为 root，引发后续脚本执行失败。