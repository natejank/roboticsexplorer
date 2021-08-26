package net.example.apstudent.roboticsexplorer.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.example.apstudent.roboticsexplorer.R;

import java.util.List;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.ViewHolder> {
    private Context context;
    private List<TeamData> eventData;
    private ClickListener listener;


    public TeamAdapter(Context context, List<TeamData> eventData, ClickListener clickListener) {
        this.context = context;
        this.eventData = eventData;
        listener = clickListener;
    }

    public Context getContext() {
        return context;
    }
    

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.team_row, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        holder.rank.setText(eventData.get(position).getRank());
        holder.team_number.setText(eventData.get(position).getTeamNumber());
        holder.team_name.setText(eventData.get(position).getTeamName());
        holder.wins.setText(eventData.get(position).getQualWins());
        holder.losses.setText(eventData.get(position).getQualLosses());
        holder.ties.setText(eventData.get(position).getQualTies());
    }

    @Override
    public int getItemCount() {
        return eventData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView rank;
        TextView team_number;
        TextView team_name;
        TextView wins;
        TextView losses;
        TextView ties;

        ClickListener listener;

        public ViewHolder(@NonNull View itemView, ClickListener clickListener) {
            super(itemView);
            listener = clickListener;

            rank = itemView.findViewById(R.id.team_ranking);
            team_number = itemView.findViewById(R.id.team_number);
            team_name = itemView.findViewById(R.id.team_name);
            wins = itemView.findViewById(R.id.label_wins);
            losses = itemView.findViewById(R.id.label_losses);
            ties = itemView.findViewById(R.id.label_ties);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onTeamClick(getAdapterPosition());
        }
    }
        public interface ClickListener {
            void onTeamClick(int position);
        }
}
