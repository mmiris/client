def call() {
    echo "【Container A】控制器工作空间: ${env.WORKSPACE}"
    def ci_tool_path = "pipeline/ci_tool"
    node(env.run_node_name) {
        try {
            echo "【Container A】当前沙箱内的节点名: ${env.run_node_name}"
            echo "【Container A】当前运行节点: ${env.NODE_NAME}"
            echo "【Container A】当前节点工作空间: ${env.WORKSPACE}"
            stage("【A】Prepare") {
                deleteDir()
                sh "git clone -q -b dev https://github.com/mmiris/client.git ."
            }
            stage("【A】Business Logic") {
                // 执行 Tier 3 Python 逻辑
                sh "python3 $ci_tool_path/binary_compare.py"
            }
        } finally {
            deleteDir()
        }
    }
}

return this