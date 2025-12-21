# 安卓项目 DMU超市管理系统 开发手册
本项目为**纯Java开发的安卓原生项目**.

## 一、项目环境要求
为避免编译兼容问题，**必须统一以下开发环境**：
| 工具/依赖       | 推荐版本                  | 备注                     |
|----------------|-------------------------  |--------------------------|
| Android Studio | 2023.1.1 Dolphin（稳定版） | 避免使用最新测试版       |
| Gradle         | 8.0                       | 与项目`gradle-wrapper.properties`一致 |
| JDK            | 21                        | 纯Java安卓开发官方标配   |
| Android SDK    | minSdk:API 31（11.0）、targetSdk:API 34（14） | 按需修改，需同步告知合作者 |

## 二、核心资源配置步骤（必做）
合作者拉取代码后，需完成以下本地资源配置，否则项目无法正常编译/运行。

### 2.1 配置Android SDK路径（local.properties）
项目根目录的`local.properties`文件已被`.gitignore`过滤（避免路径冲突），需**本地新建并配置**：
1. 在项目根目录新建文件，命名为`local.properties`；
2. 根据操作系统填写SDK本地路径（示例如下）：
   ```properties
   # Windows系统（路径用双反斜杠\\或单斜杠/）
   sdk.dir=D:\\Android\\Sdk
   # 或 sdk.dir=D:/Android/Sdk

   # macOS/Linux系统
   sdk.dir=/Users/你的用户名/Library/Android/sdk
   ```
   
### 2.2 签名文件配置（调试/打包）
安卓签名文件（.jks）含密钥信息，已被`.gitignore`过滤，分**开发调试**和**正式打包**两种配置方式：
#### 方式1：调试签名（开发阶段推荐）
Android Studio会自动生成默认调试签名，**无需手动配置**，直接运行项目即可。

#### 方式2：正式签名（发布APK用）
1. 将你的正式签名文件（如`app_sign.jks`）放在项目`app/`目录下；
2. 在`local.properties`中追加签名配置：
   ```properties
   # 签名文件路径（相对路径，放在app/目录则写app/app_sign.jks）
   SIGN_STORE_FILE=app/app_sign.jks
   # 签名别名
   SIGN_KEY_ALIAS=my_app_alias
   # 签名密码
   SIGN_KEY_PASSWORD=123456
   # 密钥库密码
   SIGN_STORE_PASSWORD=123456
   ```
3. 配置后，打包Release版APK时会自动使用该签名（项目`build.gradle`已集成读取逻辑）。

### 2.3 第三方依赖资源配置
项目使用的第三方Java库/资源已做版本控制，按以下步骤确认即可：
1. **本地JAR包**：第三方JAR包（如Gson、OkHttp）已放在`app/libs/`目录，Gradle会自动依赖，无需额外下载；
2. **网络依赖**：`build.gradle`中已配置Maven仓库，同步Gradle时会自动下载远程依赖；
3. **静态资源**：`app/src/main/res/`下的图片、布局、字符串等资源已纳入Git追踪，拉取代码后直接使用。

## 三、快速启动项目
1. **克隆仓库到本地**：
   ```bash
   git clone git@github.com:你的GitHub用户名/你的仓库名.git
   ```
2. **打开项目**：启动Android Studio → 点击「Open」→ 选中项目根目录 → 点击「OK」；
3. **同步Gradle**：等待AS自动同步Gradle（首次同步需下载依赖，确保网络畅通）；
4. **完成配置**：按「二、核心资源配置步骤」完成本地`local.properties`配置；
5. **运行项目**：连接安卓模拟器/真机 → 点击AS顶部「Run」按钮（▶️）→ 选择设备即可运行。

## 四、协作开发规范
为避免代码冲突和管理混乱，所有合作者需遵循以下规范：
### 4.1 分支管理
- **主分支（main）**：仅合并测试通过的代码，**禁止直接推送**；
- **功能分支**：新建`feature/功能名`分支开发（如`feature/商品列表`）；
- **Bug修复分支**：新建`fix/bug描述`分支（如`fix/登录按钮无响应`）；
- **优化分支**：新建`optimize/优化点`分支（如`optimize/列表加载速度`）。

### 4.2 提交信息格式
提交信息需清晰描述修改内容，格式为「**类型：描述**」：
```bash
# 示例1：新增功能
git commit -m "feat：添加商品详情页"
# 示例2：修复Bug
git commit -m "fix：修复商品价格显示错误"
# 示例3：更新文档
git commit -m "docs：完善README资源配置步骤"
# 示例4：代码优化
git commit -m "optimize：简化网络请求工具类"
```

### 4.3 代码合并流程
1. 开发完成后，推送分支到远程：
   ```bash
   git push origin feature/商品列表
   ```
2. 在GitHub仓库提交**Pull Request（PR）**，并备注修改内容；
3. 等待仓库所有者审核通过后，合并到`main`分支；
4. 合并后拉取`main`分支最新代码，清理本地过时分支：
   ```bash
   git checkout main
   git pull origin main
   git branch -d feature/商品列表
   ```

## 五、常见问题解决
1. **Gradle同步失败**
   - 检查`local.properties`中SDK路径是否正确；
   - 点击AS顶部「File → Invalidate Caches / Restart」清除缓存后重试；
   - 确认网络畅通，可正常访问Maven仓库。
2. **项目运行提示“找不到签名文件”**
   - 开发阶段无需配置正式签名，使用AS默认调试签名即可；
   - 若需打包，按「2.3 正式签名配置」补充`local.properties`信息。
3. **接口请求失败**
   - 检查`local.properties`中`API_BASE_URL`是否正确；
   - 确认真机/模拟器能访问该接口地址（测试网络连通性）；
   - 检查AndroidManifest.xml是否添加网络权限：`<uses-permission android:name="android.permission.INTERNET" />`。
4. **资源文件报错（如R文件丢失）**
   - 检查`res/`目录下的资源文件是否有命名错误（如含中文/特殊字符）；
   - 点击AS顶部「Build → Rebuild Project」重新构建项目。

