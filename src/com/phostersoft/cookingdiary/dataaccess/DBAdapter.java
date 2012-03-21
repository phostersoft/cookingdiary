package com.phostersoft.cookingdiary.dataaccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database Adapter
 */
public class DBAdapter {
	
	/**
	 * Database helper
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			for (String createStatement : DATABASE_CREATE) {
				db.execSQL(createStatement);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			switch (oldVersion) {
			case 3:
				for (String updatesStatement : DATABASE_UPDATE2) {
					db.execSQL(updatesStatement);
				}
			    break;
			default:
			    for (String updateStatement : DATABASE_UPDATE) {
				    db.execSQL(updateStatement);
			    }
			    onCreate(db);
			}
		}
		
	}
	
	private static final String DATABASE_NAME = "cookingDiaryDB";
	
	private static final int DATABASE_VERSION = 4;
	
	private static final String[] DATABASE_CREATE = {
		"create table recipe (_id integer primary key autoincrement, "
				+ "title text not null, steps text, material text, source text, note text);",
		"create table tag (_id integer primary key, tag text not null);",
		"create table recipeTag (_id integer primary key autoincrement, "
				+ "recipe_id integer not null, tag_id integer not null);",
		"create table photo (_id integer primary key autoincrement, "
				+ "recipe_id integer not null, uri text not null);"
	};
	
	private static final String[] DATABASE_UPDATE = {
		"DROP TABLE IF EXISTS recipeTag",
		"DROP TABLE IF EXISTS tag",
		"DROP TABLE IF EXISTS recipe"
	};
	
	private static final String[] DATABASE_UPDATE2 = {
		"alter table recipe add column note text;"
	};
	
	private DatabaseHelper dbHelper;
	
	private final Context context;
	
	private SQLiteDatabase database;
	
	
	public DBAdapter(Context context) {
		this.context = context;
	}

	public DBAdapter open() throws SQLException {
		dbHelper = new DatabaseHelper(context);
		database = dbHelper.getWritableDatabase();
		return this;
	}
	
	public void close() {
		dbHelper.close();
	}
	
	/**
	 * Create new recipe
	 * @param title
	 * @param steps
	 * @param material
	 * @param source
	 * @return
	 */
	public long createRecipe(String title, String steps, 
			String material, String source, String note) {
		ContentValues initialValues = 
				getRecipeContentValues(null, title, material, steps, source, note);
		return database.insert(DBConstants.TABLE_RECIPE, null, initialValues);
	}
	
	/**
	 * Update existing recipe
	 * @param id
	 * @param title
	 * @param steps
	 * @param material
	 * @param source
	 * @return
	 */
	public long updateRecipe(int id, String title, String steps, 
			String material, String source, String note) {
		ContentValues initialValues = 
				getRecipeContentValues(id, title, material, steps, source, note);
		return database.replace(DBConstants.TABLE_RECIPE, null, initialValues);
	}
	
	/**
	 * Delete existing recipe
	 * @param recipeId
	 * @return
	 */
	public long deleteRecipe(int recipeId) {
		database.delete(DBConstants.TABLE_RECIPE_TAG, 
				DBConstants.COLUMN_RECIPETAG_RECIPEID + "=" + recipeId, null);
		
		return database.delete(DBConstants.TABLE_RECIPE, 
				DBConstants.COLUMN_RECIPE_ID + "=" + recipeId, null);
	}
	
