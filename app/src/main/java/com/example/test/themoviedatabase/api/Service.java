package com.example.test.themoviedatabase.api;

import com.example.test.themoviedatabase.model.MovieResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Rodrigo S Cabral on 21/11/2017.
 */

public interface Service {
    //Traz os filmes populares
    @GET("movie/popular")
    Call<MovieResponse> getPopularMovies(@Query("api_key") String apiKey);

    @GET("movie/top_rated")
    Call<MovieResponse> getTopRatedMovies(@Query("api_key") String apiKey);

}
