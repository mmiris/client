# 服务端

## 安装

```bash
# Ubuntu/Debian
sudo apt update && sudo apt install samba -y
```

## 配置

配置文件：`/etc/samba/smb.conf`

```bash
[share_name]
    # 实际硬盘路径
    path = /data/space 
    # 允许写入
    read only = no
    # 允许在网络中被搜到
    browsable = yes
    # 不允许匿名访问
    guest ok = no

# 公网 ip 会对 445 端口进行拦截，改用其它端口可以解决。客户端连接时使用 -p|--port 参数指定端口
[global]
   smb ports = 0000000000000000000000000000000000000
```
> 注意：注释可能会导致语法错误，注意删除
> /root 目录作为共享目录可能无权限访问


## 重启

```bash
sudo systemctl restart smbd
sudo systemctl restart nmbd
```


## 创建 SMB 用户

SMB 的用户必须首先是系统的 Linux 用户，但它有一套独立的密码。

```bash
# 1. 创建一个系统用户（如果还没创建）
sudo useradd -M -s /sbin/nologin smbuser

# 2. 设置该用户的 SMB 密码
sudo smbpasswd -a smbuser

# 3. 给文件夹权限，让这个用户能写进去
sudo chown -R smbuser:smbuser /data/space
```


# 客户端

## 安装

```shell
sudo apt install smbclient -y
sudo apt install smbclient cifs-utils -y
```

## 检查服务端共享了哪些目录
```bash
smbclient -L //192.168.1.10 -U your_username
```


# Samba 增删改查 (CRUD) 指令详述

在 Tier 3 脚本中，我们通常使用 smbclient ... -c "指令" 的非交互模式。以下是各个操作的详细解析：

1. 增 (Create)：上传与创建
```bash
put <local_name> [remote_name]
```
用途：将本地文件上传到服务器。
参数：第一个参数是本地路径，第二个是远程保存的文件名（可选，不填则同名）。
自动化技巧：在 Tier 3 中，我们配合 cwd 参数使用，直接写文件名即可。

```bash
mkdir <directory_name>
```
用途：在远程共享目录中创建新文件夹。
> 注意：如果文件夹已存在，命令会报错，自动化脚本中常用 cd folder || mkdir folder 来规避。

2. 删 (Delete)：清理与移除
```bash
del <file_mask>
```
用途：删除指定的远程文件。支持通配符（如 del *.old）。

```bash
rmdir <directory_name>
```
用途：删除远程空目录。

```bash
mdel <mask/pattern>
```
用途：批量删除符合模式的所有文件。在自动化清理过期 JSON 时非常有效。

3. 改 (Update/Rename)：重命名
```bash
rename <old_name> <new_name>
```
用途：修改远程文件名或移动文件。
参数：必须提供完整的原路径和新路径。
限制：SMB 不支持直接在服务器上“编辑”文本，通常的“改”操作是先本地修改再 put 覆盖。

4. 查 (Read)：列出与下载
```bash
ls [mask]
```
用途：列出远程目录内容。
参数：可以指定匹配模式，如 ls build_*.json。

```bash
get <remote_name> [local_name]
```
用途：将远程文件下载到本地执行节点（Dynamic Node）。

```bash
cat <remote_name>
```
用途：在控制台直接打印远程文件内容。常用于 Tier 3 快速校验归档的 JSON 是否正确。


# 登录 Samba (smb: > 提示符) 后可执行的常用命令
当通过交互模式登录后，可以使用以下命令进行环境探测和文件操作：

1. 路径导航
```bash
# 进入远程服务器的子目录。
cd <path>

# 显示当前正在访问的远程目录路径。
pwd

# Local CD，更改 smbclient 进程在本地（即 Dynamic Node 容器内）的工作目录。
lcd <path>

# 显示当前本地的工作路径。
lpwd
```

2. 文件传输设置
```bash
# 切换交互模式开关。自动化脚本务必执行 prompt off，否则 mput 会不停询问。
prompt off

# 切换递归模式。开启后（recurse on），mput 或 mget 会处理子文件夹。
recurse

# 强制将下载的文件名转为小写。
lowercase
```

3. 系统辅助
```bash
# 显示所有可用命令的列表，或查看某个命令的具体用法。
help [command]
# 显示当前远程共享目录的磁盘使用情况（总容量、剩余空间）。
du
# 断开连接并退出 smbclient。
exit / quit
```