package gtg.virus.gtpr.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import gtg.virus.gtpr.R;
import gtg.virus.gtpr.entities.Audio;

import static gtg.virus.gtpr.service.AudioService.ACTION_MEDIA_PLAYER_SERVICE;
import static gtg.virus.gtpr.service.AudioService.ACTION_MEDIA_PLAYER_STOP_SERVICE;
import static gtg.virus.gtpr.service.AudioService.FILE_NAME;
public class AudioListAdapter extends AbstractListAdapter<Audio>{

    public AudioListAdapter(Context context, List<Audio> lists) {
        super(context, lists);
    }

    public AudioListAdapter(Context context){
        this(context , new ArrayList<Audio>());
    }

    protected OnRefreshList mRef = null;
    public interface OnRefreshList{
        void refresh(int pos);
    }

    public void setmRef(OnRefreshList mRef) {
        this.mRef = mRef;
    }

    public void add(Audio a){
        this.getList().add(a);
        ((Activity)getContext()).runOnUiThread(new Runnable() {

            /**
             * Starts executing the active part of the class' code. This method is
             * called when a thread is started that has been created with a class which
             * implements {@code Runnable}.
             */
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });

    }
    /**
     * returns the Override view provided by its subclass
     *
     * @param position
     * @param child
     * @param root
     */
    @Override
    public View getOverridedView(int position, View child, ViewGroup root) {
        ViewHolder vH = null;
        if(child == null){
            child = mInflater.inflate(R.layout.audio_list_row , null);
            vH = new ViewHolder(child);
            child.setTag(vH);
        }else{
            vH = (ViewHolder) child.getTag();
        }

        final Audio audio = getObject(position);

        vH.title.setText(audio.getTitle()+"");
        vH.details.setText(audio.getDetails()+"");
        final int pos = position;

        vH.button.setChecked(audio.getIsPlay());


        vH.button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Intent i = new Intent();
                    i.setAction(ACTION_MEDIA_PLAYER_SERVICE);
                    i.putExtra(FILE_NAME , audio.getTitle());
                    getContext().sendBroadcast(i);
                    if(mRef != null){
                        mRef.refresh(pos);
                    }
                }else{
                    Intent i = new Intent();
                    i.setAction(ACTION_MEDIA_PLAYER_STOP_SERVICE);
                    i.putExtra(FILE_NAME , audio.getTitle());
                    getContext().sendBroadcast(i);

                }
            }
        });

        return child;
    }

    /**
     * <p>Returns a filter that can be used to constrain data with a filtering
     * pattern.</p>
     * <p/>
     * <p>This method is usually implemented by {@link android.widget.Adapter}
     * classes.</p>
     *
     * @return a filter used to constrain data
     */
    @Override
    public Filter getFilter() {
        return null;
    }

    static class ViewHolder {
        @InjectView(R.id.txt_audio_list_title)
        TextView title;
        @InjectView(R.id.txt_audio_list_details)
        TextView details;
        @InjectView(R.id.play_stop_switch)
        Switch button;

        ViewHolder(View child){
            ButterKnife.inject(this , child);
        }
    }

}
