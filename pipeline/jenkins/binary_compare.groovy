def download_build_tool() {
    echo "Current Workspace: $env.WORKSPACE"
    deleteDir()
    sh(script: "git clone -q -b dev https://github.com/mmiris/client.git .")
}

def call() {
    stage('Binary Comparison Simulation') {
        // A 容器名称
        def container_a = "static_scheduler_overseas"
        // B 容器名称
        def container_b = "static_scheduler"

        // 存储到环境变量中
        env.setProperty("container_a", container_a)
        env.setProperty("container_b", container_b)

        // 使用 parallel 实现并行执行
        parallel(
            "Container_A": {
                node(container_a) {
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
                            def pipeline_container_a = load "pipeline/jenkins/sub_jenkins/build_container_a.groovy"
                            pipeline_container_a.call()
                        }
                    } finally {
                        // Tier 2 的必备动作：资源清理（此处模拟清理工作目录）
                        echo "【Container A】执行工作目录清理..."
                        // deleteDir()
                    }
                }
            },

            "Container_B": {
                node(container_b) { // Tier 2: 分配到本地静态节点
                    try {
                        echo "【Container B】开始工作..."
                        stage("【B】Prepare") {
                            this.download_build_tool()
                            // sh '''
                            //     echo "[Tier 3] 正在模拟下载代码..."
                            //     sleep 5
                            //     echo "[Tier 3] 正在编译二进制文件 B..."
                            //     sleep 8
                            //     echo "Build Complete." > artifact_B.bin

                            //     echo "[Tier 3] 等待 Container A 上传产物..."
                            //     # 在真实场景中，这里可以用 Python 脚本通过 smbclient 轮询检查文件是否存在
                            //     sleep 15

                            //     echo "[Tier 3] 从 Samba 下载 Container A 的产物..."
                            //     echo "Artifact_A_Content" > artifact_A_remote.bin

                            //     echo "[Tier 3] 运行二进制对比平台上传脚本..."
                            //     # 模拟 Python 脚本逻辑：python3 compare.py artifact_A_remote.bin artifact_B.bin
                            //     echo "Comparing A and B..."
                            //     sleep 5
                            //     echo "Comparison Result: 100% Match!"
                            // '''
                            sh """
                                echo "Comparison Result: 100% Match!"
                            """
                        }
                    } finally {
                        echo "【Container B】执行 finally 清理..."
                        // deleteDir()
                    }
                }
            }
        )
    }
}

return this