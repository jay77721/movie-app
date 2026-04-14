package com.movieapp.ui.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.movieapp.R;
import com.movieapp.data.repository.MovieRepository;
import com.movieapp.model.MovieBrief;
import com.movieapp.ui.adapter.MovieAdapter;
import com.movieapp.ui.detail.DetailActivity;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索页 — 输入关键词搜索电影，支持防抖
 */
public class SearchFragment extends Fragment {

    private MovieRepository repo;
    private MovieAdapter adapter;
    private ProgressBar progress;
    private TextView tvEmpty;

    // 本地搜索数据集（API 不可用时回退）
    private static final List<MovieBrief> MOCK_DB = buildMockDb();

    private static List<MovieBrief> buildMockDb() {
        String base = "https://img3.doubanio.com/view/photo/s_ratio_poster/public/";
        List<MovieBrief> list = new ArrayList<>();
        list.add(make("1", "肖申克的救赎", 9.7, base + "p480747492.jpg", "1994", "剧情/犯罪"));
        list.add(make("2", "霸王别姬", 9.6, base + "p1910813120.jpg", "1993", "剧情/爱情"));
        list.add(make("3", "阿甘正传", 9.5, base + "p512195682.jpg", "1994", "剧情/爱情"));
        list.add(make("4", "泰坦尼克号", 9.4, base + "p457760035.jpg", "1997", "剧情/爱情"));
        list.add(make("5", "千与千寻", 9.4, base + "p2578474613.jpg", "2001", "动画/奇幻"));
        list.add(make("6", "星际穿越", 9.4, base + "p2206088801.jpg", "2014", "科幻/冒险"));
        list.add(make("7", "盗梦空间", 9.3, base + "p513344864.jpg", "2010", "科幻/悬疑"));
        list.add(make("8", "楚门的世界", 9.3, base + "p479682972.jpg", "1998", "剧情/科幻"));
        list.add(make("9", "这个杀手不太冷", 9.4, base + "p511118051.jpg", "1994", "动作/犯罪"));
        list.add(make("10", "教父", 9.3, base + "p616779645.jpg", "1972", "剧情/犯罪"));
        return list;
    }

    private static MovieBrief make(String id, String title, double rating, String cover,
                                    String year, String genres) {
        MovieBrief m = new MovieBrief();
        m.setId(id); m.setTitle(title); m.setRating(rating); m.setCover(cover);
        m.setYear(year); m.setGenres(genres);
        return m;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        repo = new MovieRepository(context);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        EditText etSearch = view.findViewById(R.id.et_search);
        progress = view.findViewById(R.id.progress);
        tvEmpty = view.findViewById(R.id.tv_empty);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);

        adapter = new MovieAdapter();
        adapter.setOnItemClick(m -> {
            Intent intent = new Intent(requireContext(), DetailActivity.class);
            intent.putExtra("movie_id", m.getId());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // 500ms 防抖搜索
        android.os.Handler handler = new android.os.Handler();
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> doSearch(s.toString().trim()), 500);
            }
        });

        // 键盘搜索按钮
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch(etSearch.getText().toString().trim());
                return true;
            }
            return false;
        });

        return view;
    }

    /** 执行搜索（API → 失败用本地数据） */
    private void doSearch(String query) {
        if (query.isEmpty()) {
            adapter.setData(java.util.Collections.emptyList());
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("输入关键词搜索电影");
            return;
        }
        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        repo.search(query, 1, data -> showResults(data), msg -> showResults(localSearch(query)));
    }

    private void showResults(List<MovieBrief> data) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            progress.setVisibility(View.GONE);
            adapter.setData(data);
            if (data.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("没有找到相关电影");
            } else {
                tvEmpty.setVisibility(View.GONE);
            }
        });
    }

    /** 本地模糊搜索 */
    private List<MovieBrief> localSearch(String query) {
        String q = query.toLowerCase();
        List<MovieBrief> results = new ArrayList<>();
        for (MovieBrief m : MOCK_DB) {
            if ((m.getTitle() != null && m.getTitle().toLowerCase().contains(q)) ||
                (m.getGenres() != null && m.getGenres().toLowerCase().contains(q))) {
                results.add(m);
            }
        }
        return results;
    }
}
