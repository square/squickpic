package com.squareup.photobooth.snap;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.squareup.photobooth.App;
import com.squareup.photobooth.R;
import com.squareup.photobooth.result.DisplayPictureActivity;
import com.squareup.photobooth.settings.SettingsStore;
import com.squareup.photobooth.snap.camera.CameraSourcePreview;
import com.squareup.photobooth.snap.camera.GraphicOverlay;
import com.squareup.photobooth.snap.graphic.FaceGraphic;
import com.squareup.photobooth.snap.graphic.GraphicFactory;
import com.squareup.photobooth.util.Activities;
import com.squareup.photobooth.util.FullscreenActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import timber.log.Timber;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.squareup.photobooth.util.Animators.onAnimationEnd;
import static com.squareup.photobooth.util.RecyclerSnaps.centerSnap;

public class SnapActivity extends FullscreenActivity {

  // Higher frame rates can lead to dark preview
  // https://github.com/googlesamples/android-vision/issues/162#issuecomment-271508706
  //private static final float MAX_FRAME_RATE = 15.0f;
  // On the other hands, 30fps works fine on Pixel-C and lower leads to lag on activity transition.
  private static final float MAX_FRAME_RATE = 30f;
  private static final int COUNT_DOWN_SECONDS = 4;

  private CameraSource cameraSource = null;

  private CameraSourcePreview preview;
  private GraphicOverlay graphicOverlay;

  private static final int RC_HANDLE_GMS = 9001;
  private static final int PERMISSION_REQUEST_CODE = 1;

  private volatile int graphicIndex;

  private final List<GraphicFaceTracker> faceTrackers = new ArrayList<>();
  private BitmapBytesHolder bitmapBytesHolder;

