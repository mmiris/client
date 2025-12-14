/**
 * pipeline/general_pipeline.groovy
 * 【修正版】上下文透传模式
 * 移除 node 块，直接使用 Job 分配的工作空间
 */

def call(Map config) {
    // 默认参数合并
    def finalConfig = [
        cpu: 2,
        memory: "4G",
        repoUrl: "",
        branch: "master"
    ] + config

    def clientLib = null 
    def dynamicNodeId = null

    // === 【关键修改】不再申请新节点，直接复用当前环境 ===
    // 这里的 env.WORKSPACE 就是 Job 里拉代码的那个目录
    
    // 设置构建名称
    currentBuild.displayName = "#${BUILD_NUMBER} ${finalConfig.projectName}"
    
    try {
        stage('Tier 2: Setup') {
            echo "=== 1. 环境检查 ==="
            // 验证一下文件是否真的在 (调试用)
            sh 'ls -l scripts/' 
            
            echo "=== 2. 加载 Client 模块 ==="
            // 因为共享了工作空间，scripts/client.groovy 肯定存在
            clientLib = load 'scripts/client.groovy'
            
            echo "=== 3. 申请资源 ==="
            dynamicNodeId = clientLib.create([
                cpu: finalConfig.cpu, 
                memory: finalConfig.memory
            ])
            
            if (!dynamicNodeId) error "资源申请失败"
        }

        stage('Tier 3: Execution') {
            // 【模拟环境】
            // 因为我们在模拟，没有真实的 Docker，所以这里暂时不切换节点
            // 真实上线时：这里改成 node(dynamicNodeId) { ... }
            
            echo ">>> 上下文切换: 已进入 Tier 3 环境 (${dynamicNodeId}) <<<"
            
            // 模拟 Python 核心逻辑
            sh "echo 'Mocking: python -m pre_build'"
            sh "echo 'Mocking: python -m build --repo ${finalConfig.repoUrl}'"
            sh "echo 'Mocking: python -m post_build'"
        }

    } catch (Exception e) {
        echo "Error: ${e.message}"
        currentBuild.result = 'FAILURE'
        throw e
    } finally {
        stage('Tier 2: Cleanup') {
            if (dynamicNodeId && clientLib) {
                clientLib.delete(dynamicNodeId)
            }
        }
    }
}
return this