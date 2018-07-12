package calex.groov.activity;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.time.Clock;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import calex.groov.R;
import calex.groov.app.GroovApplication;
import calex.groov.data.RepSet;
import calex.groov.model.GroovRepository;
import calex.groov.model.GroovViewModel;

public class GroovActivity extends AppCompatActivity {

  private static final int DEFAULT_REPS = 3;

  @Inject GroovRepository groovRepository;
  @Inject Clock clock;

  private GroovViewModel viewModel;
  private TextView repCountView;
  private TextView lastSetView;
  private Button didButton;
  private int reps = DEFAULT_REPS;

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

    viewModel = ViewModelProviders.of(this).get(GroovViewModel.class);

    viewModel.repsToday().observe(
        this, reps -> repCountView.setText(String.format(Locale.getDefault(), "%d", reps)));
    viewModel.mostRecentSet().observe(this, this::onMostRecentSetChanged);
  }

  private void onDidButtonClicked() {
    viewModel.recordSet(reps);
    Toast.makeText(this, getString(R.string.reps_added, reps), Toast.LENGTH_SHORT).show();
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
          Toast.makeText(this, getString(R.string.reps_added, reps), Toast.LENGTH_SHORT).show();
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
