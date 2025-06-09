# GitHub MCP Server 使用指南

## 安装状态
✅ GitHub MCP Server 已成功安装并配置

## 配置文件
- **VS Code配置**: `.vscode/mcp.json`
- **服务器名称**: `github.com/github/github-mcp-server`
- **Docker镜像**: `ghcr.io/github/github-mcp-server:latest`

## 使用方法

### 1. 在VS Code中激活MCP服务器
1. 重启VS Code或重新加载窗口 (Cmd+Shift+P -> "Developer: Reload Window")
2. 开启Copilot Chat的Agent模式
3. 当系统提示时，输入您的GitHub Personal Access Token

### 2. 可用的工具类别
GitHub MCP服务器提供以下工具集:

#### 用户相关 (Users)
- `get_me` - 获取认证用户详情
- `search_users` - 搜索GitHub用户

#### 仓库相关 (Repositories) 
- `create_repository` - 创建新仓库
- `fork_repository` - Fork仓库
- `get_file_contents` - 获取文件内容
- `create_or_update_file` - 创建或更新文件
- `push_files` - 推送多个文件
- `list_branches` - 列出分支
- `create_branch` - 创建分支
- `list_commits` - 列出提交
- `get_commit` - 获取提交详情
- `search_repositories` - 搜索仓库
- `search_code` - 搜索代码

#### Issue相关 (Issues)
- `create_issue` - 创建Issue
- `get_issue` - 获取Issue详情
- `list_issues` - 列出Issues
- `update_issue` - 更新Issue
- `add_issue_comment` - 添加Issue评论
- `get_issue_comments` - 获取Issue评论
- `search_issues` - 搜索Issues

#### Pull Request相关 (Pull Requests)
- `create_pull_request` - 创建PR
- `get_pull_request` - 获取PR详情
- `list_pull_requests` - 列出PRs
- `update_pull_request` - 更新PR
- `merge_pull_request` - 合并PR
- `get_pull_request_files` - 获取PR文件变更
- `create_pull_request_review` - 创建PR审查
- `add_pull_request_review_comment` - 添加PR审查评论

#### 安全相关 (Code Security)
- `list_code_scanning_alerts` - 列出代码扫描警报
- `get_code_scanning_alert` - 获取代码扫描警报详情
- `list_secret_scanning_alerts` - 列出密钥扫描警报
- `get_secret_scanning_alert` - 获取密钥扫描警报详情

#### 通知相关 (Notifications)
- `list_notifications` - 列出通知
- `get_notification_details` - 获取通知详情
- `dismiss_notification` - 关闭通知
- `mark_all_notifications_read` - 标记所有通知为已读

### 3. 工具集配置
您可以通过环境变量限制可用的工具集:
```bash
GITHUB_TOOLSETS="repos,issues,pull_requests,code_security"
```

或启用所有工具集:
```bash
GITHUB_TOOLSETS="all"
```

### 4. 示例使用场景
- 自动化GitHub工作流程
- 从GitHub仓库提取和分析数据
- 构建与GitHub生态系统交互的AI工具

## 测试验证
✅ Docker已安装并运行
✅ GitHub MCP Server镜像已下载
✅ MCP配置文件已创建
✅ 服务器可以正常启动和停止

## 下一步
现在您可以在VS Code中使用Copilot Chat的Agent模式来与GitHub MCP服务器交互，执行各种GitHub操作。