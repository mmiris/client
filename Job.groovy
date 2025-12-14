// 分配给 Tier 2 静态调度节点
node('static_scheduler') {
    
    // 这一步是为了清理工作空间，防止旧文件干扰
    // 同时也为了确保我们总是拉取最新的架构代码
    stage('Bootstrap: Checkout Logic') {
        deleteDir() // 可选：先清空当前目录
        
        // 1. 拉取包含 general_pipeline.groovy 和 client.groovy 的仓库
        // 【注意】请替换为你真实的 GitHub 地址
        git branch: 'dev', url: 'https://github.com/mmiris/client.git'
    }

    stage('Debug: List Files') {
        // === 新增调试步骤：让 Jenkins 打印当前目录下的所有文件 ===
        // 这行命令会递归列出所有文件，帮我们定位路径
        sh 'ls -R' 
    }

    stage('Bootstrap: Launch') {
        // 2. 加载通用流水线框架
        // load 返回的是 general_pipeline.groovy 里的对象 (因为我们写了 return this)
        def pipelineFramework = load 'client/pipelines/general_pipeline.groovy'
        
        // 3. 传入配置，启动流水线
        // 这里是你唯一需要针对不同 Job 修改的地方
        pipelineFramework.call([
            // 项目基础信息
            projectName: "build_app", 
            
            // 业务参数 (透传给 Tier 3 的 Python)
            repoUrl: "https://github.com/mmiris/build_app.git",
            branch: "main",
            
            // 资源参数 (透传给 client.groovy)
            cpu: 4,
            memory: "8G"
        ])
    }
}