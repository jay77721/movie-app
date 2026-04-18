# 🎬 豆瓣电影 App

基于 TMDB API 的 Android 电影浏览应用，纯 Java 实现。

## 功能特性

| 功能 | 说明 |
|------|------|
| 🎞 热门推荐 | 热门电影列表，每次刷新随机推荐，含评分、上映时间、类型标签 |
| 📖 详情查看 | 剧情简介、演员阵容、导演、海报大图 |
| ❤️ 影视收藏 | 「想看」/「已看」两个收藏列表，Room 数据库本地持久化 |
| 🏷 分类筛选 | 动作、喜剧、爱情、科幻等 16 个类型标签，横向滚动选择 |
| 🔍 搜索 | 按关键词搜索，500ms 防抖，API 不可用时本地回退 |
| 📱 离线降级 | API 不可用时自动展示本地 mock 数据，不影响体验 |

## 技术栈

| 层面 | 技术 |
|------|------|
| 语言 | 纯 Java（无 Kotlin） |
| UI | XML 布局 + RecyclerView + Material3 |
| 网络 | Retrofit + OkHttp → 直连 TMDB API |
| 图片 | Glide |
| 数据库 | Room（本地收藏） |
| 代理 | 本地 proxy.py（仅模拟器转发用） |

## 项目结构

```
app/src/main/java/com/movieapp/
├── model/                         # 数据模型
│   ├── MovieBrief.java            # 电影列表项（标题、评分、封面、类型）
│   ├── MovieDetail.java           # 电影详情（简介、演员、导演、语言等）
│   └── CollectionType.java        # 收藏类型枚举（想看/已看）
├── data/
│   ├── local/                     # Room 本地数据库
│   │   ├── MovieEntity.java       # 收藏表实体（复合主键 id+collectionType）
│   │   ├── MovieDao.java          # 增删查操作
│   │   └── MovieDatabase.java     # 数据库单例
│   ├── remote/                    # 网络层
│   │   ├── TmdbApi.java           # Retrofit 接口定义
│   │   ├── TmdbResponse.java      # TMDB 原始 JSON 响应模型
│   │   └── NetworkModule.java     # OkHttp + Retrofit 单例（含 Bearer Token）
│   └── repository/
│       └── MovieRepository.java   # 数据仓库：网络请求 + 格式转换 + 本地收藏
└── ui/
    ├── adapter/MovieAdapter.java  # RecyclerView 列表适配器
    ├── home/
    │   ├── MainActivity.java      # 主界面（底部导航 + Fragment 切换）
    │   └── HomeFragment.java      # 首页（热门列表 + 分类标签 + 随机推荐）
    ├── detail/DetailActivity.java # 详情页（海报 + 简介 + 收藏按钮）
    ├── search/SearchFragment.java # 搜索页（防抖搜索 + 本地回退）
    └── collection/CollectionFragment.java  # 收藏页（想看/已看标签切换）

proxy.py                           # 极简 HTTP 转发器（模拟器访问 TMDB 用）
```

## 快速开始

### 1. 启动代理

模拟器无法直连外网，需要在宿主机跑一个转发脚本：

```bash
# 项目根目录
python proxy.py
# → 监听 0.0.0.0:8888，转发到 api.themoviedb.org
```

纯标准库，零依赖，不需要 pip install 任何东西。

### 2. 运行 App

用 Android Studio 打开项目，Run 即可。

- **模拟器**：自动访问 `10.0.2.2:8888`（代理地址），无需修改
- **真机**：修改 `NetworkModule.java` 中 `BASE_URL` 为电脑局域网 IP，如 `http://192.168.1.100:8888/`

### 3. 配置 TMDB Token

`NetworkModule.java` 中的 `BEARER_TOKEN` 需要是有效的 TMDB API Read Access Token。

从 [themoviedb.org/settings/api](https://www.themoviedb.org/settings/api) 获取。

## 数据流

```
首页加载:
  HomeFragment → MovieRepository.getHotMovies() → TmdbApi (Retrofit)
    → proxy.py → TMDB API /movie/popular
    → 返回 JSON → MovieRepository.toBrief() 转换 → MovieAdapter 展示

查看详情:
  点击电影 → DetailActivity → MovieRepository.getDetail()
    → 同时请求 /movie/{id} + /movie/{id}/credits
    → MovieRepository.toDetail() 合并详情和演职员 → 界面绑定

收藏电影:
  DetailActivity 点击「想看」→ MovieRepository.addToCollection()
    → MovieEntity.fromDetail() → MovieDao.insert() → Room SQLite
```

## 注意事项

- `NetworkModule.java` 中的 `BEARER_TOKEN` 是敏感信息，不要提交到公开仓库
- mock 数据中的封面图来自豆瓣 CDN，仅供离线演示
- `proxy.py` 仅用于开发调试，生产环境 App 应直连 TMDB 或自己的后端
