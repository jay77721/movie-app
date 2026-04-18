package com.movieapp.ui.collection;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.tabs.TabLayout;
import com.movieapp.R;
import com.movieapp.data.repository.MovieRepository;
import com.movieapp.model.CollectionType;
import com.movieapp.model.MovieBrief;
import com.movieapp.ui.adapter.MovieAdapter;
import com.movieapp.ui.detail.DetailActivity;
import java.util.List;

/**
 * 收藏页 — 「想看」和「已看」两个标签页
 * 每次 onResume 自动刷新（从详情页收藏后回来能立即看到更新）
 */
public class CollectionFragment extends Fragment {

    private MovieRepository repo;
    private MovieAdapter adapter;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvEmpty;
    private CollectionType currentTab = CollectionType.WANT_TO_WATCH;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        repo = new MovieRepository(context);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collection, container, false);

        tabLayout = view.findViewById(R.id.tab_layout);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        tvEmpty = view.findViewById(R.id.tv_empty);

        // 列表
        adapter = new MovieAdapter();
        adapter.setOnItemClick(m -> {
            Intent intent = new Intent(requireContext(), DetailActivity.class);
            intent.putExtra("movie_id", m.getId());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // 标签页
        tabLayout.addTab(tabLayout.newTab().setText("想看"));
        tabLayout.addTab(tabLayout.newTab().setText("已看"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition() == 0
                        ? CollectionType.WANT_TO_WATCH : CollectionType.WATCHED;
                loadCollection();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        swipeRefresh.setOnRefreshListener(this::loadCollection);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCollection(); // 从详情页回来时刷新
    }

    /** 加载收藏列表 */
    private void loadCollection() {
        swipeRefresh.setRefreshing(true);
        repo.getCollection(currentTab, data -> {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                adapter.setData(data);
                swipeRefresh.setRefreshing(false);
                if (data.isEmpty()) {
                    tvEmpty.setText(currentTab == CollectionType.WANT_TO_WATCH
                            ? "还没有想看的电影\n快去首页发现好电影吧！"
                            : "还没有已看过的电影\n开始你的观影之旅吧！");
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }
            });
        }, msg -> {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                swipeRefresh.setRefreshing(false);
                tvEmpty.setText("加载失败，请重试");
                tvEmpty.setVisibility(View.VISIBLE);
            });
        });
    }
}
