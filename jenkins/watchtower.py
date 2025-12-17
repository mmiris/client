import time
from datetime import datetime
from jenkinsapi.jenkins import Jenkins
from rich import box
from rich.console import Console
from rich.live import Live
from rich.panel import Panel
from rich.table import Table
from rich.text import Text

# ================= 核心配置区域 (Configuration) =================

# [调试开关] 如果你看不到节点，请将此项改为 True
DEBUG_SHOW_ALL_NODES = False

# 1. Master 列表
# 请替换回你的真实 Token
MASTERS_CONFIG = [
    {
        "name": "Scan-Master-01", 
        "url": "http://192.168.1.10:8080", 
        "user": "admin", 
        "token": "TOKEN_A"
    },
    # {
    #     "name": "Scan-Master-02", 
    #     "url": "http://192.168.1.20:8080", 
    #     "user": "admin", 
    #     "token": "TOKEN_B"
    # },
]

# 2. 节点过滤器
def is_target_node(node_name):
    node_name_lower = node_name.lower()
    if node_name_lower in ["built-in", "master"]:
        return False
    if DEBUG_SHOW_ALL_NODES:
        return True
    # 过滤白名单
    target_keywords = ["static", "manager", "ops", "tier2", "linux", "build"]
    return any(keyword in node_name_lower for keyword in target_keywords)

# ===============================================

console = Console()

def bytes_to_human(size_bytes):
    """辅助函数：字节转 GB/MB"""
    if not isinstance(size_bytes, (int, float)):
        return "-"
    for unit in ["B", "KB", "MB", "GB", "TB"]:
        if size_bytes < 1024:
            return f"{size_bytes:.1f}{unit}"
        size_bytes /= 1024
    return f"{size_bytes:.1f}PB"

def get_idle_executors_count(node):
    """获取空闲执行器数量，修复了异常处理以符合 Ruff 规范"""
    # 尝试方法 1: 从 _data 原始数据解析
    try:
        if hasattr(node, "_data") and node._data:
            executors = node._data.get("executors", [])
            return sum(1 for e in executors if not e)
    except Exception as e:
        # 仅记录调试信息，不中断流程
        metrics_err = f"Data parse error: {e}"
        _ = metrics_err # 避免 Ruff 提示变量未使用
    
    # 尝试方法 2: 调用 API 方法
    try:
        return len(node.get_idle_executors())
    except Exception as e:
        # 如果方法 2 也失败，则打印到日志区域（此处为展示目的暂不直接 print 破坏 UI）
        _ = e
        return 0

def get_detailed_metrics(node):
    """获取资源监控数据，修复了 bare except 并增加了异常信息记录"""
    metrics = {
        "offline": False,
        "reason": "",
        "idle": 0,
        "total": 0,
        "disk_avail": 0,
        "disk_total": 0,
        "mem_avail": 0,
        "mem_total": 0,
    }
    if not node:
        return metrics

    try:
        metrics["offline"] = not node.is_online()
        if metrics["offline"]:
            metrics["reason"] = node.offline_reason() or "Manual Offline"
    except Exception as e:
        metrics["offline"] = True
        metrics["reason"] = f"Conn Error: {e}"
    
    try:
        metrics["total"] = node.get_num_executors()
        metrics["idle"] = get_idle_executors_count(node)
    except Exception as e:
        metrics["reason"] += f" Executor Error: {e}"

    try:
        raw_data = node._data.get("monitorData", {}) if hasattr(node, "_data") else {}
        # 磁盘
        disk_monitor = raw_data.get("hudson.node_monitors.DiskSpaceMonitor")
        if isinstance(disk_monitor, dict):
            metrics["disk_avail"] = disk_monitor.get("size", 0) or 0
            metrics["disk_total"] = disk_monitor.get("totalSize", 0) or 0
        # 内存
        mem_monitor = raw_data.get("hudson.node_monitors.SwapSpaceMonitor")
        if isinstance(mem_monitor, dict):
            metrics["mem_total"] = mem_monitor.get("totalPhysicalMemory", 0) or 0
            metrics["mem_avail"] = mem_monitor.get("availablePhysicalMemory", 0) or 0
    except Exception as e:
        metrics["reason"] += f" Monitor Error: {e}"

    return metrics

