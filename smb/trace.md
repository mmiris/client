# 安装

```bash
# Ubuntu/Debian
sudo apt update && sudo apt install samba -y
```


# 配置

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

# 公网 ip 会对 445 端口进行拦截，改用其它端口可以解决
[global]
   smb ports = 0000000000000000000000000000000000000
```
> 注意：注释可能会导致语法错误，注意删除
> /root 目录作为共享目录可能无权限访问


# 重启

```bash
sudo systemctl restart smbd
sudo systemctl restart nmbd
```


# 创建 SMB 用户

SMB 的用户必须首先是系统的 Linux 用户，但它有一套独立的密码。

```bash
# 1. 创建一个系统用户（如果还没创建）
sudo useradd -M -s /sbin/nologin smbuser

# 2. 设置该用户的 SMB 密码
sudo smbpasswd -a smbuser

# 3. 给文件夹权限，让这个用户能写进去
sudo chown -R smbuser:smbuser /data/space
```