def call() {
    def ci_tool_path = pwd() + "/pipeline/ci_tool"

    node(run_node_name) {
        stage("Compiler") {
                echo "Start compiler ..."
                sh "python3 $ci_tool_path/binary_compare.py"
            }
        }
    }

return this