package com.systekcn.guide.beacon;

import android.content.Context;
import android.content.res.AssetManager;

import com.systekcn.guide.common.utils.ExceptionUtil;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.logging.LogManager;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Qiang on 2015/11/1.
 */
public class DefaultDistanceCalculator {

    private static final String TAG = "DefaultDistanceCalcuator";
    private static final String CONFIG_FILE = "model-distance-calculations.json";
    private Context mContext;

    public DefaultDistanceCalculator(Context mContext) {
        this.mContext = mContext;
    }

    public void setDefaultDistanceCalcuator(String distanceModelUpdateUrl) {

        /**
         * 读取应用程序asserts/model-distance-calculations.json,在应用程序私有数据空间写入同名文件。
         * java/org/altbeacon/beacon/distance/ModelSpecificDistanceCalculator.
         * java的loadModelMapFromFile()读取此文件，以构造缺省模型。
         */
        if (distanceModelUpdateUrl == null) {
            try {
                String jsonString = stringFromAssertFile(CONFIG_FILE); // 读取asserts/model-distance-calculations.json
                boolean ok = saveJson(CONFIG_FILE, jsonString); // 将距离模型json数据写入应用程序私有空间中的文件：CONFIG_FILE。
                if (ok) {
                    LogManager.d(TAG, "setDefaultDistanceCalcuator ok,from asserts/" + CONFIG_FILE);
                } else {
                    LogManager.d(TAG, "setDefaultDistanceCalcuator error,from asserts/" + CONFIG_FILE);
                }
            } catch (IOException e) {
                ExceptionUtil.handleException(e);
            }
            // 设置一个虚假的url，目的是引起BeaconService中调用ModelSpecificDistanceCalculator()-->loadModelMap()-->在应程序私有空间找CONFIG_FILE
            // 由于以上已经存储了这个文件,因此，绑定BeaconService后，执行上述序列，loadModelMap()能够成功找到该文件。
            // 鉴于此，必须在绑定BeaconService前执行此函数。loadModelMap()仅在第一次调用时才检查此文件，一旦文件已经写入，下一次就不检查了。因此测试时，每次要完全卸载程序，才能验证此程序的逻辑。
            BeaconManager.setDistanceModelUpdateUrl("nodistanceModelUpdateUrl");
        } else { // BeaconService中调用ModelSpecificDistanceCalculator()设置距离计算模型
            BeaconManager.setDistanceModelUpdateUrl(distanceModelUpdateUrl);
            LogManager.d(TAG, "setDefaultDistanceCalcuator, from " + distanceModelUpdateUrl);
        }

    }

    private boolean saveJson(String name, String jsonString) {

        FileOutputStream outputStream = null;

        try {
            outputStream = mContext.openFileOutput(name, Context.MODE_PRIVATE);
            outputStream.write(jsonString.getBytes());
            outputStream.close();
        } catch (Exception e) {
            LogManager.w(e, TAG, "Cannot write updated distance model to local storage");
            return false;
        } finally {
            try {
                if (outputStream != null)
                    outputStream.close();
            } catch (Exception e) {
            }
        }
        LogManager.i(TAG, "Successfully saved new distance model file");
        return true;

    }

    private String stringFromAssertFile(String configFile) throws IOException {
        BufferedReader bufferedReader = null;
        InputStream inputStream = null;
        StringBuilder inputStringBuilder = new StringBuilder();

        AssetManager asset = mContext.getAssets();

        try {
            inputStream = asset.open(configFile);
            if (inputStream == null) {
                throw new RuntimeException("Cannot load resource at assert:" + CONFIG_FILE);
            }
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            String line = bufferedReader.readLine();
            while (line != null) {
                inputStringBuilder.append(line);
                inputStringBuilder.append('\n');
                line = bufferedReader.readLine();
            }
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return inputStringBuilder.toString();
    }
}
