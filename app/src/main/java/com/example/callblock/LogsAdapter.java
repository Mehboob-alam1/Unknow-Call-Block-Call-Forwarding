package com.example.callblock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;



import java.util.ArrayList;

import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.Holder> {

    private Context context;
  private ArrayList<CallLog> list;

    public LogsAdapter(Context context, ArrayList<CallLog> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_call_logs,parent,false);
        return new Holder(view);
    }

    public void setList(ArrayList<CallLog> list) {
        this.list = list;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {


     CallLog log=   list.get(position);

     holder.txtDuration.setText(log.getCallDuration());
     holder.txtIncoming.setText(log.getIncoming());
     holder.txtTime.setText(log.getTimestamp());
     holder.txtSummary.setText(log.getSummary());

     holder.voicePlayerView.setAudio(log.getRecordingLink());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class Holder extends RecyclerView.ViewHolder{

        private VoicePlayerView voicePlayerView;
        private TextView txtDuration,txtIncoming,txtTime,txtSummary;

        public Holder(@NonNull View itemView) {
            super(itemView);

            voicePlayerView=itemView.findViewById(R.id.voicePlayerView);
            txtDuration=itemView.findViewById(R.id.txtDuration);
            txtIncoming=itemView.findViewById(R.id.txtIncoming);
            txtTime=itemView.findViewById(R.id.txtTime);
            txtSummary=itemView.findViewById(R.id.txtSummary);
        }
    }
}
