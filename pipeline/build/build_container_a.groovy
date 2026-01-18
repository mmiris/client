def call() {
    def tool_path = pwd()

    echo "Current workspace: ${tool_path}"

    stage("compiler") {
        echo "Start compiler ..."
    }
}

return this