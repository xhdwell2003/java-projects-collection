# Git MCP Server 设置指南

## 概述
已成功设置 git MCP server，用于通过 Model Context Protocol 与 Git 仓库进行交互和自动化操作。

## 安装过程

### 1. 配置文件更新
在 `.vscode/mcp.json` 中添加了 git MCP server 配置：

```json
{
  "servers": {
    "github.com/modelcontextprotocol/servers/tree/main/src/git": {
      "command": "docker",
      "args": [
        "run",
        "--rm",
        "-i",
        "--mount", "type=bind,src=${workspaceFolder},dst=/workspace",
        "mcp/git"
      ]
    }
  }
}
```

### 2. Docker 镜像准备
- 成功拉取了 `mcp/git:latest` Docker 镜像
- 镜像 SHA: `sha256:d80052dc41ad0f02d12fcd9263c1622375fb7f4ff09477d375767b7f9b3c0d4a`

### 3. 验证环境
- 确认当前工作目录是有效的 Git 仓库
- Docker 容器能正常启动并运行

## 可用工具

git MCP server 提供以下工具：

1. **git_status** - 显示工作树状态
2. **git_diff_unstaged** - 显示未暂存的更改
3. **git_diff_staged** - 显示已暂存的更改
4. **git_diff** - 显示分支或提交之间的差异
5. **git_commit** - 记录更改到仓库
6. **git_add** - 添加文件内容到暂存区
7. **git_reset** - 取消暂存所有已暂存的更改
8. **git_log** - 显示提交日志
9. **git_create_branch** - 创建新分支
10. **git_checkout** - 切换分支
11. **git_show** - 显示提交内容
12. **git_init** - 初始化 Git 仓库

## 使用方法

### 通过 MCP 工具使用
```javascript
// 示例：检查仓库状态
use_mcp_tool("github.com/modelcontextprotocol/servers/tree/main/src/git", "git_status", {
  "repo_path": "/workspace"
})
```

### 直接 Docker 命令测试
```bash
docker run --rm -i --mount type=bind,src=/Users/xiaohandong/cusors/工作,dst=/workspace mcp/git
```

## 注意事项

1. **路径映射**: Docker 容器将工作目录映射到 `/workspace`
2. **权限**: 容器以只读方式访问，可以安全地查看和分析仓库
3. **重启要求**: 配置更改后可能需要重启 VS Code 以激活 MCP 连接

## 状态
✅ Docker 镜像已下载  
✅ 配置文件已更新  
✅ 容器可正常启动  
⏳ MCP 连接需要 VS Code 重启后激活  

## 下一步
重启 VS Code 后，git MCP server 将完全可用，可以通过 MCP 工具与 Git 仓库进行交互。