package com.movieapp.ui.detail;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.movieapp.R;
import com.movieapp.data.repository.MovieRepository;
import com.movieapp.model.CollectionType;
import com.movieapp.model.MovieDetail;
import java.util.*;

/**
 * 电影详情页 — 展示海报、简介、演员、收藏按钮
 * 点击收藏按钮切换「想看」/「已看」状态，数据存入 Room 数据库
 */
public class DetailActivity extends AppCompatActivity {

    private MovieRepository repo;
    private String movieId;
    private MovieDetail currentDetail;

    private ImageView ivBackdrop, ivPoster;
    private TextView tvTitle, tvRating, tvMeta, tvSummary, tvCast, tvDirectors, tvLanguages;
    private MaterialButton btnWant, btnWatched;
    private ChipGroup chipGenres;
    private MaterialToolbar toolbar;

    // mock 海报 URL 前缀
    private static final String MOCK_COVER_BASE = "https://img3.doubanio.com/view/photo/s_ratio_poster/public/";

    // API 不可用时的本地详情数据
    private static final Map<String, MovieDetail> MOCK = new HashMap<>();
    static {
        MOCK.put("1", buildMock("1", "肖申克的救赎", 9.7, "p480747492.jpg", "1994",
                "20世纪40年代末，青年银行家安迪因涉嫌杀害妻子入狱。在肖申克监狱中，他结识了瑞德，利用自身才能帮助监狱长洗钱，同时用20年挖掘隧道，最终在一个雷雨夜成功越狱，重获自由。",
                "弗兰克·德拉邦特", "蒂姆·罗宾斯/摩根·弗里曼/鲍勃·冈顿", "剧情/犯罪", "142分钟", "1994-09-10"));
        MOCK.put("2", buildMock("2", "霸王别姬", 9.6, "p1910813120.jpg", "1993",
                "段小楼与程蝶衣是一对打小一起长大的师兄弟，一个演生，一个饰旦，配合天衣无缝。但两人对戏剧与生活的理解存在根本差异，最终在时代洪流中走向悲剧。",
                "陈凯歌", "张国荣/张丰毅/巩俐", "剧情/爱情", "171分钟", "1993-07-26"));
        MOCK.put("3", buildMock("3", "阿甘正传", 9.5, "p512195682.jpg", "1994",
                "阿甘虽然智商只有75，但在母亲的鼓励下，他凭借跑步天赋和善良本性，在越战、乒乓外交等历史事件中创造了奇迹般的人生。",
                "罗伯特·泽米吉斯", "汤姆·汉克斯/罗宾·怀特", "剧情/爱情", "142分钟", "1994-07-06"));
        MOCK.put("4", buildMock("4", "泰坦尼克号", 9.4, "p457760035.jpg", "1997",
                "1912年，富家少女罗丝与穷画家杰克在泰坦尼克号上相遇相爱。然而这艘巨轮撞上了冰山，他们的爱情面临生死考验。",
                "詹姆斯·卡梅隆", "莱昂纳多·迪卡普里奥/凯特·温斯莱特", "剧情/爱情/灾难", "194分钟", "1997-12-19"));
        MOCK.put("5", buildMock("5", "千与千寻", 9.4, "p2578474613.jpg", "2001",
                "千寻随父母误入神灵世界，父母因贪吃变成了猪。千寻在白龙的帮助下，在汤婆婆的澡堂工作，寻找拯救父母和回到现实世界的方法。",
                "宫崎骏", "柊瑠美/入野自由", "动画/奇幻/冒险", "125分钟", "2001-07-20"));
        MOCK.put("6", buildMock("6", "星际穿越", 9.4, "p2206088801.jpg", "2014",
                "近未来地球环境恶化，前NASA宇航员库珀为拯救人类，穿越虫洞前往另一个星系寻找宜居星球，却不得不与女儿分离数十年。",
                "克里斯托弗·诺兰", "马修·麦康纳/安妮·海瑟薇", "科幻/冒险", "169分钟", "2014-11-07"));
        MOCK.put("7", buildMock("7", "盗梦空间", 9.3, "p513344864.jpg", "2010",
                "道姆·柯布是一名能在梦境中窃取秘密的盗贼。他接受了一项不可能的任务：在目标的潜意识中植入想法，为此他组建团队深入多层梦境。",
                "克里斯托弗·诺兰", "莱昂纳多·迪卡普里奥/约瑟夫·高登-莱维特", "科幻/悬疑", "148分钟", "2010-09-01"));
        MOCK.put("8", buildMock("8", "楚门的世界", 9.3, "p479682972.jpg", "1998",
                "楚门从出生起就生活在一个巨大的摄影棚里，他的一切都是真人秀节目。直到有一天他发现了真相，决定逃离这个虚假的世界。",
                "彼得·威尔", "金·凯瑞", "剧情/科幻", "103分钟", "1998-06-05"));
    }

    private static MovieDetail buildMock(String id, String title, double rating, String posterFile,
                                          String year, String summary, String director,
                                          String cast, String genres, String duration, String date) {
        MovieDetail d = new MovieDetail();
        d.setId(id);
        d.setTitle(title);
        d.setRating(rating);
        d.setCover(MOCK_COVER_BASE + posterFile);
        d.setYear(year);
        d.setSummary(summary);
        d.setDirectors(Arrays.asList(director.split("/")));
        d.setCast(Arrays.asList(cast.split("/")));
        d.setGenres(Arrays.asList(genres.split("/")));
        d.setRatingCount(150000);
        d.setDuration(duration);
        d.setReleaseDate(date);
        d.setRegions(Arrays.asList("中国大陆"));
        d.setLanguages(Arrays.asList("普通话"));
        return d;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        repo = new MovieRepository(this);
        movieId = getIntent().getStringExtra("movie_id");

        toolbar = findViewById(R.id.toolbar);
        ivBackdrop = findViewById(R.id.iv_backdrop);
        ivPoster = findViewById(R.id.iv_poster);
        tvTitle = findViewById(R.id.tv_title);
        tvRating = findViewById(R.id.tv_rating);
        tvMeta = findViewById(R.id.tv_meta);
        tvSummary = findViewById(R.id.tv_summary);
        tvCast = findViewById(R.id.tv_cast);
        tvDirectors = findViewById(R.id.tv_directors);
        tvLanguages = findViewById(R.id.tv_languages);
        btnWant = findViewById(R.id.btn_want);
        btnWatched = findViewById(R.id.btn_watched);
        chipGenres = findViewById(R.id.chip_genres);

        toolbar.setNavigationOnClickListener(v -> finish());
        loadDetail();
    }

