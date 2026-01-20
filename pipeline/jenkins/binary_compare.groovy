def download_build_tool() {
    println "do nothing ..."
}

def call() {
    stage('Binary Comparison Simulation') {
        // A 容器名称
        def container_a = "static_scheduler_overseas"
        // B 容器名称
        def container_b = "static_scheduler"

        // // 存储到环境变量中
        // env.setProperty("container_a", container_a)
        // env.setProperty("container_b", container_b)

        // 使用 parallel 实现并行执行
        parallel(
            "Container_A": {
                env.test_var_a = "test_var_a"
                // env.run_node_name = container_a
                // echo "Container_A run_node_name ==> $run_node_name"
                try {
                    echo "【Container A】开始工作..."
                    stage("【A】Prepare") {
                        this.download_build_tool()
                    }

                    // stage('List Files') {
                    //     // === 新增调试步骤：让 Jenkins 打印当前目录下的所有文件 ===
                    //     // 这行命令会递归列出所有文件，帮我们定位路径
                    //     sh 'ls -R'
                    // }

                    stage("【A】Launch") {
                        withEnv([run_node_name: container_a]) {
                            def pipeline_container_a = load "pipeline/jenkins/sub_jenkins/build_container_a.groovy"
                            pipeline_container_a.call()
                        }
                    }
                } finally {
                    echo "【Container A】执行工作目录清理..."
                }
            },

            "Container_B": {
                // env.run_node_name = container_b
                // echo "Container_B run_node_name ==> $run_node_name"
                try {
                    echo "【Container B】开始工作..."
                    stage("【B】Prepare") {
                        this.download_build_tool()
                    }

                    // stage('List Files') {
                    //     // === 新增调试步骤：让 Jenkins 打印当前目录下的所有文件 ===
                    //     // 这行命令会递归列出所有文件，帮我们定位路径
                    //     sh 'ls -R'
                    // }

                    stage("【B】Launch") {
                        withEnv(["run_node_name=$container_b"]) {
                            def pipeline_container_b = load "pipeline/jenkins/sub_jenkins/build_container_b.groovy"
                            pipeline_container_b.call()
                        }
                    }
                } finally {
                    echo "【Container B】执行工作目录清理..."
                }
            }
        )
    }
}

return this