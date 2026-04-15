package com.movieapp.ui.adapter;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.movieapp.R;
import com.movieapp.model.MovieBrief;
import java.util.*;

/** 电影列表适配器 — 将 MovieBrief 数据绑定到 RecyclerView 卡片 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.VH> {

    /** 点击回调 */
    public interface OnItemClick {
        void onClick(MovieBrief movie);
    }

    private final List<MovieBrief> data = new ArrayList<>();
    private OnItemClick listener;

    public void setOnItemClick(OnItemClick l) { listener = l; }

    /** 替换全部数据 */
    public void setData(List<MovieBrief> list) {
        data.clear();
        data.addAll(list);
        notifyDataSetChanged();
    }

    /** 追加数据（分页加载） */
    public void appendData(List<MovieBrief> list) {
        int start = data.size();
        data.addAll(list);
        notifyItemRangeInserted(start, list.size());
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        MovieBrief m = data.get(pos);
        h.tvTitle.setText(m.getTitle());
        h.tvRating.setText(String.format("%.1f", m.getRating()));

        // 年份 + 上映日期
        String yearText = m.getYear() != null ? m.getYear() : "";
        if (m.getReleaseDate() != null && !m.getReleaseDate().isEmpty()) {
            yearText += " · " + m.getReleaseDate();
        }
        h.tvYear.setText(yearText);

        h.tvGenres.setText(m.getGenres() != null ? m.getGenres() : "");

        // 封面图
        Glide.with(h.itemView.getContext())
                .load(m.getCover())
                .placeholder(R.drawable.bg_gray)
                .into(h.ivCover);

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(m); });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvRating, tvYear, tvGenres;
        VH(View v) {
            super(v);
            ivCover = v.findViewById(R.id.iv_cover);
            tvTitle = v.findViewById(R.id.tv_title);
            tvRating = v.findViewById(R.id.tv_rating);
            tvYear = v.findViewById(R.id.tv_year);
            tvGenres = v.findViewById(R.id.tv_genres);
        }
    }
}
