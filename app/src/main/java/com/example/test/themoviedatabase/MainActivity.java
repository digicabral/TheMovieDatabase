package com.example.test.themoviedatabase;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.example.test.themoviedatabase.adapter.MoviesAdapter;
import com.example.test.themoviedatabase.api.Client;
import com.example.test.themoviedatabase.api.Service;
import com.example.test.themoviedatabase.model.Movie;
import com.example.test.themoviedatabase.model.MovieResponse;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    private RecyclerView recyclerView;
    private MoviesAdapter adapter;
    private List<Movie> movieList;
    ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String language = "pt-PT";
    MaterialSearchView searchView;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        Toolbar toolbar = (Toolbar)findViewById(R.id.searchToolbar);
        initSearchToolbar(toolbar);

        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.main_content);
        swipeRefreshLayout.setColorSchemeColors(android.R.color.holo_orange_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initViews();
                Toast.makeText(MainActivity.this, "Filmes Atualizados", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initSearchToolbar(Toolbar toolbar) {

        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null)
        {
            toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));
            getSupportActionBar().setTitle(R.string.app_bar_name);
        }
        searchView = (MaterialSearchView)findViewById(R.id.search_view);

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.isEmpty())
                {
                    loadBusca(query);
                }
                else{
                    Toast.makeText(getActivity(), R.string.insira_texto_busca, Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText!=null && !newText.isEmpty())
                {
                    loadBusca(newText);
                }
                return false;
            }
        });

    }

    public Activity getActivity(){

        Context context = this;
        while(context instanceof ContextWrapper){
            if(context instanceof Activity){
                return(Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    private void initViews() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Recebendo Filmes...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);

        //Converte a lista JSON em ArrayList
        movieList = new ArrayList<>();
        adapter = new MoviesAdapter(this, movieList);

        //Responsividade da listagem
        if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        }
        else{
            recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        }
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        checkSortOrder();
    }

    private void loadJSON(Integer typeOfSearch ){
        try{
            //Carrega o JSON criando o cliente e passando a key necessaria
            if(BuildConfig.THE_MOVIE_DB_API_TOKEN.isEmpty())
            {
                Toast.makeText(getApplicationContext(), R.string.obtenha_chave, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                return;
            }
            Client Client = new Client();
            Service apiService = Client.getClient().create(Service.class);
            Call<MovieResponse> call = null;

            switch(typeOfSearch)
            {
                case R.string.pref_now_playing:
                    call = apiService.getInTheatreMovies(BuildConfig.THE_MOVIE_DB_API_TOKEN, language);
                    break;

                case R.string.pref_most_popular:
                    call = apiService.getPopularMovies(BuildConfig.THE_MOVIE_DB_API_TOKEN, language);
                    break;

                case R.string.pref_highest_rated:
                    call = apiService.getTopRatedMovies(BuildConfig.THE_MOVIE_DB_API_TOKEN, language);
                    break;

                default:
                    Toast.makeText(MainActivity.this, R.string.erro_ao_receber_dados, Toast.LENGTH_SHORT).show();
                    break;
            }
            call.enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    //Se o carregamento for bem sucedido joga a lista para o começo e o PD Desaparece
                    List<Movie> movies = response.body().getResults();
                    recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                    recyclerView.smoothScrollToPosition(0);
                    if(swipeRefreshLayout.isRefreshing()){
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    progressDialog.dismiss();
                }
                @Override
                //Se o carregamento resultar em erro o sistema exibe mensagem de erro
                public void onFailure(Call<MovieResponse> call, Throwable t) {
                    Toast.makeText(MainActivity.this, R.string.erro_ao_receber_dados, Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e)
        {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBusca(String query){
        try{
            //Carrega o JSON criando o cliente e passando a key necessaria
            if(BuildConfig.THE_MOVIE_DB_API_TOKEN.isEmpty())
            {
                Toast.makeText(getApplicationContext(), R.string.obtenha_chave, Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
            Client Client = new Client();
            Service apiService = Client.getClient().create(Service.class);
            Call<MovieResponse> call = apiService.getSearchedMovies(BuildConfig.THE_MOVIE_DB_API_TOKEN, language, query);
            final String queryWord = query;
            call.enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                        //Se o carregamento for bem sucedido joga a lista para o começo e o PD Desaparece
                        List<Movie> movies = response.body().getResults();
                        if(movies.isEmpty())
                        {
                            Toast.makeText(MainActivity.this, "Nenhum resultado para "+ "'"+queryWord+"' ", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            recyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                            recyclerView.smoothScrollToPosition(0);
                            if(swipeRefreshLayout.isRefreshing()){
                                swipeRefreshLayout.setRefreshing(false);
                            }
                         progressDialog.dismiss();
                        }
                }

                @Override
                //Se o carregamento resultar em erro o sistema exibe mensagem de erro
                public void onFailure(Call<MovieResponse> call, Throwable t) {
                    Log.d("Erro", t.getMessage());
                    Toast.makeText(MainActivity.this, "Erro ao receber os dados.", Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e)
        {
            Log.d("Erro", e.getMessage());
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.menu_busca);
        searchView.setMenuItem(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_now_playing:
                 writeSharedPreference(R.string.pref_now_playing);
                 loadJSON(R.string.pref_now_playing);
                 break;

            case R.id.menu_bestRated:
                 writeSharedPreference(R.string.pref_highest_rated);
                 loadJSON(R.string.pref_highest_rated);
                 break;

            case R.id.menu_most_popular:
                writeSharedPreference(R.string.pref_most_popular);
                loadJSON(R.string.pref_most_popular);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void writeSharedPreference(Integer key)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(getString(R.string.pref_sort_order_key), key);
        editor.apply();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s){
        checkSortOrder();
    }

    private void checkSortOrder(){
        Integer preferenceDefault = 0;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Integer sortOrder = preferences.getInt(getString(R.string.pref_sort_order_key), preferenceDefault);

        switch (sortOrder)
        {
            case R.string.pref_now_playing:
                loadJSON(R.string.pref_now_playing);
                break;

            case R.string.pref_most_popular:
                loadJSON(R.string.pref_most_popular);
                break;

            case R.string.pref_highest_rated:
                loadJSON(R.string.pref_highest_rated);
                break;

            case 0:
                //Carrega em cartaz
                loadJSON(R.string.pref_now_playing);
                break;

            default:
                Toast.makeText(this, R.string.erro_ao_carregar_filmes, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(movieList.isEmpty()){
            checkSortOrder();
        }
    }

}
