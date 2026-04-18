package com.movieapp.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.movieapp.R;
import com.movieapp.data.repository.MovieRepository;
import com.movieapp.model.MovieBrief;
import com.movieapp.ui.adapter.MovieAdapter;
import java.util.*;

/**
 * 首页 — 热门电影列表
 * 支持：分类标签筛选 / 下拉刷新随机推荐 / 分页加载 / 回到顶部
 */
public class HomeFragment extends Fragment {

    /** 电影点击回调，由 MainActivity 实现 */
    public interface OnMovieClickListener {
        void onMovieClick(String movieId);
    }

    private MovieRepository repo;
    private MovieAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private FloatingActionButton fabScrollTop;
    private OnMovieClickListener listener;
    private LinearLayoutManager layoutManager;

    private String selectedTag = null;  // 当前选中的分类标签，null = 全部
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean hasMore = true;
    private final Random random = new Random();

    private static final int MAX_RANDOM_PAGE = 50; // 随机取页的最大页码

    // 可选分类标签
    private static final List<String> GENRE_TAGS = Arrays.asList(
            "动作", "冒险", "动画", "喜剧", "犯罪", "剧情", "家庭",
            "奇幻", "历史", "恐怖", "音乐", "悬疑", "爱情", "科幻", "惊悚", "战争"
    );

    // mock 海报 URL 前缀（API 不可用时的离线降级数据）
    private static final String MOCK_COVER_BASE = "https://img3.doubanio.com/view/photo/s_ratio_poster/public/";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnMovieClickListener) {
            listener = (OnMovieClickListener) context;
        }
        repo = new MovieRepository(context);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        ChipGroup chipGroup = view.findViewById(R.id.chip_group);
        fabScrollTop = view.findViewById(R.id.fab_scroll_top);

        // 列表
        adapter = new MovieAdapter();
        adapter.setOnItemClick(m -> { if (listener != null) listener.onMovieClick(m.getId()); });
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // 滚动监听：到底部加载更多 + 显示/隐藏回到顶部按钮
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                // 超过 3 个 item 时显示回到顶部按钮
                fabScrollTop.setVisibility(
                        layoutManager.findFirstVisibleItemPosition() > 3
                                ? View.VISIBLE : View.GONE);

                // 向下滚动到底部时加载下一页
                if (dy > 0 && !isLoading && hasMore) {
                    int lastVisible = layoutManager.findLastVisibleItemPosition();
                    if (lastVisible >= layoutManager.getItemCount() - 5) {
                        currentPage++;
                        loadData(false);
                    }
                }
            }
        });

        fabScrollTop.setOnClickListener(v -> recyclerView.smoothScrollToPosition(0));

        // 分类标签
        buildChips(chipGroup);

        // 首次加载 + 下拉刷新
        loadData(true);
        swipeRefresh.setOnRefreshListener(() -> loadData(true));

        return view;
    }

    /** 动态构建分类标签 Chip */
    private void buildChips(ChipGroup chipGroup) {
        chipGroup.removeAllViews();

        // "全部" 标签
        Chip allChip = new Chip(requireContext());
        allChip.setText("全部");
        allChip.setCheckable(true);
        allChip.setChecked(selectedTag == null);
        allChip.setOnClickListener(v -> { selectedTag = null; loadData(true); });
        chipGroup.addView(allChip);

        for (String tag : GENRE_TAGS) {
            Chip chip = new Chip(requireContext());
            chip.setText(tag);
            chip.setCheckable(true);
            chip.setChecked(tag.equals(selectedTag));
            chip.setOnClickListener(v -> { selectedTag = tag; loadData(true); });
            chipGroup.addView(chip);
        }
    }

    /**
     * 加载电影列表
     * @param refresh true=刷新（随机页 + 清空列表），false=加载下一页（追加）
     */
    private void loadData(boolean refresh) {
        if (refresh) {
            currentPage = random.nextInt(MAX_RANDOM_PAGE) + 1; // 随机选页
            hasMore = true;
            swipeRefresh.setRefreshing(true);
        }
        if (isLoading) return;
        isLoading = true;

        MovieRepository.Success<List<MovieBrief>> success = data -> {
            isLoading = false;
            if (refresh) {
                List<MovieBrief> shuffled = new ArrayList<>(data);
                Collections.shuffle(shuffled); // 打乱顺序增加随机感
                updateList(shuffled, true);
            } else {
                updateList(data, false);
            }
            if (data.size() < 20) hasMore = false;
        };

        MovieRepository.Error error = msg -> {
            isLoading = false;
            if (refresh) {
                List<MovieBrief> mock = createMockData();
                Collections.shuffle(mock);
                updateList(mock, true);
            }
            hasMore = false;
        };

        if (selectedTag != null) {
            repo.getByTag(selectedTag, currentPage, success, error);
        } else {
            repo.getHotMovies(currentPage, success, error);
        }
    }

    private void updateList(List<MovieBrief> data, boolean clear) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            if (clear) adapter.setData(data);
            else adapter.appendData(data);
            swipeRefresh.setRefreshing(false);
        });
    }

    // ═══════ 离线降级数据（API 不可用时展示）═══════

    private List<MovieBrief> createMockData() {
        return Arrays.asList(
                makeMovie("1", "肖申克的救赎", 9.7, "p480747492.jpg", "1994", "剧情/犯罪"),
                makeMovie("2", "霸王别姬", 9.6, "p1910813120.jpg", "1993", "剧情/爱情"),
                makeMovie("3", "阿甘正传", 9.5, "p512195682.jpg", "1994", "剧情/爱情"),
                makeMovie("4", "泰坦尼克号", 9.4, "p457760035.jpg", "1997", "剧情/爱情"),
                makeMovie("5", "千与千寻", 9.4, "p2578474613.jpg", "2001", "动画/奇幻"),
                makeMovie("6", "星际穿越", 9.4, "p2206088801.jpg", "2014", "科幻/冒险"),
                makeMovie("7", "盗梦空间", 9.3, "p513344864.jpg", "2010", "科幻/悬疑"),
                makeMovie("8", "楚门的世界", 9.3, "p479682972.jpg", "1998", "剧情/科幻")
        );
    }

    private MovieBrief makeMovie(String id, String title, double rating,
                                  String posterFile, String year, String genres) {
        MovieBrief m = new MovieBrief();
        m.setId(id);
        m.setTitle(title);
        m.setRating(rating);
        m.setCover(MOCK_COVER_BASE + posterFile);
        m.setYear(year);
        m.setGenres(genres);
        return m;
    }
}
