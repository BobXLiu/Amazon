package com.android.test.amazon;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.authorization.api.AmazonAuthorizationManager;
import com.amazon.identity.auth.device.authorization.api.AuthorizationListener;
import com.amazon.identity.auth.device.authorization.api.AuthzConstants;

public class MainActivity extends AppCompatActivity {
    private AmazonAuthorizationManager mAuthManager;
    private TextView tvInformation;
    private String PRODUCT_ID = "android_avs_test" ;//INSERT YOUR PRODUCT ID FROM Amazon developer portal
    private String PRODUCT_DSN = Build.SERIAL; //INSERT UNIQUE DSN FOR YOUR DEVICE;
    private String[] APP_SCOPES={"alexa:all"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    public void btnLogin(View view) {
        Bundle options = new Bundle();
        String scope_data = "{\"alexa:all\":{\"productID\":\"" + PRODUCT_ID +
                "\", \"productInstanceAttributes\":{\"deviceSerialNumber\":\"" +
                PRODUCT_DSN + "\"}}}";
        options.putString(AuthzConstants.BUNDLE_KEY.SCOPE_DATA.val, scope_data);

//        options.putBoolean(AuthzConstants.BUNDLE_KEY.GET_AUTH_CODE.val, true);
//        options.putString(AuthzConstants.BUNDLE_KEY.CODE_CHALLENGE.val, CODE_CHALLENGE);
//        options.putString(AuthzConstants.BUNDLE_KEY.CODE_CHALLENGE_METHOD.val, "S256");

        mAuthManager.authorize(APP_SCOPES, options, new AuthorizeListener());
    }

    private void init() {

        tvInformation = (TextView) findViewById(R.id.information);
        tvInformation.setMovementMethod(ScrollingMovementMethod.getInstance());

        try {
            mAuthManager = new AmazonAuthorizationManager(this, Bundle.EMPTY);
        } catch (IllegalArgumentException e) {
            output(e.toString());
        }

    }

    private void output(final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvInformation.append(content + "\n");
            }
        });
    }

    public class AuthorizeListener implements AuthorizationListener {

        @Override
        public void onCancel(Bundle bundle) {
            output("author cancel");
        }

        @Override
        public void onSuccess(Bundle bundle) {
            output("author success");
        }

        @Override
        public void onError(AuthError authError) {
            output("author error");
        }
    }
}
