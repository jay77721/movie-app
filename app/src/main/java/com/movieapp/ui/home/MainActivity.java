package com.movieapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.movieapp.R;
import com.movieapp.ui.collection.CollectionFragment;
import com.movieapp.ui.detail.DetailActivity;
import com.movieapp.ui.search.SearchFragment;

/**
 * 主界面 — 底部导航切换 首页/搜索/收藏 三个页面
 * 用 hide/show 切换 Fragment，避免重复创建
 */
public class MainActivity extends AppCompatActivity implements HomeFragment.OnMovieClickListener {

    private final HomeFragment homeFragment = new HomeFragment();
    private final SearchFragment searchFragment = new SearchFragment();
    private final CollectionFragment collectionFragment = new CollectionFragment();
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // 强制浅色模式
        setContentView(R.layout.activity_main);

        setSupportActionBar((MaterialToolbar) findViewById(R.id.toolbar));

        activeFragment = homeFragment;
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, homeFragment, "home")
                .commit();

        // 底部导航切换
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment target;
            int id = item.getItemId();
            if (id == R.id.nav_home) target = homeFragment;
            else if (id == R.id.nav_search) target = searchFragment;
            else if (id == R.id.nav_fav) target = collectionFragment;
            else return false;
            switchFragment(target);
            return true;
        });
    }

    /** hide/show 切换 Fragment（不销毁，保持状态） */
    private void switchFragment(Fragment target) {
        if (target == activeFragment) return;
        var tx = getSupportFragmentManager().beginTransaction();
        if (!target.isAdded()) tx.add(R.id.fragment_container, target, null);
        tx.hide(activeFragment).show(target).commit();
        activeFragment = target;
    }

    /** 首页电影点击 → 打开详情页 */
    @Override
    public void onMovieClick(String movieId) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("movie_id", movieId);
        startActivity(intent);
    }
}
