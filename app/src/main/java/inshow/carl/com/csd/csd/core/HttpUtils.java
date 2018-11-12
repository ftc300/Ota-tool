package inshow.carl.com.csd.csd.core;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import inshow.carl.com.csd.AppController;
import inshow.carl.com.csd.csd.http.BaseReqParam;
import inshow.carl.com.csd.csd.http.WatchInfo;

/**
 * Comment:
 * Author: ftc300
 * Date: 2018/11/9
 * Blog: www.ftc300.pub
 * GitHub: https://github.com/ftc300
 */

public class HttpUtils {
    private static final String TAG = "HttpUtils";
    private static final String URL = "http://api.inshowlife.com/v1/client/check";
    /**
     * 声明RequestQueue对象
     */
    static RequestQueue mQueue = null;

    /**
     * 声明StringRequest对象
     */
    static StringRequest stringRequest = null;

    static JsonObjectRequest mJsonObjectRequest = null;

    /**
     * 1、获取RequestQueue对象
     */
    public static RequestQueue getRequestQueue(Context context) {
        if (mQueue == null) {
            mQueue = Volley.newRequestQueue(context);
        }
        return mQueue;
    }
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            NetworkInfo[] info = cm.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 2.获取StringRequest对象
     *
     * @param url 请求的url
     * @return StringRequest
     */
    public static StringRequest postString(String url, final String name) {
        return stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "response -> " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage(), error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                //在这里设置需要post的参数
                Map<String, String> map = new HashMap<>();
                map.put("name", name);
                return map;
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");

                return headers;
            }
        };
    }


    /**
     * 2.获取StringRequest对象
     *
     * @param url 请求的url
     * @return StringRequest
     */
    public static void testNet(String url, final INet net) {
        new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        net.onSuccess();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage(), error);
                net.onFail();
            }
        });
    }


    interface INet {
        void onSuccess();

        void onFail();
    }

    /**
     * 获取JsonObjectRequest对象
     *
     * @param url 请求url
     * @return JsonObjectRequest
     */
    public static JsonObjectRequest getJsonObjectRequest(String url) {
        return mJsonObjectRequest = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("JsonObjectRequest", response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("JsonObjectRequest", "onErrorResponse: " + volleyError.getMessage());
            }
        });

    }


    public static JsonObjectRequest postInfo(String content) {
        return new JsonObjectRequest(Request.Method.POST, URL, content,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "response -> " + response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.getMessage(), error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");
                return headers;
            }
        };
    }
}
