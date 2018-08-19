package calex.groov.activity;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import calex.groov.R;
import calex.groov.app.GroovApplication;
import calex.groov.model.HistoricalRecord;
import calex.groov.model.HistoryViewModel;

public class HistoryActivity extends AppCompatActivity {

  public static Intent newIntent(Context context) {
    return new Intent(context, HistoryActivity.class);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.history_activity);

    ((GroovApplication) getApplication()).getComponent().inject(this);

    HistoryAdapter adapter = new HistoryAdapter(this);
    RecyclerView recyclerView = findViewById(R.id.recycler_view);
    LinearLayoutManager layoutManager =
        new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    recyclerView.setAdapter(adapter);

    HistoryViewModel viewModel = ViewModelProviders.of(this).get(HistoryViewModel.class);
    viewModel.historicalRecords().observe(this, adapter::setRecords);

    getSupportActionBar().setSubtitle(R.string.history);
  }

  private static class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolder> {

    private static final SimpleDateFormat DATE_FORMAT =
        new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private final Context context;
    private final List<HistoricalRecord> records;

    HistoryAdapter(Context context) {
      this.context = Preconditions.checkNotNull(context);
      records = new ArrayList<>();
    }

    public void setRecords(List<HistoricalRecord> records) {
      this.records.clear();
      this.records.addAll(records);
      notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int itemType) {
      return new HistoryViewHolder(
          LayoutInflater.from(context).inflate(R.layout.history_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder viewHolder, int position) {
      HistoricalRecord record = records.get(position);
      viewHolder.dateView.setText(DATE_FORMAT.format(record.date()));
      viewHolder.repsView.setText(String.format(Locale.getDefault(), "%d", record.reps()));
    }

    @Override
    public int getItemCount() {
      return records.size();
    }
  }

  private static class HistoryViewHolder extends RecyclerView.ViewHolder {

    public final TextView dateView;
    public final TextView repsView;

    public HistoryViewHolder(@NonNull View view) {
      super(view);
      dateView = view.findViewById(R.id.date);
      repsView = view.findViewById(R.id.reps);
    }
  }
}
