def call() {
    def ci_tool_path = pwd() + "/pipeline/ci_tool"
    echo "【Container A】当前沙箱内的节点名: ${env.run_node_name}"
    node(run_node_name) {
        stage("Compiler") {
            echo "Current Workspace: $env.WORKSPACE"
            echo "Start compiler ..."
            sh "python3 $ci_tool_path/binary_compare.py"
        }
    }
}

return this