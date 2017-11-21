package com.example.test.themoviedatabase.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.example.test.themoviedatabase.model.Movie;

import java.util.List;

/**
 * Created by Rodrigo S Cabral on 21/11/2017.
 */

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MyViewHolder> {

    private Context mContext;
    private List<Movie> movieList;

    public MoviesAdapter(Context mContext, List<Movie> movieList){
        this.mContext = mContext;
        this.movieList = movieList;
    }

    @Override
    public MoviesAdapter.MyViewHolder onCreateViewHolder
}
