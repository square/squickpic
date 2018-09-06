package com.squareup.photobooth.printer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.TextView;
import com.squareup.photobooth.App;
import com.squareup.photobooth.R;
import com.squareup.photobooth.oauth.GoogleOAuthActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PrinterSetupActivity extends AppCompatActivity {

  private CloudPrinter cloudPrinter;
  private TextView printerNameView;
  private PrinterListAdapter listAdapter;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.setup_printer);

    App app = App.from(this);
    cloudPrinter = app.cloudPrinter();

    printerNameView = findViewById(R.id.selected_printer);
    ListView printerList = findViewById(R.id.printer_list);
    listAdapter = new PrinterListAdapter();
    printerList.setAdapter(listAdapter);
    printerList.setOnItemClickListener((parent, view, position, id) -> {
      cloudPrinter.selectPrinter(listAdapter.getItem(position));
      selectedPrinterUpdated();
    });

    findViewById(R.id.clear_printer).setOnClickListener(v -> {
      cloudPrinter.clearPrinter();
      selectedPrinterUpdated();
    });

    findViewById(R.id.oauth_button).setOnClickListener(
        v -> startActivity(new Intent(PrinterSetupActivity.this, GoogleOAuthActivity.class)));

    findViewById(R.id.update_printers).setOnClickListener(v -> downloadPrinterList());

    selectedPrinterUpdated();
    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
  }

  @Override public boolean onSupportNavigateUp() {
    onBackPressed();
    return true;
  }

  private void downloadPrinterList() {
    cloudPrinter.search(new Callback<CloudPrintService.SearchResponse>() {
      @Override public void onResponse(Call<CloudPrintService.SearchResponse> call,
          Response<CloudPrintService.SearchResponse> response) {
        if (response.isSuccessful()) {
          CloudPrintService.SearchResponse searchResponse = response.body();
          if (searchResponse.success) {
            listAdapter.updatePrinterList(searchResponse.printers);
          } else {
            Snackbar.make(printerNameView, "Error: " + searchResponse.message, Snackbar.LENGTH_LONG)
                .show();
          }
        } else {
          Snackbar.make(printerNameView, "Http error", Snackbar.LENGTH_LONG).show();
        }
      }

      @Override public void onFailure(Call<CloudPrintService.SearchResponse> call, Throwable t) {
        Snackbar.make(printerNameView, "Network failure", Snackbar.LENGTH_LONG).show();
      }
    });
  }

  private void selectedPrinterUpdated() {
    String printerName = cloudPrinter.getSelectedPrinterName();
    if (printerName != null) {
      printerNameView.setText("Selected printer: " + printerName);
    } else {
      printerNameView.setText("No printer selected");
    }
  }
}
