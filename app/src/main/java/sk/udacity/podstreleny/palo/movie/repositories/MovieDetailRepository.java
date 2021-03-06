package sk.udacity.podstreleny.palo.movie.repositories;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

import sk.udacity.podstreleny.palo.movie.AppExecutor;
import sk.udacity.podstreleny.palo.movie.db.MovieDatabase;
import sk.udacity.podstreleny.palo.movie.db.dao.MovieDao;
import sk.udacity.podstreleny.palo.movie.db.dao.ReviewDao;
import sk.udacity.podstreleny.palo.movie.db.dao.VideoDao;
import sk.udacity.podstreleny.palo.movie.db.entity.Movie;
import sk.udacity.podstreleny.palo.movie.db.entity.Review;
import sk.udacity.podstreleny.palo.movie.db.entity.Video;
import sk.udacity.podstreleny.palo.movie.model.NetworkBoundResource;
import sk.udacity.podstreleny.palo.movie.model.Resource;
import sk.udacity.podstreleny.palo.movie.model.ReviewList;
import sk.udacity.podstreleny.palo.movie.model.VideoList;
import sk.udacity.podstreleny.palo.movie.model.response.ApiResponse;
import sk.udacity.podstreleny.palo.movie.servicies.RetrofitProvider;
import sk.udacity.podstreleny.palo.movie.servicies.ReviewService;
import sk.udacity.podstreleny.palo.movie.servicies.VideoService;

public class MovieDetailRepository {

    private static final String TAG = MovieDetailRepository.class.getSimpleName();
    private static MovieDetailRepository INSTANCE;
    private final VideoService videoService;
    private final MovieDao movieDao;
    private final ReviewDao reviewDao;
    private final VideoDao videoDao;
    private final AppExecutor appExecutor;
    private final ReviewService reviewService;

    private MovieDetailRepository(AppExecutor appExecutor, MovieDao movieDao, ReviewDao reviewDao, VideoDao videoDao, ReviewService reviewService, VideoService videoService) {
        this.appExecutor = appExecutor;
        this.movieDao = movieDao;
        this.reviewDao = reviewDao;
        this.videoDao = videoDao;
        this.reviewService = reviewService;
        this.videoService = videoService;
    }

    public static MovieDetailRepository getInstance(Application context) {
        if (INSTANCE == null) {
            synchronized (MovieDetailRepository.class) {
                INSTANCE = new MovieDetailRepository(new AppExecutor(),
                        MovieDatabase.getDatabaseInstance(context).movieDao(),
                        MovieDatabase.getDatabaseInstance(context).reviewDao(),
                        MovieDatabase.getDatabaseInstance(context).videoDao(),
                        RetrofitProvider.getService(ReviewService.class),
                        RetrofitProvider.getService(VideoService.class)
                );
            }
        }
        return INSTANCE;
    }

    public LiveData<Movie> getMovieById(final int id) {
        return movieDao.getMovieById(id);
    }


    public void updateMovie(final Movie movie) {
        appExecutor.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                movieDao.updateMovie(movie);
            }
        });
    }

    public LiveData<Resource<List<Review>>> getReviews(final int movieID) {
        return new NetworkBoundResource<List<Review>, ReviewList>(appExecutor) {

            @Override
            protected void saveCallResult(@NonNull ReviewList item) {
                for (Review review : item.getResults()) {
                    review.setMovieID(item.getId());
                }
                reviewDao.insertAll(item.getResults());
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Review> data) {
                return (data == null || data.isEmpty());
            }

            @NonNull
            @Override
            protected LiveData<List<Review>> loadFromDb() {
                return reviewDao.getAllReviews(movieID);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<ReviewList>> createCall() {
                return reviewService.getReviewsWithMovieID(movieID);
            }

            @Override
            protected void onFetchFailed() {
                Log.d(TAG, "problem with fetching data!");
            }


        }.getAsLiveData();
    }

    public LiveData<Resource<List<Video>>> getVideos(final int movieID) {
        return new NetworkBoundResource<List<Video>, VideoList>(appExecutor) {

            @Override
            protected void saveCallResult(@NonNull VideoList item) {
                for (Video video : item.getVideos()) {
                    video.setMovie_id(item.getId());
                }
                videoDao.insertAll(item.getVideos());
            }

            @Override
            protected boolean shouldFetch(@Nullable List<Video> data) {
                return (data == null || data.isEmpty());
            }

            @NonNull
            @Override
            protected LiveData<List<Video>> loadFromDb() {
                return videoDao.getAllVideos(movieID);
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<VideoList>> createCall() {
                return videoService.getVideosWithMovieID(movieID);
            }
        }.getAsLiveData();
    }


}
