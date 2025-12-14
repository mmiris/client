/**
 * scripts/client.groovy
 * 这是一个被 load 加载的模块，它运行在 Jenkins 进程内部
 */

// 方法 1: 申请资源
// 接收一个 Map 配置，返回一个 String (Node ID)
def create(Map config) {
    // 能够直接使用 Jenkins 的 echo 步骤
    echo "--- [Client Module] 正在初始化资源申请流程 ---"
    echo "    CPU核心: ${config.cpu}"
    echo "    内存规格: ${config.memory}"

    // 在这里编写您的核心逻辑
    // 比如：调用 Shell 执行 curl 命令去请求云厂商 API
    // output = sh(script: "curl -X POST ...", returnStdout: true)
    
    // === 模拟环境逻辑 ===
    // 模拟 API 延迟
    sleep 2 
    
    // 模拟生成 ID
    def mockId = "dynamic-node-${UUID.randomUUID().toString().take(8)}"
    
    echo "--- [Client Module] 资源申请成功，ID: ${mockId} ---"
    
    // 直接返回变量，不需要 println
    return mockId
}

// 方法 2: 销毁资源
def delete(String nodeId) {
    echo "--- [Client Module] 收到销毁请求 ---"
    
    if (!nodeId) {
        // 直接使用 Jenkins 的 error 步骤中断
        error "[Client Module] 错误：未传入 Node ID，无法销毁"
    }
    
    echo "    目标节点: ${nodeId}"
    // sh "curl -X DELETE ..."
    
    sleep 1
    echo "--- [Client Module] 节点 ${nodeId} 已销毁 ---"
}

// ！！！至关重要！！！
// 文件末尾必须返回 this，否则 load 命令无法获取这个对象
return this