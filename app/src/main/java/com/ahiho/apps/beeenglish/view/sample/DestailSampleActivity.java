package com.ahiho.apps.beeenglish.view.sample;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.ahiho.apps.beeenglish.R;
import com.ahiho.apps.beeenglish.util.Identity;
import com.ahiho.apps.beeenglish.util.UtilString;
import com.ahiho.apps.beeenglish.view.BaseActivity;

public class DestailSampleActivity extends BaseActivity  {
    private WebView wvSample;
    private final int SHARE_REQUEST_CODE=10;
    private String linkDownload="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destail_sample);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        wvSample = (WebView) findViewById(R.id.wvSample);
        wvSample.setBackgroundColor(Color.TRANSPARENT);
        wvSample.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        Intent intent = getIntent();
        String input = intent.getStringExtra(Identity.EXTRA_SAMPLE_DESCRIPTION);
        linkDownload = intent.getStringExtra(Identity.EXTRA_SAMPLE_LINK);
        String name = intent.getStringExtra(Identity.EXTRA_SAMPLE_NAME);
        setTitle(name);
        if(input!=null&&!input.isEmpty()) {
            String cssString = "<style rel=\"stylesheet\" type=\"text/css\">pre {\n" +
                    " white-space: pre-wrap;       /* css-3 */\n" +
                    " white-space: -moz-pre-wrap;  /* Mozilla, since 1999 */\n" +
                    " white-space: -pre-wrap;      /* Opera 4-6 */\n" +
                    " white-space: -o-pre-wrap;    /* Opera 7 */\n" +
                    " word-wrap: break-word;       /* Internet Explorer 5.5+ */\n" +
                    "}</style>";
            String text = new UtilString().htmlText(cssString+input);
            wvSample.loadDataWithBaseURL(null, text, "text/html", "utf-8", null);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.menu_sample, menu);

        // Locate MenuItem with ShareActionProvider
        // Fetch and store ShareActionProvider
        // Return true to display menu
        return true;
    }



    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.share_text_download)+" "+linkDownload);
        return shareIntent;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_share:
                startActivityForResult(createShareIntent(),SHARE_REQUEST_CODE);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==SHARE_REQUEST_CODE){
            if(resultCode==RESULT_OK){
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
