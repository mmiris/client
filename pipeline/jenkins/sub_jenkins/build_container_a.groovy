def call() {
    def tool_path = pwd()
    echo "Current workspace: $tool_path"

    echo "test_var: $test_var"
    def ci_tool_path = pwd() + "/pipeline/ci_tool"
    echo "ci_tool_path: $ci_tool_path"

    stage("compiler") {
        echo "Start compiler ..."
        sh "python3 $ci_tool_path/binary_compare.py"
    }
}

return this