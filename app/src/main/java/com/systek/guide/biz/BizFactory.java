package com.systek.guide.biz;

import com.systek.guide.IConstants;


/**
 * Created by Qiang on 2015/10/22.
 */
public class BizFactory {


    public static IConstants getDownloadBiz(){
        return  new DownloadBiz();
    }

    /*public static IConstants getBeansManageBiz(Context context){

        return new BeansManageBiz(context);
    }
    public static IConstants getDataBiz(){
        return  new GetDataBiz();
    }*/
}
