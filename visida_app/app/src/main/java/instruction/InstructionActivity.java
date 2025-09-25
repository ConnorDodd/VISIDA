package instruction;

import android.content.Intent;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import au.edu.newcastle.jnc985.visida.R;

import static bo.AppConstants.ACTIVITY_LOG_TAG;
import static instruction.InstructionHandler.INSTRUCTION;

public class InstructionActivity extends AppCompatActivity {
    private static final String TAG = "InstructionActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);

        setTitle(R.string.instruction);

        Instruction instruction = null;
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        if(extras != null){
             instruction = extras.getParcelable(INSTRUCTION);
        }

        InstructionFragmentAdapter adapter = new InstructionFragmentAdapter(this, getSupportFragmentManager(), instruction);

        ViewPager pager = findViewById(R.id.instructionPager);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int currentPosition = 0;
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {
                Log.i(ACTIVITY_LOG_TAG, TAG + ": Swiped to page " + position);
                FragmentLifecycle fragmentToShow = (FragmentLifecycle)adapter.getItem(position);
                fragmentToShow.onFragmentResume();

                FragmentLifecycle fragmentToHide = (FragmentLifecycle)adapter.getItem(currentPosition);
                fragmentToHide.onFragmentPause();

                currentPosition = position;
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        pager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(pager, true);

        ImageButton closeButton = findViewById(R.id.btnDismiss);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(ACTIVITY_LOG_TAG, TAG + ": Closed");
                finish();
            }
        });
    }
}
