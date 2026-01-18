def call() {
    def tool_path = pwd()
    echo "Current workspace: $tool_path"

    echo "test_var: $test_var"
    def ci_tool_dir = "pipeline/ci_tool"
    def ci_tool_path = pwd() + ci_tool_dir
    echo "ci_tool_path: $ci_tool_path"

    stage("compiler") {
        echo "Start compiler ..."
        sh "python3 $ci_tool_path/binaery_comare.py"
    }
}

return this