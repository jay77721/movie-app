"""
TMDB 电影 API 代理服务
启动: cd server && source venv/bin/activate && python main.py
"""
import httpx
import logging
import os
from fastapi import FastAPI, Query
from fastapi.responses import JSONResponse
from contextlib import asynccontextmanager

logging.basicConfig(level=logging.INFO)
log = logging.getLogger("tmdb")

TMDB_BASE = "https://api.themoviedb.org/3"
TMDB_IMG = "https://image.tmdb.org/t/p"

# 从环境变量读取 TMDB Token，优先 .env.local
def load_token() -> str:
    # 尝试从 .env.local 读取
    env_local = os.path.join(os.path.dirname(__file__), ".env.local")
    if os.path.exists(env_local):
        with open(env_local) as f:
            for line in f:
                if line.startswith("TMDB_BEARER_TOKEN="):
                    return line.split("=", 1)[1].strip()
    # 尝试从系统环境变量读取
    token = os.environ.get("TMDB_BEARER_TOKEN", "")
    if token:
        return token
    raise RuntimeError("缺少 TMDB_BEARER_TOKEN，请在 server/.env.local 中配置")

BEARER = load_token()

HEADERS = {
    "Authorization": f"Bearer {BEARER}",
    "Accept": "application/json",
}

client: httpx.AsyncClient = None
genre_map: dict = {}  # genre_id -> name


@asynccontextmanager
async def lifespan(app: FastAPI):
    global client, genre_map
    client = httpx.AsyncClient(headers=HEADERS, timeout=15, follow_redirects=True)
    # 加载类型映射
    try:
        r = await client.get(f"{TMDB_BASE}/genre/movie/list", params={"language": "zh-CN"})
        genre_map = {g["id"]: g["name"] for g in r.json().get("genres", [])}
        log.info(f"TMDB 已启动, 加载 {len(genre_map)} 个类型")
    except Exception as e:
        log.error(f"加载类型失败: {e}")
    yield
    await client.aclose()


app = FastAPI(title="TMDB 电影 API", lifespan=lifespan)


# ═══════ 工具函数 ═══════

def poster_url(path: str) -> str:
    return f"{TMDB_IMG}/w500{path}" if path else ""


def backdrop_url(path: str) -> str:
    return f"{TMDB_IMG}/w780{path}" if path else ""


def extract_year(date_str: str) -> str:
    if date_str and len(date_str) >= 4:
        return date_str[:4]
    return ""


def parse_brief(movie: dict) -> dict:
    """TMDB movie object -> 我们的 MovieBrief 格式"""
    genre_ids = movie.get("genre_ids", [])
    # 如果没有 genre_ids，尝试从 genres 列表取
    if not genre_ids and "genres" in movie:
        genre_ids = [g["id"] for g in movie["genres"]]
    genres_str = "/".join(genre_map.get(gid, "") for gid in genre_ids if gid in genre_map)

    return {
        "id": str(movie.get("id", "")),
        "title": movie.get("title", ""),
        "rating": round(movie.get("vote_average", 0), 1),
        "cover": poster_url(movie.get("poster_path", "")),
        "year": extract_year(movie.get("release_date", "")),
        "release_date": movie.get("release_date", ""),
        "genres": genres_str,
        "directors": "",  # 列表接口不含导演，详情页才有
    }


def parse_detail(movie: dict, credits: dict) -> dict:
    """TMDB detail + credits -> 我们的 MovieDetail 格式"""
    # 提取导演
    directors = []
    for crew in credits.get("crew", []):
        if crew.get("job") == "Director":
            directors.append(crew["name"])
        if len(directors) >= 3:
            break

    # 提取主演（最多12位）
    cast_list = []
    for c in credits.get("cast", [])[:12]:
        cast_list.append(c["name"])

    genres = [g["name"] for g in movie.get("genres", [])]
    countries = [c["name"] for c in movie.get("production_countries", [])]
    languages = [l["name"] for l in movie.get("spoken_languages", [])]

    return {
        "id": str(movie.get("id", "")),
        "title": movie.get("title", ""),
        "original_title": movie.get("original_title", ""),
        "rating": round(movie.get("vote_average", 0), 1),
        "rating_count": movie.get("vote_count", 0),
        "cover": poster_url(movie.get("poster_path", "")),
        "backdrop": backdrop_url(movie.get("backdrop_path", "")),
        "year": extract_year(movie.get("release_date", "")),
        "directors": directors,
        "writers": [],  # TMDB 不区分编剧角色
        "cast": cast_list,
        "genres": genres,
        "regions": countries,
        "languages": languages,
        "duration": f"{movie.get('runtime', 0)}分钟" if movie.get("runtime") else "",
        "release_date": movie.get("release_date", ""),
        "summary": movie.get("overview", ""),
        "aka": [],
    }


