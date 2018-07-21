package calex.groov.activity;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.time.Clock;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.WorkStatus;
import calex.groov.R;
import calex.groov.app.GroovApplication;
import calex.groov.constant.Constants;
import calex.groov.constant.Keys;
import calex.groov.data.RepSet;
import calex.groov.model.GroovViewModel;
import calex.groov.model.RemindSetting;
import calex.groov.worker.DeleteMostRecentSetWorker;
import calex.groov.worker.ExportWorker;
import calex.groov.worker.ImportWorker;

public class GroovActivity extends AppCompatActivity {

  private static final int SELECT_IMPORT_FILE_REQUEST_CODE = 1;
  private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 2;
  private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 3;

  public static Intent newIntent(Context context) {
    return new Intent(context, GroovActivity.class);
  }

  @Inject Clock clock;
  @Inject WorkManager workManager;

  private GroovViewModel viewModel;
  private TextView repCountView;
  private TextView lastSetView;
  private Button didButton;
  private int reps = Constants.DEFAULT_REPS;
  private String importPath;
  private View deleteLastSetView;
  private CheckBox remindView;
  private CompoundButton.OnCheckedChangeListener doNothingCheckedChangeListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.groov_activity);
    ((GroovApplication) getApplication()).getComponent().inject(this);

    repCountView = findViewById(R.id.count);
    lastSetView = findViewById(R.id.last_set);
    didButton = findViewById(R.id.did_button);
    didButton.setText(generateDidButtonText());
    didButton.setOnClickListener(v -> onDidButtonClicked());
    TextView differentRepsButton = findViewById(R.id.did_different_reps);
    differentRepsButton.setText(Html.fromHtml(differentRepsButton.getText().toString(), 0));
    differentRepsButton.setOnClickListener(v -> onDifferentRepsButtonClicked());
    findViewById(R.id.menu).setOnClickListener(this::onMenuButtonClicked);
    deleteLastSetView = findViewById(R.id.delete_last_set);
    deleteLastSetView.setOnClickListener(v -> onDeleteLastSetButtonClicked());
    remindView = findViewById(R.id.remind);
    doNothingCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton checkBox, boolean checked) {
        checkBox.setOnCheckedChangeListener(null);
        if (checkBox.isChecked()) {
          checkBox.setChecked(false);
        } else {
          checkBox.setChecked(true);
        }
        checkBox.setOnCheckedChangeListener(this);
      }
    };
    remindView.setOnCheckedChangeListener(doNothingCheckedChangeListener);
    remindView.setOnClickListener(v -> onRemindCheckBoxClicked());

    viewModel = ViewModelProviders.of(this).get(GroovViewModel.class);

    viewModel.repsToday().observe(
        this, reps -> repCountView.setText(String.format(Locale.getDefault(), "%d", reps)));
    viewModel.mostRecentSet().observe(this, this::onMostRecentSetChanged);
    viewModel.remind().observe(this, this::onRemindChanged);
  }

  private void onRemindChanged(RemindSetting remindSetting) {
    remindView.setOnCheckedChangeListener(null);
    remindView.setChecked(remindSetting.enabled());
    remindView.setOnCheckedChangeListener(doNothingCheckedChangeListener);
    remindView.setText(getString(R.string.remind_template, remindSetting.intervalMins()));
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    if (requestCode == SELECT_IMPORT_FILE_REQUEST_CODE) {
      if (resultCode != RESULT_OK || data == null) {
        return;
      }

      Uri uri = data.getData();
      if (uri == null) {
        return;
      }

      importPath = uri.toString();
      importSets();
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    switch (requestCode) {
      case WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE:
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          exportSets();
        }
        break;

      case READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE:
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          importSets();
        }
        break;
    }
  }

  private void onDeleteLastSetButtonClicked() {
    workManager.enqueue(new OneTimeWorkRequest.Builder(DeleteMostRecentSetWorker.class).build());
  }

  private void onRemindCheckBoxClicked() {
    new AlertDialog.Builder(this)
        .setTitle(R.string.title_remind_me_dialog)
        .setItems(R.array.remind_me_dialog_items, (dialogInterface, which) -> {
          switch (which) {
            case 0:
              viewModel.setRemind(false, 0);
              break;

            case 1:
              viewModel.setRemind(true, 15);
              break;

            case 2:
              viewModel.setRemind(true, 20);
              break;

            case 3:
              viewModel.setRemind(true, 30);
              break;

            case 4:
              viewModel.setRemind(true, 45);
              break;

            case 5:
              viewModel.setRemind(true, 60);
              break;

            case 6:
              viewModel.setRemind(true, 90);
              break;

            case 7:
              viewModel.setRemind(true, 120);
              break;

            case 8:
              viewModel.setRemind(true, 180);
              break;

          }
        })
        .show();
  }

  private void onMenuButtonClicked(View menuButton) {
    PopupMenu popupMenu = new PopupMenu(this, menuButton);
    popupMenu.inflate(R.menu.menu);
    popupMenu.setOnMenuItemClickListener(menuItem -> {
      switch (menuItem.getItemId()) {
        case R.id.menu_import:
          Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
          intent.addCategory(Intent.CATEGORY_OPENABLE);
          intent.setType("text/*");
          startActivityForResult(intent, SELECT_IMPORT_FILE_REQUEST_CODE);
          break;

        case R.id.menu_export:
          exportSets();
          break;
      }
      return true;
    });
    popupMenu.show();
  }

  private void exportSets() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
      if (ActivityCompat.shouldShowRequestPermissionRationale(
          this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.title_export_permissions_rationale)
            .setMessage(R.string.message_export_permissions_rationale)
            .setPositiveButton(R.string.ok, (dialogInterface, which) -> {
              ActivityCompat.requestPermissions(
                  this,
                  new String[] {
                      Manifest.permission.WRITE_EXTERNAL_STORAGE,
                  },
                  WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
      } else {
        ActivityCompat.requestPermissions(
            this,
            new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            },
            WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
      }
      return;
    }

    WorkRequest workRequest = new OneTimeWorkRequest.Builder(ExportWorker.class).build();
    workManager.enqueue(workRequest);
    workManager.getStatusById(workRequest.getId()).observe(
        this,
        new Observer<WorkStatus>() {
          @Override
          public void onChanged(@Nullable WorkStatus workStatus) {
            if (workStatus.getState().isFinished()) {
              workManager.getStatusById(workRequest.getId()).removeObserver(this);
              Data outputData = workStatus.getOutputData();
              if (outputData == null) {
                return;
              }

              String path = outputData.getString(Keys.PATH, null);
              if (path == null) {
                return;
              }

              Uri uri = FileProvider.getUriForFile(
                  GroovActivity.this,
                  getApplicationContext().getPackageName() + ".calex.groov.provider",
                  new File(path));
              Intent intent = new Intent();
              intent.setAction(Intent.ACTION_SEND);
              intent.setType("text/csv");
              intent.putExtra(Intent.EXTRA_STREAM, uri);
              intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
              try {
                startActivity(intent);
              } catch (ActivityNotFoundException e) {
                new AlertDialog.Builder(GroovActivity.this)
                    .setMessage(getString(R.string.no_activity_found, path))
                    .setPositiveButton(R.string.ok, null)
                    .show();
              }
            }
          }
        });
  }

  private void importSets() {
    if (importPath == null) {
      return;
    }

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED) {
      if (ActivityCompat.shouldShowRequestPermissionRationale(
          this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.title_import_permissions_rationale)
            .setMessage(R.string.message_import_permissions_rationale)
            .setPositiveButton(R.string.ok, (dialogInterface, which) -> {
              ActivityCompat.requestPermissions(
                  this,
                  new String[] {
                      Manifest.permission.READ_EXTERNAL_STORAGE,
                  },
                  READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
      } else {
        ActivityCompat.requestPermissions(
            this,
            new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE,
            },
            READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
      }
      return;
    }

    WorkRequest workRequest = new OneTimeWorkRequest.Builder(ImportWorker.class)
        .setInputData(new Data.Builder().putString(Keys.PATH, importPath).build())
        .build();
    workManager.enqueue(workRequest);
    workManager.getStatusById(workRequest.getId()).observe(
        this,
        new Observer<WorkStatus>() {
          @Override
          public void onChanged(@Nullable WorkStatus workStatus) {
            if (workStatus.getState().isFinished()) {
              workManager.getStatusById(workRequest.getId()).removeObserver(this);
              Toast.makeText(GroovActivity.this, R.string.sets_imported, Toast.LENGTH_SHORT).show();
            }
          }
        });
  }

  private void onDidButtonClicked() {
    viewModel.recordSet(reps);
  }

  private void onDifferentRepsButtonClicked() {
    View view = View.inflate(this, R.layout.reps_input, null);
    EditText editText = view.findViewById(R.id.reps);
    new AlertDialog.Builder(this)
        .setView(view)
        .setPositiveButton(R.string.ok, (dialog, which) -> {
          String repsString = editText.getText().toString();
          int reps = !TextUtils.isEmpty(repsString) ? Integer.parseInt(repsString) : 0;
          if (reps < 1) {
            new AlertDialog.Builder(this)
                .setMessage(R.string.error_less_than_one_rep)
                .setPositiveButton(R.string.ok, null)
                .show();
            return;
          }
          GroovActivity.this.reps = reps;
          viewModel.recordSet(reps);
          hideKeyboard();
        })
        .setNegativeButton(R.string.cancel, (d, i) -> {
          hideKeyboard();
        })
        .show();
    editText.requestFocus();
    showKeyboard();
  }

  private void showKeyboard() {
    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    if (imm != null) {
      imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }
  }

  private void hideKeyboard() {
    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    if (imm != null) {
      imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
  }

  private void onMostRecentSetChanged(Optional<RepSet> repSetOptional) {
    lastSetView.setVisibility(repSetOptional.isPresent() ? View.VISIBLE : View.GONE);
    deleteLastSetView.setVisibility(repSetOptional.isPresent() ? View.VISIBLE : View.GONE);
    if (repSetOptional.isPresent()) {
      RepSet repSet = repSetOptional.get();
      lastSetView.setText(
          getString(
              R.string.last_set_template,
              repSet.getReps(),
              DateUtils.getRelativeTimeSpanString(
                  repSet.getDate().getTime(), clock.millis(), TimeUnit.MINUTES.toMillis(1))));
      reps = repSet.getReps();
      didButton.setText(generateDidButtonText());
    }
  }

  private CharSequence generateDidButtonText() {
    return getString(R.string.did_button_template, reps);
  }
}
