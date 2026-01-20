def call() {
    echo "【Container B】当前沙箱内的节点名: ${env.run_node_name}"
    echo "【Container A】当前沙箱内的工作空间: ${env.WORKSPACE}"
    def ci_tool_path = "pipeline/ci_tool"
    node(env.run_node_name) {
        try {
            stage("【B】Prepare") {
                deleteDir()
                sh "git clone -q -b dev https://github.com/mmiris/client.git ."
            }
            stage("【B】Business Logic") {
                sh "ls -lR"
                sh "pwd"
                // 执行 Tier 3 Python 逻辑
                sh "python3 $ci_tool_path/binary_compare.py"
            }
        } finally {
            deleteDir()
        }
    }
}

return this