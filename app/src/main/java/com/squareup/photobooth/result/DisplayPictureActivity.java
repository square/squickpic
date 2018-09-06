package com.squareup.photobooth.result;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.print.PrintHelper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.photobooth.R;
import com.squareup.photobooth.printer.CloudPrinter;
import com.squareup.photobooth.start.KioskStartActivity;
import com.squareup.photobooth.twitter.Tweeter;
import com.squareup.photobooth.util.FullscreenActivity;

import static com.squareup.photobooth.printer.CloudPrinter.PRINT_FILENAME;

public class DisplayPictureActivity extends FullscreenActivity {

  private static final String GRAPHIC_INDEX_EXTRA = "graphicIndex";
  private static final String PICTURE_RATIO_EXTRA = "pictureRatio";

  public static void start(Activity activity, int graphicIndex, float pictureRatio) {
    Intent intent = new Intent(activity, DisplayPictureActivity.class);
    intent.putExtra(GRAPHIC_INDEX_EXTRA, graphicIndex);
    intent.putExtra(PICTURE_RATIO_EXTRA, pictureRatio);
    activity.startActivity(intent);
  }

  private PictureViewModel pictureViewModel;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.result);

    findViewById(R.id.back).setOnClickListener(v -> finish());
    ImageView pictureView = findViewById(R.id.picture);

    TextView printButton = findViewById(R.id.print);
    TextView printAndTweetButton = findViewById(R.id.print_tweet);

    printButton.setEnabled(false);
    printAndTweetButton.setEnabled(false);

    Tweeter tweeter = new Tweeter();
    if (!tweeter.isLoggedIn()) {
      printAndTweetButton.setVisibility(View.GONE);
    }

    printButton.setOnClickListener(__ -> submit(false));
    printAndTweetButton.setOnClickListener(__ -> submit(true));

    pictureViewModel = ViewModelProviders.of(this).get(PictureViewModel.class);
    if (savedInstanceState == null) {
      Intent intent = getIntent();
      int graphicIndex = intent.getIntExtra(GRAPHIC_INDEX_EXTRA, -1);
      float pictureRatio = intent.getFloatExtra(PICTURE_RATIO_EXTRA, -1f);
      pictureViewModel.renderBitmap(graphicIndex, pictureRatio);
    }

    pictureViewModel.renderedBitmap().observe(this, (bitmap) -> {
      printButton.setEnabled(true);
      printAndTweetButton.setEnabled(true);
      pictureView.setImageBitmap(bitmap);
    });
  }

  private void submit(boolean tweet) {
    boolean printingAsync = pictureViewModel.submit(tweet);
    if (printingAsync) {
      done();
    } else {
      localPrint();
    }
  }

  private void localPrint() {
    Bitmap bitmap = pictureViewModel.renderedBitmap().getValue();
    PrintHelper photoPrinter = new PrintHelper(this);
    photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
    photoPrinter.printBitmap(PRINT_FILENAME, bitmap, this::done);
  }

  private void done() {
    Intent intent = new Intent(this, KioskStartActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(intent);
  }
}