	/**
	 * Fetch recipe by id
	 * @param recipeId
	 * @return
	 */
	public Cursor fetchRecipe(int recipeId) {
		Cursor cursor =
				database.query(true, DBConstants.TABLE_RECIPE, 
						new String[] { DBConstants.COLUMN_RECIPE_ID, 
							DBConstants.COLUMN_RECIPE_TITLE, 
							DBConstants.COLUMN_RECIPE_STEPS, 
							DBConstants.COLUMN_RECIPE_MATERIAL, 
							DBConstants.COLUMN_RECIPE_SOURCE,
							DBConstants.COLUMN_RECIPE_NOTE }, 
							DBConstants.COLUMN_RECIPE_ID + "=" + recipeId, 
						null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	/** 
	 * Fetch recipe by title part
	 * @param titlePart
	 * @return
	 */
	public Cursor fetchRecipeByTitle(String titlePart) {
		Cursor cursor = null;
		String condition = titlePart == null ? null : 
			DBConstants.COLUMN_RECIPE_TITLE + " like '%" + titlePart + "%'";
		cursor =
				database.query(true, "recipe", 
						new String[] { DBConstants.COLUMN_RECIPE_ID, 
							DBConstants.COLUMN_RECIPE_TITLE }, 
						condition, null, null, null, 
						DBConstants.COLUMN_RECIPE_TITLE + " COLLATE NOCASE ASC", null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	/** 
	 * Fetch recipe by title part
	 * @param titlePart
	 * @return
	 */
	public Cursor fetchRecipeWithTagByTitle(String titlePart, int tagId) {
		if (tagId < 0) {
			return fetchRecipeByTitle(titlePart);
		} else {
			String titleCondition = titlePart == null ? "" : " AND " 
					+ DBConstants.COLUMN_RECIPE_TITLE + " like '%" 
					+ titlePart + "%'";
			String tagCondition = DBConstants.TABLE_RECIPE_TAG + "."
					+ DBConstants.COLUMN_RECIPETAG_TAGID + "=" + tagId;
			
			String table = DBConstants.TABLE_RECIPE + " JOIN "
					+ DBConstants.TABLE_RECIPE_TAG + " ON "
					+ DBConstants.TABLE_RECIPE + "." + DBConstants.COLUMN_RECIPE_ID
					+ "=" + DBConstants.TABLE_RECIPE_TAG + "." 
					+ DBConstants.COLUMN_RECIPETAG_RECIPEID;
			
			Cursor cursor = database.query(true, table,
					new String[] { DBConstants.TABLE_RECIPE + "." +
						DBConstants.COLUMN_RECIPE_ID, 
						DBConstants.COLUMN_RECIPE_TITLE
					},
					tagCondition + titleCondition,
					null, null, null, 
					DBConstants.COLUMN_RECIPE_TITLE + " COLLATE NOCASE ASC", null);
			if (cursor != null) {
				cursor.moveToFirst();
			}
			return cursor;
		}
	}
	
	/**
	 * Create new tag
	 * @param tag
	 * @return
	 */
	public long createTag(String tag) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(DBConstants.COLUMN_TAG_TAG, tag);
		
		return database.insert(DBConstants.TABLE_TAG, null, initialValues);
	}
	
	/**
	 * Link recipe to existing tag
	 * @param recipeId
	 * @param tagId
	 * @return
	 */
	public long tagRecipe(int recipeId, int tagId) {
		removeRecipeTag(recipeId, tagId);
		
		ContentValues initialValues = new ContentValues();
		initialValues.put(DBConstants.COLUMN_RECIPETAG_RECIPEID, recipeId);
		initialValues.put(DBConstants.COLUMN_RECIPETAG_TAGID, tagId);
		
		long result = database.insert(DBConstants.TABLE_RECIPE_TAG, null, initialValues);
		return result;
	}
	
	/**
	 * Remove link from recipe to tag
	 * @param recipeId
	 * @param tagId
	 * @return
	 */
	public long removeRecipeTag(int recipeId, int tagId) {
		long result = database.delete(DBConstants.TABLE_RECIPE_TAG, 
				DBConstants.COLUMN_RECIPETAG_RECIPEID + "=" + recipeId + 
				" and " + DBConstants.COLUMN_RECIPETAG_TAGID + "=" + tagId, null);
		
		return result;
	}
	
	public long deleteTagIfNoRecipe(int tagId) {
		Cursor c = fetchRecipeWithTagByTitle("", tagId);
		if (c != null && c.getCount() == 0) {
			return database.delete(DBConstants.TABLE_TAG, 
					DBConstants.COLUMN_TAG_ID + "=" + tagId, null);
		} else {
			return -1;
		}
	}
	
	/**
	 * Create tag if not exists
	 * @param tag
	 * @return
	 */
	public long createTagIfNotExists(String tag) {
		tag = tag.trim().toLowerCase();
		
		Cursor cursor =
				database.query(true, DBConstants.TABLE_TAG, 
						new String[] {DBConstants.COLUMN_TAG_ID, 
							DBConstants.COLUMN_TAG_TAG }, 
						DBConstants.COLUMN_TAG_TAG + " = '" + tag + "'", 
						null, null, null, null, null);
		
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();
			return cursor.getInt(0);
		} else {
			ContentValues initialValues = new ContentValues();
			initialValues.put(DBConstants.COLUMN_TAG_TAG, tag);
			return database.insert(DBConstants.TABLE_TAG, null, initialValues);
		}
	}

	/**
	 * Fetch recipe tags
	 * @param recipeId
	 * @return
	 */
	public Cursor fetchRecipeTags(int recipeId) {
		String table = DBConstants.TABLE_RECIPE_TAG + " JOIN "
				+ DBConstants.TABLE_TAG + " ON "
				+ DBConstants.TABLE_RECIPE_TAG + "." 
				+ DBConstants.COLUMN_RECIPETAG_TAGID + "="
				+ DBConstants.TABLE_TAG + "."
				+ DBConstants.COLUMN_TAG_ID;
		
		String condition = DBConstants.TABLE_RECIPE_TAG + "."
				+ DBConstants.COLUMN_RECIPETAG_RECIPEID + "=" + recipeId;
		Cursor cursor = database.query(true, table,
				new String[] { DBConstants.COLUMN_RECIPETAG_TAGID, 
						DBConstants.COLUMN_TAG_TAG }, 
				condition, null, null, null, null, null);

		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}

	/**
	 * Fetch all tags
	 * @return
	 */
	public Cursor fetchTags() {
		Cursor cursor = database.query(true, DBConstants.TABLE_TAG,
				new String[] { DBConstants.COLUMN_TAG_ID, 
					DBConstants.COLUMN_TAG_TAG },
				null, null, null, null, DBConstants.COLUMN_TAG_TAG + " COLLATE NOCASE ASC", null);
		
		if (cursor != null) {
			cursor.moveToFirst();
		}
		
		return cursor;
	}
	
	private ContentValues getRecipeContentValues(Integer id, String title,
			String material, String steps, String source, String note) {
		ContentValues returnValue = new ContentValues();
		
		if (id != null) {
			returnValue.put(DBConstants.COLUMN_RECIPE_ID, id);
		}
		returnValue.put(DBConstants.COLUMN_RECIPE_TITLE, title);
		returnValue.put(DBConstants.COLUMN_RECIPE_STEPS, steps);
		returnValue.put(DBConstants.COLUMN_RECIPE_MATERIAL, material);
		returnValue.put(DBConstants.COLUMN_RECIPE_SOURCE, source);
		returnValue.put(DBConstants.COLUMN_RECIPE_NOTE, note);
		
		return returnValue;
	}
	
	public Cursor fetchImagesForRecipe(int recipeId) {
		
		Cursor cursor =
				database.query(true, DBConstants.TABLE_PHOTO, 
						new String[] {DBConstants.COLUMN_PHOTO_ID, 
							DBConstants.COLUMN_PHOTO_URI }, 
						DBConstants.COLUMN_PHOTO_RECIPEID + " = " + recipeId, 
						null, null, null, null, null);
		
		if (cursor != null) {
			cursor.moveToFirst();
		} 
		return cursor;
	}
	
	public long addPhotoForRecipe(int recipeId, String uri) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(DBConstants.COLUMN_PHOTO_RECIPEID, recipeId);
		initialValues.put(DBConstants.COLUMN_PHOTO_URI, uri);
		return database.insert(DBConstants.TABLE_PHOTO, null, initialValues);
	}
	
	public long removePhoto(int id) {
		return database.delete(DBConstants.TABLE_PHOTO, 
				DBConstants.COLUMN_PHOTO_ID + "=" + id, null);
	}
	
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
}
