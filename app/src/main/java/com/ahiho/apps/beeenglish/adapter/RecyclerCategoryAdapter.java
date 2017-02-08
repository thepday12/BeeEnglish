package com.ahiho.apps.beeenglish.adapter;

/**
 * Created by Thep on 10/18/2015.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ahiho.apps.beeenglish.R;
import com.ahiho.apps.beeenglish.model.CategoryObject;
import com.ahiho.apps.beeenglish.model.FunctionObject;
import com.ahiho.apps.beeenglish.util.Identity;
import com.ahiho.apps.beeenglish.util.UtilSharedPreferences;
import com.ahiho.apps.beeenglish.view.BooksActivity;
import com.ahiho.apps.beeenglish.view.DictionaryActivity;
import com.ahiho.apps.beeenglish.view.GrammarActivity;
import com.ahiho.apps.beeenglish.view.VocabularyActivity;
import com.ahiho.apps.beeenglish.view.communication.CommunicationActivity;
import com.ahiho.apps.beeenglish.view.sample.SampleActivity;

import java.util.List;

import io.realm.Realm;


public class RecyclerCategoryAdapter extends RecyclerView
        .Adapter<RecyclerCategoryAdapter
        .DataObjectHolder> {
    private List<FunctionObject> mDataset;
    private Context mContext;
    private boolean mIsRecent;
    private int screenWidth;
    private int mColCount;
    private UtilSharedPreferences mUtilSharedPreferences;


    public RecyclerCategoryAdapter(List<FunctionObject> dataset, int colCount, boolean isRecent) {
        mDataset = dataset;
        mIsRecent = isRecent;
        mColCount = colCount;
    }

    public static class DataObjectHolder extends RecyclerView.ViewHolder {
        ImageView ivFunctionIcon;
        TextView tvFunctionName;
        RelativeLayout rlFunctionBg;
        CardView cvMain;
        private View view;

        public DataObjectHolder(View itemView) {
            super(itemView);
            view = itemView;
            cvMain = (CardView) itemView.findViewById(R.id.cvMain);
            rlFunctionBg = (RelativeLayout) itemView.findViewById(R.id.rlFunctionBg);
            ivFunctionIcon = (ImageView) itemView.findViewById(R.id.ivFunctionIcon);
            tvFunctionName = (TextView) itemView.findViewById(R.id.tvFunctionName);
        }

    }


    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_function, parent, false);
        mContext = view.getContext();
        mUtilSharedPreferences= UtilSharedPreferences.getInstanceSharedPreferences(mContext);
        float scale = mContext.getResources().getDisplayMetrics().density;
        int paddingSub = (int) (40 * scale + 0.5f);
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = (size.x - paddingSub) / mColCount;
        DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, final int position) {
        final FunctionObject functionObject = mDataset.get(position);
        ViewGroup.LayoutParams layoutParams = holder.cvMain.getLayoutParams();
        final int id = functionObject.getId();
        layoutParams.width = screenWidth;
        layoutParams.height = screenWidth;
        holder.cvMain.setLayoutParams(layoutParams);
        holder.ivFunctionIcon.setImageResource(functionObject.getIcon());
        holder.tvFunctionName.setText(functionObject.getName());
        if (!mIsRecent)
            holder.rlFunctionBg.setBackgroundColor(Color.parseColor("#eb5557"));
        holder.view.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               if (mUtilSharedPreferences.isUpdateFirst()) {
                                                   long time = mUtilSharedPreferences.getTrialTimeExpired() - System.currentTimeMillis();
                                                   if (time > 0) {
                                                       openCategory(id);
                                                       Realm mRealm = null;
                                                       try {
                                                           mRealm = Realm.getDefaultInstance();
                                                           mRealm.beginTransaction();
                                                           try {
                                                               CategoryObject object = mRealm.where(CategoryObject.class).equalTo("id", id).findFirst();
                                                               if (object != null) {
                                                                   object.setCount(object.getCount() + 1);
                                                               } else {
                                                                   mRealm.copyToRealm(new CategoryObject(id, 1));
                                                               }
                                                           } catch (Exception e) {
                                                           }
                                                           mRealm.commitTransaction();

                                                       } catch (Exception e) {

                                                       } finally {
                                                           if (mRealm != null)
                                                               mRealm.close();
                                                       }
                                                   } else {
                                                       Intent intent = new Intent(Identity.EXPIRED_BROADCAST);
                                                       mContext.sendBroadcast(intent);
                                                   }
                                               }else{
                                                   Intent intent = new Intent(Identity.UPDATE_FIRST_BROADCAST);
                                                   mContext.sendBroadcast(intent);
                                               }
                                           }
                                       }

        );
    }

    public void openCategory(int id) {
        Intent intent;
        switch (id) {
            case Identity.FUN_ID_DICTIONARY:
                intent = new Intent(mContext, DictionaryActivity.class);
                break;
            case Identity.FUN_ID_BOOK:
                intent = new Intent(mContext, BooksActivity.class);
                break;
            case Identity.FUN_ID_SAMPLE:
                intent = new Intent(mContext, SampleActivity.class);
                break;
            case Identity.FUN_ID_GRAMMAR:
                intent = new Intent(mContext, GrammarActivity.class);
                break;
            case Identity.FUN_ID_VOCABULARY:
                intent = new Intent(mContext, VocabularyActivity.class);
                break;
            case Identity.FUN_ID_COMMUNICATE:
                intent = new Intent(mContext, CommunicationActivity.class);
                break;
            default:
                intent = new Intent(mContext, DictionaryActivity.class);
                break;
        }
        mContext.startActivity(intent);
    }

    public void addItem(FunctionObject dataObj, int index) {
        mDataset.add(dataObj);
        notifyItemInserted(index);
    }

    public void deleteItem(int index) {
        mDataset.remove(index);
        notifyItemRemoved(index);
    }

    public FunctionObject getData(int index) {
        return mDataset.get(index);
    }

    public List<FunctionObject> getAllData() {
        return mDataset;
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }



}