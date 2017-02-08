package com.ahiho.apps.beeenglish.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ahiho.apps.beeenglish.R;
import com.ahiho.apps.beeenglish.model.ResponseData;
import com.ahiho.apps.beeenglish.util.Identity;
import com.ahiho.apps.beeenglish.util.MyConnection;
import com.ahiho.apps.beeenglish.util.UtilSharedPreferences;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanQRActivity extends BaseActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private MyConnection mMyConnection;
    private UtilSharedPreferences mUtilSharedPreferences;
    private Dialog dialogActive;
    private TextView tvContent;
    private ImageView ivSuccess;
    private ImageButton btSelectImage;
    private RelativeLayout rlLoading;
    private LinearLayout llButton;
    private Button btTryAgain, btCancel;
    private boolean isActive = false;
    private final int RESULT_SELECT_IMAGE = 21;

    private String TAG = "QR_RESPONSE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (grantPermissionCamera()) {
            init();
        }
    }

    private void init() {
        setContentView(R.layout.activity_scan_qr);
        mScannerView = new ZXingScannerView(this);
        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);

        contentFrame.addView(mScannerView);
        mMyConnection = MyConnection.getInstanceMyConnection(ScanQRActivity.this);
        mUtilSharedPreferences = UtilSharedPreferences.getInstanceSharedPreferences(ScanQRActivity.this);
