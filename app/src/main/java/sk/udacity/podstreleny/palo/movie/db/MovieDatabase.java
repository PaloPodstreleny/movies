package sk.udacity.podstreleny.palo.movie.db;

import android.app.Application;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import sk.udacity.podstreleny.palo.movie.db.dao.MovieDao;
import sk.udacity.podstreleny.palo.movie.db.dao.ReviewDao;
import sk.udacity.podstreleny.palo.movie.db.dao.VideoDao;
import sk.udacity.podstreleny.palo.movie.db.entity.Movie;
import sk.udacity.podstreleny.palo.movie.db.entity.Review;
import sk.udacity.podstreleny.palo.movie.db.entity.Video;
import sk.udacity.podstreleny.palo.movie.util.MovieTypeConvertor;

@Database(entities = {Movie.class, Review.class, Video.class}, version = 1, exportSchema = false)
@TypeConverters({MovieTypeConvertor.class})
public abstract class MovieDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "movie_database";
    private static MovieDatabase INSTANCE;

    public abstract MovieDao movieDao();

    public abstract ReviewDao reviewDao();

    public abstract VideoDao videoDao();

    public static MovieDatabase getDatabaseInstance(Application context) {
        if (INSTANCE == null) {
            synchronized (MovieDatabase.class) {
                INSTANCE = Room.databaseBuilder(context, MovieDatabase.class, DATABASE_NAME)
                        .build();
            }
        }
        return INSTANCE;
    }

}
