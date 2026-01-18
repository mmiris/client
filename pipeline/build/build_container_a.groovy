def tool_path = pwd()

def call() {
    stage("compare") {
        git branch: 'dev', url: 'https://github.com/mmiris/client.git'
    }
    echo "Current workspace: ${tool_path}"

    stage("compiler") {
        echo "Start compiler ..."
    }
}