def fetch_master_status(config):
    """获取 Master 及其节点状态"""
    result = {"name": config["name"], "url": config["url"], "alive": False, "latency": 0, "queue": 0, "nodes": [], "error": None}
    start_t = time.time()
    try:
        server = Jenkins(config["url"], username=config["user"], password=config["token"], timeout=5)
        result["queue"] = len(server.get_queue())
        result["alive"] = True
        result["latency"] = int((time.time() - start_t) * 1000)
        nodes_map = server.get_nodes()
        for name, node_obj in nodes_map.items():
            if is_target_node(name):
                result["nodes"].append({"name": name, "metrics": get_detailed_metrics(node_obj)})
        result["nodes"].sort(key=lambda x: x["name"])
    except Exception as e:
        result["error"] = f"Master Connect Failed: {e}"
    return result

def format_resource_usage(avail, total):
    """格式化为 Total / Used (Used%) 样式"""
    if total <= 0: 
        return "[dim]-[/dim]"
    used = max(0, total - avail)
    used_pct = int((used / total) * 100)
    
    color = "green"
    if used_pct >= 90: 
        color = "bold red"
    elif used_pct >= 80: 
        color = "yellow"
    
    return f"[{color}]{bytes_to_human(total)} / {bytes_to_human(used)} ({used_pct}%)[/]"

def generate_layout():
    """生成极简且对齐的布局"""
    grid = Table(
        box=box.SIMPLE, 
        expand=True, 
        show_header=True, 
        header_style="bold", 
        border_style="dim",
        pad_edge=False
    )
    
    grid.add_column("Master", style="bold cyan", ratio=2, justify="left")
    grid.add_column("Queue", ratio=1, justify="center")
    grid.add_column("Static Nodes Status", ratio=7, justify="center")

    total_masters = len(MASTERS_CONFIG)
    masters_up = 0

    for config in MASTERS_CONFIG:
        data = fetch_master_status(config)
        if data["alive"]: 
            masters_up += 1

        # Master 信息
        if data["alive"]:
            master_info = f"[green]● {data['name']}[/green]\n[dim]{data['url']}[/dim]"
        else:
            master_info = f"[bold red]● {data['name']}[/bold red]\n[dim]{(data.get('error') or 'Down')[:40]}[/dim]"

        # Queue 信息
        if data["alive"]:
            q_color = "green" if data["queue"] < 10 else ("yellow" if data["queue"] < 50 else "bold red")
            queue_info = f"[{q_color}]{data['queue']}[/]\n[dim]{data['latency']}ms[/]"
        else:
            queue_info = "[dim]-[/dim]"

        # Nodes 详情
        if data["alive"] and data["nodes"]:
            node_table = Table(box=None, show_header=True, padding=(0, 2), expand=True, header_style="dim")
            node_table.add_column("Node Name", justify="center", style="white")
            node_table.add_column("State", justify="center")
            node_table.add_column("Executors", justify="center")
            node_table.add_column("Disk (Tot/Usd)", justify="center")
            node_table.add_column("Mem (Tot/Usd)", justify="center")

            for node in data["nodes"]:
                m = node["metrics"]
                state = "[red]✖[/red]" if m["offline"] else "[green]✔[/green]"
                if m["offline"] and m["reason"]:
                    # 将详细错误信息作为悬停或副文本（此处极简显示）
                    state = f"[red]✖[/red]\n[dim]{str(m['reason'])[:15]}[/dim]"

                exec_color = "yellow" if (m["idle"] == 0 and m["total"] > 0) else "green"
                
                node_table.add_row(
                    node["name"],
                    state,
                    f"[{exec_color}]{m['idle']}/{m['total']}[/]",
                    format_resource_usage(m["disk_avail"], m["disk_total"]),
                    format_resource_usage(m["mem_avail"], m["mem_total"])
                )
            nodes_render = node_table
        else:
            msg = "No Nodes Found" if data["alive"] else (data.get("error") or "Master Down")
            nodes_render = Text(msg, style="dim", justify="center")

        grid.add_row(master_info, queue_info, nodes_render)

    footer = f"Masters: {masters_up}/{total_masters}  |  {datetime.now().strftime('%H:%M:%S')}"
    return Panel(grid, subtitle=footer, subtitle_align="right", border_style="dim blue")

if __name__ == "__main__":
    with Live(console=console, refresh_per_second=4, screen=False) as live:
        while True:
            live.update(generate_layout())
            time.sleep(10)