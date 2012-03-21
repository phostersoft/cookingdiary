package com.phostersoft.cookingdiary;

import com.phostersoft.cookingdiary.R;
import com.phostersoft.cookingdiary.utils.ImageUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class CookingDiaryMainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cooking_diary_main_activity);
        ImageUtils.init(this);
    }
    
    public void createNewRecipe(View view) {
    	Intent intent = new Intent(this, RecipeEditorView.class);
    	startActivity(intent);
    }
    
    public void showRecipeList(View view) {
    	Intent intent = new Intent(this, RecipeListView.class);
    	startActivity(intent);
    }
    
    public void showTagList(View view) {
    	Intent intent = new Intent(this, TagListView.class);
    	startActivity(intent);
    }
    
}