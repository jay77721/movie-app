# 🎬 热门影视推荐 APP

基于 TMDB API 的 Android 电影推荐应用，纯 Java 实现。

## 功能特性

| 功能 | 说明 |
|------|------|
| 🎞 热门推荐 | 展示热门电影列表，含评分、上映时间、类型标签 |
| 📖 详情查看 | 剧情简介、演员阵容、导演、海报 |
| ❤️ 影视收藏 | 想看 / 已看 两个收藏列表，本地持久化 |
| 🏷 分类筛选 | 喜剧、动作、爱情、科幻等 16 个类型标签 |
| 🔍 搜索 | 按关键词搜索电影，支持防抖 |
| 💾 离线缓存 | Room 数据库本地存储，无需登录 |

## 技术栈

- **语言**: 纯 Java（不用 Kotlin/Compose）
- **UI**: XML 布局 + RecyclerView + Material3
- **网络**: Retrofit + OkHttp
- **图片**: Glide
- **数据库**: Room
- **后端**: Python FastAPI + TMDB API

## 项目结构

```
app/src/main/java/com/movieapp/
├── model/                    # 数据模型
│   ├── MovieBrief.java       # 电影列表项
│   ├── MovieDetail.java      # 电影详情
│   └── CollectionType.java   # 收藏类型枚举
├── data/
│   ├── local/                # Room 数据库
│   │   ├── MovieEntity.java  # 收藏实体（复合主键）
│   │   ├── MovieDao.java     # 数据访问
│   │   └── MovieDatabase.java
│   ├── remote/               # 网络层
│   │   ├── TmdbApi.java      # Retrofit 接口定义
│   │   └── NetworkModule.java
│   └── repository/
│       └── MovieRepository.java  # 数据仓库（网络+本地）
└── ui/
    ├── adapter/MovieAdapter.java
    ├── home/                 # 首页
    ├── detail/DetailActivity # 详情页
    ├── search/SearchFragment # 搜索页
    └── collection/           # 收藏页
server/
└── main.py                   # TMDB API 代理服务
```

## 快速开始

### 1. 启动后端

```bash
cd server
python3 -m venv venv
source venv/bin/activate   # Windows: venv\Scripts\activate
pip install -r requirements.txt
python main.py
```

服务运行在 `http://127.0.0.1:8888`

### 2. 运行 Android

用 Android Studio 打开项目，Run 即可。

- 模拟器自动访问 `10.0.2.2:8888`
- 真机需修改 `NetworkModule.java` 中的 IP 为电脑局域网地址

## API 接口

| 接口 | 说明 |
|------|------|
| `GET /api/movies/hot?page=1` | 热门电影 |
| `GET /api/movies/top250?page=1` | 高分电影 |
| `GET /api/movies/tag/{类型}?page=1` | 按类型筛选 |
| `GET /api/movies/search?q=关键词` | 搜索 |
| `GET /api/movies/{id}` | 电影详情 |
| `GET /api/movies/tags` | 类型标签列表 |
