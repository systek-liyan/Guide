package com.systek.guide.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.systek.guide.IConstants;
import com.systek.guide.R;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.lyric.LyricAdapter;
import com.systek.guide.lyric.LyricDownloadManager;
import com.systek.guide.lyric.LyricLoadHelper;
import com.systek.guide.lyric.LyricSentence;
import com.systek.guide.utils.ExceptionUtil;

import java.io.File;
import java.util.List;

public class LyricFragment extends BaseFragment implements IConstants{

    private static final String ARG_PARAM1 = "param1";
    private ExhibitBean exhibit;
    private OnFragmentInteractionListener mListener;
    private String currentLyricUrl;
    private String currentMuseumId;
    private LyricLoadHelper mLyricLoadHelper;
    private LyricAdapter mLyricAdapter;
    private ListView lvLyric;
    private TextView tvContent;
    private ImageView ivWordCtrl;

    public LyricFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(contentView);
        if (getArguments() != null) {
            String exhibitJson = getArguments().getString(ARG_PARAM1);
            if(!TextUtils.isEmpty(exhibitJson)){
                exhibit= JSON.parseObject(exhibitJson,ExhibitBean.class);
            }
        }
    }

    @Override
    void initView() {
        setContentView(R.layout.fragment_lyric);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mLyricLoadHelper = new LyricLoadHelper();
        mLyricLoadHelper.setLyricListener(mLyricListener);
        mLyricAdapter = new LyricAdapter(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        loadLyricByHand();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setExhibit(ExhibitBean exhibit) {
        this.exhibit = exhibit;
        currentMuseumId=exhibit.getMuseumId();
    }

    public void loadLyricByHand() {

        if(exhibit==null){return;}
        try{
            if(mLyricLoadHelper==null){
                mLyricLoadHelper=new LyricLoadHelper();
                mLyricLoadHelper.setLyricListener(mLyricListener);
            }
            if(mLyricAdapter==null){
                mLyricAdapter = new LyricAdapter(getActivity());
                lvLyric.setAdapter(mLyricAdapter);
            }

            currentLyricUrl = exhibit.getTexturl();
            String name = currentLyricUrl.replaceAll("/", "_");
            // 取得歌曲同目录下的歌词文件绝对路径
            String lyricFilePath = LOCAL_ASSETS_PATH+currentMuseumId+"/"+ name;
            File lyricFile = new File(lyricFilePath);
            if (lyricFile.exists()) {
                // 本地有歌词，直接读取
                mLyricLoadHelper.loadLyric(lyricFilePath);
            } else {
                //mIsLyricDownloading = true;
                // 尝试网络获取歌词
                //LogUtil.i("ZHANG", "loadLyric()--->本地无歌词，尝试从网络获取");
                new LyricDownloadAsyncTask().execute(currentLyricUrl);
            }
        }catch (Exception e){
            ExceptionUtil.handleException(e);
        }
    }

    public void notifyTime(long time){
        if(mLyricLoadHelper!=null){
            mLyricLoadHelper.notifyTime(time);
        }
    }

    private void addListener() {
        ivWordCtrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.ivWordCtrl:
                        if (lvLyric.getVisibility() == View.VISIBLE) {
                            lvLyric.setVisibility(View.INVISIBLE);
                            tvContent.setVisibility(View.VISIBLE);
                            contentView.setBackgroundResource(R.drawable.fullscreen_bg_gradient);
                        } else {
                            lvLyric.setVisibility(View.VISIBLE);
                            tvContent.setVisibility(View.INVISIBLE);
                            contentView.setBackgroundResource(0);
                        }
                        break;
                    default:break;
                }
            }
        });
    }

    private void initView(View view) {
        lvLyric=(ListView)view.findViewById(R.id.lvLyric);
        tvContent=(TextView)view.findViewById(R.id.tvContent);
        ivWordCtrl=(ImageView)view.findViewById(R.id.ivWordCtrl);
        lvLyric.setAdapter(mLyricAdapter);
        lvLyric.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
        addListener();
    }

    private LyricLoadHelper.LyricListener mLyricListener = new LyricLoadHelper.LyricListener() {

        @Override
        public void onLyricLoaded(List<LyricSentence> lyricSentences, int index) {
            if (lyricSentences != null) {
                //LogUtil.i(TAG, "onLyricLoaded--->歌词句子数目=" + lyricSentences.size() + ",当前句子索引=" + index);
                if(mLyricAdapter==null){
                    mLyricAdapter=new LyricAdapter(getActivity());
                    lvLyric.setAdapter(mLyricAdapter);
                }
                mLyricAdapter.setLyric(lyricSentences);
                mLyricAdapter.setCurrentSentenceIndex(index);
                mLyricAdapter.notifyDataSetChanged();
            }
        }
        @Override
        public void onLyricSentenceChanged(int indexOfCurSentence) {
            if(mLyricAdapter==null){return;}
            mLyricAdapter.setCurrentSentenceIndex(indexOfCurSentence);
            mLyricAdapter.notifyDataSetChanged();
            lvLyric.smoothScrollToPositionFromTop(indexOfCurSentence, lvLyric.getHeight() / 2, 500);
            tvContent.setText(exhibit.getContent());

        }
    };

    public void onButtonPressed(ExhibitBean exhibit) {
        if (mListener != null) {
            mListener.onFragmentInteraction(exhibit);
        }
    }



    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(ExhibitBean exhibit);
    }

    private  class LyricDownloadAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            LyricDownloadManager mLyricDownloadManager = new LyricDownloadManager(getActivity());
            // 从网络获取歌词，然后保存到本地
            String savePath=LOCAL_ASSETS_PATH + currentMuseumId + "/";
            String lyricName=params[0];
            // 返回本地歌词路径
            // mIsLyricDownloading = false;
            return mLyricDownloadManager.searchLyricFromWeb(lyricName, savePath);
        }

        @Override
        protected void onPostExecute(String lyricSavePath) {
            // Log.i(TAG, "网络获取歌词完毕，歌词保存路径:" + result);
            // 读取保存到本地的歌曲
            if(mLyricLoadHelper!=null){
                mLyricLoadHelper.loadLyric(lyricSavePath);
            }
        }
    }

}
