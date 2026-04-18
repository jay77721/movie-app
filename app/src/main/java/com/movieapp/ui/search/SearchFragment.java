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
import java.util.*;

/**
 * 搜索页 — 输入关键词搜索电影
 * 支持：实时防抖（500ms）/ 键盘搜索按钮 / API 失败时本地回退
 */
public class SearchFragment extends Fragment {

    private MovieRepository repo;
    private MovieAdapter adapter;
    private ProgressBar progress;
    private TextView tvEmpty;

    // mock 海报 URL 前缀
    private static final String MOCK_COVER_BASE = "https://img3.doubanio.com/view/photo/s_ratio_poster/public/";

    // 本地搜索数据集（API 不可用时回退）
    private static final List<MovieBrief> MOCK_DB = Arrays.asList(
            makeMock("1", "肖申克的救赎", 9.7, "p480747492.jpg", "1994", "剧情/犯罪"),
            makeMock("2", "霸王别姬", 9.6, "p1910813120.jpg", "1993", "剧情/爱情"),
            makeMock("3", "阿甘正传", 9.5, "p512195682.jpg", "1994", "剧情/爱情"),
            makeMock("4", "泰坦尼克号", 9.4, "p457760035.jpg", "1997", "剧情/爱情"),
            makeMock("5", "千与千寻", 9.4, "p2578474613.jpg", "2001", "动画/奇幻"),
            makeMock("6", "星际穿越", 9.4, "p2206088801.jpg", "2014", "科幻/冒险"),
            makeMock("7", "盗梦空间", 9.3, "p513344864.jpg", "2010", "科幻/悬疑"),
            makeMock("8", "楚门的世界", 9.3, "p479682972.jpg", "1998", "剧情/科幻"),
            makeMock("9", "这个杀手不太冷", 9.4, "p511118051.jpg", "1994", "动作/犯罪"),
            makeMock("10", "教父", 9.3, "p616779645.jpg", "1972", "剧情/犯罪")
    );

    private static MovieBrief makeMock(String id, String title, double rating,
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

        // 列表
        adapter = new MovieAdapter();
        adapter.setOnItemClick(m -> {
            Intent intent = new Intent(requireContext(), DetailActivity.class);
            intent.putExtra("movie_id", m.getId());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // 500ms 防抖搜索（输入停止 500ms 后才发起请求）
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
            adapter.setData(Collections.emptyList());
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("输入关键词搜索电影");
            return;
        }
        progress.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        repo.search(query, 1,
                data -> showResults(data),
                msg -> showResults(localSearch(query))
        );
    }

    private void showResults(List<MovieBrief> data) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            progress.setVisibility(View.GONE);
            adapter.setData(data);
            tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
            if (data.isEmpty()) tvEmpty.setText("没有找到相关电影");
        });
    }

    /** 本地模糊搜索（按标题和类型匹配） */
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
