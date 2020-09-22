package com.sdunk.jiraestimator.data.db.user;

import android.content.Context;
import android.util.Log;

import com.sdunk.jiraestimator.data.model.User;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SupportFactory;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {User.class}, version = 1, exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {

    private static final String LOG_TAG = UserDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DB_NAME = "user_data";
    private static final char[] DB_PASSPHRASE = {'p' + 'a' + 's' + 's'};
    private static UserDatabase instance;

    public static UserDatabase getInstance(Context context) {

        if (instance == null) {
            synchronized (LOCK) {
                Log.d(LOG_TAG, "Creating db instance");
                final byte[] passphrase = SQLiteDatabase.getBytes(DB_PASSPHRASE);
                final SupportFactory factory = new SupportFactory(passphrase);
                instance = Room.databaseBuilder(context.getApplicationContext(), UserDatabase.class, DB_NAME).openHelperFactory(factory).build();
            }
        }
        Log.d(LOG_TAG, "Getting db instance");
        return instance;
    }

    public abstract UserDAO userDao();

    public boolean isLoggedIn() {
        return userDao().getLoggedInUser() != null;
    }
}
