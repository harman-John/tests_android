package jblcontroller.hcs.soundx.ui.preference;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import jbl.stc.com.R;


public class PreferencePager extends PagerAdapter {

    Context mContext;
    LayoutInflater mLayoutInflater;
    int[] mResources = {
            R.mipmap.a_image, R.mipmap.b_image,  R.mipmap.c_image,
            R.mipmap.d_image , R.mipmap.e_image, R.mipmap.f_image
    };

    public PreferencePager(Context context) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return mResources.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((FrameLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.shade_item, container, false);

//        de.hdodenhof.circleimageview.CircleImageView imageView = itemView.findViewById(R.id.image);
//        imageView.setImageResource(mResources[position]);

        ImageView type = itemView.findViewById(R.id.audio_option_item);

        type.setImageResource(mResources[position]);

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((FrameLayout) object);
    }

    public int getName(int position){
       return mResources[position];
    }
}