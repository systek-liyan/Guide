package com.systek.guide.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.alibaba.fastjson.JSON;
import com.systek.guide.IConstants;
import com.systek.guide.R;
import com.systek.guide.entity.ExhibitBean;
import com.systek.guide.lyric.LyricAdapter;
import com.systek.guide.lyric.LyricDownloadManager;
import com.systek.guide.lyric.LyricLoadHelper;
import com.systek.guide.lyric.LyricSentence;
import com.systek.guide.utils.ExceptionUtil;
import com.systek.guide.utils.ImageLoaderUtil;
import com.systek.guide.utils.Tools;

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
    private ImageView imgExhibitIcon;

    public LyricFragment() {
    }

    public ExhibitBean getExhibit() {
        return exhibit;
    }

    public void setExhibit(ExhibitBean exhibit) {
        this.exhibit = exhibit;
        currentMuseumId=exhibit.getMuseumId();
    }



    public void loadLyricByHand() {
        if(exhibit==null){return;}
        try{
            currentLyricUrl = exhibit.getTexturl();
            String name = currentLyricUrl.replaceAll("/", "_");
            // 取得歌曲同目录下的歌词文件绝对路径
            String lyricFilePath = LOCAL_ASSETS_PATH+currentMuseumId+"/"+LOCAL_FILE_TYPE_LYRIC+"/"+ name;
            File lyricFile = new File(lyricFilePath);
            if (lyricFile.exists()&&mLyricLoadHelper!=null) {
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

    public static LyricFragment newInstance(String exhibitJson) {
        LyricFragment fragment = new LyricFragment();
        if(!TextUtils.isEmpty(exhibitJson)){
            Bundle args = new Bundle();
            args.putString(ARG_PARAM1, exhibitJson);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLyricLoadHelper = new LyricLoadHelper();
        mLyricAdapter = new LyricAdapter(getActivity());
        if (getArguments() != null) {
            String exhibitJson = getArguments().getString(ARG_PARAM1);
            if(!TextUtils.isEmpty(exhibitJson)){
                exhibit= JSON.parseObject(exhibitJson,ExhibitBean.class);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_lyric, container, false);
        lvLyric=(ListView)view.findViewById(R.id.lvLyric);
        imgExhibitIcon=(ImageView)view.findViewById(R.id.iv_exhibit_icon);



        mLyricLoadHelper.setLyricListener(mLyricListener);
        lvLyric.setAdapter(mLyricAdapter);

        initIcon();

        return view;

    }

    public void initIcon() {
        if(exhibit==null){return;}
        String imageUrl=exhibit.getIconurl();
        if(imageUrl==null||imgExhibitIcon==null){return;}
        String imageName = Tools.changePathToName(imageUrl);
        String imgLocalUrl = LOCAL_ASSETS_PATH+currentMuseumId + "/" + LOCAL_FILE_TYPE_IMAGE+"/"+imageName;
        File file = new File(imgLocalUrl);
        // 判断sdcard上有没有图片
        if (file.exists()) {
            // 显示sdcard
            ImageLoaderUtil.displaySdcardBlurImage(getActivity(), imgLocalUrl, imgExhibitIcon);
        } else {
            ImageLoaderUtil.displayNetworkBlurImage(getActivity(), BASE_URL + imageUrl, imgExhibitIcon);// TODO: 2016/2/16
        }
    }



    private LyricLoadHelper.LyricListener mLyricListener = new LyricLoadHelper.LyricListener() {

        @Override
        public void onLyricLoaded(List<LyricSentence> lyricSentences, int index) {
            if (lyricSentences != null) {
                //LogUtil.i(TAG, "onLyricLoaded--->歌词句子数目=" + lyricSentences.size() + ",当前句子索引=" + index);
                mLyricAdapter.setLyric(lyricSentences);
                mLyricAdapter.setCurrentSentenceIndex(index);
                mLyricAdapter.notifyDataSetChanged();
            }
        }
        @Override
        public void onLyricSentenceChanged(int indexOfCurSentence) {
            mLyricAdapter.setCurrentSentenceIndex(indexOfCurSentence);
            mLyricAdapter.notifyDataSetChanged();
            lvLyric.smoothScrollToPositionFromTop(indexOfCurSentence, lvLyric.getHeight() / 2, 500);
        }
    };

    public void onButtonPressed(ExhibitBean exhibit) {
        if (mListener != null) {
            mListener.onFragmentInteraction(exhibit);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(ExhibitBean exhibit);
    }

    private class LyricDownloadAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            LyricDownloadManager mLyricDownloadManager = new LyricDownloadManager(getActivity());
            // 从网络获取歌词，然后保存到本地
            String lyricFilePath = mLyricDownloadManager.searchLyricFromWeb(params[0],
                    LOCAL_ASSETS_PATH + currentMuseumId + "/" + LOCAL_FILE_TYPE_LYRIC);
            // 返回本地歌词路径
            // mIsLyricDownloading = false;
            return lyricFilePath;
        }

        @Override
        protected void onPostExecute(String lyricSavePath) {
            // Log.i(TAG, "网络获取歌词完毕，歌词保存路径:" + result);
            // 读取保存到本地的歌曲
            mLyricLoadHelper.loadLyric(lyricSavePath);
        }
    }

}
