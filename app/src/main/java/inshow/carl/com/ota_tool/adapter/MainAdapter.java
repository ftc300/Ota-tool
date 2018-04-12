/*
 * Copyright 2016 Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package inshow.carl.com.ota_tool.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import inshow.carl.com.ota_tool.R;
import inshow.carl.com.ota_tool.entity.DeviceEntity;

import static inshow.carl.com.ota_tool.tools.Const.PROCESS_INDETERMINATE_FALSE;
import static inshow.carl.com.ota_tool.tools.Const.PROCESS_INDETERMINATE_TRUE;
import static inshow.carl.com.ota_tool.tools.Const.STATE_FAIL;
import static inshow.carl.com.ota_tool.tools.Const.STATE_INIT;
import static inshow.carl.com.ota_tool.tools.Const.STATE_PROCESSING;
import static inshow.carl.com.ota_tool.tools.Const.STATE_SUCCESS;

/**
 * Created by YOLANDA on 2016/7/22.
 */
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    private List<DeviceEntity> mDataList = new ArrayList<>();
    private Context context;
    private int currentPos ;

    public void setCurrentPos(int currentPos) {
        this.currentPos = currentPos;
    }

    public int getCurrentPos(){
        return currentPos;
    }

    public  MainAdapter(Context context) {
        this.context = context;
    }

    public void notifyDataSetChanged(List<DeviceEntity> dataList) {
        this.mDataList = dataList;
        super.notifyDataSetChanged();
    }

    public List<DeviceEntity> getDataList(){
        return mDataList;
    }

    public void removeAtNotify(int pos) {
        this.mDataList.remove(pos);
        super.notifyDataSetChanged();
    }

    public void addNotify(DeviceEntity item) {
        this.mDataList.add(item);
        super.notifyDataSetChanged();
    }

    public DeviceEntity getItem(int pos){
        if(null!=mDataList && mDataList.size()>0){
            return  mDataList.get(pos);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return mDataList == null ? 0 : mDataList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(context,LayoutInflater.from(context).inflate(R.layout.item_menu_main, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DeviceEntity  entity = getItem(position);
        holder.tvTitle.setText(entity.mac);
        if(entity.state == STATE_PROCESSING){
            holder.pb.setVisibility(View.VISIBLE);
            if(entity.process == PROCESS_INDETERMINATE_TRUE){
                holder.pb.setIndeterminate(true);
                holder.tvResult.setText("init");
            }else if (entity.process == PROCESS_INDETERMINATE_FALSE) {
                holder.pb.setIndeterminate(false);
            }else if(entity.process<=100 && entity.process >=0){
                holder.pb.setIndeterminate(false);
                holder.pb.setProgress(entity.process);
                holder.tvResult.setText(entity.process + "%");
            }
            holder.imgState.setVisibility(View.GONE);
        }else if(entity.state == STATE_FAIL){
            holder.pb.setVisibility(View.GONE);
            holder.imgState.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.fail));
            holder.imgState.setVisibility(View.VISIBLE);
            holder.tvResult.setText("fail");
        }else if(entity.state == STATE_SUCCESS){
            holder.imgState.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.success));
            holder.imgState.setVisibility(View.VISIBLE);
            holder.pb.setVisibility(View.GONE);
            holder.tvResult.setText("done");
        } else if(entity.state == STATE_INIT){
            holder.imgState.setVisibility(View.GONE);
            holder.pb.setVisibility(View.GONE);
            holder.tvResult.setText("");
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle , tvResult;
        ProgressBar pb;
        ImageView imgState;
        Context context;

        public ViewHolder(Context context,View itemView) {
            super(itemView);
            this.context = context;
            tvTitle = (TextView) itemView.findViewById(R.id.tv_mac);
            tvResult = (TextView) itemView.findViewById(R.id.tv_result);
            pb = (ProgressBar) itemView.findViewById(R.id.item_progressbar);
            imgState = (ImageView) itemView.findViewById(R.id.img_state);
        }

    }



}
