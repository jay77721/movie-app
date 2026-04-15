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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 首页 — 热门电影列表 + 分类标签筛选 + 分页加载 + 随机推荐
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
    private String selectedTag = null; // null = 全部
    private LinearLayoutManager layoutManager;

    // 分页相关
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean hasMore = true;
    private final Random random = new Random();

    // TMDB 热门电影总页数（保守估计，用于随机取页）
    private static final int MAX_RANDOM_PAGE = 50;

    // TMDB 类型标签（与后端返回一致）
    private static final List<String> GENRE_TAGS = Arrays.asList(
            "动作", "冒险", "动画", "喜剧", "犯罪", "剧情", "家庭",
            "奇幻", "历史", "恐怖", "音乐", "悬疑", "爱情", "科幻", "惊悚", "战争"
    );

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

        // 列表适配器
        adapter = new MovieAdapter();
        adapter.setOnItemClick(m -> { if (listener != null) listener.onMovieClick(m.getId()); });
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // 滚动监听：到底部加载更多 + 显示/隐藏回到顶部按钮
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                // 距顶部超过一屏时显示回到顶部按钮
                int firstVisible = layoutManager.findFirstVisibleItemPosition();
                if (firstVisible > 3) {
                    fabScrollTop.show();
                } else {
                    fabScrollTop.hide();
                }

                // 向下滚动到底部时加载下一页
                if (dy <= 0) return;
                int totalItemCount = layoutManager.getItemCount();
                int lastVisible = layoutManager.findLastVisibleItemPosition();
                if (!isLoading && hasMore && lastVisible >= totalItemCount - 5) {
                    currentPage++;
                    loadData(false);
                }
            }
        });

        // 回到顶部按钮
        fabScrollTop.setOnClickListener(v -> {
            recyclerView.smoothScrollToPosition(0);
        });

        // 分类标签
        buildChips(chipGroup);

        // 首次加载 + 下拉刷新（随机推荐）
        loadData(true);
        swipeRefresh.setOnRefreshListener(() -> loadData(true));

        return view;
    }

    /** 构建分类标签栏 */
    private void buildChips(ChipGroup chipGroup) {
        chipGroup.removeAllViews();

        // "全部" 标签
        Chip allChip = new Chip(requireContext());
        allChip.setText("全部");
        allChip.setCheckable(true);
        allChip.setChecked(selectedTag == null);
        allChip.setOnClickListener(v -> { selectedTag = null; loadData(true); });
        chipGroup.addView(allChip);

        // 类型标签
        for (String tag : GENRE_TAGS) {
            Chip chip = new Chip(requireContext());
            chip.setText(tag);
            chip.setCheckable(true);
            chip.setChecked(tag.equals(selectedTag));
            chip.setOnClickListener(v -> { selectedTag = tag; loadData(true); });
            chipGroup.addView(chip);
        }
    }

    /** 加载电影列表（支持分页 + 随机推荐）
     *  @param refresh true=刷新（随机页，清空列表），false=加载下一页（追加） */
    private void loadData(boolean refresh) {
        if (refresh) {
            // 随机选一页，每次刷新看到不同电影
            currentPage = random.nextInt(MAX_RANDOM_PAGE) + 1;
            hasMore = true;
            swipeRefresh.setRefreshing(true);
        }
        if (isLoading) return;
        isLoading = true;

        MovieRepository.Success<List<MovieBrief>> success = data -> {
            isLoading = false;
            if (refresh) {
                // 刷新时打乱顺序，增加随机感
                List<MovieBrief> shuffled = new ArrayList<>(data);
                Collections.shuffle(shuffled);
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
            if (clear) {
                adapter.setData(data);
            } else {
                adapter.appendData(data);
            }
            swipeRefresh.setRefreshing(false);
        });
    }

    // ═══════ API 不可用时的本地模拟数据 ═══════

    private List<MovieBrief> createMockData() {
        String base = "https://img3.doubanio.com/view/photo/s_ratio_poster/public/";
        List<MovieBrief> list = new ArrayList<>();
        list.add(makeMovie("1", "肖申克的救赎", 9.7, base + "p480747492.jpg", "1994", "剧情/犯罪"));
        list.add(makeMovie("2", "霸王别姬", 9.6, base + "p1910813120.jpg", "1993", "剧情/爱情"));
        list.add(makeMovie("3", "阿甘正传", 9.5, base + "p512195682.jpg", "1994", "剧情/爱情"));
        list.add(makeMovie("4", "泰坦尼克号", 9.4, base + "p457760035.jpg", "1997", "剧情/爱情"));
        list.add(makeMovie("5", "千与千寻", 9.4, base + "p2578474613.jpg", "2001", "动画/奇幻"));
        list.add(makeMovie("6", "星际穿越", 9.4, base + "p2206088801.jpg", "2014", "科幻/冒险"));
        list.add(makeMovie("7", "盗梦空间", 9.3, base + "p513344864.jpg", "2010", "科幻/悬疑"));
        list.add(makeMovie("8", "楚门的世界", 9.3, base + "p479682972.jpg", "1998", "剧情/科幻"));
        return list;
    }

    private MovieBrief makeMovie(String id, String title, double rating, String cover,
                                 String year, String genres) {
        MovieBrief m = new MovieBrief();
        m.setId(id); m.setTitle(title); m.setRating(rating); m.setCover(cover);
        m.setYear(year); m.setGenres(genres);
        return m;
    }
}
