def tool_path = pwd()

def call() {
    echo "Current workspace: ${tool_path}"

    stage("compiler") {
        echo "Start compiler ..."
    }
}

return this