//        mScannerView.setResultHandler(this);
//        mScannerView.startCamera();
        btSelectImage = (ImageButton) findViewById(R.id.btSelectImage);

        btSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_SELECT_IMAGE);
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Identity.REQUEST_PERMISSION_CAMERA:
                if (grantResults.length > 0) {
                    int result = 0;
                    for (int grant : grantResults) {
                        result += grant;
                    }
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        init();
                        mScannerView.setResultHandler(this);
                        mScannerView.startCamera();
                        return;
                    }
                    showToast(R.string.err_permission_camera, Toast.LENGTH_LONG);
                    finish();
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mScannerView != null) {
            mScannerView.setResultHandler(this);
            mScannerView.startCamera();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mScannerView != null) {
            mScannerView.stopCamera();
        }
    }

    @Override
    public void handleResult(Result rawResult) {
//        String data =  "Contents = " + rawResult.getText() +
//                "\n Format = " + rawResult.getBarcodeFormat().toString();
//        Toast.makeText(this,data, Toast.LENGTH_SHORT).show();
//        // Note:
//        // * Wait 2 seconds to resume the preview.
//        // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
//        // * I don't know why this is the case but I don't have the time to figure out.

        showDialogActive(rawResult.getText());

    }

    private void showDialogActive(String data) {
        if (dialogActive != null && dialogActive.isShowing())
            return;

        dialogActive = new Dialog(ScanQRActivity.this);
        dialogActive.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogActive.setCanceledOnTouchOutside(false);
        dialogActive.setContentView(R.layout.dialog_active_loading);
        ivSuccess = (ImageView) dialogActive.findViewById(R.id.ivSuccess);
        tvContent = (TextView) dialogActive.findViewById(R.id.tvContent);
        rlLoading = (RelativeLayout) dialogActive.findViewById(R.id.rlLoading);
        llButton = (LinearLayout) dialogActive.findViewById(R.id.llButton);
        btTryAgain = (Button) dialogActive.findViewById(R.id.btTryAgain);
        btCancel = (Button) dialogActive.findViewById(R.id.btCancel);

        btTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogActive.dismiss();
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogActive.dismiss();
                finish();
            }
        });
        dialogActive.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (isActive) {
                    finish();
                } else {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mScannerView.resumeCameraPreview(ScanQRActivity.this);
                        }
                    }, 500);
                }
            }
        });
        dialogActive.show();
        try {
            JSONObject jsonObject = new JSONObject(data);
            new ActiveKey(jsonObject.getString("payment_key"), jsonObject.getString("agency_id")).execute();
        } catch (JSONException e) {
            invalidCode(getString(R.string.err_qr_not_match));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SELECT_IMAGE:

                if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                    try {
                        Uri selectedImage = data.getData();
//                        String[] filePathColumn = {MediaStore.Images.Media.DATA };
//                        Cursor cursor = getContentResolver().query(selectedImage,
//                                filePathColumn, null, null, null);
//                        cursor.moveToFirst();
//                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//                        String picturePath = cursor.getString(columnIndex);
//                        cursor.close();

                      new ScanQRFromImage(selectedImage).execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Intent returnFromGalleryIntent = new Intent();
                        setResult(RESULT_CANCELED, returnFromGalleryIntent);
                        finish();
                    }
                }
                break;
        }
    }

    private void invalidCode(String errorData) {
        rlLoading.setVisibility(View.GONE);
        llButton.setVisibility(View.VISIBLE);
        tvContent.setVisibility(View.VISIBLE);
        ivSuccess.setVisibility(View.VISIBLE);
        tvContent.setText(errorData);
        ivSuccess.setImageResource(R.drawable.ic_warning_white_48dp);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tvContent.setTextColor(getColor(R.color.colorError));
            ivSuccess.setColorFilter(getColor(R.color.colorError));
        } else {
            tvContent.setTextColor(Color.parseColor("#cc0000"));
            ivSuccess.setColorFilter(Color.parseColor("#cc0000"));
        }
        btTryAgain.setVisibility(View.VISIBLE);
    }

    private void validCode() {
        rlLoading.setVisibility(View.GONE);
        llButton.setVisibility(View.VISIBLE);
        tvContent.setVisibility(View.VISIBLE);
        ivSuccess.setVisibility(View.VISIBLE);
    }

    class ActiveKey extends AsyncTask<Void, Void, ResponseData> {
        private String activeKey, agencyId;

        public ActiveKey(String activeKey, String agencyId) {
            this.activeKey = activeKey;
            this.agencyId = agencyId;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected ResponseData doInBackground(Void... params) {
            try {
                JSONObject jsonObject = new JSONObject(mUtilSharedPreferences.getUserData());
                String userName = jsonObject.getString("username");
                return mMyConnection.activeKey(userName, activeKey, agencyId);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(ResponseData responseData) {
            if (responseData != null) {
                if (responseData.isResponseState()) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseData.getResponseData());
                        if (!jsonObject.isNull("success") && jsonObject.getBoolean("success")) {
                            isActive = true;
                            validCode();
                            String activeData =jsonObject.getString("data");
                            JSONObject jsonObjectActive =new JSONObject(activeData);
//                            mUtilSharedPreferences.setTrialTimeExpired(UtilString.convertString2Date(jsonObjectActive.getString("expired_time")));
                            mUtilSharedPreferences.setActiveData(activeData);
                        } else {
                            invalidCode(new JSONObject(responseData.getResponseData()).getJSONObject("error").getString("message"));
                        }
                    } catch (JSONException e) {
                        showToast(R.string.err_json_exception, Toast.LENGTH_LONG);
                        finish();
                    }
                } else {
                    invalidCode(responseData.getResponseData());
                }
            }
            super.onPostExecute(responseData);
        }
    }

    class ScanQRFromImage extends AsyncTask<Void,Void,Result>{
        private Uri uri;
        public ScanQRFromImage(Uri uri){
            showDialogLoading();
            this.uri=uri;
        }
        @Override
        protected Result doInBackground(Void... params)
        {
            try
            {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap == null)
                {
                    Log.e(TAG, "uri is not a bitmap," + uri.toString());
                    return null;
                }
                int width = bitmap.getWidth(), height = bitmap.getHeight();
                int[] pixels = new int[width * height];
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                bitmap.recycle();
                bitmap = null;
                RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
                BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
                QRCodeMultiReader reader = new QRCodeMultiReader();
                try
                {
                    Result result = reader.decode(bBitmap);
                    return result;
                }
                catch (Exception e)
                {
                    Log.e(TAG, "decode exception", e);
                    return null;
                }
            }
            catch (FileNotFoundException e)
            {
                Log.e(TAG, "can not open file" + uri.toString(), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Result result) {
            dismissDialogLoading();
            if(result!=null) {
                Log.e(TAG,result.getText());
                showDialogActive(result.getText());
            }
            super.onPostExecute(result);
        }
    }

}