    /** 加载详情（API → 失败用本地 mock） */
    private void loadDetail() {
        if (movieId == null || movieId.isEmpty()) {
            Toast.makeText(this, "电影ID无效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        repo.getDetail(movieId,
                d -> { currentDetail = d; runOnUiThread(() -> bindDetail(d)); },
                msg -> {
                    MovieDetail mock = MOCK.get(movieId);
                    if (mock != null) {
                        currentDetail = mock;
                        runOnUiThread(() -> bindDetail(mock));
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "加载失败", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                });
    }

    /** 绑定详情数据到界面 */
    private void bindDetail(MovieDetail d) {
        toolbar.setTitle(d.getTitle() != null ? d.getTitle() : "未知电影");

        // 海报 + 背景图
        String cover = d.getCover();
        if (cover != null && !cover.isEmpty()) {
            Glide.with(this).load(cover).placeholder(R.drawable.bg_gray).into(ivPoster);
            Glide.with(this).load(cover).placeholder(R.drawable.bg_gray).into(ivBackdrop);
        }

        tvTitle.setText(d.getTitle());

        // 评分
        tvRating.setText(d.getRating() > 0
                ? String.format("%.1f  (%d人评价)", d.getRating(), d.getRatingCount())
                : "暂无评分");

        // 元信息：年份 · 上映日期 · 地区 · 时长
        StringBuilder meta = new StringBuilder();
        if (d.getYear() != null) meta.append(d.getYear());
        if (d.getReleaseDate() != null && !d.getReleaseDate().isEmpty()) meta.append(" · ").append(d.getReleaseDate());
        if (d.getRegions() != null && !d.getRegions().isEmpty()) meta.append(" · ").append(String.join("/", d.getRegions()));
        if (d.getDuration() != null && !d.getDuration().isEmpty()) meta.append(" · ").append(d.getDuration());
        tvMeta.setText(meta.length() > 0 ? meta.toString() : "暂无信息");

        // 类型标签
        chipGenres.removeAllViews();
        if (d.getGenres() != null) {
            for (String g : d.getGenres()) {
                Chip chip = new Chip(this);
                chip.setText(g);
                chip.setClickable(false);
                chipGenres.addView(chip);
            }
        }

        // 剧情简介
        tvSummary.setText(d.getSummary() != null && !d.getSummary().isEmpty() ? d.getSummary() : "暂无简介");

        // 演员
        tvCast.setText(d.getCast() != null && !d.getCast().isEmpty()
                ? "主演: " + String.join("、", d.getCast()) : "主演: 暂无信息");

        // 导演
        if (d.getDirectors() != null && !d.getDirectors().isEmpty()) {
            tvDirectors.setText("导演: " + String.join("/", d.getDirectors()));
            tvDirectors.setVisibility(View.VISIBLE);
        } else {
            tvDirectors.setVisibility(View.GONE);
        }

        // 语言
        if (d.getLanguages() != null && !d.getLanguages().isEmpty()) {
            tvLanguages.setText("语言: " + String.join("、", d.getLanguages()));
            tvLanguages.setVisibility(View.VISIBLE);
        } else {
            tvLanguages.setVisibility(View.GONE);
        }

        // 收藏按钮
        btnWant.setOnClickListener(v -> toggleCollection(CollectionType.WANT_TO_WATCH));
        btnWatched.setOnClickListener(v -> toggleCollection(CollectionType.WATCHED));
        checkCollection();
    }

    /** 检查收藏状态，更新按钮文字 */
    private void checkCollection() {
        repo.isInCollection(movieId, CollectionType.WANT_TO_WATCH,
                in -> runOnUiThread(() -> btnWant.setText(in ? "已想看" : "想看")), msg -> {});
        repo.isInCollection(movieId, CollectionType.WATCHED,
                in -> runOnUiThread(() -> btnWatched.setText(in ? "已看过" : "已看")), msg -> {});
    }

    /** 切换收藏状态（已收藏则取消，未收藏则添加） */
    private void toggleCollection(CollectionType type) {
        if (currentDetail == null) return;
        String label = type == CollectionType.WANT_TO_WATCH ? "想看" : "已看";

        repo.isInCollection(movieId, type, in -> {
            if (in) {
                repo.removeFromCollection(movieId, type);
                runOnUiThread(() -> {
                    if (type == CollectionType.WANT_TO_WATCH) btnWant.setText("想看");
                    else btnWatched.setText("已看");
                    Toast.makeText(this, "已取消" + label, Toast.LENGTH_SHORT).show();
                });
            } else {
                repo.addToCollection(currentDetail, type);
                runOnUiThread(() -> {
                    if (type == CollectionType.WANT_TO_WATCH) btnWant.setText("已想看");
                    else btnWatched.setText("已看过");
                    Toast.makeText(this, "已添加到" + label, Toast.LENGTH_SHORT).show();
                });
            }
        }, msg -> {});
    }
}
