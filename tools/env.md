# Ubuntu 开发环境配置手册 (用户级与离线安装)

本教程涵盖了在没有 sudo 权限或离线环境下，如何在 Ubuntu 上配置 Python 开发工具链。

## 1. 仅为当前用户安装软件包

在不污染系统全局环境的前提下，可以通过以下两种方式安装工具。

### A. 安装 Python 工具包 (pip)
使用 `--user` 参数，包会被安装到 `~/.local/lib`，其对应的可执行命令会放在 `~/.local/bin`。
```bash
python3 -m pip install --user <package_name>
```

### B. 手动提取系统级安装包 (apt/deb)

如果你需要某个 .deb 包里的二进制文件但没有 sudo 权限：

```bash
# 1. 下载 deb 包到当前目录
apt-get download <package_name>

# 2. 解压到指定目录
mkdir -p ~/my_local_tools
dpkg -x <package_name>*.deb ~/my_local_tools

# 3. 将二进制路径加入 PATH（建议写入 ~/.bashrc）
export PATH="$HOME/my_local_tools/usr/bin:$PATH"
```

2. 离线安装 uv
uv 是单二进制文件，非常适合离线部署。

步骤 A：准备文件（有网环境）

从 uv GitHub Releases 下载适合 Linux 的压缩包，例如：uv-x86_64-unknown-linux-gnu.tar.gz。

步骤 B：执行安装（目标离线机器）

将文件拷贝到 Ubuntu 后，执行以下操作：

```bash
# 1. 解压
tar -xzf uv-x86_64-unknown-linux-gnu.tar.gz

# 2. 移动到用户二进制目录
mkdir -p ~/.local/bin
cp uv-x86_64-unknown-linux-gnu/uv* ~/.local/bin/

# 3. 赋予执行权限并配置环境变量
chmod +x ~/.local/bin/uv*
echo 'export PATH="$HOME/.local/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc
3. 安装后指定 Python 下载源 (uv)
当执行 uv python install 时，uv 默认访问 GitHub。你可以将其重定向到国内镜像源（如清华源）。
```

方法：修改配置文件

创建或编辑 ~/.config/uv/uv.toml，添加以下内容：
```toml
Ini, TOML
[tool.uv]
# 指定 Python 解释器的下载镜像
python-install-mirror = "[https://mirrors.tuna.tsinghua.edu.cn/python-release/](https://mirrors.tuna.tsinghua.edu.cn/python-release/)"
```
注：也可以设置环境变量 export UV_PYTHON_INSTALL_MIRROR=...

4. 指定 pip / PyPI 镜像源
为了加快包的下载速度，需要配置 PyPI 镜像。

A. 配置 uv 的下载源

继续编辑 ~/.config/uv/uv.toml，添加索引配置：
```toml
Ini, TOML
[[tool.uv.index]]
url = "[https://pypi.tuna.tsinghua.edu.cn/simple](https://pypi.tuna.tsinghua.edu.cn/simple)"
default = true
```
B. 配置传统 pip 的下载源

如果你偶尔还需要用到原生 pip，建议配置 ~/.pip/pip.conf：
```toml
Ini, TOML
[global]
index-url = [https://pypi.tuna.tsinghua.edu.cn/simple](https://pypi.tuna.tsinghua.edu.cn/simple)
trusted-host = pypi.tuna.tsinghua.edu.cn
```
5. 验证配置
执行以下命令确认一切正常：

```shell
uv --version           # 检查 uv 是否可用
uv python list         # 检查是否能看到 Python 版本列表（走镜像源）
uv pip install requests # 检查安装速度（走 PyPI 镜像源）
```