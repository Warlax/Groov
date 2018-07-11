package calex.groov.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import calex.groov.R;
import calex.groov.app.GroovApplication;
import calex.groov.model.GroovRepository;

public class MainActivity extends AppCompatActivity {

  @Inject GroovRepository groovRepository;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ((GroovApplication) getApplication()).getComponent().inject(this);
  }
}
