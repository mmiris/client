/**
 * pipelines/general_pipeline.groovy
 */

def call(Map config) {
    // 默认配置合并
    def finalConfig = [
        cpu: 2,
        memory: "4G",
        repoUrl: "",
        branch: "master"
    ] + config

    def dynamicNodeId = null
    def clientLib = null // 用于存放加载进来的库对象

    node('static_scheduler') {
        currentBuild.displayName = "#${BUILD_NUMBER} ${finalConfig.projectName}"
        
        try {
            stage('Tier 2: Setup') {
                echo "=== 1. 拉取基础设施代码 ==="
                // 模拟 git clone，确保 scripts/client.groovy 存在
                // git url: 'git@github.com:your-repo/client.git'
                
                echo "=== 2. 加载 Client 模块 ==="
                // 【核心修正点】
                // load 会执行那个文件，并返回文件末尾的 'this' 对象
                // 假设文件在当前 workspace 的 scripts 目录下
                clientLib = load 'client/scripts/client.groovy'
                
                echo "=== 3. 调用模块方法申请资源 ==="
                // 直接传递 Map 参数，接收返回值
                dynamicNodeId = clientLib.create([
                    cpu: finalConfig.cpu,
                    memory: finalConfig.memory
                ])
                
                if (!dynamicNodeId) {
                    error "未能获取到动态节点 ID"
                }
            }

            stage('Tier 3: Execution') {
                // 模拟切入动态节点 (因为没有真实 Docker，暂时还在 static 上跑)
                // 真实代码：node(dynamicNodeId) { ... }
                node('static_scheduler') {
                    echo ">>> 上下文切换: 已进入 Tier 3 环境 (${dynamicNodeId}) <<<"
                    
                    // 这里执行 Python 核心逻辑
                    // 您的业务核心：Groovy 只是胶水，Python 是灵魂
                    sh "echo '模拟: python -m pre_build'"
                    sh "echo '模拟: python -m build --repo ${finalConfig.repoUrl}'"
                    sh "echo '模拟: python -m post_build'"
                }
            }

        } catch (Exception e) {
            echo "!!! 流程发生异常: ${e.message}"
            currentBuild.result = 'FAILURE'
            throw e // 继续抛出异常，确保 Jenkins 标记为红灯
            
        } finally {
            stage('Tier 2: Cleanup') {
                // 兜底清理
                if (dynamicNodeId && clientLib) {
                    echo "=== 4. 执行资源清理 ==="
                    // 直接调用库方法
                    clientLib.delete(dynamicNodeId)
                }
            }
        }
    }
}

return this