  private int countDown = 0;
  private TextView countDownView;
  private SettingsStore settingsStore;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.snap);

    App app = App.from(this);
    bitmapBytesHolder = app.bitmapHolder();
    settingsStore = app.settingsStore();

    preview = findViewById(R.id.preview);
    countDownView = findViewById(R.id.count_down);

    graphicOverlay = preview.getGraphicOverlay();

    if (hasPermission(CAMERA) && hasPermission(WRITE_EXTERNAL_STORAGE)) {
      createCameraSource();
    } else {
      requestRequiredPermissions();
    }

    RecyclerView recyclerView = findViewById(R.id.graphic_recycler_view);

    centerSnap(recyclerView, (position) -> updateGraphicFactory(GraphicFactory.values()[position]));
  }

  private boolean hasPermission(String permission) {
    return ActivityCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED;
  }

  public void updateGraphicFactory(GraphicFactory graphicFactory) {
    graphicIndex = graphicFactory.ordinal();
    for (GraphicFaceTracker faceTracker : faceTrackers) {
      faceTracker.faceGraphic = createFaceGraphic();
    }
    graphicOverlay.postInvalidate();
  }

  private FaceGraphic createFaceGraphic() {
    return GraphicFactory.values()[graphicIndex].create(this);
  }

  private void requestRequiredPermissions() {
    String[] permissions = new String[] { CAMERA, WRITE_EXTERNAL_STORAGE };

    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, CAMERA)
        && !ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)) {
      ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
      return;
    }

    View.OnClickListener listener =
        view -> ActivityCompat.requestPermissions(SnapActivity.this, permissions,
            PERMISSION_REQUEST_CODE);

    Snackbar.make(graphicOverlay, "Access to camera and external storage permissions required.",
        Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, listener).show();
  }

  private void createCameraSource() {
    Context context = getApplicationContext();
    FaceDetector detector = createFaceDetector(context);

    detector.setProcessor(new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory()).build());

    if (!detector.isOperational()) {
      Timber.d("Face detector dependencies are not yet available.");
    }

    cameraSource = new CameraSource.Builder(context, detector) //
        // Camera will decide actual size, and we'll crop accordingly in layout.
        .setRequestedPreviewSize(640, 480)
        .setFacing(CameraSource.CAMERA_FACING_FRONT)
        .setRequestedFps(MAX_FRAME_RATE)
        .setAutoFocusEnabled(true)
        .build();
    findViewById(R.id.photo_button).setOnClickListener(v -> startCountDown());
  }

  private void startCountDown() {
    countDown = COUNT_DOWN_SECONDS + 1;
    countDownView.setVisibility(View.VISIBLE);
    countDown();
  }

  private void countDown() {
    countDownView.setScaleX(1);
    countDownView.setScaleY(1);
    countDownView.animate()
        .setDuration(1000)
        .scaleX(0)
        .scaleY(0)
        .setListener(onAnimationEnd((animator) -> {
          if (countDown == COUNT_DOWN_SECONDS + 1) {
            // count down got restarted.
            return;
          }
          if (countDown == 1) {
            countDownView.setVisibility(View.INVISIBLE);
            takePicture();
          } else {
            countDown();
          }
        }));
    countDown--;
    countDownView.setText(String.format(Locale.getDefault(), "%d", countDown));
  }

  private void takePicture() {
    CameraSource.ShutterCallback shutterCallback = () -> {
    };
    CameraSource.PictureCallback pictureCallback = data -> {
      bitmapBytesHolder.hold(data);
      DisplayPictureActivity.start(this, graphicIndex, PictureRenderer.PRINTED_CARD_RATIO);
    };
    try {
      cameraSource.takePicture(shutterCallback, pictureCallback);
    } catch (RuntimeException e) {
      // Sometimes Camera.native_takePicture throws with no explanation.
      Timber.d(e, "takePicture native code threw an exception");
      Toast.makeText(this, "Failed to take picture", Toast.LENGTH_SHORT).show();
    }
  }

  private FaceDetector createFaceDetector(Context context) {
    return new FaceDetector.Builder(context) //
        .setLandmarkType(FaceDetector.ALL_LANDMARKS) //
        .setClassificationType(FaceDetector.NO_CLASSIFICATIONS) //
        .setTrackingEnabled(true).setMode(FaceDetector.FAST_MODE).build();
  }

  @Override protected void onResume() {
    super.onResume();
    startCameraSource();
  }

  @Override protected void onPause() {
    super.onPause();
    preview.stop();
  }

  @Override public void onBackPressed() {

    if (settingsStore.arePaymentsEnabled()) {
      new AlertDialog.Builder(this).setTitle("Are you sure?")
          .setMessage("You will lose your credit if you leave now")
          .setPositiveButton("No Picture for me!", (dialog, which) -> finish())
          .setNegativeButton("Stay here", null)
          .show();
    } else {
      if (Activities.isAppInLockTaskMode(this)) {
        Toast.makeText(this, "App task is locked", Toast.LENGTH_SHORT).show();
        return;
      } else {
        finish();
      }
    }
  }

  /**
   * Releases the resources associated with the camera source, the associated detector, and the
   * rest of the processing pipeline.
   */
  @Override protected void onDestroy() {
    super.onDestroy();
    if (cameraSource != null) {
      cameraSource.release();
    }
  }

  @Override public void onRequestPermissionsResult(int requestCode, String[] permissions,
      int[] grantResults) {
    if (requestCode != PERMISSION_REQUEST_CODE) {
      Timber.d("Got unexpected permission result: " + requestCode);
      super.onRequestPermissionsResult(requestCode, permissions, grantResults);
      return;
    }

    if (grantResults.length == 2
        && grantResults[0] == PERMISSION_GRANTED
        && grantResults[1] == PERMISSION_GRANTED) {
      Timber.d("Permissions granted - initialize the camera source");
      createCameraSource();
      return;
    }

    new AlertDialog.Builder(this).setTitle("Camera or storage permission missing")
        .setMessage("The camera and storage permissions are required to take pictures.")
        .setPositiveButton(R.string.ok, (dialog, id) -> finish())
        .show();
  }

  private void startCameraSource() {
    // check that the device has play services available.
    int code =
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());
    if (code != ConnectionResult.SUCCESS) {
      GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS).show();
    }
    if (cameraSource != null) {
      preview.start(cameraSource, graphicOverlay, PictureRenderer.PRINTED_CARD_RATIO);
    }
  }

  private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
    @Override public Tracker<Face> create(Face face) {
      return new GraphicFaceTracker();
    }
  }

  private class GraphicFaceTracker extends Tracker<Face> implements Graphic {
    // Updated when changing graphic.
    private FaceGraphic faceGraphic;

    @Override public void onNewItem(int i, Face face) {
      faceGraphic = createFaceGraphic();
      faceTrackers.add(this);
    }

    @Override public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
      faceGraphic.updateFace(face);
      graphicOverlay.update(this);
    }

    @Override public void onMissing(FaceDetector.Detections<Face> detectionResults) {
      graphicOverlay.forget(this);
      faceGraphic.forgetFace();
    }

    @Override public void onDone() {
      graphicOverlay.forget(this);
      faceGraphic.forgetFace();
      faceTrackers.remove(this);
    }

    @Override public void draw(Canvas canvas) {
      faceGraphic.draw(canvas, graphicOverlay);
    }
  }
}
