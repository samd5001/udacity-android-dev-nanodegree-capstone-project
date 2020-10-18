package com.sdunk.jiraestimator.db.user;

import android.content.Context;
import android.util.Log;

import com.sdunk.jiraestimator.model.User;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SupportFactory;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import timber.log.Timber;

@Database(entities = {User.class}, version = 1, exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {

    private static final Object LOCK = new Object();
    private static final String DB_NAME = "user_data";
    private static final char[] DB_PASSPHRASE = {'p' + 'a' + 's' + 's'};
    private static UserDatabase instance;

    public static UserDatabase getInstance(Context context) {

        if (instance == null) {
            synchronized (LOCK) {
                Timber.d("Creating db instance");
                final byte[] passphrase = SQLiteDatabase.getBytes(DB_PASSPHRASE);
                final SupportFactory factory = new SupportFactory(passphrase);
                instance = Room.databaseBuilder(context.getApplicationContext(), UserDatabase.class, DB_NAME).openHelperFactory(factory).build();
            }
        }
        Timber.d("Getting db instance");
        return instance;
    }

    public abstract UserDAO userDao();

    public boolean isLoggedIn() {
        return userDao().getLoggedInUser() != null;
    }
}
