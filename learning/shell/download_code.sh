#!/usr/bin/env bash


function download_tool() {
    # local url="$1"
    # local branch="${2:-dev}"
    # local local_path="${3:-$HOME/Desktop/tmp}"

    local repo_url=""
    local repo_branch=""
    local local_path=""

    while [[ $# -gt 0 ]]; do
        case $1 in
            --repo-url) repo_url=$2; shift 2;;
            --repo-branch) repo_branch=$2; shift 2;;
            --local-path) local_path=${2:-$HOME/Desktop/tmp}; shift 2;;
            *) echo "unknown option: $1" >&2; return 1;;
        esac
    done

    if [[ -d "$local_path" ]]; then
        echo "warning : [$0: line$LINENO]: $local_path already exists, cleaning up ..."
        rm -rf "$local_path"
    fi

    git_cmd=(git clone "$repo_url" -b "$repo_branch" "$local_path")

    if ! err_msg=$("${git_cmd[@]}" 2>&1 > output.log); then
        echo -e "error : download code failed. message: $err_msg" >&2
        return 1
    else
        echo "info : download code successfully"
        return 0
    fi
}

download_tool --repo-url "https://github.com/mmiris/client.git" --repo-branch "dev" --local-path ""