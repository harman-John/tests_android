package jbl.stc.com.config;

import android.content.Context;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jbl.stc.com.logger.Logger;


public class DeviceFeatureMap {
    private static final String TAG = DeviceFeatureMap.class.getSimpleName();
    private static Map<String, List<Feature>> allDeviceFeatureMap = null;
    private static final String FILE_NAME = "product_feature_listing.xml";
    private static final String TAG_NAME_MAP = "map";
    private static final String TAG_NAME_PRODUCT = "product";
    private static final String TAG_NAME_FEATURE = "feature";

    private static final String ATTRIBUTE_NAME_FEATURE = "name";

    private DeviceFeatureMap() {

    }

    public static void init(Context context) {
        if (allDeviceFeatureMap == null) {
            Logger.i(TAG,"Init all device feature map");
            allDeviceFeatureMap = Collections.unmodifiableMap(parseXml(context));
        }else{
            Logger.d(TAG,"device feature map already initialized");
        }
    }

    /**
     * Check if a feature is supported in a product
     * @param deviceName pid of speaker
     * @param feature {@link Feature}
     * @return boolean
     */
    public static boolean isFeatureSupported(String deviceName, Feature feature){
        return allDeviceFeatureMap.get(deviceName.toUpperCase()) != null
                && allDeviceFeatureMap.get(deviceName.toUpperCase()).contains(feature);
    }

    /**
     * Function to parse product_feature_listing.xml and return product-feature map
     * @param context
     * @return Map<String, List<Feature>>
     */
    private static Map<String,List<Feature>> parseXml(Context context) {
        if(context!=null) {
            Map<String, List<Feature>> map = null;
            List<Feature> featureList = null;

            InputStream inputStream = null;
            XmlPullParser parser = null;
            try {
                inputStream  = context.getResources().getAssets().open(FILE_NAME);
                parser = Xml.newPullParser();
                parser.setInput(inputStream, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }

            String productName = null, supportFeature;

            try {
                assert parser != null;
                int eventType = parser.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_DOCUMENT) {
                        Logger.d(TAG, " parseXml() Parsing document started");
                    } else if (eventType == XmlPullParser.START_TAG) {
                        if (parser.getName().equals(TAG_NAME_MAP)) {
                            map = new HashMap<>();
                        } else if (parser.getName().equals(TAG_NAME_PRODUCT)) {
                            productName = parser.getAttributeValue(null, ATTRIBUTE_NAME_FEATURE);
                            Logger.i(TAG,"product name = "+productName);
                            if (null == productName) {
                                inputStream.close();
                                return null;
                            }else{
                                featureList = new ArrayList<>();
                            }
                        } else if (parser.getName().equals(TAG_NAME_FEATURE)) {
                            supportFeature = parser.getAttributeValue(null, ATTRIBUTE_NAME_FEATURE);
                            if (null == supportFeature) {
                                inputStream.close();
                                return null;
                            }else {
                                if (featureList != null) {
                                    Feature featureEnum = Feature.getEnum(supportFeature);
                                    featureList.add(featureEnum);
                                }
                            }
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        if(parser.getName().equals(TAG_NAME_PRODUCT)){
                            if (map != null) {
                                map.put(productName, featureList);
                            }
                            productName = null;
                        }
                    }
                    eventType = parser.next();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return map;
        }else{
            return null;
        }
    }
}
