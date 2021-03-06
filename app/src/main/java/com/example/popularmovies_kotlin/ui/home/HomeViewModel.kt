package com.example.popularmovies_kotlin.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.popularmovies_kotlin.api.models.Movie
import com.example.popularmovies_kotlin.api.repository.MovieRepository
import com.example.popularmovies_kotlin.MovieApiStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject


class HomeViewModel

@Inject
constructor(private val movieRepository: MovieRepository) : ViewModel() {

    // The most recent API response
    private val _apiStatus = MutableLiveData<MovieApiStatus>()
    val apiStatus: LiveData<MovieApiStatus>
        get() = _apiStatus

    // A Movie
    private val _movies = MutableLiveData<List<Movie>>()
    val movies: LiveData<List<Movie>>
        get() = _movies

    // Holds the selected Movie data
    private val _navigateToSelectedMovie = MutableLiveData<Movie>()
    val navigateToSelectedMovie: LiveData<Movie>
        get() = _navigateToSelectedMovie

    // Coroutines Job
    private var viewModelJob = Job()

    // A coroutine scope for that new job using the main dispatcher
    private val coroutineScope = CoroutineScope(
        viewModelJob + Dispatchers.Main
    )

    init {
        getTopRatedMovies(MovieApiFilter.POPULAR_MOVIES)
    }


    private fun getTopRatedMovies(filter: MovieApiFilter) {
        // Using Coroutines
        coroutineScope.launch {
            var getMoviesDeferred = movieRepository.getTopRatedMovies(filter)

            try {
                _apiStatus.value = MovieApiStatus.LOADING
                var apiResult = getMoviesDeferred.await()
                _apiStatus.value = MovieApiStatus.DONE
                _movies.value = apiResult.results
            } catch (e: Exception) {
                _apiStatus.value = MovieApiStatus.ERROR
                _movies.value = ArrayList()
            }
        }
    }

    // sets _navigateToSelectedMovie to the selected Mars Movie, when this is set the switch to detail page will happen
    fun displayMovieDetails(movie: Movie) {
        _navigateToSelectedMovie.value = movie
    }

    // sets _navigateToSelectedMovie back to null/resets it, this way when we return to overview page it doesn't automatically go back to the detail page
    fun displayMovieDetailsComplete() {
        _navigateToSelectedMovie.value = null
    }

    fun updateFilter(filter: MovieApiFilter) {
        getTopRatedMovies(filter)
    }

    // Cancel the Coroutines Job
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}

enum class MovieApiFilter(val value: String) {
    POPULAR_MOVIES("popularity.desc"),
    RELEASE_MOVIE("release_date.desc"),
    REVENUE_MOVIES("revenue.desc"),
    VOTE_MOVIES("vote_average.desc"),
    TOP_RATED_MOVIES("vote_count.desc")
}