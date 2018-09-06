package com.squareup.photobooth.util;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.View;
import android.view.ViewTreeObserver;
import com.squareup.photobooth.snap.ui.GraphicRecyclerViewAdapter;

public class RecyclerSnaps {

  public interface OnItemSnapListener {
    void onItemSnap(int position);
  }

  public static void snapToView(View view) {
    RecyclerView recyclerView = (RecyclerView) view.getParent();
    int center = (recyclerView.getLeft() + recyclerView.getRight()) / 2;
    int horizontalScroll = center - view.getLeft() - view.getWidth() / 2;
    recyclerView.smoothScrollBy(-horizontalScroll, 0);
  }

  public static void centerSnap(RecyclerView recyclerView, OnItemSnapListener listener) {
    Context context = recyclerView.getContext();
    SnapHelper snapHelper = new LinearSnapHelper();
    snapHelper.attachToRecyclerView(recyclerView);
    LinearLayoutManager layoutManager =
        new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(new GraphicRecyclerViewAdapter());
    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
          int first = layoutManager.findFirstVisibleItemPosition();
          int last = layoutManager.findLastVisibleItemPosition();
          int viewCenter = (recyclerView.getLeft() + recyclerView.getRight()) / 2;
          int centerPosition = -1;
          for (int i = first; i <= last; i++) {
            View view = layoutManager.findViewByPosition(i);
            if (view.getLeft() < viewCenter && view.getRight() > viewCenter) {
              centerPosition = i;
              break;
            }
          }
          if (centerPosition != -1) {
            listener.onItemSnap(centerPosition);
          }
        }
      }
    });
    recyclerView.smoothScrollToPosition(0);
    // We use padding to enable centering the first and last element.
    recyclerView.setClipToPadding(false);

    recyclerView.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            // Add left padding to center first item
            View view = layoutManager.findViewByPosition(0);
            RecyclerView.LayoutParams layoutParams =
                (RecyclerView.LayoutParams) view.getLayoutParams();
            int leftPadding =
                recyclerView.getWidth() / 2 - view.getWidth() / 2 - layoutParams.getMarginStart();
            recyclerView.setPadding(leftPadding, 0, recyclerView.getWidth() / 2, 0);
          }
        });
  }

  private RecyclerSnaps() {
    throw new AssertionError();
  }
}
