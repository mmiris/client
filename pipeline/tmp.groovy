node('built-in') {
    stage("Clean cache") {
        deleteDir() // 可选：先清空当前目录
        sh('echo $(whoami); ls -al')

        // 1. 拉取包含 general_pipeline.groovy 和 pipeline_script.groovy 的仓库
        git branch: 'dev', url: 'https://github.com/mmiris/client.git'
    }

    stage('List Files') {
        // === 新增调试步骤：让 Jenkins 打印当前目录下的所有文件 ===
        // 这行命令会递归列出所有文件，帮我们定位路径
        sh 'ls -R'
    }

    stage('Launch') {
        // 2. 加载通用流水线框架
        // load 返回的是 general_pipeline.groovy 里的对象 (因为我们写了 return this)
        def pipelineFramework = load 'pipeline/binary_compare.groovy'

        // 3. 传入配置，启动流水线
        // 这里是你唯一需要针对不同 Job 修改的地方
        pipelineFramework.call()
    }
}