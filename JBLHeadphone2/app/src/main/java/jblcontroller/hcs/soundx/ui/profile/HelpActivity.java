package jblcontroller.hcs.soundx.ui.profile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import jbl.stc.com.R;
import jblcontroller.hcs.soundx.base.BaseActivity;

public class HelpActivity extends BaseActivity {
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.beginner_title)
    TextView mBeginnerTitle;
    @BindView(R.id.beginner_msg)
    TextView mBeginnerMsg;
    @BindView(R.id.avg_title)
    TextView mAvgTitle;
    @BindView(R.id.avg_msg)
    TextView mAvgMsg;
    @BindView(R.id.lot_title)
    TextView alotTitle;
    @BindView(R.id.alot_msg)
    TextView alotMsg;
    @BindView(R.id.trained_title)
    TextView mTrainedTitle;
    @BindView(R.id.trained_msg)
    TextView mTrainedMsg;

    @BindView(R.id.close)
    ImageView mClose;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listening_experience);
        ButterKnife.bind(this);
        mTitle.setTypeface(getBoldFont());
        mBeginnerTitle.setTypeface(getBoldFont());
        mAvgTitle.setTypeface(getBoldFont());
        alotTitle.setTypeface(getBoldFont());
        mTrainedTitle.setTypeface(getBoldFont());

        mBeginnerMsg.setTypeface(getRegularFont());
        mAvgMsg.setTypeface(getRegularFont());
        alotMsg.setTypeface(getRegularFont());
        mTrainedMsg.setTypeface(getRegularFont());

        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
