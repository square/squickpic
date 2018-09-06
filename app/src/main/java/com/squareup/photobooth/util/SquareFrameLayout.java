package com.squareup.photobooth.util;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class SquareFrameLayout extends FrameLayout {

  // Android is full of nonsense for getting actual screen size including decor. More info at
  // http://stackoverflow.com/questions/1016896/how-to-get-screen-dimensions

  private static int screenWidth = -1;
  private static int screenHeight = -1;

  private static boolean generatedInPortait;

  public static Point getScreenDimens(Context context) {
    if (screenWidth == -1 || screenHeight == -1) {
      initDimens(context);
    }

    if (generatedInPortait == isPortrait(context)) {
      return new Point(screenWidth, screenHeight);
    } else {
      //noinspection SuspiciousNameCombination
      return new Point(screenHeight, screenWidth);
    }
  }

  private static void initDimens(Context context) {
    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    Point screenSize = new Point();
    Display display = windowManager.getDefaultDisplay();
    display.getRealSize(screenSize);
    screenWidth = screenSize.x;
    screenHeight = screenSize.y;
    generatedInPortait = isPortrait(context);
  }

  public static boolean isPortrait(Context context) {
    Configuration config = context.getResources().getConfiguration();
    return config.orientation == Configuration.ORIENTATION_PORTRAIT;
  }

  private final int panelWidth;
  private final int panelHeight;
  private final boolean isLandscape;

  public SquareFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);

    Point screenDimens = getScreenDimens(getContext());
    panelWidth = screenDimens.x;
    panelHeight = screenDimens.y;
    isLandscape = panelWidth > panelHeight;
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec);
    int height = MeasureSpec.getSize(heightMeasureSpec);
    int desiredVerticalPadding;
    int desiredHorizontalPadding;

    if (isLandscape) {
      desiredHorizontalPadding = (width - panelHeight) / 2;
      desiredVerticalPadding = 0;
    } else {
      desiredHorizontalPadding = 0;
      desiredVerticalPadding = (height - panelWidth) / 2;
    }

    setPadding(desiredHorizontalPadding, desiredVerticalPadding, desiredHorizontalPadding,
        desiredVerticalPadding);

    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }
}