# ═══════ API 路由 ═══════

@app.get("/api/movies/hot")
async def hot(page: int = Query(1, ge=1)):
    """热门电影"""
    try:
        r = await client.get(f"{TMDB_BASE}/movie/popular",
                             params={"language": "zh-CN", "page": page})
        data = r.json()
        results = [parse_brief(m) for m in data.get("results", [])]
        return {"results": results, "page": page}
    except Exception as e:
        log.error(f"热门失败: {e}")
        return {"results": [], "page": page}


@app.get("/api/movies/top250")
async def top250(page: int = Query(1, ge=1)):
    """高分电影（替代豆瓣 Top250）"""
    try:
        r = await client.get(f"{TMDB_BASE}/movie/top_rated",
                             params={"language": "zh-CN", "page": page})
        data = r.json()
        results = [parse_brief(m) for m in data.get("results", [])]
        return {"results": results, "page": page}
    except Exception as e:
        log.error(f"高分失败: {e}")
        return {"results": [], "page": page}


@app.get("/api/movies/tag/{tag}")
async def by_tag(tag: str, page: int = Query(1, ge=1)):
    """按类型筛选"""
    try:
        # 先查 tag 对应的 genre_id
        genre_id = None
        for gid, name in genre_map.items():
            if name == tag:
                genre_id = gid
                break

        if genre_id:
            r = await client.get(f"{TMDB_BASE}/discover/movie",
                                 params={"language": "zh-CN", "page": page,
                                         "with_genres": genre_id,
                                         "sort_by": "popularity.desc"})
        else:
            # 不是类型名，当关键词搜索
            r = await client.get(f"{TMDB_BASE}/search/movie",
                                 params={"language": "zh-CN", "page": page, "query": tag})
        data = r.json()
        results = [parse_brief(m) for m in data.get("results", [])]
        return {"results": results, "tag": tag, "page": page}
    except Exception as e:
        log.error(f"标签筛选失败 [{tag}]: {e}")
        return {"results": [], "tag": tag, "page": page}


@app.get("/api/movies/tags")
async def tags():
    """可用类型标签"""
    tag_list = sorted(genre_map.values(), key=lambda x: len(x))
    return {"tags": tag_list}


@app.get("/api/movies/search")
async def search(q: str = Query(..., min_length=1), page: int = Query(1, ge=1)):
    """搜索电影"""
    try:
        r = await client.get(f"{TMDB_BASE}/search/movie",
                             params={"language": "zh-CN", "page": page, "query": q})
        data = r.json()
        results = [parse_brief(m) for m in data.get("results", [])]
        return {"results": results, "query": q, "page": page}
    except Exception as e:
        log.error(f"搜索失败 [{q}]: {e}")
        return {"results": [], "query": q, "page": page}


@app.get("/api/movies/{movie_id}")
async def detail(movie_id: str):
    """电影详情"""
    try:
        # 并发获取详情 + 演职人员
        import asyncio
        detail_r, credits_r = await asyncio.gather(
            client.get(f"{TMDB_BASE}/movie/{movie_id}",
                       params={"language": "zh-CN"}),
            client.get(f"{TMDB_BASE}/movie/{movie_id}/credits",
                       params={"language": "zh-CN"}),
        )

        if detail_r.status_code != 200:
            return JSONResponse(404, {"error": "电影不存在"})

        movie = detail_r.json()
        credits = credits_r.json() if credits_r.status_code == 200 else {"cast": [], "crew": []}
        return parse_detail(movie, credits)
    except Exception as e:
        log.error(f"详情失败 [{movie_id}]: {e}")
        return JSONResponse(500, {"error": str(e)})


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8888)
