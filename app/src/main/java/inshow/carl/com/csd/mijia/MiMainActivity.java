
package inshow.carl.com.csd.mijia;

import android.Manifest;
import android.accounts.OperationCanceledException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.xiaomi.account.openauth.XMAuthericationException;
import com.xiaomi.account.openauth.XiaomiOAuthConstants;
import com.xiaomi.account.openauth.XiaomiOAuthFuture;
import com.xiaomi.account.openauth.XiaomiOAuthResults;
import com.xiaomi.account.openauth.XiaomiOAuthorize;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import inshow.carl.com.csd.R;
import static android.content.pm.PackageManager.PERMISSION_DENIED;

public class MiMainActivity extends Activity {
    private static final String TAG = "OAuthDemoActivity";

    public static final Long appId = 179887661252608L;
    public static final String redirectUri = "http://xiaomi.com";
    XiaomiOAuthResults results;
    private AsyncTask waitResultTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi);
        ((EditText) findViewById(R.id.appId)).setText(String.valueOf(appId));
        ((EditText) findViewById(R.id.redirectUrl)).setText(redirectUri);

        findViewById(R.id.get_token).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean skipConfirm = ((CheckBox) findViewById(R.id.skipConfirm)).isChecked();
                XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
                        .setAppId(getAppId())
                        .setRedirectUrl(getRedirectUri())
                        .setScope(getScopeFromUi())
                        .setKeepCookies(getTryKeepCookies()) // 不调的话默认是false
                        .setNoMiui(getNoMiui()) // 不调的话默认是false，不建议设置
                        .setSkipConfirm(skipConfirm) // 不调的话默认是false
                        .setPhoneNumAutoFill(MiMainActivity.this.getApplicationContext(), true)
                        // 自定义非miui上的登录界面，设置actionbar、进度条等
                        //.setCustomizedAuthorizeActivityClass(CustomizedAuthorizedActivity.class)
                        .startGetAccessToken(MiMainActivity.this);
                waitAndShowFutureResult(future);
            }
        });

        findViewById(R.id.get_code).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int[] scopes = getScopeFromUi();
                boolean noMiui = getNoMiui();
                boolean skipConfirm = ((CheckBox) findViewById(R.id.skipConfirm)).isChecked();
                XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
                        .setAppId(getAppId())
                        .setRedirectUrl(getRedirectUri())
                        .setScope(scopes)
                        .setKeepCookies(getTryKeepCookies()) // 不调的话默认是false
                        .setNoMiui(getNoMiui()) // 不调的话默认是false
                        .setSkipConfirm(skipConfirm) // 不调的话默认是false
                        .setPhoneNumAutoFill(MiMainActivity.this.getApplicationContext(), true)
                        .startGetOAuthCode(MiMainActivity.this);
                waitAndShowFutureResult(future);
            }

        });

        findViewById(R.id.profile_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (results == null){
                    Toast.makeText(MiMainActivity.this,"请先获取ｔｏｋｅｎ",Toast.LENGTH_SHORT).show();
                    return;
                }
                XiaomiOAuthFuture<String> future = new XiaomiOAuthorize()
                        .callOpenApi(MiMainActivity.this,
                                getAppId(),
                                XiaomiOAuthConstants.OPEN_API_PATH_PROFILE,
                                results.getAccessToken(),
                                results.getMacKey(),
                                results.getMacAlgorithm());
                waitAndShowFutureResult(future);
            }

        });

        findViewById(R.id.relation_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (results == null){
                    Toast.makeText(MiMainActivity.this,"请先获取ｔｏｋｅｎ",Toast.LENGTH_SHORT).show();
                    return;
                }
                XiaomiOAuthFuture<String> future = new XiaomiOAuthorize()
                        .callOpenApi(MiMainActivity.this,
                                getAppId(),
                                XiaomiOAuthConstants.OPEN_API_PATH_RELATION,
                                results.getAccessToken(),
                                results.getMacKey(),
                                results.getMacAlgorithm());
                waitAndShowFutureResult(future);
            }
        });

        findViewById(R.id.openid_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (results == null){
                    Toast.makeText(MiMainActivity.this,"请先获取ｔｏｋｅｎ",Toast.LENGTH_SHORT).show();
                    return;
                }
                XiaomiOAuthFuture<String> future = new XiaomiOAuthorize()
                        .callOpenApi(MiMainActivity.this,
                                getAppId(),
                                XiaomiOAuthConstants.OPEN_API_PATH_OPEN_ID,
                                results.getAccessToken(),
                                results.getMacKey(),
                                results.getMacAlgorithm());
                waitAndShowFutureResult(future);
            }
        });

        findViewById(R.id.phone_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (results == null){
                    Toast.makeText(MiMainActivity.this,"请先获取ｔｏｋｅｎ",Toast.LENGTH_SHORT).show();
                    return;
                }
                XiaomiOAuthFuture<String> future = new XiaomiOAuthorize()
                        .callOpenApi(MiMainActivity.this,
                                getAppId(),
                                XiaomiOAuthConstants.OPEN_API_PATH_PHONE,
                                results.getAccessToken(),
                                results.getMacKey(),
                                results.getMacAlgorithm());
                waitAndShowFutureResult(future);
            }
        });

        findViewById(R.id.fast_oauth_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean skipConfirm = ((CheckBox) findViewById(R.id.skipConfirm)).isChecked();

                    XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
                            .setAppId(getAppId())
                            .setRedirectUrl(getRedirectUri())
                            .setScope(getScopeFromUi())
                            .setSkipConfirm(skipConfirm) // 不调的话默认是false
                            .fastOAuth(MiMainActivity.this, XiaomiOAuthorize.TYPE_TOKEN);
                    waitAndShowFutureResult(future);

            }
        });

        if(savedInstanceState == null){
            XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
                    .setAppId(getAppId())
                    .setRedirectUrl(getRedirectUri())
                    .setScope(getScopeFromUi())
                    .fastOAuth(MiMainActivity.this, XiaomiOAuthorize.TYPE_TOKEN);
            waitAndShowFutureResult(future);
        }

        requestPermission();
    }

    private void requestPermission() {
        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
            1000);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PERMISSION_DENIED) {
            finish();
        }
    }

    private boolean getTryKeepCookies() {
        return ((CheckBox) findViewById(R.id.tryKeepCookies)).isChecked();
    }

    private boolean getNoMiui() {
        return ((CheckBox) findViewById(R.id.nonMiui)).isChecked();
    }

    private String getRedirectUri() {
        String uri = ((EditText) findViewById(R.id.redirectUrl)).getText().toString();
        if (uri.isEmpty()) {
            return MiMainActivity.redirectUri;
        }
        return uri;
    }

    private Long getAppId() {
        String appId = ((EditText) findViewById(R.id.appId)).getText().toString();
        if (appId.isEmpty()) {
            return MiMainActivity.appId;
        }
        return Long.valueOf(appId);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onMenuItemSelected(int featureId, MenuItem item) {
//        if (item.getItemId() == R.id.oldApiDemo) {
////            startActivity(new Intent(this, OldMainActivity.class));
//        } else if (item.getItemId() == R.id.customizeAuthorizeActivity) {
//            XiaomiOAuthFuture<XiaomiOAuthResults> future = new XiaomiOAuthorize()
//                    .setAppId(getAppId())
//                    .setRedirectUrl(getRedirectUri())
//                    .setNoMiui(true) // set to true only because we want to simulate behavior on Non-MIUI ROM.
//                    .setScope(getScopeFromUi())
//                    .setCustomizedAuthorizeActivityClass(CustomizedAuthorizedActivity.class)
//                    .startGetAccessToken(this);
//            waitAndShowFutureResult(future);
//        }
//        return super.onMenuItemSelected(featureId, item);
//    }

    private int[] getScopeFromUi() {
        HashMap<Integer, Integer> scopeMap = new HashMap<Integer, Integer>();
        scopeMap.put(R.id.scopeProfile, XiaomiOAuthConstants.SCOPE_PROFILE);
        scopeMap.put(R.id.scopeRelation, XiaomiOAuthConstants.SCOPE_RELATION);
        scopeMap.put(R.id.scopeOpenId, XiaomiOAuthConstants.SCOPE_OPEN_ID);
        scopeMap.put(R.id.scopePhone, XiaomiOAuthConstants.SCOPE_PHONE);

        int[] scopes = new int[scopeMap.size()];
        int checkedCount = 0;

        for (Integer id : scopeMap.keySet()) {
            CheckBox cb = (CheckBox) findViewById(id);
            if (cb.isChecked()) {
                scopes[checkedCount] = scopeMap.get(id);
                checkedCount++;
            }
        }

        return Arrays.copyOf(scopes, checkedCount);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(waitResultTask!=null && !waitResultTask.isCancelled()){
            waitResultTask.cancel(false);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private <V> void waitAndShowFutureResult(final XiaomiOAuthFuture<V> future) {
        waitResultTask = new AsyncTask<Void, Void, V>() {
            Exception e;

            @Override
            protected void onPreExecute() {
                showResult("waiting for Future result...");
            }

            @Override
            protected V doInBackground(Void... params) {
                showResult("waiting for Future result getting...");
                V v = null;
                try {
                    v = future.getResult();
                } catch (IOException e1) {
                    this.e = e1;
                } catch (OperationCanceledException e1) {
                    this.e = e1;
                } catch (XMAuthericationException e1) {
                    this.e = e1;
                }
                return v;
            }

            @Override
            protected void onPostExecute(V v) {
                if (v != null) {
                    if (v instanceof XiaomiOAuthResults) {
                        results = (XiaomiOAuthResults) v;
                    }
                    showResult(v.toString());
                } else if (e != null) {
                    showResult(e.toString());
                } else {
                    showResult("done and ... get no result :(");
                }
            }
        }.executeOnExecutor(mExecutor);
    }

    Executor mExecutor = Executors.newCachedThreadPool();

    private void showResult(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String timeFormatted = new SimpleDateFormat("HH:mm:ss:SSS").format(new Date(System.currentTimeMillis()));
                ((TextView) findViewById(R.id.content)).setText(timeFormatted + "\n" + text);
                Log.v(TAG, "result:" + text);
            }
        });
    }
}
