package com.squareup.photobooth.snap.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.squareup.photobooth.R;
import com.squareup.photobooth.snap.graphic.GraphicFactory;
import com.squareup.photobooth.util.RecyclerSnaps;

public class GraphicRecyclerViewAdapter
    extends RecyclerView.Adapter<GraphicRecyclerViewAdapter.ViewHolder> {

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    View itemView = inflater.inflate(R.layout.graphic_item, parent, false);
    return new ViewHolder(itemView);
  }

  @Override public void onBindViewHolder(ViewHolder holder, int position) {
    holder.bind(GraphicFactory.values()[position]);
  }

  @Override public int getItemCount() {
    return GraphicFactory.values().length;
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {

    public ViewHolder(View itemView) {
      super(itemView);
    }

    public void bind(GraphicFactory graphicFactory) {
      ((ImageView) itemView).setImageResource(graphicFactory.drawableResId);
      itemView.setOnClickListener((view) -> RecyclerSnaps.snapToView(itemView));
    }
  }
}
