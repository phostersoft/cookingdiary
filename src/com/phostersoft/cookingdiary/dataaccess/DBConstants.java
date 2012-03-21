package com.phostersoft.cookingdiary.dataaccess;

/**
 * Constants for database table and columns name
 */
public class DBConstants {
	/* Table Names */
	public static final String TABLE_RECIPE = "recipe";
	
	public static final String TABLE_TAG = "tag";
	
	public static final String TABLE_RECIPE_TAG = "recipeTag";
	
	public static final String TABLE_PHOTO = "photo";
	
	/* Recipe table columns */
	public static final String COLUMN_RECIPE_ID = "_id";
	
	public static final String COLUMN_RECIPE_TITLE = "title";
	
	public static final String COLUMN_RECIPE_MATERIAL = "material";
	
	public static final String COLUMN_RECIPE_STEPS = "steps";
	
	public static final String COLUMN_RECIPE_SOURCE = "source";
	
	public static final String COLUMN_RECIPE_NOTE = "note";
	
	/* Tag table columns */
	public static final String COLUMN_TAG_ID = "_id";
	
	public static final String COLUMN_TAG_TAG = "tag";
	
	/* RecipeTag table column */
	public static final String COLUMN_RECIPETAG_ID = "_id";
	
	public static final String COLUMN_RECIPETAG_RECIPEID = "recipe_id";
	
	public static final String COLUMN_RECIPETAG_TAGID = "tag_id";
	
	/* Photo table column */
	public static final String COLUMN_PHOTO_ID = "_id";
	
	public static final String COLUMN_PHOTO_RECIPEID = "recipe_id";
	
	public static final String COLUMN_PHOTO_URI = "uri";
}
